package doop;

/**
 * Created by anantoni on 15/6/2015.
 */
public class MethodDeclaration {
    private String methodSignature;
    private long startLine;
    private long startColumn;
    private long endLine;
    private long endColumn;

    public MethodDeclaration() {
        this(-1, 1, -1, -1, null);
    }

    public MethodDeclaration(String methodSignature) {
        this(-1, -1, -1, -1, methodSignature);
    }

    public MethodDeclaration(long startLine, long startColumn, long endColumn, String methodSignature) {
        this(startLine, startColumn, startLine, endColumn, methodSignature);
    }

    public MethodDeclaration(long startLine, long startColumn, long endLine, long endColumn, String methodSignature) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.methodSignature = methodSignature;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
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
}