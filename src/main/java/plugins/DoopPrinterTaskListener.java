package plugins;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;
import visitors.DoopPrinter;
import java.io.StringWriter;

public class DoopPrinterTaskListener implements TaskListener {
	private JavacTask task;
	
	public DoopPrinterTaskListener(JavacTask javactask) {
		task = javactask;
	}

	@Override
	public void finished(TaskEvent arg0) {
		System.out.println("# Task Kind: " + arg0.getKind());

		if (arg0.getKind().equals(TaskEvent.Kind.ANALYZE)) {
            JCTree tree = (JCTree) arg0.getCompilationUnit();
            StringWriter s = new StringWriter();
			tree.accept(new DoopPrinter(s, false));
            System.out.println(s.toString());
		}
	}

	@Override
	public void started(TaskEvent arg0) {
	}
}
