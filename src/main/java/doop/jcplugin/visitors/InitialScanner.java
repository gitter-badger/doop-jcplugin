package doop.jcplugin.visitors;

import com.sun.source.tree.LineMap;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Assert;
import com.sun.tools.javac.util.List;
import doop.jcplugin.representation.DoopRepresentationBuilder;

import java.util.HashMap;
import java.util.Map;

import doop.jcplugin.util.SourceFileReport;
import doop.persistent.elements.HeapAllocation;
import doop.persistent.elements.Method;
import doop.persistent.elements.Position;
import doop.persistent.elements.Class;

import static com.sun.tools.javac.code.Symbol.*;
import static com.sun.tools.javac.tree.JCTree.*;

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
 *
 * This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.
 */

/**
 * InitialScanner scans the compilation unit for heap allocations and method definitions.
 */
public class InitialScanner extends TreeScanner {

    private final LineMap lineMap;
    private final DoopRepresentationBuilder doopReprBuilder;
    private String sourceFileName;
    private ClassSymbol currentClassSymbol;
    private Class currentClass;
    private final Map<ClassSymbol, Map<String, Integer>> methodNamesPerClassMap;
    private MethodSymbol currentMethodSymbol;
    private Method currentMethod;
    private String currentMethodDoopSignature;
    private String currentMethodCompactName;
    private int heapAllocationCounter;
    private Map<String, Integer> heapAllocationCounterMap = null;

