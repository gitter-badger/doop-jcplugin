package reporters;

import com.google.gson.Gson;
import conf.Configuration;
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
    private PrintWriter instanceFieldPointsToWriter = null;
    private Gson gson = null;

    private Map<Long, Set<VarPointsTo>> varPointsToMap = null;
    private Map<Long, Set<CallGraphEdge>> callGraphEdgeMap = null;
    private Map<Long, Set<InstanceFieldPointsTo>> instanceFieldPointsToMap = null;

    public FileReporter() {
        gson = new Gson();
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
    public void reportInstanceFieldPointsTo(InstanceFieldPointsTo instanceFieldPointsTo) {
        long line = instanceFieldPointsTo.getBaseHeapAllocation().getStartLine();

        if (!this.instanceFieldPointsToMap.containsKey(line)) {
            Set<InstanceFieldPointsTo> instanceFieldPointsToSet = new HashSet<>();
            instanceFieldPointsToSet.add(instanceFieldPointsTo);
            this.instanceFieldPointsToMap.put(line, instanceFieldPointsToSet);
        }
        else {
            System.out.println("Line: " + line);
            this.instanceFieldPointsToMap.get(line).add(instanceFieldPointsTo);
        }
    }

    /**
     * @param sourceFile   the source file object of the currently processed compilation unit.
     */
    public void openJSONReportFiles(JavaFileObject sourceFile) {
        try {
            this.varPointsToMap = new HashMap<>();
            this.callGraphEdgeMap = new HashMap<>();
            this.instanceFieldPointsToMap = new HashMap<>();

            String varPointsToFilePath = FilenameUtils.concat(Configuration.DEFAULT_OUTPUT_DIR + Configuration.SELECTED_PROJECT, sourceFile.getName().replace(".java", "-VarPointsTo.json"));
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(varPointsToFilePath)));

            String callGraphEdgeFilePath = FilenameUtils.concat(Configuration.DEFAULT_OUTPUT_DIR + Configuration.SELECTED_PROJECT, sourceFile.getName().replace(".java", "-CallGraphEdge.json"));
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(callGraphEdgeFilePath)));

            String instanceFieldPointsToFilePath = FilenameUtils.concat(Configuration.DEFAULT_OUTPUT_DIR + Configuration.SELECTED_PROJECT, sourceFile.getName().replace(".java", "-InstanceFieldPointsTo.json"));
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(callGraphEdgeFilePath)));

            this.varPointsToWriter = new PrintWriter(varPointsToFilePath, "UTF-8");
            this.callGraphEdgeWriter = new PrintWriter(callGraphEdgeFilePath, "UTF-8");
            this.instanceFieldPointsToWriter = new PrintWriter(instanceFieldPointsToFilePath, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the JSON files for this particular compilation unit.
     */
    public void writeJSONReport() {
        this.varPointsToWriter.write(gson.toJson(varPointsToMap));
        this.callGraphEdgeWriter.write(gson.toJson(callGraphEdgeMap));
        this.instanceFieldPointsToWriter.write(gson.toJson(instanceFieldPointsToMap));
    }

    /**
     * Closes the JSON files generated for this particular compilation unit.
     */
    public void closeJSONReportFiles() {
        this.varPointsToWriter.close();
        this.callGraphEdgeWriter.close();
        this.instanceFieldPointsToWriter.close();
    }
}