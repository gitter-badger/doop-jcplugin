/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package doop.javac_plugin.reporters;

import doop.javac_plugin.representations.CallGraphEdge;
import doop.javac_plugin.representations.InstanceFieldPointsTo;
import doop.javac_plugin.representations.VarPointsTo;

/**
 * @author anantoni
 */
public interface Reporter {

    void reportVarPointsTo(VarPointsTo varPointsTo);
    void reportCallGraphEdge(CallGraphEdge callGraphEdge);
    void reportInstanceFieldPointsTo(InstanceFieldPointsTo instanceFieldPointsTo);

}
