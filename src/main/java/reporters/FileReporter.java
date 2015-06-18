package reporters;

import com.google.gson.Gson;
import doop.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.*;

/**
 * Created by anantoni on 27/4/2015.
 */
public class FileReporter implements Reporter {
    private PrintWriter varPointsToWriter = null;
    private PrintWriter callGraphEdgeWriter = null;
    private Gson gson = null;

    private Map<Long, Set<VarPointsTo>> varPointsToMap = null;
    private Map<Long, Set<CallGraphEdge>> callGraphEdgeMap = null;

//    private Map<String, Set<HeapAllocation>> heapAllocationMap = null;
    private static final String DEFAULT_OUTPUT_DIR = "/home/anantoni/plugin-output-projects/";

    public FileReporter() {
        gson = new Gson();
    }


    @Override
    public void reportVar(int startPos, int endPos, String representation) {

    }

    @Override
    public void reportVarPointsTo(VarPointsTo varPointsTo) {
        long line = varPointsTo.getStartLine();
        if (!this.varPointsToMap.containsKey(line)) {
            Set<VarPointsTo> varPointsToSet = new HashSet<>();
            varPointsToSet.add(varPointsTo);
            this.varPointsToMap.put(line, varPointsToSet);
        }
        else {
            System.out.println("Line: " + line);
            this.varPointsToMap.get(line).add(varPointsTo);
        }
    }

//    public void reportHeapAllocation(HeapAllocation heapAllocation) {
//        for (Set<VarPointsTo> varPointsToSet : varPointsToMap.values()) {
//            for (VarPointsTo varPointsTo : varPointsToSet) {
//                Set<HeapAllocation> heapAllocationSet = varPointsTo.getHeapAllocationSet();
//
//                for (HeapAllocation initialHeapAllocation : heapAllocationSet) {
//                    if (initialHeapAllocation.getDoopAllocationName().equals(heapAllocation.getDoopAllocationName())) {
//                        initialHeapAllocation.setStartLine(heapAllocation.getStartLine());
//                        initialHeapAllocation.setEndLine(heapAllocation.getEndLine());
//                        initialHeapAllocation.setStartColumn(heapAllocation.getStartColumn());
//                        initialHeapAllocation.setEndColumn(heapAllocation.getEndColumn());
//                    }
//                }
//            }
//        }
//    }

    @Override
    public void reportCallGraphEdge(CallGraphEdge callGraphEdge) {

        long line = callGraphEdge.getStartLine();
        if (!this.callGraphEdgeMap.containsKey(line)) {
            Set<CallGraphEdge> callGraphEdgeSet = new HashSet<>();
            callGraphEdgeSet.add(callGraphEdge);
            this.callGraphEdgeMap.put(line, callGraphEdgeSet);
        }
        else {
            System.out.println("Line: " + line);
            this.callGraphEdgeMap.get(line).add(callGraphEdge);
        }


    }

    @Override
    public void reportFieldAccess() {

    }

    /**
     * @param sourceFile   the source file object of the currently processed compilation unit.
     */
    public void openJSONFiles(JavaFileObject sourceFile) {
        try {
            varPointsToMap = new HashMap<>();
            callGraphEdgeMap = new HashMap<>();

            String varPointsToFilePath = FilenameUtils.concat(DEFAULT_OUTPUT_DIR + plugins.DoopPrinterTaskListener.DEFAULT_PROJECT, sourceFile.getName().replace(".java", "-VarPointsTo.json"));
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(varPointsToFilePath)));

            String callGraphEdgeFilePath = FilenameUtils.concat(DEFAULT_OUTPUT_DIR + plugins.DoopPrinterTaskListener.DEFAULT_PROJECT, sourceFile.getName().replace(".java", "-CallGraphEdge.json"));
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(callGraphEdgeFilePath)));

            varPointsToWriter = new PrintWriter(varPointsToFilePath, "UTF-8");
            callGraphEdgeWriter = new PrintWriter(callGraphEdgeFilePath, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the JSON files for this particular compilation unit.
     */
    public void writeJSON() {
        varPointsToWriter.write(gson.toJson(varPointsToMap));
        callGraphEdgeWriter.write(gson.toJson(callGraphEdgeMap));
    }

    /**
     * Closes the JSON files generated for this particular compilation unit.
     */
    public void closeJSONFiles() {
        varPointsToWriter.close();
        callGraphEdgeWriter.close();
    }
}
