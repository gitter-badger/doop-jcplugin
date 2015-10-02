package doop.jcplugin.util;

import doop.persistent.elements.Method;
import doop.persistent.elements.Variable;
import doop.persistent.elements.Field;
import doop.persistent.elements.Class;
import doop.persistent.elements.MethodInvocation;
import doop.persistent.elements.HeapAllocation;
import java.util.List;

/**
 * Created by anantoni on 29/9/2015.
 */
public class SourceFileReport {
    public static List<Class> classList;
    public static List<Field> fieldList;
    public static List<Method> methodList;
    public static List<MethodInvocation> invocationList;
    public static List<Variable> variableList;
    public static List<HeapAllocation> heapAllocationList;
}