    /**
     * @param lineMap holds the line, column information for each symbol.
     */
    public InitialScanner(String sourceFileName, LineMap lineMap) {

        this.doopReprBuilder = DoopRepresentationBuilder.getInstance();
        this.lineMap = lineMap;
        this.heapAllocationCounter = 0;
        this.methodNamesPerClassMap = new HashMap<>();
        this.heapAllocationCounterMap = new HashMap<>();
        this.sourceFileName = sourceFileName;
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
            for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
                scan(l.head);
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


    /**
     * Visit class declaration AST node.
     *
     * @param tree
     */
    @Override
    public void visitClassDef(JCClassDecl tree) {

        this.currentClassSymbol = tree.sym;
        Map<String, Integer> methodNamesMap;


        /**
         * Add class to source file report.
         */
        System.out.println("Reporting class: " + this.currentClassSymbol.className());
        System.out.println("Source file name: " + this.sourceFileName);

        Position position = new Position(lineMap.getLineNumber(tree.pos),
                                            lineMap.getColumnNumber(tree.pos),
                                            lineMap.getColumnNumber(tree.pos + this.currentClassSymbol.className().length()));

        this.currentClass = new Class(position, this.sourceFileName, this.currentClassSymbol.className());
        SourceFileReport.classList.add(this.currentClass);



        if (!methodNamesPerClassMap.containsKey(this.currentClassSymbol)) {
            methodNamesMap = new HashMap<>();
            /**
             * Fills the method names map in order to be able to identify overloaded methods for each class.
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
            methodNamesPerClassMap.put(this.currentClassSymbol, methodNamesMap);
        }

        scan(tree.mods);
        scan(tree.typarams);
        scan(tree.extending);
        scan(tree.implementing);
        scan(tree.defs);
    }


    /**
     * Visit method declaration AST node.
     *
     * @param tree
     */
    @Override
    public void visitMethodDef(JCMethodDecl tree) {

        this.currentMethodSymbol = tree.sym;
        this.currentMethodDoopSignature = this.doopReprBuilder.buildDoopMethodSignature(currentMethodSymbol);
        this.currentMethodCompactName = this.doopReprBuilder.buildDoopMethodCompactName(currentMethodSymbol);

        scan(tree.mods);
        scan(tree.restype);
        scan(tree.typarams);
        scan(tree.recvparam);
        scan(tree.params);
        scan(tree.thrown);
        scan(tree.defaultValue);
        scan(tree.body);

        List<JCVariableDecl> parametersList = tree.getParameters();

        Position position = new Position(lineMap.getLineNumber(tree.pos),
                                         lineMap.getColumnNumber(tree.pos),
                                         lineMap.getColumnNumber(tree.pos + tree.name.toString().length()));

        String[] params = new String[parametersList.length()];
        String[] paramTypes = new String[parametersList.length()];

        int counter = 0;
        for (JCVariableDecl jcVariableDecl : parametersList) {
            params[counter] = jcVariableDecl.getName().toString();
            paramTypes[counter] = jcVariableDecl.vartype.toString();
            counter++;
        }
        /**
         * Add method to source file report.
         */
        System.out.println("Reporting method: " + this.currentMethodSymbol.name);
        System.out.println("Source file name: " + this.sourceFileName);
        System.out.println("Declaring class id: " + this.currentClass.getId());
        System.out.println("Return type: " + this.currentMethodSymbol.getReturnType().toString());
        System.out.println("Doop method signature: " + this.currentMethodDoopSignature);
        System.out.println("Method compact name: " + this.currentMethodCompactName);
        System.out.println("Method parameters: " + params.toString());
        System.out.println("Method parameter types: " + paramTypes.toString());

        this.currentMethod = new Method(position,
                                        this.sourceFileName,
                                        this.currentMethodSymbol.name.toString(),
                                        this.currentClass.getId(),
                                        this.currentMethodSymbol.getReturnType().toString(),
                                        this.currentMethodDoopSignature,
                                        this.currentMethodCompactName,
                                        params,
                                        paramTypes);
        SourceFileReport.methodList.add(this.currentMethod);
    }


    /**
     * Visit variable declaration AST node.
     *
     * @param tree
     */
    @Override
    public void visitVarDef(JCVariableDecl tree) {
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
        scan(tree.args);
    }

    /**
     * Visit "new <T>()" AST node aka heap allocation.
     *
     * @param tree
     */
    @Override
    public void visitNewClass(JCNewClass tree) {
        scan(tree.encl);
        scan(tree.typeargs);
        scan(tree.clazz);
        scan(tree.args);
        scan(tree.def);

        String doopHeapAllocationID;
        /**
         * If current method is overloaded use its signature to build the heap allocation.
         */
        if (this.methodNamesPerClassMap.get(this.currentClassSymbol).get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
            doopHeapAllocationID = this.doopReprBuilder.buildDoopHeapAllocation(currentMethodDoopSignature, tree.clazz.type.getOriginalType().toString());
        /**
         * Otherwise use its compact name.
         */
        else
            doopHeapAllocationID = this.doopReprBuilder.buildDoopHeapAllocation(currentMethodCompactName, tree.clazz.type.getOriginalType().toString());

        /**
         * Evaluate heap allocation counter within method.
         */
        if (heapAllocationCounterMap.containsKey(doopHeapAllocationID)) {
            heapAllocationCounter = heapAllocationCounterMap.get(doopHeapAllocationID) + 1;
            heapAllocationCounterMap.put(doopHeapAllocationID, heapAllocationCounter);
        }
        else {
            heapAllocationCounter = 0;
            heapAllocationCounterMap.put(doopHeapAllocationID, 0);
        }
        doopHeapAllocationID += "/" + heapAllocationCounter;

        /**
         * Add Heap Allocation to source file report.
         */
        System.out.println("Reporting heap allocation: " + doopHeapAllocationID);
        System.out.println("Source file name: " + this.sourceFileName);
        System.out.println("Allocated object type: " + tree.clazz.type.toString());
        System.out.println("Allocating method id: " + this.currentMethod.getId());

        Position position = new Position(lineMap.getLineNumber(tree.clazz.pos),
                                            lineMap.getColumnNumber(tree.clazz.pos),
                                            lineMap.getColumnNumber(tree.clazz.pos + tree.clazz.toString().length()));

        SourceFileReport.heapAllocationList.add(new HeapAllocation(position,
                                                this.sourceFileName,
                                                doopHeapAllocationID,
                                                tree.clazz.type.toString(),
                                                this.currentMethod.getId()));


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

    /**
     * Visit field access AST node.
     *
     * @param tree
     */
    @Override
    public void visitSelect(JCFieldAccess tree) {
        scan(tree.selected);
        if (tree.sym != null && tree.sym instanceof VarSymbol) {
            System.out.println(tree.sym.getClass());
            String fieldSignature = this.doopReprBuilder.buildDoopFieldSignature((VarSymbol) tree.sym);
            System.out.println("Field Signature: " + fieldSignature);

//            if (this.fieldSignatureMap.containsKey(fieldSignature)) {
//                if (this.lineMap.getLineNumber(((VarSymbol) tree.sym).pos) > 0)
//                    this.fieldSignatureMap.get(fieldSignature).add(new Position(this.lineMap.getLineNumber(tree.pos),
//                            this.lineMap.getColumnNumber(tree.pos),
//                            this.lineMap.getColumnNumber(tree.pos + tree.sym.getQualifiedName().toString().length())));
//            }
//            else {
//                Set<Position> positionSet = new HashSet<>();
//                if (this.lineMap.getLineNumber(((VarSymbol) tree.sym).pos) > 0)
//                    positionSet.add(new Position(this.lineMap.getLineNumber(tree.pos),
//                            this.lineMap.getColumnNumber(tree.pos),
//                            this.lineMap.getColumnNumber(tree.pos + tree.sym.getQualifiedName().toString().length())));
//
//                this.fieldSignatureMap.put(fieldSignature, positionSet);
//            }
//            System.out.println(this.fieldSignatureMap);
        }
    }

    @Override
    public void visitReference(JCMemberReference tree) {
        scan(tree.expr);
        scan(tree.typeargs);
    }

    @Override
    public void visitIdent(JCIdent tree) {
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