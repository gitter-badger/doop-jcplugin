package doop.javac_plugin.symbols;

/**
 * Created by anantoni on 10/6/2015.
 */
@SuppressWarnings("unused")
public class HeapAllocation {
    private String heapAllocationID;
    private String type;
    private Method enclosingMethod;

    private long startLine;
    private long startColumn;
    private long endLine;
    private long endColumn;
    private String doopAllocationName = null;

    public HeapAllocation(String doopAllocationName) {
        this(-1, -1, -1, -1, doopAllocationName);
    }

    public HeapAllocation(long startLine, long startColumn, long endColumn, String doopAllocationName) {
        this(startLine, startColumn, startLine, endColumn, doopAllocationName);
    }

    private HeapAllocation(long startLine, long startColumn, long endLine, long endColumn, String doopAllocationName) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.doopAllocationName = doopAllocationName;
    }

    public long getStartLine() {
        return startLine;
    }

    public void setStartLine(long startLine) {
        this.startLine = startLine;
    }

    public long getStartColumn() {
        return this.startColumn;
    }

    public void setStartColumn(long startColumn) {
        this.startColumn = startColumn;
    }

    public long getEndLine() {
        return this.endLine;
    }

    public void setEndLine(long endLine) {
        this.endLine = endLine;
    }

    public long getEndColumn() {
        return this.endColumn;
    }

    public void setEndColumn(long endColumn) {
        this.endColumn = endColumn;
    }

    public String getDoopAllocationName() {
        return this.doopAllocationName;
    }

    public void setDoopAllocationName(String doopAllocationName) {
        this.doopAllocationName = doopAllocationName;
    }
}
