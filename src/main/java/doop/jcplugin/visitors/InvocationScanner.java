package doop.jcplugin.visitors;

import com.sun.source.tree.LineMap;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import doop.jcplugin.representation.DoopRepresentationBuilder;
import doop.jcplugin.util.SourceFileReport;
import doop.persistent.elements.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anantoni on 22/10/2015.
 */
public class InvocationScanner extends TreeScanner{

    private ClassSymbol currentClassSymbol;
    private MethodSymbol currentMethodSymbol;
    private Method currentMethod;
    private String currentMethodDoopSignature;
    private String currentMethodCompactName;
    private final LineMap lineMap;
    private String sourceFileName;
    private int constructorInvocationCounter = 0;
    private int methodInvocationCounter = 0;
    private Map<String, Integer> methodInvocationCounterMap;
    private int heapAllocationCounter;
    private Map<String, Integer> heapAllocationCounterMap;
    private final DoopRepresentationBuilder doopReprBuilder;
    private Map<ClassSymbol, Map<String, Integer>> methodNamesPerClassMap;

    public InvocationScanner(String sourceFileName, LineMap lineMap, ClassSymbol currentClassSymbol, MethodSymbol currentMethodSymbol, Method currentMethod, String currentMethodDoopSignature, String currentMethodCompactName, Map<String, Integer> heapAllocationCounterMap, Map<ClassSymbol, Map<String, Integer>> methodNamesPerClassMap) {
        this.currentClassSymbol = currentClassSymbol;
        this.currentMethodSymbol = currentMethodSymbol;
        this.sourceFileName = sourceFileName;
        this.lineMap = lineMap;
        this.currentMethod = currentMethod;
        this.currentMethodDoopSignature = currentMethodDoopSignature;
        this.currentMethodCompactName = currentMethodCompactName;
        this.heapAllocationCounterMap = heapAllocationCounterMap;
        this.methodNamesPerClassMap = methodNamesPerClassMap;
        this.doopReprBuilder = DoopRepresentationBuilder.getInstance();
        this.methodInvocationCounter = 0;
        this.methodInvocationCounterMap = new HashMap<>();

    }

    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {
        scan(tree.typeargs);
        scan(tree.meth);
        scan(tree.args);

        java.lang.Class<?> clazz = tree.meth.getClass();
        java.lang.reflect.Field field;
        Object fieldValue = null;

        System.out.println(tree.meth.toString());

        try {
            field = clazz.getField("sym");
            fieldValue = field.get(tree.meth);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (fieldValue instanceof MethodSymbol) {

            String doopMethodInvocation;
            /**
             * If current method is overloaded use its signature to build the variable name.
             */
            if (this.methodNamesPerClassMap.get(this.currentClassSymbol).get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
                doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodDoopSignature,
                        this.doopReprBuilder.buildDoopMethodInvocation((MethodSymbol) fieldValue));
            /**
             * Otherwise use its compact name.
             */
            else
                doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodCompactName,
                        this.doopReprBuilder.buildDoopMethodInvocation((MethodSymbol) fieldValue));

            String methodName = fieldValue.toString();
            if (methodName.contains("("))
                methodName = methodName.substring(0, methodName.indexOf("("));
            if (methodName.contains("."))
                methodName = methodName.substring(methodName.lastIndexOf("."));


            /**
             * Evaluate heap allocation counter within method.
             */
            if (!doopMethodInvocation.endsWith("<init>")) {
                if (methodInvocationCounterMap.containsKey(methodName)) {
                    methodInvocationCounter = methodInvocationCounterMap.get(methodName) + 1;
                    methodInvocationCounterMap.put(methodName, methodInvocationCounter);
                } else {
                    methodInvocationCounter = 0;
                    methodInvocationCounterMap.put(methodName, 0);
                }
                doopMethodInvocation += "/" + this.methodInvocationCounter;
            }
            else {
                doopMethodInvocation += "/" + this.constructorInvocationCounter++;
            }

            long invocationPos;
            if (tree.meth.toString().contains("."))
                invocationPos = tree.meth.pos + 1;
            else
                invocationPos = tree.meth.pos;

            Position position = new Position(this.lineMap.getLineNumber(invocationPos),
                    this.lineMap.getColumnNumber(invocationPos),
                    this.lineMap.getLineNumber(invocationPos + methodName.length()));

            System.out.println("\033[35m Method Invocation from visitApply: \033[0m" + doopMethodInvocation);
            SourceFileReport.invocationList.add(new MethodInvocation(position, this.sourceFileName, doopMethodInvocation, this.currentMethod.getId()));
        }
    }

