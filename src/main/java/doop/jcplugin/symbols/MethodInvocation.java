package doop.jcplugin.symbols;

import doop.jcplugin.util.Position;

/**
 * Created by anantoni on 22/7/2015.
 */
public class MethodInvocation extends Symbol {
    private String methodInvocationID;
    private Method invokingMethod;

    public MethodInvocation() {}

    public MethodInvocation(Position position) {
        super(position);
    }

    public String getMethodInvocationID() {
        return methodInvocationID;
    }

    public void setMethodInvocationID(String methodInvocationID) {
        this.methodInvocationID = methodInvocationID;
    }

    public Method getInvokingMethod() {
        return invokingMethod;
    }

    public void setInvokingMethod(Method invokingMethod) {
        this.invokingMethod = invokingMethod;
    }
}
