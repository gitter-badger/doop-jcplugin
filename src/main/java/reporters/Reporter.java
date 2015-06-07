/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporters;

import doop.MethodInvocation;
import doop.VarPointsTo;

import java.lang.reflect.Method;

/**
 * @author anantoni
 */
public interface Reporter {
    void reportVar(int startPos, int endPos, String representation);

    void reportVarPointsTo(VarPointsTo varPointsTo);

    void reportMethodInvocation(MethodInvocation methodInvocation);

    void reportFieldAccess(int startPos, int endPos, String representation);

}
