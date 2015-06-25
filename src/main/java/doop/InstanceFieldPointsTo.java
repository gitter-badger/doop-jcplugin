package doop;

import util.Position;
import java.util.Set;

/**
 * Created by anantoni on 24/6/2015.
 */
public class InstanceFieldPointsTo {

    private String fieldSignature = null;
    private Set<HeapAllocation> heapAllocationSet = null;
    private Set<Position> occurrences = null;
    private HeapAllocation baseHeapAllocation = null;

    /**
     * Constructs a VarPointsTo with a variable name and a heapAllocationSet and its line and column number in the source
     * code level.
     *
     */
    public InstanceFieldPointsTo(String fieldSignature, HeapAllocation baseHeapAllocation, Set<HeapAllocation> heapAllocationSet, Set<Position> occurrences) {
        this.fieldSignature = fieldSignature;
        this.heapAllocationSet = heapAllocationSet;
        this.baseHeapAllocation = baseHeapAllocation;
        this.occurrences = occurrences;
    }

    public String getFieldSignature() {
        return this.fieldSignature;
    }

    public void setFieldSignature(String fieldSignature) {
        this.fieldSignature = fieldSignature;
    }

    public Set<HeapAllocation> getHeapAllocationSet() {
        return this.heapAllocationSet;
    }

    public void setHeapAllocationSet(Set<HeapAllocation> heapAllocationSet) {
        this.heapAllocationSet = heapAllocationSet;
    }

    public Set<Position> getOccurrences() {
        return this.occurrences;
    }

    public void setOccurrences(Set<Position> occurrences) {
        this.occurrences = occurrences;
    }

    public HeapAllocation getBaseHeapAllocation() {
        return this.baseHeapAllocation;
    }

    public void setBaseHeapAllocation(HeapAllocation baseHeapAllocation) {
        this.baseHeapAllocation = baseHeapAllocation;
    }
}