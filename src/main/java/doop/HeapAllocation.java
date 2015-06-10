package doop;

/**
 * Created by anantoni on 10/6/2015.
 */
public class HeapAllocation {
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

    public HeapAllocation(long startLine, long startColumn, long endLine, long endColumn, String doopAllocationName) {
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

    public String getDoopAllocationName() {
        return doopAllocationName;
    }

    public void setDoopAllocationName(String doopAllocationName) {
        this.doopAllocationName = doopAllocationName;
    }
}
