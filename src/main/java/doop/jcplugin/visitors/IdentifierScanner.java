package doop.jcplugin.visitors;

import com.sun.source.tree.LineMap;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Assert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;
import doop.jcplugin.reporters.Reporter;
import doop.jcplugin.representation.*;
import doop.persistent.elements.*;
import doop.jcplugin.util.SourceFileReport;
import doop.persistent.elements.Class;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    private final LineMap lineMap;
    private final DoopRepresentationBuilder doopReprBuilder;

    private MethodSymbol currentMethodSymbol;
    private ClassSymbol currentClassSymbol;
    private String compilationUnitName;
    private String currentMethodDoopSignature;
    private String currentMethodCompactName;

    private final Map<ClassSymbol, Map<String, Integer>> methodNamesPerClassMap;
    private int constructorInvocationCounter;

    private int methodInvocationCounter;
    private Map<String, Integer> methodInvocationCounterMap;
    private boolean scanForInvocations;

    /**
     *
     * @param lineMap
     */
    public IdentifierScanner(String compilationUnitName, LineMap lineMap) {
        this.doopReprBuilder = DoopRepresentationBuilder.getInstance();
        this.lineMap = lineMap;
        this.constructorInvocationCounter = 0;
        this.methodInvocationCounter = 0;
        this.methodNamesPerClassMap = new HashMap<>();
        this.methodInvocationCounterMap = null;
        this.scanForInvocations = false;
        this.compilationUnitName = compilationUnitName;
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

        /**
         * Add Class to source file report.
         */
        this.currentClassSymbol = tree.sym;
        SourceFileReport.classList.add(new doop.persistent.elements.Class(null, null, this.currentClassSymbol.className()));

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

        }

        scan(tree.mods);
        scan(tree.vartype);
        scan(tree.nameexpr);
        scan(tree.init);
    }

    @Override
    public void visitSkip(JCSkip tree) {
    }

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
        java.lang.Class<?> clazz = tree.meth.getClass();
        scan(tree.args);
        java.lang.reflect.Field field;
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
            SourceFileReport.invocationList.add(new MethodInvocation(null, null, doopMethodInvocation, null));
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
            SourceFileReport.invocationList.add(new MethodInvocation(null, null, doopMethodInvocation, null));
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
        scan(tree.selected);
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
            SourceFileReport.variableList.add(new Variable());
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