package plugins;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import reporters.ConsoleReporter;
import reporters.FileReporter;
import reporters.Reporter;
import visitors.IdentifierScanner;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoopPrinterTaskListener implements TaskListener {
    private static final String DEFAULT_REPORTER = "reporters.FileReporter";
    private final JavacTask task;
    private final Reporter reporter;
    private final Map<String, Set<String>> vptMap;

    public DoopPrinterTaskListener(JavacTask javactask) {
        task = javactask;
        reporter = initReporter();
        vptMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("analysis-results/VarPointsTo.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                assert (columns.length == 4);

                Set<String> value;
                if ((value = vptMap.get(columns[3])) == null) {
                    Set<String> tempSet = new HashSet<>();
                    tempSet.add(columns[1].trim());
                    vptMap.put(columns[3].trim(), tempSet);
                } else
                    value.add(columns[1].trim());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DoopPrinterTaskListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DoopPrinterTaskListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected Reporter initReporter() {
        String className = System.getProperty("reporter", DEFAULT_REPORTER);
        try {
            return (Reporter) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            return new ConsoleReporter();
        }

    }

    @Override
    public void finished(TaskEvent arg0)
    {
        System.out.println("# Task Kind: " + arg0.getKind());
        System.out.println(arg0.getSourceFile().getName());
        if (arg0.getKind().equals(TaskEvent.Kind.ANALYZE))
        {
            JCTree tree = (JCTree) arg0.getCompilationUnit();
            StringWriter s = new StringWriter();
            tree.accept(new IdentifierScanner(reporter, vptMap));
            System.out.println(s.toString());
            if (reporter instanceof FileReporter)
                reporter.closeFiles();
        }
    }

    @Override
    public void started(TaskEvent arg0) {}


}
