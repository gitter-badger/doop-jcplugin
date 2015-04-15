/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporters;

/**
 * @author anantoni
 */
public interface Reporter {
    void reportVar(int startx, int starty, int endx, int endy, String representation);

    void reportMethodInvocation(int startx, int starty, int endx, int endy, String representation);

    void reportFieldAccess(int startx, int starty, int endx, int endy, String representation);
}
