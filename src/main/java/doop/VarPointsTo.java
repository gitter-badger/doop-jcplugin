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
@SuppressWarnings("unused")
public class VarPointsTo {
    private long startLine;
    private long startColumn;
    private long endLine;
    private long endColumn;

    private String varName = null;
    private Set<HeapAllocation> heapAllocationSet = null;

    public long getStartLine() {
        return startLine;
    }

    public void setStartLine(long startLine) {
        this.startLine = startLine;
    }

    public long getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(long startColumn) {
        this.startColumn = startColumn;
    }

    public long getEndLine() {
        return endLine;
    }

    public void setEndLine(long endLine) {
        this.endLine = endLine;
    }

    public long getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(long endColumn) {
        this.endColumn = endColumn;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    /**
     * Constructs a VarPointsTo with a refmode
     *
     * @param var the variable of VarPointsTo
     */
    public VarPointsTo(String var) {
        this(-1, -1, -1, -1, var, null);
    }

    /**
     * Constructs a VarPointsTo with a variable name and a heapAllocationSet.
     *
     * @param varName           the variable name.
     * @param heapAllocationSet the set of possible heap allocations the variable may point to.
     */
    public VarPointsTo(String varName, Set<HeapAllocation> heapAllocationSet) {
        this(-1, -1, -1, -1, varName, heapAllocationSet);
    }

    /**
     * Constructs a VarPointsTo with a variable name and a heapAllocationSet and its line and column number in the source
     * code level.
     *
     * @param varName           the variable name.
     * @param heapAllocationSet the set of possible heap allocations the variable may point to.
     * @param startLine         the starting line number of the variable identifier in the source code
     * @param endLine           the ending line number of the variable identifier in the source code
     *                          (should be the same as startLine in most cases)
     * @param startColumn       the starting column of the variable identifier in the source code
     * @param endColumn         the ending column of the variable identifier in the source code
     */
    public VarPointsTo(long startLine, long startColumn, long endLine, long endColumn, String varName, Set<HeapAllocation> heapAllocationSet) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.varName = varName;
        this.heapAllocationSet = heapAllocationSet;
    }

    /**
     * Returns the var of this VarPointsTo.
     *
     * @return the var of this VarPointsTo
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
    public Set<HeapAllocation> getHeapAllocationSet() {
        return this.heapAllocationSet;
    }

    /**
     * Sets the type of this variable.
     *
     * @param heapAllocationSet
     */
    public void setHeapAllocationSet(Set<HeapAllocation> heapAllocationSet) {
        this.heapAllocationSet = heapAllocationSet;
    }
}
