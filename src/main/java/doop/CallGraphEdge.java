package doop;

import java.util.Set;

/**
 * Created by anantoni on 15/6/2015.
 */
public class CallGraphEdge {

    private long startLine;
    private long startColumn;
    private long endLine;
    private long endColumn;

    private String methodInvocationInDoop = null;
    private Set<MethodDeclaration> methodDeclarationSet = null;


    public CallGraphEdge(long startLine, long startColumn, long endColumn,
                         String methodInvocationInDoop, Set<MethodDeclaration> methodDeclarationSet)
    {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = startLine;
        this.endColumn = endColumn;

        this.methodInvocationInDoop = methodInvocationInDoop;
        this.methodDeclarationSet = methodDeclarationSet;
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

    public String getMethodInvocationInDoop() {
        return methodInvocationInDoop;
    }

    public void setMethodInvocationInDoop(String methodInvocationInDoop) {
        this.methodInvocationInDoop = methodInvocationInDoop;
    }

    public Set<MethodDeclaration> getMethodDeclarationSet() {
        return methodDeclarationSet;
    }

    public void setMethodDeclarationSet(Set<MethodDeclaration> methodDeclarationSet) {
        this.methodDeclarationSet = methodDeclarationSet;
    }
}
