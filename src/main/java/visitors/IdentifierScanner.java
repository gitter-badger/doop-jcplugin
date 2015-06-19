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
import doop.*;
import reporters.Reporter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private final Map<String, Set<String>> miMap;
    private final Reporter reporter;
    private final LineMap lineMap;
    private final DoopRepresentationBuilder doopReprBuilder;

    private final Map<String, HeapAllocation> heapAllocationMap;
    private final Map<String, MethodDeclaration>  methodDeclarationMap;

    private MethodSymbol currentMethodSymbol;
    private ClassSymbol currentClassSymbol;
    private String currentMethodDoopSignature;
    private String currentMethodCompactName;

    private final Map<String, Integer> methodNamesMap;
    private int constructorInvocationCounter;

    private int methodInvocationCounter;
    private Map<String, Integer> methodInvocationCounterMap;
    private boolean scanForInvocations;

    /**
     *
     * @param reporter
     */
    public IdentifierScanner(Reporter reporter) {
        this(reporter, null, null, null, null, null);
    }

    /**
     *
     * @param reporter
     * @param vptMap
     * @param lineMap
     */
    public IdentifierScanner(Reporter reporter, Map<String, Set<String>> vptMap, Map<String, Set<String>> miMap,
                             LineMap lineMap,
                             Map<String, HeapAllocation> heapAllocationMap,
                             Map<String, MethodDeclaration> methodDeclarationMap)

    {
        this.doopReprBuilder = DoopRepresentationBuilder.getInstance();
        this.reporter = reporter;
        this.vptMap = vptMap;
        this.lineMap = lineMap;
        this.miMap = miMap;
        this.constructorInvocationCounter = 0;
        this.methodInvocationCounter = 0;
        this.heapAllocationMap = heapAllocationMap;
        this.methodDeclarationMap = methodDeclarationMap;
        this.methodNamesMap = new HashMap<>();
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
     * Visitor method: Scan a single node for a method Invocation.
     *
     * @param tree
     */
    public void scanForMethodInvocation(JCTree tree) {
        if (tree instanceof JCTree.JCIdent) {
            if (((JCTree.JCIdent) tree).sym instanceof MethodSymbol) {
                String doopMethodInvocation;
                /**
                 * If current method is overloaded use its signature to build the variable name.
                 */
                if (this.methodNamesMap.get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
                    doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodDoopSignature,
                            this.doopReprBuilder.buildDoopMethodInvocation((Symbol.MethodSymbol) ((JCTree.JCIdent) tree).sym));
                /**
                 * Otherwise use its compact name.
                 */
                else
                    doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodCompactName,
                            this.doopReprBuilder.buildDoopMethodInvocation((Symbol.MethodSymbol) ((JCTree.JCIdent) tree).sym));

                /**
                 * Evaluate heap allocation counter within method.
                 */
                if (methodInvocationCounterMap.containsKey(doopMethodInvocation)) {
                    methodInvocationCounter = methodInvocationCounterMap.get(doopMethodInvocation) + 1;
                    methodInvocationCounterMap.put(doopMethodInvocation, methodInvocationCounter);
                }
                else {
                    methodInvocationCounter = 0;
                    methodInvocationCounterMap.put(doopMethodInvocation, 0);
                }
                doopMethodInvocation += "/" + this.methodInvocationCounter;

                System.out.println("\033[35m Method Invocation from scan: \033[0m" + doopMethodInvocation);
                mapMethodInvocation(doopMethodInvocation,
                                    tree.pos,
                                    ((JCTree.JCIdent) tree).sym.name.subName(((JCTree.JCIdent) tree).sym.name.toString().lastIndexOf("."),((JCTree.JCIdent) tree).sym.name.toString().indexOf("(")).toString());

            }
        }
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
                if (l.head instanceof JCTree.JCIdent) {
                    if (((JCTree.JCIdent) l.head).sym instanceof MethodSymbol)
                        System.out.println("Scan list of nodes: " + ((JCTree.JCIdent) l.head).sym.getQualifiedName().toString());
                }
                scan(l.head);
            }
    }


    /**
     * Add VarPointsTo information to [line : VarPointsTo] map.
     *
     * @param varNameInDoop
     * @param pos
     * @param varName
     */
    private void mapVarPointsTo(String varNameInDoop, long pos, Name varName) {

        if (this.vptMap != null) {
            if (this.vptMap.containsKey(varNameInDoop)) {
                Set<String> heapAllocationReprSet = this.vptMap.get(varNameInDoop);
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
                                                                varNameInDoop,
                                                                heapAllocationSet));
            }
        }
        else {
            this.reporter.reportVarPointsTo(new VarPointsTo(lineMap.getLineNumber(pos),
                                                            lineMap.getColumnNumber(pos),
                                                            lineMap.getLineNumber(pos + varName.length()),
                                                            lineMap.getColumnNumber(pos + varName.length()),
                                                            varNameInDoop,
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

        if (this.miMap != null) {
            if (this.miMap.containsKey(doopMethodInvocation)) {
                Set<String> methodDeclarationReprSet = this.miMap.get(doopMethodInvocation);
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
    public void visitTopLevel(JCTree.JCCompilationUnit tree) {
        scan(tree.packageAnnotations);
        scan(tree.pid);
        scan(tree.defs);
    }

    @Override
    public void visitImport(JCTree.JCImport tree) {
        scan(tree.qualid);
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl tree) {
        /**
         * TODO: Consider multiple nested classes, lose current class symbol and regain it.
         */
        this.currentClassSymbol = tree.sym;
        /**
         * Fills the method names map in order to be able to identify overloaded methods.
         */
        for (Symbol symbol : this.currentClassSymbol.getEnclosedElements()) {
            if (symbol instanceof MethodSymbol) {
                MethodSymbol methodSymbol = (MethodSymbol)symbol;
                if (!methodNamesMap.containsKey(methodSymbol.getQualifiedName().toString()))
                    methodNamesMap.put(methodSymbol.getQualifiedName().toString(), 1);
                else {
                    int methodNameCounter = methodNamesMap.get(methodSymbol.getQualifiedName().toString());
                    methodNamesMap.put(methodSymbol.getQualifiedName().toString(), ++methodNameCounter);
                }
            }
        }

        scan(tree.mods);
        scan(tree.typarams);
        scan(tree.extending);
        scan(tree.implementing);
        scan(tree.defs);
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {

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
    public void visitVarDef(JCTree.JCVariableDecl tree) {

        if (tree.sym.isLocal()) {
            String varNameInDoop;
            /**
             * If current method is overloaded use its signature to build the variable name.
             */
            if (this.methodNamesMap.get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
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
    public void visitSkip(JCTree.JCSkip tree) {
    }

    /**
     * @param tree
     */
    @Override
    public void visitBlock(JCTree.JCBlock tree) {
        scan(tree.stats);
    }

    @Override
    public void visitDoLoop(JCTree.JCDoWhileLoop tree) {
        scan(tree.body);
        scan(tree.cond);
    }

    @Override
    public void visitWhileLoop(JCTree.JCWhileLoop tree) {
        scan(tree.cond);
        scan(tree.body);
    }

    @Override
    public void visitForLoop(JCTree.JCForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        scan(tree.step);
        scan(tree.body);
    }

    @Override
    public void visitForeachLoop(JCTree.JCEnhancedForLoop tree) {
        scan(tree.var);
        scan(tree.expr);
        scan(tree.body);
    }

    @Override
    public void visitLabelled(JCTree.JCLabeledStatement tree) {
        scan(tree.body);
    }

    @Override
    public void visitSwitch(JCTree.JCSwitch tree) {
        scan(tree.selector);
        scan(tree.cases);
    }

    @Override
    public void visitCase(JCTree.JCCase tree) {
        scan(tree.pat);
        scan(tree.stats);
    }

    @Override
    public void visitSynchronized(JCTree.JCSynchronized tree) {
        scan(tree.lock);
        scan(tree.body);
    }

    @Override
    public void visitTry(JCTree.JCTry tree) {
        scan(tree.resources);
        scan(tree.body);
        scan(tree.catchers);
        scan(tree.finalizer);
    }

    @Override
    public void visitCatch(JCTree.JCCatch tree) {
        scan(tree.param);
        scan(tree.body);
    }

    @Override
    public void visitConditional(JCTree.JCConditional tree) {
        scan(tree.cond);
        scan(tree.truepart);
        scan(tree.falsepart);
    }

    @Override
    public void visitIf(JCTree.JCIf tree) {
        scan(tree.cond);
        scan(tree.thenpart);
        scan(tree.elsepart);
    }

    @Override
    public void visitExec(JCTree.JCExpressionStatement tree) {
        scan(tree.expr);
    }

    @Override
    public void visitBreak(JCTree.JCBreak tree) {
    }

    @Override
    public void visitContinue(JCTree.JCContinue tree) {
    }

    @Override
    public void visitReturn(JCTree.JCReturn tree) {
        scan(tree.expr);
    }

    @Override
    public void visitThrow(JCTree.JCThrow tree) {
        scan(tree.expr);
    }

    @Override
    public void visitAssert(JCTree.JCAssert tree) {
        scan(tree.cond);
        scan(tree.detail);
    }

    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {
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
            if (this.methodNamesMap.get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
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

            long invocationPos = 0;
            if (tree.meth.toString().contains("."))
                invocationPos = tree.meth.pos + 1;
            else
                invocationPos = tree.meth.pos;

            System.out.println("\033[35m Method Invocation from visitApply: \033[0m" + doopMethodInvocation);
            mapMethodInvocation(doopMethodInvocation, invocationPos, methodName);
        }
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass tree) {
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
            if (this.methodNamesMap.get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
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
    public void visitNewArray(JCTree.JCNewArray tree) {
        scan(tree.annotations);
        scan(tree.elemtype);
        scan(tree.dims);
        tree.dimAnnotations.stream().forEach(this::scan);
        scan(tree.elems);
    }

    @Override
    public void visitLambda(JCTree.JCLambda tree) {
        scan(tree.body);
        scan(tree.params);
    }

    @Override
    public void visitParens(JCTree.JCParens tree) {
        scan(tree.expr);
    }

    @Override
    public void visitAssign(JCTree.JCAssign tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    @Override
    public void visitAssignop(JCTree.JCAssignOp tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    @Override
    public void visitUnary(JCTree.JCUnary tree) {
        scan(tree.arg);
    }

    @Override
    public void visitBinary(JCTree.JCBinary tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    @Override
    public void visitTypeCast(JCTree.JCTypeCast tree) {
        scan(tree.clazz);
        scan(tree.expr);
    }

    @Override
    public void visitTypeTest(JCTree.JCInstanceOf tree) {
        scan(tree.expr);
        scan(tree.clazz);
    }

    @Override
    public void visitIndexed(JCTree.JCArrayAccess tree) {
        scan(tree.indexed);
        scan(tree.index);
    }

    @Override
    public void visitSelect(JCTree.JCFieldAccess tree) {
        scan(tree.selected);
    }

    @Override
    public void visitReference(JCTree.JCMemberReference tree) {
        scan(tree.expr);
        scan(tree.typeargs);
    }

    @Override
    public void visitIdent(JCTree.JCIdent tree) {

        /**
         * If identifier is a local variable.
         */
        if (tree.sym != null && tree.sym instanceof VarSymbol && tree.sym.isLocal()) {
            String varNameInDoop;
            if (this.methodNamesMap.get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
                varNameInDoop = this.doopReprBuilder.buildDoopVarName(this.currentMethodDoopSignature, tree.sym.getQualifiedName().toString());
            else
                varNameInDoop = this.doopReprBuilder.buildDoopVarName(this.currentMethodCompactName, tree.sym.getQualifiedName().toString());
            System.out.println("Variable name in Doop: " + varNameInDoop);
            System.out.println("##########################################################################################################################");

            mapVarPointsTo(varNameInDoop, tree.pos, tree.name);
        }
    }

    @Override
    public void visitLiteral(JCTree.JCLiteral tree) {
    }

    @Override
    public void visitTypeIdent(JCTree.JCPrimitiveTypeTree tree) {
    }

    @Override
    public void visitTypeArray(JCTree.JCArrayTypeTree tree) {
        scan(tree.elemtype);
    }

    @Override
    public void visitTypeApply(JCTree.JCTypeApply tree) {
        scan(tree.clazz);
        scan(tree.arguments);
    }

    @Override
    public void visitTypeUnion(JCTree.JCTypeUnion tree) {
        scan(tree.alternatives);
    }

    @Override
    public void visitTypeIntersection(JCTree.JCTypeIntersection tree) {
        scan(tree.bounds);
    }

    @Override
    public void visitTypeParameter(JCTree.JCTypeParameter tree) {
        scan(tree.annotations);
        scan(tree.bounds);
    }

    @Override
    public void visitWildcard(JCTree.JCWildcard tree) {
        scan(tree.kind);
        if (tree.inner != null)
            scan(tree.inner);
    }

    @Override
    public void visitTypeBoundKind(JCTree.TypeBoundKind that) {
    }

    @Override
    public void visitModifiers(JCTree.JCModifiers tree) {
        scan(tree.annotations);
    }

    @Override
    public void visitAnnotation(JCTree.JCAnnotation tree) {
        scan(tree.annotationType);
        scan(tree.args);
    }

    @Override
    public void visitAnnotatedType(JCTree.JCAnnotatedType tree) {
        scan(tree.annotations);
        scan(tree.underlyingType);
    }

    @Override
    public void visitErroneous(JCTree.JCErroneous tree) {
    }

    @Override
    public void visitLetExpr(JCTree.LetExpr tree) {
        scan(tree.defs);
        scan(tree.expr);
    }

    @Override
    public void visitTree(JCTree tree) {
        Assert.error();
    }
}