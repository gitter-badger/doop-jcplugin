package reporters;

import com.google.gson.Gson;
import doop.VarPointsTo;

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

    public FileReporter() {
        gson = new Gson();
        varPointsToList = new ArrayList<>();
        try {
            varPointsToWriter = new PrintWriter("json-output/VarPointsTo.json", "UTF-8");
            methodInvocationWriter = new PrintWriter("json-output/MethodInvocation.json", "UTF-8");
            fieldPointsToWriter = new PrintWriter("json-output/FieldPointsTo.json", "UTF-8");
            varPointsToWriter.close();
            methodInvocationWriter.close();
            fieldPointsToWriter.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reportVar(int startPos, int endPos, String representation) {

    }

    @Override
    public void reportVarPointsTo(VarPointsTo varPointsTo) {
//        varPointsToWriter.println(gson.toJson(varPointsTo));
        varPointsToList.add(varPointsTo);
    }

    @Override
    public void reportMethodInvocation(int startPos, int endPos, String representation) {

    }

    @Override
    public void reportFieldAccess(int startPos, int endPos, String representation) {

    }

    public void openFiles() {
        try {
            varPointsToWriter = new PrintWriter(new BufferedWriter(new FileWriter("json-output/VarPointsTo.json", true)));
            methodInvocationWriter = new PrintWriter(new BufferedWriter(new FileWriter("json-output/MethodInvocation.json", true)));
            fieldPointsToWriter = new PrintWriter(new BufferedWriter(new FileWriter("json-output/FieldPointsTo.json", true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeJson() {
        varPointsToWriter.write(gson.toJson(varPointsToList));
    }

    public void closeFiles() {
        varPointsToWriter.close();
        methodInvocationWriter.close();
        fieldPointsToWriter.close();
    }
}
