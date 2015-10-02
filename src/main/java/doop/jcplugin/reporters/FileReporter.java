package doop.jcplugin.reporters;

import com.google.gson.Gson;
import doop.jcplugin.conf.Configuration;
import doop.jcplugin.representation.CallGraphEdge;
import doop.jcplugin.representation.InstanceFieldPointsTo;
import doop.jcplugin.representation.VarPointsTo;
import doop.jcplugin.util.SourceFileReport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.*;

/**
 * Created by anantoni on 27/4/2015.
 */
public class FileReporter implements Reporter {

    private PrintWriter reportFileWriter = null;
    private Gson gson = null;

    private Map<Long, Set<VarPointsTo>> varPointsToMap = null;
    private Map<Long, Set<CallGraphEdge>> callGraphEdgeMap = null;
    private Map<Long, Set<InstanceFieldPointsTo>> instanceFieldPointsToMap = null;

    public FileReporter() {
        this.gson = new Gson();
    }

    /**
     * Adds the VarPointsTo object to the [line:{VarPointsTo}] map.
     *
     * @param varPointsTo
     */
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

    /**
     * Adds the CallGraphEdge object to the [line:{CallGraphEdge}] map.
     *
     * @param callGraphEdge
     */
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

    /**
     * Adds the instanceFieldPointsTo object to the [line:{InstanceFieldPointsTo}] map.
     *
     * @param instanceFieldPointsTo
     */
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
     * Writes the JSON files for this particular compilation unit.
     */
    public void writeJSONReport() {
        this.reportFileWriter.write(this.gson.toJson(SourceFileReport.classList));
        this.reportFileWriter.write(this.gson.toJson(SourceFileReport.methodList));
        this.reportFileWriter.write(this.gson.toJson(SourceFileReport.invocationList));
    }

    public void openJSONReportFile(JavaFileObject sourceFile) {
        this.varPointsToMap = new HashMap<>();
        this.callGraphEdgeMap = new HashMap<>();
        this.instanceFieldPointsToMap = new HashMap<>();

        String reportFilePath = FilenameUtils.concat(Configuration.DEFAULT_OUTPUT_DIR + Configuration.SELECTED_PROJECT,
                                                        sourceFile.getName().replace("/", ".").replace(".java", ".json").replaceAll("^\\.+", ""));

        System.out.println(reportFilePath);


        try {
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(reportFilePath)));
            this.reportFileWriter = new PrintWriter(reportFilePath, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Closes the JSON files generated for this particular compilation unit.
     */
    public void closeJSONReportFile() {
        this.reportFileWriter.close();
    }
}