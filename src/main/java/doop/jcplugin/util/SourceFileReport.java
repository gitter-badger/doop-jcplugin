package doop.jcplugin.util;

import doop.persistent.elements.*;
import doop.persistent.elements.Class;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anantoni on 29/9/2015.
 */
public class SourceFileReport {

    public static List<Class> classList;
    public static List<Field> fieldList;
    public static List<Method> methodList;
    public static List<Variable> variableList;
    public static List<MethodInvocation> invocationList;
    public static List<HeapAllocation> heapAllocationList;
    public static List<Occurrence> occurrenceList;

    public static void initializeSymbolLists() {
        SourceFileReport.classList = new ArrayList<>();
        SourceFileReport.fieldList = new ArrayList<>();
        SourceFileReport.methodList = new ArrayList<>();
        SourceFileReport.variableList = new ArrayList<>();
        SourceFileReport.invocationList = new ArrayList<>();
        SourceFileReport.heapAllocationList = new ArrayList<>();
        SourceFileReport.occurrenceList = new ArrayList<>();
    }
}
