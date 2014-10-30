package visitors;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Assert;
import com.sun.tools.javac.util.List;

/** A subclass of Tree.Visitor, this class defines
 *  a general tree scanner pattern. Translation proceeds recursively in
 *  left-to-right order down a tree. There is one visitor method in this class
 *  for every possible kind of tree node.  To obtain a specific
 *  scanner, it suffices to override those visitor methods which
 *  do some interesting work. The scanner class itself takes care of all
 *  navigational aspects.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class IdentifierScanner extends TreeScanner {

    /** Visitor method: Scan a single node.
     */
    public void scan(JCTree tree) {
        if(tree!=null) tree.accept(this);
    }

    /** Visitor method: scan a list of nodes.
     */
    public void scan(List<? extends JCTree> trees) {
        if (trees != null)
            for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
                scan(l.head);
    }

    public String buildFQType(String type, String packge) {
        StringBuilder fqType = new StringBuilder();
        fqType.append(type.replaceFirst("^[a-z].*\\.", "").replace(".", "$"));

        if (!(packge.equals("")))
            fqType.insert(0, packge);

        return fqType.toString();
    }

    public String buildMethodSignature(JCTree.JCIdent tree) {
        /**
         *  Building the declaring method signature
         */

        /**
         * STEP 1: Build enclosing class name
         * Remove "package." if it exists and replace all occurences of '.' with '$'.
         * Afterwards insert the package name at the start of the sequence.
         * e.g
         * test.Test.NestedTest.NestedNestedTest will be converted to
         * test.Test$NestedTest$NestedNestedTest
         */
        StringBuilder methodSignature = new StringBuilder();
        methodSignature.append(tree.sym.enclClass().toString().
                replaceFirst("^[a-z].*\\.", "").replace(".", "$")).append(':');

        if (!(tree.sym.packge().getQualifiedName().toString().equals("")))
            methodSignature.insert(0, tree.sym.packge().getQualifiedName().toString());

        /**
         * STEP 2: Append method signature: <return type> <method name>((<parameter type>,)*<parameter type?)
         */
        Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) tree.sym.getEnclosingElement();
        methodSignature.append(" ").append(methodSymbol.getReturnType()).
                append(" ").append(methodSymbol.getQualifiedName()).
                append("(");

        List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Symbol.VarSymbol param = parameters.get(i);
            String fqType = buildFQType(param.type.toString(), param.packge().getQualifiedName().toString());

            if (i != parameters.size() - 1)
                methodSignature.append(fqType).append(',');
            else
                methodSignature.append(fqType).append(')');
        }
        methodSignature.insert(0, '<');
        methodSignature.append('>');

        return methodSignature.toString();
    }

    public String buildMethodSignature(JCTree.JCVariableDecl tree) {
        /**
         *  Building the declaring method signature
         */

        /**
         * STEP 1: Build enclosing class name
         * Remove "package." if it exists and replace all occurences of '.' with '$'.
         * Afterwards insert the package name at the start of the sequence.
         * e.g
         * test.Test.NestedTest.NestedNestedTest will be converted to
         * test.Test$NestedTest$NestedNestedTest
         */
        StringBuilder methodSignature = new StringBuilder();
        methodSignature.append(tree.sym.enclClass().toString().
                replaceFirst("^[a-z].*\\.", "").replace(".", "$")).append(':');

        if (!(tree.sym.packge().getQualifiedName().toString().equals("")))
            methodSignature.insert(0, tree.sym.packge().getQualifiedName().toString());

        /**
         * STEP 2: Append method signature: <return type> <method name>((<parameter type>,)*<parameter type?)
         */
        Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) tree.sym.getEnclosingElement();
        methodSignature.append(" ").append(methodSymbol.getReturnType()).
                append(" ").append(methodSymbol.getQualifiedName()).
                append("(");

        List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Symbol.VarSymbol param = parameters.get(i);
            String fqType = buildFQType(param.type.toString(), param.packge().getQualifiedName().toString());

            if (i != parameters.size() - 1)
                methodSignature.append(fqType).append(',');
            else
                methodSignature.append(fqType).append(')');
        }
        methodSignature.insert(0, '<');
        methodSignature.append('>');

        return methodSignature.toString();
    }


