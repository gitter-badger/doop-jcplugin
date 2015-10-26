package doop.jcplugin.visitors;

import com.sun.source.tree.LineMap;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import doop.jcplugin.util.SourceFileReport;
import doop.persistent.elements.*;

import java.util.Map;
import static doop.persistent.elements.OccurrenceType.WRITE;

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
            if (this.varSymbolMap.containsKey(tree.sym.hashCode()) && this.varSymbolMap.get(tree.sym.hashCode()) instanceof Variable) {

                Position position = new Position(this.lineMap.getLineNumber(tree.pos),
                        this.lineMap.getColumnNumber(tree.pos),
                        this.lineMap.getColumnNumber(tree.pos + tree.sym.name.toString().length()));


                Occurrence occurrence = new Occurrence(position,
                        this.sourceFileName,
                        this.varSymbolMap.get(tree.sym.hashCode()).getId(),
                        WRITE);

                SourceFileReport.occurrenceList.add(occurrence);
                Variable var = (Variable) this.varSymbolMap.get(tree.sym.hashCode());
                System.out.println("Reported variable occurrence (def): " + var.getName());
                System.out.println("Source file name: " + var.getSourceFileName());
                System.out.println("Position: " + var.getPosition().getStartLine());
            }
            else if (this.varSymbolMap.containsKey(tree.sym.hashCode()) && this.varSymbolMap.get(tree.sym.hashCode()) instanceof Field) {

                Position position = new Position(this.lineMap.getLineNumber(tree.pos),
                        this.lineMap.getColumnNumber(tree.pos),
                        this.lineMap.getColumnNumber(tree.pos + tree.sym.name.toString().length()));


                Occurrence occurrence = new Occurrence(position,
                        this.sourceFileName,
                        this.varSymbolMap.get(tree.sym.hashCode()).getId(),
                        WRITE);

                SourceFileReport.occurrenceList.add(occurrence);
                Field field = (Field) this.varSymbolMap.get(tree.sym.hashCode());
                System.out.println("Reported field occurrence (def): " + field.getName());
                System.out.println("Source file name: " + field.getSourceFileName());
                System.out.println("Position: " + field.getPosition().getStartLine());
            }
            else {
                System.out.println("*** Occurrence of other identifier: " + tree.sym.getQualifiedName());
            }
        }
    }
}
