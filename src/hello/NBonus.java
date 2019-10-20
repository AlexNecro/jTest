/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hello;

import javax.microedition.lcdui.Graphics;
/**
 *
 * @author Necro
 */
//bonus is something that is falling from top to bottom
public class NBonus {
    private NBoard board;
    public int x = 0;
    public int y = 0;
    public int radius = 10;
    public int color = 0xff0000;
    public int speed;//pixels per second
    public int dx;//current horizontal speed
    public int dy;//current vertical speed
    public long birth;

    public NBonus(NBoard _board) {
        board = _board;
    } //NBall()
    
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRoundRect(board.boardLeft+x-radius/2, board.boardTop+y-radius/2, radius, radius, radius, radius);
    } //draw()

    public void process (long time) {
        //so, we do _speed pixels per second; _time in milliseconds
        x+=(dx*time/1000);
        y+=(dy*time/1000);
        //int bound = board.checkBounds(this);
    } //process()
}
