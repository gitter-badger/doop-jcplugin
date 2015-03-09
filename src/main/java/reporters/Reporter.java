/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporters;

/**
 *
 * @author anantoni
 */
public interface Reporter {
    public void reportVar(int startx, int starty, int endx, int endy, String representation);
    public void reportMethodInvocation(int startx, int starty, int endx, int endy, String representation);
    public void reportFieldAccess(int startx, int starty, int endx, int endy, String representation);
}
