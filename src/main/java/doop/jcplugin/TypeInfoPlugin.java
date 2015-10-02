package doop.jcplugin;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;

public class TypeInfoPlugin implements Plugin {

    @Override
    public String getName() {
        return "TypeInfoPlugin";
    }

    @Override
    public void init(JavacTask task, String... arg1) {
        task.addTaskListener(new TypeInfoTaskListener());
    }
}
