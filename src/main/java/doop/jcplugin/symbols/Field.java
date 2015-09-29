package doop.jcplugin.symbols;

import doop.jcplugin.util.Position;

import java.lang.*;
import java.util.ArrayList;

/**
 * Created by anantoni on 22/7/2015.
 */
public class Field extends Symbol {
    private String signature;
    private String type;
    private Class enclosingClass;
    private boolean isStatic;
    private ArrayList<long[]> defOccurrences;
    private ArrayList<long[]> useOccurrences;

    public Field() {}

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Class getEnclosingClass() {
        return enclosingClass;
    }

    public void setEnclosingClass(Class enclosingClass) {
        this.enclosingClass = enclosingClass;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setIsStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public ArrayList<long[]> getDefOccurrences() {
        return defOccurrences;
    }

    public void setDefOccurrences(ArrayList<long[]> defOccurrences) {
        this.defOccurrences = defOccurrences;
    }

    public ArrayList<long[]> getUseOccurrences() {
        return useOccurrences;
    }

    public void setUseOccurrences(ArrayList<long[]> useOccurrences) {
        this.useOccurrences = useOccurrences;
    }

    public Field(Position position) {
        super(position);
    }
}
