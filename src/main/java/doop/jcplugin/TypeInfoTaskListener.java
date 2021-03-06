package doop.jcplugin;

import com.sun.source.tree.LineMap;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import doop.jcplugin.conf.Configuration;
import doop.jcplugin.reporters.JSONReporter;
import doop.jcplugin.reporters.Reporter;
import doop.jcplugin.util.SourceFileReport;
import doop.jcplugin.visitors.SymbolScanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import static doop.jcplugin.conf.Configuration.DEFAULT_OUTPUT_DIR;
import static doop.jcplugin.conf.Configuration.PROCESSED_PROJECT;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getName;

class TypeInfoTaskListener implements TaskListener {

    private final Reporter reporter;

    /**
     * TypeInfoTaskListener constructor.
     *
     */
    public TypeInfoTaskListener() {

        /**
         * Initialize the reporter.
         */
        this.reporter = initReporter();
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
            Logger.getLogger(TypeInfoTaskListener.class.getName()).log(Level.SEVERE, null, ex);
            return new JSONReporter();
        }
    }

    /**
     * After the ANALYZE task for each compilation unit the initial scanner is called to identify method declarations
     * heap allocations
     * @param arg0 the finished TaskEvent
     */
    @Override
    public void finished(TaskEvent arg0) {
        if (arg0.getKind().equals(TaskEvent.Kind.ANALYZE)) {
            System.out.println("\033[31m # Generating report for file: " + arg0.getSourceFile().getName() + "\033[0m");

            /**
             * Open all necessary json files to write facts.
             */
            if (this.reporter instanceof JSONReporter) {
                ((JSONReporter) this.reporter).openJSONReportFile(arg0);
            }

            /**
             * Get the AST root for this source code file.
             */
            JCTree treeRoot = (JCTree) arg0.getCompilationUnit();
            String sourceFileName = getName(arg0.getSourceFile().getName());
            String sourceFilePath = arg0.getCompilationUnit().getPackageName() + "." + sourceFileName;

            /**
             * Get the LineMap for this compilation unit in order to match positions with lines and columns.
             */
            LineMap lineMap = arg0.getCompilationUnit().getLineMap();

            SourceFileReport.initializeSymbolLists();
            /**
             * Visitor passes.
             */
            treeRoot.accept(new SymbolScanner(sourceFilePath, lineMap));

            /**
             * Write and close all files.
             */
            if (this.reporter instanceof JSONReporter) {
                ((JSONReporter) this.reporter).writeJSONReport();
                ((JSONReporter) this.reporter).closeJSONReportFile();
            }
        }
    }

    @Override
    public void started(TaskEvent arg0) {}
}