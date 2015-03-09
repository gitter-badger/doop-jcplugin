/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doop.facts;

/**
 *
 * @author anantoni
 */
public class VarPointsTo {
    private String var = null;
    private String heapAllocation = null;


    /**
     * Constructs a VarPointsTo with a refmode
     *
     * @param var the variable of VarPointsTo
     */
    public VarPointsTo(String var)
    {
        this(var, null);
    }


    /**
     * Constructs a VarPointsTo with a variable and a heapAllocation.
     *
     * @param var  the name 
     * @param heapAllocation  the heapAllocation of VarPointsTo
     */
    public VarPointsTo(String var, String heapAllocation)
    {
        this.var = var;
        this.heapAllocation = heapAllocation;
    }

    /**
     * Sets the var of this VarPointsTo.
     *
     * @param var the var to set
     */
    public void setVar(String var) {
        this.var = var;
    }


    /**
     * Returns the var of this VarPointsTo.
     *
     * @return var the var of this VarPointsTo
     */
    public String getVar() {
        return this.var;
    }

    /**
     * Sets the type of this variable.
     *
     * @param heapAllocation
     */
    public void setHeapAllocation(String heapAllocation) {
        this.heapAllocation = heapAllocation;
    }

    /**
     * Returns the type of this variable
     *
     * @return type  the type of this variable
     */
    public String getHeapAllocation() {
        return this.heapAllocation;
    }
}
