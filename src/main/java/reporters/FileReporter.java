package reporters;

import com.google.gson.Gson;
import doop.MethodInvocation;
import doop.VarPointsTo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anantoni on 27/4/2015.
 */
public class FileReporter implements Reporter {
    private PrintWriter varPointsToWriter = null;
    private PrintWriter methodInvocationWriter = null;
    private Gson gson = null;
    private List<VarPointsTo> varPointsToList = null;
    private List<MethodInvocation> methodInvocationList = null;
    private FileUtils fileUtils = null;
    private static final String DEFAULT_OUTPUT_DIR = "/home/anantoni/plugin-output-projects/";

    public FileReporter() {
        gson = new Gson();
    }

    @Override
    public void reportVar(int startPos, int endPos, String representation) {

    }

    @Override
    public void reportVarPointsTo(VarPointsTo varPointsTo) {
        //varPointsToWriter.println(gson.toJson(varPointsTo));
        varPointsToList.add(varPointsTo);
    }

    @Override
    public void reportMethodInvocation(MethodInvocation methodInvocation) {

    }

    @Override
    public void reportFieldAccess(int startPos, int endPos, String representation) {

    }

    /**
     * @param sourceFile   the source file object of the currently processed compilation unit.
     * @param projectName  the fileName of the currently processed compilation unit.
     */
    public void openJSONFiles(JavaFileObject sourceFile, String projectName) {
        try {
            varPointsToList = new ArrayList<>();
            methodInvocationList = new ArrayList<>();

            String varPointsToPath = FilenameUtils.concat(DEFAULT_OUTPUT_DIR + projectName, sourceFile.getName().replace(".java", "-VarPointsTo.json"));
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(varPointsToPath)));

            String methodInvocationPath = FilenameUtils.concat(DEFAULT_OUTPUT_DIR + projectName, sourceFile.getName().replace(".java", "-MethodInvocation.json"));
            FileUtils.forceMkdir(new File(FilenameUtils.getFullPath(methodInvocationPath)));

            varPointsToWriter = new PrintWriter(methodInvocationPath, "UTF-8");
            methodInvocationWriter = new PrintWriter(methodInvocationPath, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the JSON files for this particular compilation unit.
     */
    public void writeJSON() {
        varPointsToWriter.write(gson.toJson(varPointsToList));
    }

    /**
     * Closes the JSON files generated for this particular compilation unit.
     */
    public void closeJSONFiles() {
        varPointsToWriter.close();
        methodInvocationWriter.close();
    }
}
