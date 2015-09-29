package doop.jcplugin.symbols;

import doop.jcplugin.util.Position;

/**
 * Created by anantoni on 22/7/2015.
 */
public class Symbol {
    long[] coordinates;
    String compilationUnitName;

    public Symbol() {}

    public Symbol(Position position) {

        this.coordinates = new long[4];
        this.coordinates[0] = position.getStartLine();
        this.coordinates[1] = position.getStartColumn();
        this.coordinates[2] = position.getEndLine();
        this.coordinates[3] = position.getEndColumn();
        this.compilationUnitName = position.getCompilationUnitName();

    }

    public long getStartLine() {
        return this.coordinates[0];
    }

    public long getStartColumn() {
        return this.coordinates[1];
    }

    public long getEndLine() {
        return this.coordinates[2];
    }

    public long getEndColumn() {
        return this.coordinates[3];
    }

    public String getCompilationUnitName() {
        return this.compilationUnitName;
    }
}
