package visitors;

import com.sun.source.tree.LineMap;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Assert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;
import doop.*;
import reporters.Reporter;
import util.Position;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sun.tools.javac.tree.JCTree.*;

/**
 * A subclass of Tree.Visitor, this class defines
 * a general tree scanner pattern. Translation proceeds recursively in
 * left-to-right order down a tree. There is one visitor method in this class
 * for every possible kind of tree node.  To obtain a specific
 * scanner, it suffices to override those visitor methods which
 * do some interesting work. The scanner class itself takes care of all
 * navigational aspects.
 * <p>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
public class IdentifierScanner extends TreeScanner {
    private final Map<String, Set<String>> vptMap;
    private final Map<String, Set<String>> cfeMap;
    private final Map<Pair<String, String>, Set<String>> ifptMap;
    private final Reporter reporter;
    private final LineMap lineMap;
    private final DoopRepresentationBuilder doopReprBuilder;

    private final Map<String, HeapAllocation> heapAllocationMap;
    private final Map<String, MethodDeclaration>  methodDeclarationMap;
    private final Map<String, Set<Position>> fieldSignatureMap;

    private MethodSymbol currentMethodSymbol;
    private ClassSymbol currentClassSymbol;
    private String currentMethodDoopSignature;
    private String currentMethodCompactName;

    private final Map<ClassSymbol, Map<String, Integer>> methodNamesPerClassMap;
    private int constructorInvocationCounter;

    private int methodInvocationCounter;
    private Map<String, Integer> methodInvocationCounterMap;
    private boolean scanForInvocations;

    /**
     *
     * @param reporter
     */
    public IdentifierScanner(Reporter reporter) {
        this(reporter, null, null, null, null, null, null, null);
    }

    /**
     *
     * @param reporter
     * @param vptMap
     * @param cfeMap
     * @param ifptMap
     * @param lineMap
     * @param heapAllocationMap
     * @param methodDeclarationMap
     */
    public IdentifierScanner(Reporter reporter, Map<String, Set<String>> vptMap, Map<String, Set<String>> cfeMap,
                             Map<Pair<String, String>, Set<String>> ifptMap, LineMap lineMap,
                             Map<String, HeapAllocation> heapAllocationMap,
                             Map<String, MethodDeclaration> methodDeclarationMap,
                             Map<String, Set<Position>> fieldSignatureMap)

    {
        this.doopReprBuilder = DoopRepresentationBuilder.getInstance();
        this.reporter = reporter;
        this.vptMap = vptMap;
        this.ifptMap = ifptMap;
        this.lineMap = lineMap;
        this.cfeMap = cfeMap;
        this.constructorInvocationCounter = 0;
        this.methodInvocationCounter = 0;
        this.heapAllocationMap = heapAllocationMap;
        this.methodDeclarationMap = methodDeclarationMap;
        this.fieldSignatureMap = fieldSignatureMap;
        this.methodNamesPerClassMap = new HashMap<>();
        this.methodInvocationCounterMap = null;
        this.scanForInvocations = false;
    }


    /**
     * Visitor method: Scan a single node.
     *
     * @param tree
     */
    @Override
    public void scan(JCTree tree) {
        if (tree != null) tree.accept(this);
    }

    /**
     * Visitor method: scan a list of nodes.
     *
     * @param trees
     */
    @Override
    public void scan(List<? extends JCTree> trees) {
        if (trees != null)
            for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
                if (l.head instanceof JCIdent) {
                    if (((JCIdent) l.head).sym instanceof MethodSymbol)
                        System.out.println("Scan list of nodes: " + ((JCIdent) l.head).sym.getQualifiedName().toString());
                }
                scan(l.head);
            }
    }


    /**
     * Add VarPointsTo information to [line : VarPointsTo] map.
     *
     * @param doopVarName
     * @param pos
     * @param varName
     */
    private void mapVarPointsTo(String doopVarName, long pos, Name varName) {

        /**
         * If the VarPointsTo map is not null, try to match the doop variable name with doop VarPointsTo facts.
         */
        if (this.vptMap != null) {
            if (this.vptMap.containsKey(doopVarName)) {
                Set<String> heapAllocationReprSet = this.vptMap.get(doopVarName);
                Set<HeapAllocation> heapAllocationSet = new HashSet<>();

                for (String heapAllocationRepr : heapAllocationReprSet) {
                    if (heapAllocationMap.containsKey(heapAllocationRepr))
                        heapAllocationSet.add(heapAllocationMap.get(heapAllocationRepr));
                    else
                        heapAllocationSet.add(new HeapAllocation(heapAllocationRepr));
                }
                this.reporter.reportVarPointsTo(new VarPointsTo(lineMap.getLineNumber(pos),
                                                                lineMap.getColumnNumber(pos),
                                                                lineMap.getLineNumber(pos + varName.length()),
                                                                lineMap.getColumnNumber(pos + varName.length()),
                                                                doopVarName,
                                                                heapAllocationSet));
            }
        }
        /**
         * Else report a VarPointsTo object with an empty points-to heap allocation set.
         */
        else {
            this.reporter.reportVarPointsTo(new VarPointsTo(lineMap.getLineNumber(pos),
                                                            lineMap.getColumnNumber(pos),
                                                            lineMap.getLineNumber(pos + varName.length()),
                                                            lineMap.getColumnNumber(pos + varName.length()),
                                                            doopVarName,
                                                            new HashSet<>()));
        }
    }

    /**
     * Add CallGraphEdge information to [line : CallGraphEdge] map.
     *
     * @param doopMethodInvocation
     * @param pos
     * @param methodName
     */
    private void mapMethodInvocation(String doopMethodInvocation, long pos, String methodName) {

        /**
         * If the CallGraphEdge Map is not set to null, try to match the doop method invocation representation with doop CallGraphEdge facts.
         */
        if (this.cfeMap != null) {
            if (this.cfeMap.containsKey(doopMethodInvocation)) {
                Set<String> methodDeclarationReprSet = this.cfeMap.get(doopMethodInvocation);
                Set<MethodDeclaration> methodDeclarationSet = new HashSet<>();

                for (String methodDeclarationRepr : methodDeclarationReprSet) {
                    if (methodDeclarationMap.containsKey(methodDeclarationRepr))
                        methodDeclarationSet.add(methodDeclarationMap.get(methodDeclarationRepr));
                    else
                        methodDeclarationSet.add(new MethodDeclaration(methodDeclarationRepr));
                }
                System.out.println("Reported!");
                this.reporter.reportCallGraphEdge(new CallGraphEdge(lineMap.getLineNumber(pos),
                                                                    lineMap.getColumnNumber(pos),
                                                                    lineMap.getColumnNumber(pos + methodName.length()),
                                                                    doopMethodInvocation,
                                                                    methodDeclarationSet));
            }
            else
                System.out.println("Method invocation not found: " + doopMethodInvocation);
        }
        /**
         * Else report a CallGraphEdge object with an empty possibly called declaration set.
         */
        else {
            this.reporter.reportCallGraphEdge(new CallGraphEdge(lineMap.getLineNumber(pos),
                                                                lineMap.getColumnNumber(pos),
                                                                lineMap.getColumnNumber(pos + methodName.length()) ,
                                                                doopMethodInvocation,
                                                                new HashSet<>()));
        }
    }

    /**
     * *************************************************************************
     * Visitor methods
     * **************************************************************************
     */
    @Override
    public void visitTopLevel(JCCompilationUnit tree) {
        scan(tree.packageAnnotations);
        scan(tree.pid);
        scan(tree.defs);
    }

    @Override
    public void visitImport(JCImport tree) {
        scan(tree.qualid);
    }

    @Override
    public void visitClassDef(JCClassDecl tree) {

        this.currentClassSymbol = tree.sym;
        Map<String, Integer> methodNamesMap;
        if (!methodNamesPerClassMap.containsKey(this.currentClassSymbol)) {
            methodNamesMap = new HashMap<>();
            /**
             * Fills the method names map in order to be able to identify overloaded methods for each class.
             */
            for (Symbol symbol : this.currentClassSymbol.getEnclosedElements()) {
                if (symbol instanceof MethodSymbol) {
                    MethodSymbol methodSymbol = (MethodSymbol) symbol;
                    if (!methodNamesMap.containsKey(methodSymbol.getQualifiedName().toString()))
                        methodNamesMap.put(methodSymbol.getQualifiedName().toString(), 1);
                    else {
                        int methodNameCounter = methodNamesMap.get(methodSymbol.getQualifiedName().toString());
                        methodNamesMap.put(methodSymbol.getQualifiedName().toString(), ++methodNameCounter);
                    }
                }
            }
            methodNamesPerClassMap.put(this.currentClassSymbol, methodNamesMap);
        }

        scan(tree.mods);
        scan(tree.typarams);
        scan(tree.extending);
        scan(tree.implementing);
        scan(tree.defs);
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {

        this.scanForInvocations = true;
        this.currentMethodSymbol = tree.sym;
        this.constructorInvocationCounter = 0;
        this.methodInvocationCounter = 0;
        this.currentMethodDoopSignature = this.doopReprBuilder.buildDoopMethodSignature(currentMethodSymbol);
        this.currentMethodCompactName = this.doopReprBuilder.buildDoopMethodCompactName(currentMethodSymbol);
        this.methodInvocationCounterMap = new HashMap<>();

        scan(tree.mods);
        scan(tree.restype);
        scan(tree.typarams);
        scan(tree.recvparam);
        scan(tree.params);
        scan(tree.thrown);
        scan(tree.defaultValue);
        scan(tree.body);

        this.scanForInvocations = false;
    }

    @Override
    public void visitVarDef(JCVariableDecl tree) {

        if (tree.sym.isLocal()) {
            String varNameInDoop;
            /**
             * If current method is overloaded use its signature to build the variable name.
             */
            if (this.methodNamesPerClassMap.get(currentClassSymbol).get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
                varNameInDoop = this.doopReprBuilder.buildDoopVarName(this.currentMethodDoopSignature, tree.sym.getQualifiedName().toString());
            /**
             * Otherwise use its compact name.
             */
            else
                varNameInDoop = this.doopReprBuilder.buildDoopVarName(this.currentMethodCompactName, tree.sym.getQualifiedName().toString());

            System.out.println("Variable name in Doop: " + varNameInDoop);
            System.out.println("##########################################################################################################################");

            mapVarPointsTo(varNameInDoop, tree.pos, tree.name);
        }

        scan(tree.mods);
        scan(tree.vartype);
        scan(tree.nameexpr);
        scan(tree.init);
    }

    @Override
    public void visitSkip(JCSkip tree) {
    }

    /**
     * @param tree
     */
    @Override
    public void visitBlock(JCBlock tree) {
        scan(tree.stats);
    }

    @Override
    public void visitDoLoop(JCDoWhileLoop tree) {
        scan(tree.body);
        scan(tree.cond);
    }

    @Override
    public void visitWhileLoop(JCWhileLoop tree) {
        scan(tree.cond);
        scan(tree.body);
    }

    @Override
    public void visitForLoop(JCForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        scan(tree.step);
        scan(tree.body);
    }

    @Override
    public void visitForeachLoop(JCEnhancedForLoop tree) {
        scan(tree.var);
        scan(tree.expr);
        scan(tree.body);
    }

    @Override
    public void visitLabelled(JCLabeledStatement tree) {
        scan(tree.body);
    }

    @Override
    public void visitSwitch(JCSwitch tree) {
        scan(tree.selector);
        scan(tree.cases);
    }

    @Override
    public void visitCase(JCCase tree) {
        scan(tree.pat);
        scan(tree.stats);
    }

    @Override
    public void visitSynchronized(JCSynchronized tree) {
        scan(tree.lock);
        scan(tree.body);
    }

    @Override
    public void visitTry(JCTry tree) {
        scan(tree.resources);
        scan(tree.body);
        scan(tree.catchers);
        scan(tree.finalizer);
    }

    @Override
    public void visitCatch(JCCatch tree) {
        scan(tree.param);
        scan(tree.body);
    }

    @Override
    public void visitConditional(JCConditional tree) {
        scan(tree.cond);
        scan(tree.truepart);
        scan(tree.falsepart);
    }

    @Override
    public void visitIf(JCIf tree) {
        scan(tree.cond);
        scan(tree.thenpart);
        scan(tree.elsepart);
    }

    @Override
    public void visitExec(JCExpressionStatement tree) {
        scan(tree.expr);
    }

    @Override
    public void visitBreak(JCBreak tree) {
    }

    @Override
    public void visitContinue(JCContinue tree) {
    }

    @Override
    public void visitReturn(JCReturn tree) {
        scan(tree.expr);
    }

    @Override
    public void visitThrow(JCThrow tree) {
        scan(tree.expr);
    }

    @Override
    public void visitAssert(JCAssert tree) {
        scan(tree.cond);
        scan(tree.detail);
    }

    @Override
    public void visitApply(JCMethodInvocation tree) {
        scan(tree.typeargs);
        scan(tree.meth);
        Class<?> clazz = tree.meth.getClass();
        scan(tree.args);
        Field field;
        Object fieldValue = null;

        System.out.println(tree.meth.toString());

        try {
            field = clazz.getField("sym");
            fieldValue = field.get(tree.meth);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (fieldValue instanceof MethodSymbol && scanForInvocations) {

            String doopMethodInvocation;
            /**
             * If current method is overloaded use its signature to build the variable name.
             */
            if (this.methodNamesPerClassMap.get(this.currentClassSymbol).get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
                doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodDoopSignature,
                        this.doopReprBuilder.buildDoopMethodInvocation((Symbol.MethodSymbol) fieldValue));
            /**
             * Otherwise use its compact name.
             */
            else
                doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodCompactName,
                        this.doopReprBuilder.buildDoopMethodInvocation((Symbol.MethodSymbol) fieldValue));

            String methodName = fieldValue.toString();
            if (methodName.contains("("))
                methodName = methodName.substring(0, methodName.indexOf("("));
            if (methodName.contains("."))
                methodName = methodName.substring(methodName.lastIndexOf("."));


            /**
             * Evaluate heap allocation counter within method.
             */
            if (!doopMethodInvocation.endsWith("<init>")) {
                if (methodInvocationCounterMap.containsKey(methodName)) {
                    methodInvocationCounter = methodInvocationCounterMap.get(methodName) + 1;
                    methodInvocationCounterMap.put(methodName, methodInvocationCounter);
                } else {
                    methodInvocationCounter = 0;
                    methodInvocationCounterMap.put(methodName, 0);
                }
                doopMethodInvocation += "/" + this.methodInvocationCounter;
            }
            else {
                doopMethodInvocation += "/" + this.constructorInvocationCounter++;
            }

            long invocationPos;
            if (tree.meth.toString().contains("."))
                invocationPos = tree.meth.pos + 1;
            else
                invocationPos = tree.meth.pos;

            System.out.println("\033[35m Method Invocation from visitApply: \033[0m" + doopMethodInvocation);
            mapMethodInvocation(doopMethodInvocation, invocationPos, methodName);
        }
    }

    @Override
    public void visitNewClass(JCNewClass tree) {
        scan(tree.encl);
        scan(tree.typeargs);
        scan(tree.clazz);
        scan(tree.args);
        scan(tree.def);

        if (this.scanForInvocations) {
            /**
             * Method Invocation: Constructor
             */
            String doopMethodInvocation;
            /**
             * If current method is overloaded use its signature to build the variable name.
             */
            if (this.methodNamesPerClassMap.get(this.currentClassSymbol).get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
                doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodDoopSignature,
                        this.doopReprBuilder.buildDoopMethodInvocation((MethodSymbol) tree.constructor) + "/" + this.constructorInvocationCounter++);
            /**
             * Otherwise use its compact name.
             */
            else
                doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodCompactName,
                        this.doopReprBuilder.buildDoopMethodInvocation((MethodSymbol) tree.constructor) + "/" + this.constructorInvocationCounter++);

            System.out.println("\033[35m Method Invocation (Constructor): \033[0m" + doopMethodInvocation);
            mapMethodInvocation(doopMethodInvocation, tree.clazz.pos, tree.clazz.type.getOriginalType().tsym.name.toString());
        }
    }

    @Override
    public void visitNewArray(JCNewArray tree) {
        scan(tree.annotations);
        scan(tree.elemtype);
        scan(tree.dims);
        tree.dimAnnotations.stream().forEach(this::scan);
        scan(tree.elems);
    }

    @Override
    public void visitLambda(JCLambda tree) {
        scan(tree.body);
        scan(tree.params);
    }

    @Override
    public void visitParens(JCParens tree) {
        scan(tree.expr);
    }

    @Override
    public void visitAssign(JCAssign tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    @Override
    public void visitAssignop(JCAssignOp tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    @Override
    public void visitUnary(JCUnary tree) {
        scan(tree.arg);
    }

    @Override
    public void visitBinary(JCBinary tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    @Override
    public void visitTypeCast(JCTypeCast tree) {
        scan(tree.clazz);
        scan(tree.expr);
    }

    @Override
    public void visitTypeTest(JCInstanceOf tree) {
        scan(tree.expr);
        scan(tree.clazz);
    }

    @Override
    public void visitIndexed(JCArrayAccess tree) {
        scan(tree.indexed);
        scan(tree.index);
    }

    @Override
    public void visitSelect(JCFieldAccess tree) {

        /**
         *  Report InstanceFieldPointsTo
         *  Need to report an InstanceFieldPointTO object with empty possibly pointed-to heap allocation set.
         */
        scan(tree.selected);
        if (tree.sym != null && tree.sym instanceof VarSymbol) {
            System.out.println(tree.sym.getClass());
            String fieldSignature = this.doopReprBuilder.buildDoopFieldSignature((VarSymbol) tree.sym);

            this.ifptMap.keySet().stream().filter(baseHeapAllocationField -> baseHeapAllocationField.snd.equals(fieldSignature)).forEach(baseHeapAllocationField -> {
                Set<HeapAllocation> heapAllocationSet = new HashSet<>();
                Set<String> heapAllocationAsStringSet = this.ifptMap.get(baseHeapAllocationField);

                heapAllocationSet.addAll(heapAllocationAsStringSet.stream().filter(heapAllocationAsString -> this.heapAllocationMap.containsKey(heapAllocationAsString)).map(this.heapAllocationMap::get).collect(Collectors.toList()));

                System.out.println(this.fieldSignatureMap);
                if (this.heapAllocationMap.get(baseHeapAllocationField.fst) != null) {

                    this.reporter.reportInstanceFieldPointsTo(new InstanceFieldPointsTo(fieldSignature,
                            this.heapAllocationMap.get(baseHeapAllocationField.fst),
                            heapAllocationSet,
                            this.fieldSignatureMap.get(fieldSignature)));
                }
            });
        }
    }

    @Override
    public void visitReference(JCMemberReference tree) {
        scan(tree.expr);
        scan(tree.typeargs);
    }

    @Override
    public void visitIdent(JCIdent tree) {

        /**
         * If identifier is a local variable.
         */
        if (tree.sym != null && tree.sym instanceof VarSymbol && tree.sym.isLocal()) {
            String varNameInDoop;
            if (this.methodNamesPerClassMap.get(this.currentClassSymbol).get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
                varNameInDoop = this.doopReprBuilder.buildDoopVarName(this.currentMethodDoopSignature, tree.sym.getQualifiedName().toString());
            else
                varNameInDoop = this.doopReprBuilder.buildDoopVarName(this.currentMethodCompactName, tree.sym.getQualifiedName().toString());
            System.out.println("Variable name in Doop: " + varNameInDoop);
            System.out.println("##########################################################################################################################");

            mapVarPointsTo(varNameInDoop, tree.pos, tree.name);
        }
    }

    @Override
    public void visitLiteral(JCLiteral tree) {
    }

    @Override
    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
    }

    @Override
    public void visitTypeArray(JCArrayTypeTree tree) {
        scan(tree.elemtype);
    }

    @Override
    public void visitTypeApply(JCTypeApply tree) {
        scan(tree.clazz);
        scan(tree.arguments);
    }

    @Override
    public void visitTypeUnion(JCTypeUnion tree) {
        scan(tree.alternatives);
    }

    @Override
    public void visitTypeIntersection(JCTypeIntersection tree) {
        scan(tree.bounds);
    }

    @Override
    public void visitTypeParameter(JCTypeParameter tree) {
        scan(tree.annotations);
        scan(tree.bounds);
    }

    @Override
    public void visitWildcard(JCWildcard tree) {
        scan(tree.kind);
        if (tree.inner != null)
            scan(tree.inner);
    }

    @Override
    public void visitTypeBoundKind(TypeBoundKind that) {
    }

    @Override
    public void visitModifiers(JCModifiers tree) {
        scan(tree.annotations);
    }

    @Override
    public void visitAnnotation(JCAnnotation tree) {
        scan(tree.annotationType);
        scan(tree.args);
    }

    @Override
    public void visitAnnotatedType(JCAnnotatedType tree) {
        scan(tree.annotations);
        scan(tree.underlyingType);
    }

    @Override
    public void visitErroneous(JCErroneous tree) {
    }

    @Override
    public void visitLetExpr(LetExpr tree) {
        scan(tree.defs);
        scan(tree.expr);
    }

    @Override
    public void visitTree(JCTree tree) {
        Assert.error();
    }
}