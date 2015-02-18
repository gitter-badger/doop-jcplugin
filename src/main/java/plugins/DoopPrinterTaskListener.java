package plugins;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import doop.facts.VarPointsTo;
import visitors.IdentifierScanner;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import reporters.ConsoleReporter;
import reporters.Reporter;

public class DoopPrinterTaskListener implements TaskListener {
	private final JavacTask task;
    private static final String DEFAULT_REPORTER = "reporters.ConsoleReporter";
	private final Reporter reporter;
    
	public DoopPrinterTaskListener(JavacTask javactask) {
		task = javactask;
        reporter = initReporter();
	}

	@Override
	public void finished(TaskEvent arg0) {
		System.out.println("# Task Kind: " + arg0.getKind());
        System.out.println(arg0.getSourceFile().getName());
		if (arg0.getKind().equals(TaskEvent.Kind.ANALYZE)) {
            Map<String,Set<String>> vptMap = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader("analysis-results/VarPointsTo.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");
                    assert (columns.length == 4);
                    
                    Set<String> value;
                    if ((value = vptMap.get(columns[3])) == null) {
                        Set<String> tempSet = new HashSet<>();
                        tempSet.add(columns[1]);
                        vptMap.put(columns[3], tempSet);
                    }
                    else 
                        value.add(columns[1]);
                        
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DoopPrinterTaskListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DoopPrinterTaskListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("# VarPointsTo facts: " + vptMap.size());
            JCTree tree = (JCTree) arg0.getCompilationUnit();
            StringWriter s = new StringWriter();
			tree.accept(new IdentifierScanner(reporter));
//            tree.accept(new DoopPrinter(s, false));
            System.out.println(s.toString());

            Writer writer = null;

            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(arg0.getSourceFile().getName().replaceFirst("\\.java", ".html")), "utf-8"));
                writer.write(s.toString());
            } catch (IOException ex) {
                // report
            } finally {
                try {writer.close();} catch (Exception ex) {}
            }
		}
	}

	@Override
	public void started(TaskEvent arg0) {
	}
    
    protected Reporter initReporter()  {
        String className = System.getProperty("reporter", DEFAULT_REPORTER);
        try {
            return (Reporter) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            return new ConsoleReporter();
        }
        
    }
}
