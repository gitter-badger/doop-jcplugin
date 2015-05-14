/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doop;

import java.util.Set;

/**
 * @author anantoni
 */
public class VarPointsTo {
    private int startPos;
    private int endPos;
    private String varName = null;
    private Set<String> heapAllocationSet = null;


    /**
     * Constructs a VarPointsTo with a refmode
     *
     * @param var the variable of VarPointsTo
     */
    public VarPointsTo(String var) {
        this(-1, -1, var, null);
    }


    /**
     * Constructs a VarPointsTo with a variable and a heapAllocationSet.
     *
     * @param varName           the name
     * @param heapAllocationSet the heapAllocationSet of VarPointsTo
     */
    public VarPointsTo(String varName, Set<String> heapAllocationSet) {
        this(-1, -1, varName, heapAllocationSet);
    }

    public VarPointsTo(int startPos, int endPos, String varName, Set<String> heapAllocationSet) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.varName = varName;
        this.heapAllocationSet = heapAllocationSet;
    }

    /**
     * Returns the var of this VarPointsTo.
     *
     * @return var the var of this VarPointsTo
     */
    public String getVar() {
        return this.varName;
    }

    /**
     * Sets the var of this VarPointsTo.
     *
     * @param var the var to set
     */
    public void setVar(String var) {
        this.varName = var;
    }

    /**
     * Returns the type of this variable
     *
     * @return type  the type of this variable
     */
    public Set<String> getHeapAllocationSet() {
        return this.heapAllocationSet;
    }

    /**
     * Sets the type of this variable.
     *
     * @param heapAllocationSet
     */
    public void setHeapAllocationSet(Set<String> heapAllocationSet) {
        this.heapAllocationSet = heapAllocationSet;
    }
}
