package doop.jcplugin.visitors;

import com.sun.source.tree.LineMap;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import doop.jcplugin.util.SourceFileReport;
import doop.persistent.elements.Occurrence;
import doop.persistent.elements.Position;
import doop.persistent.elements.Symbol;

import java.util.Map;

import static doop.persistent.elements.OccurrenceType.READ;

/**
 * Created by anantoni on 22/10/2015.
 */
public class DefOccurrenceScanner extends OccurrenceScanner {

    /**
     *
     * @param lineMap
     */
    public DefOccurrenceScanner(String sourceFileName, LineMap lineMap, Map<Integer, Symbol> varSymbolMap) {
        super(sourceFileName, lineMap, varSymbolMap);
    }

    @Override
    public void visitIdent(JCIdent tree) {
        if (tree.sym != null) {
            if (this.varSymbolMap.containsKey(tree.sym.hashCode())) {
                Position position = new Position(this.lineMap.getLineNumber(tree.pos),
                        this.lineMap.getColumnNumber(tree.pos),
                        this.lineMap.getColumnNumber(tree.pos + tree.sym.name.toString().length()));


                Occurrence occurrence = new Occurrence(position,
                        this.sourceFileName,
                        this.varSymbolMap.get(tree.sym.hashCode()).getId(),
                        READ);

                SourceFileReport.occurrenceList.add(occurrence);
            }
        }
    }
}
