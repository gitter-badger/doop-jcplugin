package visitors;

import com.sun.source.tree.LineMap;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Assert;
import com.sun.tools.javac.util.List;
import doop.DoopRepresentationBuilder;
import doop.HeapAllocation;
import doop.MethodDeclaration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anantoni on 11/6/2015.
 */

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

/**
 * InitialScanner scans the compilation unit for heap allocations and method definitions.
 */
public class InitialScanner extends TreeScanner {

    private final LineMap lineMap;
    private final DoopRepresentationBuilder doopReprBuilder;

    private ClassSymbol currentClassSymbol;
    private final Map<String, Integer> methodNamesMap;
    private MethodSymbol currentMethodSymbol;
    private String currentMethodDoopSignature;
    private String currentMethodCompactName;

    private int heapAllocationCounter;
    private Map<String, Integer> heapAllocationCounterMap = null;

    /**
     * The following two maps will be used by the IdentifierScanner.
     */
    private Map<String, HeapAllocation> heapAllocationMap = null;
    private Map<String, MethodDeclaration> methodDeclarationMap = null;


    /**
     * *************************************************************************
     * Constructors
     * *************************************************************************
     */
    public InitialScanner() {
        this(null);
    }


    /**
     * @param lineMap holds the line, column information for each symbol.
     */
    public InitialScanner(LineMap lineMap) {
        this.doopReprBuilder = DoopRepresentationBuilder.getInstance();
        this.lineMap = lineMap;
        this.heapAllocationCounter = 0;
        this.methodNamesMap = new HashMap<>();
        this.heapAllocationCounterMap = new HashMap<>();
        this.heapAllocationMap = new HashMap<>();
        this.methodDeclarationMap = new HashMap<>();
    }



    /**
     * *************************************************************************
     * Getters and Setters
     * *************************************************************************
     */
    public Map<String, HeapAllocation> getHeapAllocationMap() {
        return heapAllocationMap;
    }

    public void setHeapAllocationMap(Map<String, HeapAllocation> heapAllocationMap) {
        this.heapAllocationMap = heapAllocationMap;
    }

    public Map<String, MethodDeclaration> getMethodDeclarationMap() {
        return methodDeclarationMap;
    }

    public void setMethodDeclarationMap(Map<String, MethodDeclaration> methodDeclarationMap) {
        this.methodDeclarationMap = methodDeclarationMap;
    }



    /**
     * Visitor method: Scan a single node.
     *
     * @param tree
     */
    @Override
    public void scan(JCTree tree) {
        if (tree instanceof JCTree.JCIdent) {
            if (((JCTree.JCIdent) tree).sym instanceof MethodSymbol)
                System.out.println(((JCTree.JCIdent) tree).sym.getQualifiedName().toString());
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
                        System.out.println(((JCTree.JCIdent) l.head).sym.getQualifiedName().toString());
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

        System.out.println("Method names map: " + this.methodNamesMap);

        scan(tree.mods);
        scan(tree.typarams);
        scan(tree.extending);
        scan(tree.implementing);
        scan(tree.defs);
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        this.currentMethodSymbol = tree.sym;
        this.currentMethodDoopSignature = this.doopReprBuilder.buildDoopMethodSignature(currentMethodSymbol);
        this.currentMethodCompactName = this.doopReprBuilder.buildDoopMethodCompactName(currentMethodSymbol);

        scan(tree.mods);
        scan(tree.restype);
        methodDeclarationMap.put(this.currentMethodDoopSignature,
                                    new MethodDeclaration(lineMap.getLineNumber(tree.pos),
                                                            lineMap.getColumnNumber(tree.pos),
                                                            lineMap.getColumnNumber(tree.pos + 4),
                                                            this.currentMethodDoopSignature));

        System.out.println("Method Declaration: " + currentMethodDoopSignature);

        scan(tree.typarams);
        scan(tree.recvparam);
        scan(tree.params);
        scan(tree.thrown);
        scan(tree.defaultValue);
        scan(tree.body);
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
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
        scan(tree.args);
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass tree) {
        scan(tree.encl);
        scan(tree.typeargs);
        scan(tree.clazz);
        scan(tree.args);
        scan(tree.def);

        System.out.println("tree.clazz.type: " + tree.clazz.toString());

        for (Symbol symbol : currentClassSymbol.getEnclosedElements()) {
            System.out.println(symbol.toString());
        }

        String heapAllocation;
        /**
         * If current method is overloaded use its signature to build the heap allocation.
         */
        if (this.methodNamesMap.get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
            heapAllocation = this.doopReprBuilder.buildDoopHeapAllocation(currentMethodDoopSignature, tree.clazz.type.getOriginalType().toString());
        /**
         * Otherwise use its compact name.
         */
        else
            heapAllocation = this.doopReprBuilder.buildDoopHeapAllocation(currentMethodCompactName, tree.clazz.type.getOriginalType().toString());

        /**
         * Evaluate heap allocation counter within method.
         */
        if (heapAllocationCounterMap.containsKey(heapAllocation)) {
            heapAllocationCounter = heapAllocationCounterMap.get(heapAllocation) + 1;
            heapAllocationCounterMap.put(heapAllocation, heapAllocationCounter);
        }
        else {
            heapAllocationCounter = 0;
            heapAllocationCounterMap.put(heapAllocation, 0);
        }
        heapAllocation += "/" + heapAllocationCounter;

        /**
         * Report Heap Allocation
         */
        heapAllocationMap.put(heapAllocation, new HeapAllocation(lineMap.getLineNumber(tree.clazz.pos),
                                                                    lineMap.getColumnNumber(tree.clazz.pos),
                                                                    lineMap.getColumnNumber(tree.clazz.pos + tree.clazz.toString().length()),
                                                                    heapAllocation));
        System.out.println("Found HeapAllocation: " + heapAllocation);
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