    /**
     * Visit "new <T>()" AST node aka heap allocation.
     *
     * @param tree
     */
    @Override
    public void visitNewClass(JCTree.JCNewClass tree) {
        scan(tree.encl);
        scan(tree.typeargs);
        scan(tree.clazz);
        scan(tree.args);
        scan(tree.def);

        String doopHeapAllocationID;
        /**
         * If current method is overloaded use its signature to build the heap allocation.
         */
        if (this.methodNamesPerClassMap.get(this.currentClassSymbol).get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
            doopHeapAllocationID = this.doopReprBuilder.buildDoopHeapAllocation(currentMethodDoopSignature, tree.clazz.type.getOriginalType().toString());
        /**
         * Otherwise use its compact name.
         */
        else
            doopHeapAllocationID = this.doopReprBuilder.buildDoopHeapAllocation(currentMethodCompactName, tree.clazz.type.getOriginalType().toString());

        /**
         * Evaluate heap allocation counter within method.
         */
        if (heapAllocationCounterMap.containsKey(doopHeapAllocationID)) {
            heapAllocationCounter = heapAllocationCounterMap.get(doopHeapAllocationID) + 1;
            heapAllocationCounterMap.put(doopHeapAllocationID, heapAllocationCounter);
        }
        else {
            heapAllocationCounter = 0;
            heapAllocationCounterMap.put(doopHeapAllocationID, 0);
        }
        doopHeapAllocationID += "/" + heapAllocationCounter;

        /**
         * Add Heap Allocation to source file report.
         */
        System.out.println("Reporting heap allocation: " + doopHeapAllocationID);
        System.out.println("Source file name: " + this.sourceFileName);
        System.out.println("Allocated object type: " + tree.clazz.type.toString());
        System.out.println("Allocating method id: " + this.currentMethod.getId());

        Position position = new Position(lineMap.getLineNumber(tree.clazz.pos),
                lineMap.getColumnNumber(tree.clazz.pos),
                lineMap.getColumnNumber(tree.clazz.pos + tree.clazz.toString().length()));

        SourceFileReport.heapAllocationList.add(new HeapAllocation(position,
                                                                    this.sourceFileName,
                                                                    doopHeapAllocationID,
                                                                    tree.clazz.type.toString(),
                                                                    this.currentMethod.getId()));

        /**
         * Method Invocation: Constructor
         */
        String doopMethodInvocation;
        /**
         * If current method is overloaded use its signature to build the variable name.
         */
        if (this.methodNamesPerClassMap.get(this.currentClassSymbol).get(this.currentMethodSymbol.getQualifiedName().toString()) > 1)
            doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodDoopSignature,
                    this.doopReprBuilder.buildDoopMethodInvocation((MethodSymbol) tree.constructor) + "/" + this.constructorInvocationCounter++);
        /**
         * Otherwise use its compact name.
         */
        else
            doopMethodInvocation = this.doopReprBuilder.buildDoopMethodInvocationInMethod(this.currentMethodCompactName,
                    this.doopReprBuilder.buildDoopMethodInvocation((MethodSymbol) tree.constructor) + "/" + this.constructorInvocationCounter++);

        position = new Position(lineMap.getLineNumber(tree.pos),
                lineMap.getColumnNumber(tree.pos),
                lineMap.getLineNumber(tree.pos) + tree.clazz.toString().length());

        System.out.println("\033[35m Method Invocation (Constructor): \033[0m" + doopMethodInvocation);
        SourceFileReport.invocationList.add(new MethodInvocation(position, this.sourceFileName, doopMethodInvocation, this.currentMethod.getId()));

    }
}
