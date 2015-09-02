package doop.javac_plugin.symbols;

import doop.javac_plugin.util.Position;

import java.lang.*;

/**
 * Created by anantoni on 22/7/2015.
 */
public class Method extends SymbolPosition {
    private String name;
    private Class enclosingClass;
    private String returnType;
    private String signature;
    private String doopMethodDeclaration;
    private String[] args;
    private String[] argTypes;

    public Method(Position position, String name, Class enclosingClass, String returnType, String signature, String doopMethodDeclaration,
                    String[] args, String[] argTypes) {
        super(position);
        this.name = name;
        this.enclosingClass = enclosingClass;
        this.returnType = returnType;
        this.signature = signature;
        this.doopMethodDeclaration = doopMethodDeclaration;
        this.args = args;
        this.argTypes = argTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getEnclosingClass() {
        return enclosingClass;
    }

    public void setEnclosingClass(Class enclosingClass) {
        this.enclosingClass = enclosingClass;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getDoopMethodDeclaration() {
        return doopMethodDeclaration;
    }

    public void setDoopMethodDeclaration(String doopMethodDeclaration) {
        this.doopMethodDeclaration = doopMethodDeclaration;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String[] getArgTypes() {
        return argTypes;
    }

    public void setArgTypes(String[] argTypes) {
        this.argTypes = argTypes;
    }
}
