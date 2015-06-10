package plugins;

import com.sun.source.tree.LineMap;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import doop.HeapAllocation;
import reporters.ConsoleReporter;
import reporters.FileReporter;
import reporters.Reporter;
import visitors.IdentifierScanner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoopPrinterTaskListener implements TaskListener {
    private static final String DEFAULT_REPORTER = "reporters.FileReporter";
    private static final String DEFAULT_ANALYSIS_RESULTS_DIR = "./analysis-results/";
    private static final String DEFAULT_PROJECT = "advancedTest";
    private static final boolean MATCH_DOOP_RESULTS = true;


    private final JavacTask task;
    private final Reporter reporter;
    private Map<String, Set<HeapAllocation>> vptMap;

    /**
     * DoopPrinterTaskListener constructor.
     *
     * @param javactask
     */
    public DoopPrinterTaskListener(JavacTask javactask) {

        this.task = javactask;
        /**
         * Initialize the reporter.
         */
        if (DEFAULT_REPORTER.equals("reporters.FileReporter"))
            this.reporter = initReporter();
        else
            this.reporter = initReporter();

        BufferedReader br = null;
        try {
            /**
             * If doop results are locally available then create the necessary maps to match static information
             * with doop points-to results.
             */
            if (MATCH_DOOP_RESULTS) {
                this.vptMap = new HashMap<>();
                String line;
                String cvsSplitBy = ", ";
                br = new BufferedReader(new FileReader(DEFAULT_ANALYSIS_RESULTS_DIR + "VarPointsTo.txt"));

                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(cvsSplitBy);
                    assert (columns.length == 4);

                    String var = columns[3].trim();
                    String heapAllocation = columns[1].trim();

                    if (!vptMap.containsKey(var)) {
                        Set<HeapAllocation> heapAllocationSet = new HashSet<>();
                        heapAllocationSet.add(new HeapAllocation(heapAllocation));
                        vptMap.put(var, heapAllocationSet);
                    } else {
                        Set<HeapAllocation> heapAllocationSet = vptMap.get(var);
                        heapAllocationSet.add(new HeapAllocation(heapAllocation));
                    }
                }

                System.out.println("VarPointsTo map size: " + vptMap.size());
                System.out.println(vptMap);
                int counter = 0;
                for (Set<HeapAllocation> set : vptMap.values())
                    counter += set.size();
                System.out.println(counter);
            }
            /**
             * Otherwise set map field to null and generate empty sets representing doop information such as
             * heap allocation sets for varPointsTo. The empty sets will be filled later by another Java application.
             */
            else
                this.vptMap = null;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DoopPrinterTaskListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DoopPrinterTaskListener.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(DoopPrinterTaskListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }


    /**
     * Initializes the doop facts reporter.
     *
     * @return the initialized reporter
     */
    protected Reporter initReporter() {
        String className = System.getProperty("reporter", DEFAULT_REPORTER);
        try {
            return (Reporter) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(DoopPrinterTaskListener.class.getName()).log(Level.SEVERE, null, ex);
            return new ConsoleReporter();
        }
    }

    /**
     * After the ANALYZE task for each compilation unit the identifier scanner is called to identify variables and
     * method invocations.
     *
     * @param arg0 the finished TaskEvent
     */
    @Override
    public void finished(TaskEvent arg0) {
        if (arg0.getKind().equals(TaskEvent.Kind.ANALYZE)) {
            System.out.println("\033[31m # Task Kind: " + arg0.getKind() + " finished in file: " + arg0.getSourceFile().getName() + "\033[0m");

            /**
             * Get the LineMap for this compilation unit in order to much positions with lines and columns.
             */
            LineMap lineMap = arg0.getCompilationUnit().getLineMap();

            /**
             * Open all necessary json files to write facts.
             */
            if (reporter instanceof FileReporter)
                ((FileReporter) reporter).openJSONFiles(arg0.getSourceFile(), DEFAULT_PROJECT);

            /**
             * Get AST root for this source code file.
             */
            JCTree treeRoot = (JCTree) arg0.getCompilationUnit();
            treeRoot.accept(new IdentifierScanner(reporter, vptMap, lineMap));

            /**
             * Close all files.
             */
            if (reporter instanceof FileReporter) {
                ((FileReporter) reporter).writeJSON();
                ((FileReporter) reporter).closeJSONFiles();
            }
        }
    }

    @Override
    public void started(TaskEvent arg0) {}
}
