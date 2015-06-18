package doop;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
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
     * @param methodSigOrCmpctName
     * @param originalType
     * @return
     */
    public String buildDoopHeapAllocation(String methodSigOrCmpctName, String originalType) {
        return methodSigOrCmpctName + "/new " + originalType;
    }

    /**
     *
     * @param doopMethodSignature  the enclosing method signature
     * @param doopMethodInvocation the method invocation string representation
     * @return                     the string representation of the method invocation in Doop
     */
    public String buildDoopMethodInvocationInMethod(String doopMethodSignature, String doopMethodInvocation) {
        StringBuilder doopMethodInvocationInMethod = new StringBuilder();

        doopMethodInvocationInMethod.append(doopMethodSignature + "/" + doopMethodInvocation);
        return doopMethodInvocationInMethod.toString();
    }


    /**
     *
     * @param methodSymbol
     * @return
     */
    public String buildDoopMethodInvocation(MethodSymbol methodSymbol) {
        StringBuilder methodInvocation = new StringBuilder();

        String fqType = buildFQType(methodSymbol.enclClass().getQualifiedName().toString(), methodSymbol.packge());
        methodInvocation.append(fqType).append(".").append(methodSymbol.getQualifiedName());

        return methodInvocation.toString();
    }

    /**
     *
     * @param methodSymbol
     * @return
     */
    public StringBuilder buildDoopMethodSignatureNoArgs(MethodSymbol methodSymbol) {
        /**
         * STEP 1: Build enclosing class name
         * Remove "package." if it exists and replace all occurrences of '.' with '$'.
         * Afterwards insert the package name at the start of the sequence.
         * e.g
         * test.Test.NestedTest.NestedNestedTest will be converted to
         * test.Test$NestedTest$NestedNestedTest
         */
        StringBuilder methodSignatureNoArgs = new StringBuilder();
        String fqType = buildFQType(methodSymbol.enclClass().getQualifiedName().toString(), methodSymbol.packge());

        methodSignatureNoArgs.append(fqType + ":").
                                append(" " +methodSymbol.getReturnType() + " ").
                                append(methodSymbol.getQualifiedName());

        return methodSignatureNoArgs;
    }

    /**
     * Builds the signature of a method based on its method symbol.
     *
     * @param methodSymbol the symbol of the method
     * @return the signature of the method as a string
     */
    public String buildDoopMethodSignature(MethodSymbol methodSymbol) {
        /**
         * Append method signature: <return_type> <method_name>((<parameter_type>,)*<parameter_type>?)
         */
        StringBuilder methodSignature = new StringBuilder();

        methodSignature.append(buildDoopMethodSignatureNoArgs(methodSymbol)).append("(");
        methodSignature.insert(0, '<');

        /**
         * Append fully qualified types of method arguments
         */
        List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Symbol.VarSymbol param = parameters.get(i);
            String fqType = buildFQType(param.type.toString(), param.packge());

            if (i != parameters.size() - 1)
                methodSignature.append(fqType).append(',');
            else
                methodSignature.append(fqType);
        }
        methodSignature.append(")>");

        return methodSignature.toString();
    }

    /**
     * Builds the signature of the declaring method of a variable symbol.
     *
     * @param methodSymbol the symbol of the method
     * @return the compact name of the method as a string
     */
    public String buildDoopMethodCompactName(MethodSymbol methodSymbol) {
        StringBuilder methodCompactName = new StringBuilder();

        String fqType = buildFQType(methodSymbol.enclClass().getQualifiedName().toString(), methodSymbol.packge());
        methodCompactName.append(fqType + "." + methodSymbol.getQualifiedName().toString());

        return methodCompactName.toString();
    }
}
