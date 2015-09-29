package doop.jcplugin.symbols;

import doop.jcplugin.util.Position;

import java.lang.*;

/**
 * Created by anantoni on 22/7/2015.
 */
public class Method extends Symbol {
    private String name;
    private Class enclosingClass;
    private String returnType;
    private String signature;
    private String doopSignature;
    private String doopCompactName;
    private String[] args;
    private String[] argTypes;

    public Method() {}

    public Method(Position position) {
        this(position, null, null, null, null, null, null, null, null);
    }

    public Method(Position position, String name) {
        this(position, name, null, null, null, null, null, null, null);
    }

    public Method(Position position, String name, Class enclosingClass) {
        this(position, name, enclosingClass, null, null, null, null, null, null);
    }

    public Method(Position position, String name, Class enclosingClass, String returnType) {
        this(position, name, enclosingClass, returnType, null, null, null, null, null);
    }

    public Method(Position position, String name, Class enclosingClass, String returnType, String doopSignature) {
        this(position, name, enclosingClass, returnType, doopSignature, null, null, null, null);
    }

    public Method(Position position, String name, Class enclosingClass, String returnType, String doopSignature, String doopCompactName) {
        this(position, name, enclosingClass, returnType, doopSignature, doopCompactName, null, null, null);
    }

    public Method(Position position, String name, Class enclosingClass, String returnType, String doopSignature, String doopCompactName, String[] args) {
        this(position, name, enclosingClass, returnType, doopSignature, doopSignature, doopCompactName, args, null);
    }

    public Method(Position position, String name, Class enclosingClass, String returnType, String signature, String doopSignature,
                    String doopCompactName, String[] args, String[] argTypes) {
        super(position);
        this.name = name;
        this.enclosingClass = enclosingClass;
        this.returnType = returnType;
        this.signature = signature;
        this.doopSignature = doopSignature;
        this.doopCompactName = doopCompactName;
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

    public String getDoopSignature() {
        return doopSignature;
    }

    public void setDoopSignature(String doopSignature) {
        this.doopSignature = doopSignature;
    }

    public String getDoopCompactName() { return doopCompactName; }

    public void setDoopCompactName(String doopCompactName) {
        this.doopCompactName = doopCompactName;
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
