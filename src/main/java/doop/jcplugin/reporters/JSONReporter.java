package doop.jcplugin.reporters;

import com.google.gson.Gson;
import com.sun.source.util.TaskEvent;
import doop.jcplugin.util.SourceFileReport;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static doop.jcplugin.conf.Configuration.DEFAULT_OUTPUT_DIR;
import static doop.jcplugin.conf.Configuration.PROCESSED_PROJECT;
import static org.apache.commons.io.FilenameUtils.*;

/**
 * Created by anantoni on 27/4/2015.
 */
public class JSONReporter implements Reporter {

    private PrintWriter reportFileWriter = null;
    private Gson gson = null;


    public JSONReporter() {
        this.gson = new Gson();
    }

    /**
     * Writes the JSON files for this particular compilation unit.
     */
    public void writeJSONReport() {
        Map<String, List> jsonReport = new HashMap<>();
        jsonReport.put("Class", SourceFileReport.classList);
        jsonReport.put("Field", SourceFileReport.fieldList);
        jsonReport.put("Method", SourceFileReport.methodList);
        jsonReport.put("Variable", SourceFileReport.variableList);
        jsonReport.put("HeapAllocation", SourceFileReport.heapAllocationList);
        jsonReport.put("MethodInvocation", SourceFileReport.invocationList);
        this.reportFileWriter.write(this.gson.toJson(jsonReport));
    }

    public void openJSONReportFile(TaskEvent analyzeEvent) {
        String sourceBaseName = getBaseName(analyzeEvent.getSourceFile().getName());
        String reportPath = concat(DEFAULT_OUTPUT_DIR + PROCESSED_PROJECT,
                                   analyzeEvent.getCompilationUnit().getPackageName() + "." + sourceBaseName + ".json");

        try {
            FileUtils.forceMkdir(new File(getFullPath(reportPath)));
            this.reportFileWriter = new PrintWriter(reportPath, "UTF-8");

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