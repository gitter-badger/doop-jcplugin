package symbols;

import jdk.nashorn.internal.ir.Symbol;
import util.Position;

/**
 * Created by anantoni on 22/7/2015.
 */
public class Class extends SymbolPosition {
    private String name;

    public Class(Position position, String name) {
        super(position);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
