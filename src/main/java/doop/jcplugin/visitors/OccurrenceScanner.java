package doop.jcplugin.visitors;

import com.sun.source.tree.LineMap;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.TreeScanner;
import doop.jcplugin.representation.DoopRepresentationBuilder;
import doop.persistent.elements.Symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * A subclass of Tree.Visitor, this class defines
 * a general tree scanner pattern. Translation proceeds recursively in
 * left-to-right order down a tree. There is one visitor method in this class
 * for every possible kind of tree node.  To obtain a specific
 * scanner, it suffices to override those visitor methods which
 * do some interesting work. The scanner class itself takes care of all
 * navigational aspects.
 * <p>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
public class OccurrenceScanner extends TreeScanner {

    protected final LineMap lineMap;
    protected final DoopRepresentationBuilder doopReprBuilder;

    protected String sourceFileName;

    protected final Map<ClassSymbol, Map<String, Integer>> methodNamesPerClassMap;
    protected int constructorInvocationCounter;

    protected int methodInvocationCounter;
    protected Map<String, Integer> methodInvocationCounterMap;
    protected Map<Integer, Symbol> varSymbolMap;

    /**
     *
     * @param lineMap
     */
    public OccurrenceScanner(String sourceFileName, LineMap lineMap, Map<Integer, Symbol> varSymbolMap) {
        this.doopReprBuilder = DoopRepresentationBuilder.getInstance();
        this.lineMap = lineMap;
        this.constructorInvocationCounter = 0;
        this.methodInvocationCounter = 0;
        this.methodNamesPerClassMap = new HashMap<>();
        this.methodInvocationCounterMap = null;
        this.sourceFileName = sourceFileName;
        this.varSymbolMap = varSymbolMap;
    }


}