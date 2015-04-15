package plugins;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;

public class DoopPrinterPlugin implements Plugin {

    @Override
    public String getName() {
        return "DoopPrinterPlugin";
    }

    @Override
    public void init(JavacTask task, String... arg1) {
        task.addTaskListener(new DoopPrinterTaskListener(task));
    }
}
