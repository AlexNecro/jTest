/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NCrowd;

import javax.microedition.lcdui.Graphics;
/**
 *
 * @author Necro
 */
public class NCaret {
    private NBoard board;
    public int x = 0;
    public int y = 0;
    public int width = 40;
    public int height = 10;
    public int color = 0x0000ff;
    public int speed = 20; //pixels per move

    public NCaret(NBoard _board) {        
        board = _board;
    } //NBall()

    public void moveLeft()
    {
        x-=speed;
        board.checkBounds(this);
    }

    public void moveRight()
    {
        x+=speed;
        board.checkBounds(this);
    }

    public void draw (Graphics g) {
        g.setColor(color);
        g.fillRect(board.boardLeft+x-width/2, board.boardTop+y-height/2, width, height);
    } //draw()
}
