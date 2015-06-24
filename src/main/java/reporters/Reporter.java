/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporters;

import doop.CallGraphEdge;
import doop.InstanceFieldPointsTo;
import doop.VarPointsTo;
import sun.security.jca.GetInstance;

/**
 * @author anantoni
 */
public interface Reporter {

    void reportVarPointsTo(VarPointsTo varPointsTo);
    void reportCallGraphEdge(CallGraphEdge callGraphEdge);
    void reportInstanceFieldPointsTo(InstanceFieldPointsTo instanceFieldPointsTo);

}
