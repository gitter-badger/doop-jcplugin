package doop.jcplugin.reporters;

import com.google.gson.Gson;
import doop.jcplugin.conf.Configuration;
import doop.jcplugin.util.SourceFileReport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import javax.tools.JavaFileObject;
import java.io.*;

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
        this.reportFileWriter.write(this.gson.toJson(SourceFileReport.classList));
        this.reportFileWriter.write(this.gson.toJson(SourceFileReport.methodList));
        this.reportFileWriter.write(this.gson.toJson(SourceFileReport.invocationList));
    }

    public void openJSONReportFile(JavaFileObject sourceFile) {
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