/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporters;

import doop.VarPointsTo;

/**
 * @author anantoni
 */
public class ConsoleReporter implements Reporter {

    @Override
    public void reportVar(int startPos, int endPos, String representation) {
        System.out.println("(" + startPos + ", " + endPos + ")" + " => " + representation);
    }

    @Override
    public void reportVarPointsTo(VarPointsTo varPointsTo) {

    }

    @Override
    public void reportMethodInvocation(int startPos, int endPos, String representation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reportFieldAccess(int startPos, int endPos, String representation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
