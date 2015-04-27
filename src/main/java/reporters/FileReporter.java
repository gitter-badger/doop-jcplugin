package reporters;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * Created by anantoni on 27/4/2015.
 */
public class FileReporter implements Reporter {
    private PrintWriter varPointsToWriter = null;
    private PrintWriter methodInvocationWriter = null;
    private PrintWriter fieldPointsToWriter = null;

    public FileReporter() {
        try {
            varPointsToWriter = new PrintWriter("VarPointsTo.json", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            methodInvocationWriter = new PrintWriter("MethodInvocation.json", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            fieldPointsToWriter = new PrintWriter("FieldPointsTo.json", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reportVar(int startPos, int endPos, String representation) {

    }

    @Override
    public void reportVarPointsTo(int startPos, int endPos, String representation, Set<String> heapAllocationSet) {
        varPointsToWriter.println("{");
        varPointsToWriter.println("\"startPosition\":\"" + startPos + "\"");
        varPointsToWriter.println("\"endPosition\":\"" + endPos + "\"");
        varPointsToWriter.println("\"representation\":\"" + representation + "\"");
        varPointsToWriter.println("\"heapAllocationSet\":[");
        int counter = 0;
        for (String heapAllocation : heapAllocationSet) {
            if (counter == 0)
                varPointsToWriter.println("\"" + heapAllocation + "\"");
            else
                varPointsToWriter.println(",\"" + heapAllocation + "\"");
            counter++;
        }
        varPointsToWriter.println("]};");

        varPointsToWriter.println(startPos);



    }

    @Override
    public void reportMethodInvocation(int startPos, int endPos, String representation) {

    }

    @Override
    public void reportFieldAccess(int startPos, int endPos, String representation) {

    }

    @Override
    public void closeFiles() {
        varPointsToWriter.close();
        methodInvocationWriter.close();
        fieldPointsToWriter.close();
    }
}
