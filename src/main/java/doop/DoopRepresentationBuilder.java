package doop;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.List;

/**
 * Created by anantoni on 14/5/2015.
 */
public class DoopRepresentationBuilder {
    private static DoopRepresentationBuilder instance = null;

    /**
     * Forbid instantiation outside the class
     */
    private DoopRepresentationBuilder() {}

    /**
     * @return the sole DoopRepresentationBuilder instance
     */
    public static DoopRepresentationBuilder getInstance() {
        if(instance == null)
            instance = new DoopRepresentationBuilder();
        return instance;
    }

    /**
     * Builds the fully qualified name of a type.
     *
     * @param type             the string representation of the type
     * @param packageSymbol    the package symbol
     * @return fqType          the fully qualified type
     */
    public String buildFQType(String type, Symbol.PackageSymbol packageSymbol) {
        StringBuilder fqTypeName = new StringBuilder();
        String packge = packageSymbol.getQualifiedName().toString();

        if (type.startsWith(packge) && !(packge.equals(""))) {
            type = type.substring(packge.length() + 1).replace('.', '$');
            fqTypeName.append(packge).append('.').append(type);
        } else
            fqTypeName.append(type);
        String fqType = fqTypeName.toString();
        return fqType;
    }

    /**
     * Builds the string representation of a variable name in Doop by combining the declaring method signature in Doop
     * and the qualified name of the variable.
     *
     * @param doopMethodSignature the declaring method signature
     * @param varQualifiedName    the qualified name of the variable
     * @return                    the string representation of the variable name in Doop
     */
    public String buildDoopVarName(String doopMethodSignature, String varQualifiedName) {
        return doopMethodSignature + "/" + varQualifiedName;
    }

    /**
     *
     * @param doopMethodSignature  the enclosing method signature
     * @param doopMethodInvocation the method invocation string representation
     * @return                     the string representation of the method invocation in Doop
     */
    public String buildDoopMethodInvocation(String doopMethodSignature, String doopMethodInvocation) {
        return doopMethodSignature + "/" + doopMethodInvocation;
    }

    /**
     * Builds the signature of the declaring method of a variable symbol.
     *
     * @param methodSymbol the symbol of the variable declaration
     * @return the signature of the declaring method as a string
     */
    public String buildDoopMethodSignature(Symbol.MethodSymbol methodSymbol) {

        /**
         * STEP 1: Build enclosing class name
         * Remove "package." if it exists and replace all occurrences of '.' with '$'.
         * Afterwards insert the package name at the start of the sequence.
         * e.g
         * test.Test.NestedTest.NestedNestedTest will be converted to
         * test.Test$NestedTest$NestedNestedTest
         */
        StringBuilder methodSignature = new StringBuilder();
        String fqType = buildFQType(methodSymbol.enclClass().getQualifiedName().toString(), methodSymbol.packge());

        /**
         * STEP 2: Append method signature: <return_type> <method_name>((<parameter_type>,)*<parameter_type>?)
         *
         * Special handling for main method
         */
        if (methodSymbol.isStatic() && methodSymbol.name.toString().equals("main") &&
                methodSymbol.params.length() == 1) {
            Symbol.VarSymbol param = methodSymbol.params.get(0);
            String paramFQType = buildFQType(param.type.toString(), param.packge());
            if (paramFQType.equals("java.lang.String[]"))
                return fqType + "." + methodSymbol.getQualifiedName().toString();
        }

        /**
         * Constructors and other methods don't need any kind of special handling
         * the only difference is that the method name in the case of constructors
         * is <init>
         */
        methodSignature.append(fqType).append(":");
        methodSignature.append(" ").append(methodSymbol.getReturnType()).
                append(" ").append(methodSymbol.getQualifiedName()).
                append("(");

        List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();
        /**
         * Append fully qualified types of method arguments
         */
        for (int i = 0; i < parameters.size(); i++) {
            Symbol.VarSymbol param = parameters.get(i);
            fqType = buildFQType(param.type.toString(), param.packge());

            if (i != parameters.size() - 1)
                methodSignature.append(fqType).append(',');
            else
                methodSignature.append(fqType);
        }

        methodSignature.insert(0, '<');
        methodSignature.append(")>");

        return methodSignature.toString();
    }
}
