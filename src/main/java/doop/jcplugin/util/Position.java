package doop.jcplugin.util;

/**
 * Created by anantoni on 24/6/2015.
 */
public class Position {
    private long startLine;
    private long startColumn;
    private long endLine;
    private long endColumn;
    private String compilationUnitName;

    public Position(long startLine, long startColumn, long endColumn) {
        this(startLine, startColumn, startLine, endColumn);
    }

    public Position(long startLine, long startColumn, long endLine, long endColumn) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
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

    public String getCompilationUnitName() {
        return this.compilationUnitName;
    }

    public String toString() {
        return "(" + this.startLine + ", " + this.startColumn + ", " + this.endColumn + ")";
    }


}