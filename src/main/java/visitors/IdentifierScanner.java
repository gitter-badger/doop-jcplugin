package visitors;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Assert;
import com.sun.tools.javac.util.List;
import doop.DoopRepresentationBuilder;
import doop.VarPointsTo;
import reporters.Reporter;

import java.lang.reflect.Field;
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
    private final Reporter reporter;
    private DoopRepresentationBuilder doopReprBuilder = null;
    private int methodInvocationCounter;
    private int constructorInvocationCounter;
    private MethodSymbol currentMethodSymbol;

    public IdentifierScanner(Reporter reporter) {
        this(reporter, null);
    }

    public IdentifierScanner(Reporter reporter, Map<String, Set<String>> vptMap) {
        this.doopReprBuilder = DoopRepresentationBuilder.getInstance();
        this.reporter = reporter;
        this.vptMap = vptMap;
        this.constructorInvocationCounter = 0;
        this.methodInvocationCounter = 0;
    }

    /**
     * Visitor method: Scan a single node.
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
        scan(tree.mods);
        scan(tree.typarams);
        scan(tree.extending);
        scan(tree.implementing);
        scan(tree.defs);
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        this.constructorInvocationCounter = 0;
        this.methodInvocationCounter = 0;
        this.currentMethodSymbol = tree.sym;
        scan(tree.mods);
        scan(tree.restype);
        scan(tree.typarams);
        scan(tree.recvparam);
        scan(tree.params);
        scan(tree.thrown);
        scan(tree.defaultValue);
        scan(tree.body);
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
        if (tree.sym.getEnclosingElement() instanceof MethodSymbol) {
            System.out.println("##########################################################################################################################");
            System.out.println("Variable name: " + tree.sym.getQualifiedName().toString());
            System.out.println("Type: " + tree.type);
            String methodSignatureInDoop = this.doopReprBuilder.buildDoopMethodSignature((MethodSymbol)tree.sym.getEnclosingElement());
            String varNameInDoop = this.doopReprBuilder.buildDoopVarName(methodSignatureInDoop, tree.sym.getQualifiedName().toString());
            System.out.println("Variable name in Doop: " + varNameInDoop);
            System.out.println("##########################################################################################################################");

            if( this.vptMap != null) {
                if (this.vptMap.containsKey(varNameInDoop)) {
                    Set<String> heapAllocationSet = this.vptMap.get(varNameInDoop);
                    reporter.reportVarPointsTo(new VarPointsTo(tree.pos, tree.pos + tree.name.length(), varNameInDoop, heapAllocationSet));
                }
            }
            else {
                reporter.reportVarPointsTo(new VarPointsTo(tree.pos, tree.pos + tree.name.length(), varNameInDoop, new HashSet<>()));
            }
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
        Field field;
        Object fieldValue = null;

        try {
            field = clazz.getField("sym");
            fieldValue = field.get(tree.meth);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (fieldValue instanceof MethodSymbol)
            System.out.println("\033[35m Method Invocation: \033[0m" + this.doopReprBuilder.buildDoopMethodInvocationInMethod(
                    this.doopReprBuilder.buildDoopMethodSignature(currentMethodSymbol),
                    this.doopReprBuilder.buildDoopMethodInvocation((Symbol.MethodSymbol) fieldValue) + "/" + this.methodInvocationCounter++));
        scan(tree.args);
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass tree) {
        scan(tree.encl);
        scan(tree.typeargs);
        scan(tree.clazz);
        scan(tree.args);
        scan(tree.def);
        System.out.println("\033[35m Method Invocation (Constructor): \033[0m" + this.doopReprBuilder.buildDoopMethodInvocationInMethod(
                this.doopReprBuilder.buildDoopMethodSignature(currentMethodSymbol),
                this.doopReprBuilder.buildDoopMethodInvocation((MethodSymbol) tree.constructor) + "/" + this.constructorInvocationCounter++));
    }

    @Override
    public void visitNewArray(JCTree.JCNewArray tree) {
        scan(tree.annotations);
        scan(tree.elemtype);
        scan(tree.dims);
        tree.dimAnnotations.stream().forEach((annos) -> scan(annos));
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
        System.out.println("Field access/Method invocation: " + tree.toString());
        scan(tree.selected);
    }

    @Override
    public void visitReference(JCTree.JCMemberReference tree) {
        System.out.println("Member reference: " + tree.toString());
        scan(tree.expr);
        scan(tree.typeargs);
    }

    @Override
    public void visitIdent(JCTree.JCIdent tree) {
        if (tree.sym != null && tree.sym.isLocal()) {
            System.out.println("##########################################################################################################################");
            System.out.println("IDENTIFIER name: " + tree.sym.getQualifiedName().toString());
            System.out.println("Declaring method signature: " + this.doopReprBuilder.buildDoopMethodSignature((MethodSymbol) tree.sym.getEnclosingElement()));
            System.out.println("Qualified name: " + tree.sym.getQualifiedName());
            System.out.println("Type: " + tree.sym.type);
            System.out.println("##########################################################################################################################");
        }
        if (tree.sym != null && tree.sym instanceof MethodSymbol) {
            System.out.println("##########################################################################################################################");
            System.out.println("Identifier: " + tree.name);
            System.out.println("IDENTIFIER name: " + tree.sym.getQualifiedName().toString());
            System.out.println("Method signature: " + this.doopReprBuilder.buildDoopMethodSignature((MethodSymbol) tree.sym));
            System.out.println("Qualified name: " + tree.sym.getQualifiedName());
            System.out.println("Type: " + tree.sym.type);
            System.out.println("Position: " + tree.pos);
            System.out.println("##########################################################################################################################");
//            System.out.println("Method invocation: " + ((Symbol.MethodSymbol) tree.sym).name.toString()+invocationCounter++);
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