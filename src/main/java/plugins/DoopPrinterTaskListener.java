package plugins;

import com.sun.source.tree.LineMap;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;
import conf.Configuration;
import reporters.ConsoleReporter;
import reporters.FileReporter;
import reporters.Reporter;
import visitors.InitialScanner;
import visitors.IdentifierScanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class DoopPrinterTaskListener implements TaskListener {

    private final Reporter reporter;
    private Map<String, Set<String>> vptMap;
    private Map<String, Set<String>> miMap;
    private Map<Pair<String, String>, Set<String>> ifptMap;

    /**
     * DoopPrinterTaskListener constructor.
     *
     */
    public DoopPrinterTaskListener() {

        /**
         * Initialize the reporter.
         */
        if (Configuration.DEFAULT_REPORTER.equals("reporters.FileReporter"))
            this.reporter = initReporter();
        else
            this.reporter = initReporter();

        BufferedReader br = null;
        try {
            /**
             * If doop results are locally available then create the necessary maps to match static information
             * with doop points-to results.
             */
            if (Configuration.MATCH_DOOP_RESULTS) {
                this.vptMap = new HashMap<>();
                String line;
                String cvsSplitBy = ", ";
                br = new BufferedReader(new FileReader(Configuration.DEFAULT_ANALYSIS_RESULTS_DIR + "VarPointsTo.txt"));

                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(cvsSplitBy);
                    assert (columns.length == 4);

                    String var = columns[3].trim();
                    String heapAllocation = columns[1].trim();

                    if (!this.vptMap.containsKey(var)) {
                        Set<String> heapAllocationSet = new HashSet<>();
                        heapAllocationSet.add(heapAllocation);
                        this.vptMap.put(var, heapAllocationSet);
                    }
                    else {
                        Set<String> heapAllocationSet = this.vptMap.get(var);
                        heapAllocationSet.add(heapAllocation);
                    }
                }

                int counter = 0;
                for (Set<String> set : this.vptMap.values())
                    counter += set.size();
                System.out.println(counter);

                this.miMap = new HashMap<>();
                br = new BufferedReader(new FileReader(Configuration.DEFAULT_ANALYSIS_RESULTS_DIR + "CallGraphEdge.txt"));

                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(cvsSplitBy);
                    assert (columns.length == 4);

                    String methodSignature = columns[3].trim();
                    String methodInvocation = columns[1].trim();

                    if (!this.miMap.containsKey(methodInvocation)) {
                        Set<String> methodSignatureSet = new HashSet<>();
                        methodSignatureSet.add(methodSignature);
                        this.miMap.put(methodInvocation, methodSignatureSet);
                    }
                    else {
                        Set<String> heapAllocationSet = this.miMap.get(methodInvocation);
                        heapAllocationSet.add(methodSignature);
                    }
                }

                this.ifptMap = new HashMap<>();
                br = new BufferedReader(new FileReader(Configuration.DEFAULT_ANALYSIS_RESULTS_DIR + "InstanceFieldPointsTo.txt"));

                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(cvsSplitBy);
                    assert (columns.length == 5);

                    String fieldSignature = columns[2].trim();
                    String baseHeapAllocation = columns[4].trim();
                    String heapAllocation = columns[1].trim();

                    Pair<String, String> baseHeapAllocationField = new Pair<>(baseHeapAllocation, fieldSignature);

                    if (!this.ifptMap.containsKey(baseHeapAllocationField)) {
                        Set<String> heapAllocationSet = new HashSet<>();
                        heapAllocationSet.add(heapAllocation);
                        this.ifptMap.put(baseHeapAllocationField, heapAllocationSet);
                    }
                    else {
                        Set<String> heapAllocationSet = this.ifptMap.get(baseHeapAllocationField);
                        heapAllocationSet.add(heapAllocation);
                    }
                }
                System.out.println(this.ifptMap);
            }
            /**
             * Otherwise set map field to null and generate empty sets representing doop information such as
             * heap allocation sets for varPointsTo. The empty sets will be filled later by another Java application.
             */
            else
                this.vptMap = null;
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
    private Reporter initReporter() {
        String className = System.getProperty("reporter", Configuration.DEFAULT_REPORTER);
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
            if (this.reporter instanceof FileReporter)
                ((FileReporter) this.reporter).openJSONReportFiles(arg0.getSourceFile());

            /**
             * Get AST root for this source code file.
             */
            JCTree treeRoot = (JCTree) arg0.getCompilationUnit();
            InitialScanner initialScanner = new InitialScanner(lineMap);
            treeRoot.accept(initialScanner);
            treeRoot.accept(new IdentifierScanner(this.reporter, this.vptMap, this.miMap, this.ifptMap,
                                                    lineMap,
                                                    initialScanner.getHeapAllocationMap(),
                                                    initialScanner.getMethodDeclarationMap(),
                                                    initialScanner.getFieldSignatureMap()));
            /**
             * Close all files.
             */
            if (this.reporter instanceof FileReporter) {
                ((FileReporter) this.reporter).writeJSONReport();
                ((FileReporter) this.reporter).closeJSONReportFiles();
            }
        }
    }

    @Override
    public void started(TaskEvent arg0) {}
}
