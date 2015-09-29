/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doop.jcplugin.reporters;

import doop.jcplugin.representation.CallGraphEdge;
import doop.jcplugin.representation.InstanceFieldPointsTo;
import doop.jcplugin.representation.VarPointsTo;

/**
 * @author anantoni
 */
public interface Reporter {

    void reportVarPointsTo(VarPointsTo varPointsTo);
    void reportCallGraphEdge(CallGraphEdge callGraphEdge);
    void reportInstanceFieldPointsTo(InstanceFieldPointsTo instanceFieldPointsTo);

}
