/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporters;

/**
 * @author anantoni
 */
public class ConsoleReporter implements Reporter {

    @Override
    public void reportVar(int startx, int starty, int endx, int endy, String representation) {
        System.out.println("(" + startx + ", " + starty + ")" + "(" + endx + ", " + endy + ")" + " => " + representation);
    }

    @Override
    public void reportMethodInvocation(int startx, int starty, int endx, int endy, String representation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reportFieldAccess(int startx, int starty, int endx, int endy, String representation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