/* ***************************************************************************
 * Visitor methods
 ****************************************************************************/

    public void visitTopLevel(JCTree.JCCompilationUnit tree) {
        scan(tree.packageAnnotations);
        scan(tree.pid);
        scan(tree.defs);
    }

    public void visitImport(JCTree.JCImport tree) {
        scan(tree.qualid);
    }

    public void visitClassDef(JCTree.JCClassDecl tree) {
        scan(tree.mods);
        scan(tree.typarams);
        scan(tree.extending);
        scan(tree.implementing);
        scan(tree.defs);
    }

    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        scan(tree.mods);
        scan(tree.restype);
        scan(tree.typarams);
        scan(tree.recvparam);
        scan(tree.params);
        scan(tree.thrown);
        scan(tree.defaultValue);
        scan(tree.body);
    }

    public void visitVarDef(JCTree.JCVariableDecl tree) {
        if (tree.sym.getEnclosingElement() instanceof Symbol.MethodSymbol) {
            System.out.println("##########################################################################################################################");
            System.out.println("Variable name: " + tree.sym.getQualifiedName().toString());
            System.out.println("Declaring method signature: " + buildMethodSignature(tree));
            System.out.println("Type: " + tree.type);
            System.out.println("##########################################################################################################################");
        }


        scan(tree.mods);
        scan(tree.vartype);
        scan(tree.nameexpr);
        scan(tree.init);
    }

    public void visitSkip(JCTree.JCSkip tree) {
    }

    public void visitBlock(JCTree.JCBlock tree) {
        scan(tree.stats);
    }

    public void visitDoLoop(JCTree.JCDoWhileLoop tree) {
        scan(tree.body);
        scan(tree.cond);
    }

    public void visitWhileLoop(JCTree.JCWhileLoop tree) {
        scan(tree.cond);
        scan(tree.body);
    }

    public void visitForLoop(JCTree.JCForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        scan(tree.step);
        scan(tree.body);
    }

    public void visitForeachLoop(JCTree.JCEnhancedForLoop tree) {
        scan(tree.var);
        scan(tree.expr);
        scan(tree.body);
    }

    public void visitLabelled(JCTree.JCLabeledStatement tree) {
        scan(tree.body);
    }

    public void visitSwitch(JCTree.JCSwitch tree) {
        scan(tree.selector);
        scan(tree.cases);
    }

    public void visitCase(JCTree.JCCase tree) {
        scan(tree.pat);
        scan(tree.stats);
    }

    public void visitSynchronized(JCTree.JCSynchronized tree) {
        scan(tree.lock);
        scan(tree.body);
    }

    public void visitTry(JCTree.JCTry tree) {
        scan(tree.resources);
        scan(tree.body);
        scan(tree.catchers);
        scan(tree.finalizer);
    }

    public void visitCatch(JCTree.JCCatch tree) {
        scan(tree.param);
        scan(tree.body);
    }

    public void visitConditional(JCTree.JCConditional tree) {
        scan(tree.cond);
        scan(tree.truepart);
        scan(tree.falsepart);
    }

    public void visitIf(JCTree.JCIf tree) {
        scan(tree.cond);
        scan(tree.thenpart);
        scan(tree.elsepart);
    }

    public void visitExec(JCTree.JCExpressionStatement tree) {
        scan(tree.expr);
    }

    public void visitBreak(JCTree.JCBreak tree) {
    }

    public void visitContinue(JCTree.JCContinue tree) {
    }

    public void visitReturn(JCTree.JCReturn tree) {
        scan(tree.expr);
    }

    public void visitThrow(JCTree.JCThrow tree) {
        scan(tree.expr);
    }

    public void visitAssert(JCTree.JCAssert tree) {
        scan(tree.cond);
        scan(tree.detail);
    }

    public void visitApply(JCTree.JCMethodInvocation tree) {
        scan(tree.typeargs);
        scan(tree.meth);
        scan(tree.args);
    }

    public void visitNewClass(JCTree.JCNewClass tree) {
        scan(tree.encl);
        scan(tree.typeargs);
        scan(tree.clazz);
        scan(tree.args);
        scan(tree.def);
    }

    public void visitNewArray(JCTree.JCNewArray tree) {
        scan(tree.annotations);
        scan(tree.elemtype);
        scan(tree.dims);
        for (List<JCTree.JCAnnotation> annos : tree.dimAnnotations)
            scan(annos);
        scan(tree.elems);
    }

    public void visitLambda(JCTree.JCLambda tree) {
        scan(tree.body);
        scan(tree.params);
    }

    public void visitParens(JCTree.JCParens tree) {
        scan(tree.expr);
    }

    public void visitAssign(JCTree.JCAssign tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    public void visitAssignop(JCTree.JCAssignOp tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    public void visitUnary(JCTree.JCUnary tree) {
        scan(tree.arg);
    }

    public void visitBinary(JCTree.JCBinary tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    public void visitTypeCast(JCTree.JCTypeCast tree) {
        scan(tree.clazz);
        scan(tree.expr);
    }

    public void visitTypeTest(JCTree.JCInstanceOf tree) {
        scan(tree.expr);
        scan(tree.clazz);
    }

    public void visitIndexed(JCTree.JCArrayAccess tree) {
        scan(tree.indexed);
        scan(tree.index);
    }

    public void visitSelect(JCTree.JCFieldAccess tree) {
        scan(tree.selected);
    }

    public void visitReference(JCTree.JCMemberReference tree) {
        scan(tree.expr);
        scan(tree.typeargs);
    }

    public void visitIdent(JCTree.JCIdent tree) {

        if (tree.sym.getEnclosingElement() instanceof Symbol.MethodSymbol) {
            System.out.println("##########################################################################################################################");
            System.out.println("Variable name: " + tree.sym.getQualifiedName().toString());
            System.out.println("Declaring method signature: " + buildMethodSignature(tree));
            System.out.println("Type: " + tree.sym.type);
            System.out.println("##########################################################################################################################");
        }
    }

    public void visitLiteral(JCTree.JCLiteral tree) {
    }

    public void visitTypeIdent(JCTree.JCPrimitiveTypeTree tree) {
    }

    public void visitTypeArray(JCTree.JCArrayTypeTree tree) {
        scan(tree.elemtype);
    }

    public void visitTypeApply(JCTree.JCTypeApply tree) {
        scan(tree.clazz);
        scan(tree.arguments);
    }

    public void visitTypeUnion(JCTree.JCTypeUnion tree) {
        scan(tree.alternatives);
    }

    public void visitTypeIntersection(JCTree.JCTypeIntersection tree) {
        scan(tree.bounds);
    }

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

    public void visitModifiers(JCTree.JCModifiers tree) {
        scan(tree.annotations);
    }

    public void visitAnnotation(JCTree.JCAnnotation tree) {
        scan(tree.annotationType);
        scan(tree.args);
    }

    public void visitAnnotatedType(JCTree.JCAnnotatedType tree) {
        scan(tree.annotations);
        scan(tree.underlyingType);
    }

    public void visitErroneous(JCTree.JCErroneous tree) {
    }

    public void visitLetExpr(JCTree.LetExpr tree) {
        scan(tree.defs);
        scan(tree.expr);
    }

    public void visitTree(JCTree tree) {
        Assert.error();
    }
}
