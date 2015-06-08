package reporters;

import com.google.gson.Gson;
import doop.MethodInvocation;
import doop.VarPointsTo;

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
    private PrintWriter fieldPointsToWriter = null;
    private Gson gson = null;
    private List<VarPointsTo> varPointsToList = null;
    private List<MethodInvocation> methodInvocationList = null;
    private File outputProjectsDirectory;

    public FileReporter() {
        gson = new Gson();

//        try {
//            varPointsToWriter = new PrintWriter("json-output/VarPointsTo.json", "UTF-8");
//            methodInvocationWriter = new PrintWriter("json-output/MethodInvocation.json", "UTF-8");
//            fieldPointsToWriter = new PrintWriter("json-output/FieldPointsTo.json", "UTF-8");
//            varPointsToWriter.close();
//            methodInvocationWriter.close();
//            fieldPointsToWriter.close();
//        } catch (FileNotFoundException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    public void setOutDir(String outDir) {

        this.outputProjectsDirectory = new File(outDir);
        if (!outputProjectsDirectory.exists())
            outputProjectsDirectory.mkdirs();
        else
            if (outputProjectsDirectory.isFile()) {
                System.err.println("A file with the proposed directory name already exists");
                System.exit(-1);
            }

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
    public void openFiles(JavaFileObject sourceFile, String projectName) {
        try {
            varPointsToList = new ArrayList<>();
            methodInvocationList = new ArrayList<>();

            int index = sourceFile.getName().indexOf(projectName);
            String trimmedFilePath = sourceFile.getName().substring(index + projectName.length() + 1);

            String outFileName = this.outputProjectsDirectory.getPath() + "/" + projectName + "/" + trimmedFilePath;
            File outFile = new File(outFileName);
            outFile.mkdirs();
            System.out.println("Out file: " + outFileName);

            varPointsToWriter = new PrintWriter(outFileName.replace(".java", "") + "-VarPointsTo.json", "UTF-8");
            methodInvocationWriter = new PrintWriter(outFileName.replace(".java", "") + "-MethodInvocation.json", "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the JSON files for this particular compilation unit.
     */
    public void writeJson() {
        varPointsToWriter.write(gson.toJson(varPointsToList));
    }

    /**
     * Closes the JSON files generated for this particular compilation unit.
     */
    public void closeFiles() {
        varPointsToWriter.close();
        methodInvocationWriter.close();
    }
}
