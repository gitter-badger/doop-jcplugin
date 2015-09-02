package doop.javac_plugin.symbols;

import doop.javac_plugin.util.Position;

import java.util.ArrayList;

/**
 * Created by anantoni on 22/7/2015.
 */
public class Variable extends SymbolPosition {
    private String name;
    private String doopName;
    private String type;
    private Method declaringMethod;
    private ArrayList<long[]> defOccurrences;
    private ArrayList<long[]> useOccurrences;

    public Variable(Position position) {
        super(position);
    }


}
