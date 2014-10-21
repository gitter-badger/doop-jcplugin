package plugins;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import visitors.DecoratorScanner;
import visitors.DoopPrinter;

import java.io.*;

public class DoopPrinterTaskListener implements TaskListener {
	private JavacTask task;
	
	public DoopPrinterTaskListener(JavacTask javactask) {
		task = javactask;
	}

	@Override
	public void finished(TaskEvent arg0) {
		System.out.println("# Task Kind: " + arg0.getKind());
        System.out.println(arg0.getSourceFile().getName());
		if (arg0.getKind().equals(TaskEvent.Kind.ANALYZE)) {

            JCTree tree = (JCTree) arg0.getCompilationUnit();
            StringWriter s = new StringWriter();
			tree.accept(new DecoratorScanner());
            tree.accept(new DoopPrinter(s, false));
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
}
