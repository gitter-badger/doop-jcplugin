package doop;

import util.Position;

import java.util.Set;

/**
 * Created by anantoni on 24/6/2015.
 */
public class InstanceFieldPointsTo {

    private String fieldSignature = null;
    private Set<HeapAllocation> heapAllocationSet = null;
    private Set<Position> occurences = null;
    private String baseHeapAllocation = null;

    public Set<Position> getOccurences() {
        return occurences;
    }

    /**
     * Constructs a VarPointsTo with a variable name and a heapAllocationSet and its line and column number in the source
     * code level.
     *
     */
    public InstanceFieldPointsTo(String fieldSignature, String baseHeapAllocation, Set<HeapAllocation> haepAllocationSet, Set<Position> occurences) {
        this.fieldSignature = fieldSignature;
        this.heapAllocationSet = heapAllocationSet;
        this.baseHeapAllocation = baseHeapAllocation;
        this.occurences = occurences;
    }

    public String getFieldSignature() {
        return fieldSignature;
    }

    public void setFieldSignature(String fieldSignature) {
        this.fieldSignature = fieldSignature;
    }

    public Set<HeapAllocation> getHeapAllocationSet() {
        return heapAllocationSet;
    }

    public void setHeapAllocationSet(Set<HeapAllocation> heapAllocationSet) {
        this.heapAllocationSet = heapAllocationSet;
    }

    public void setOccurences(Set<Position> occurences) {
        this.occurences = occurences;
    }

    public String getBaseHeapAllocation() {
        return baseHeapAllocation;
    }

    public void setBaseHeapAllocation(String baseHeapAllocation) {
        this.baseHeapAllocation = baseHeapAllocation;
    }
}
