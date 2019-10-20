/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hello;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
/**
 *
 * @author Necro
 */
public class NBall {
    private NBoard board;
    private int frame = 0;//animation frame
    public int x = 0;
    public int y = 0;
    public int radius = 10;
    public int color = 0xff0000;
    public int speed;//pixels per second
    public int dx;//current horizontal speed
    public int dy;//current vertical speed
    public long birth;
    //ball can be regular ball or bonus
    public static final int ballREGULAR = 100;
    public static final int ballSPAWN = 200;//spawns some amount of new balls
    public static final int ballGROW = 300;//increases caret size
    public static final int ballSPEED = 400;//increase ball speed
    public static final int ballSHRINK = 500;//restores caret to original size
    public static final int ballKILL = 600;//destroys some of balls

    public int type = ballREGULAR;

    public NBall(NBoard _board) {
        birth = System.currentTimeMillis();
        board = _board;
        speed = 0;
        dx = speed;
        dy = speed;
    } //NBall()

    public void setSpeed(int _speed)
    {
        speed = _speed;
        if (dx<0) dx = -speed;
        else dx = speed;
        if (dy < 0) dy = -speed;
        else dy = speed;
    }//setSpeed()

    public void drawBasicBall(Graphics g) {
        g.setColor(0x003f3f3f);
        g.drawRoundRect(board.boardLeft+x-radius/2, board.boardTop+y-radius/2, radius, radius, radius, radius);
        g.setColor(color);
        g.fillRoundRect(board.boardLeft+x-radius/2, board.boardTop+y-radius/2, radius, radius, radius, radius);
        g.setColor(0);
        g.drawRoundRect(board.boardLeft+x-radius/2+1, board.boardTop+y-radius/2+1, radius, radius, radius, radius);
    }//drawBasicBall()

    public void draw (Graphics g) {                
        switch (type) {
            case ballREGULAR:
                drawBasicBall(g);
                break;
            case ballSHRINK:
                g.setColor(0x000000);
                g.fillTriangle(x-radius, y-radius, x+radius, y-radius, x, y+radius);
                g.setColor(0xff0f0f);
                g.fillTriangle(x-radius/2, y+radius/2, x, y-radius/2, x+radius/2, y+radius/2);
                break;
            case ballKILL:
                g.setColor(0x000000);
                g.fillArc(x-radius/2, y-radius/2, radius, radius, 0, 360);
                g.setColor(0xffffff);
                g.drawArc(x-radius/2, y-radius/2, radius, radius, 0, 360);
                g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL));
                g.drawString("+",board.boardLeft+x,board.boardTop+y-g.getFont().getHeight()/2,Graphics.TOP|Graphics.HCENTER);
                break;
            case ballSPAWN:
                drawBasicBall(g);
                g.setColor(0x000000);
                g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL));
                g.drawString("B",board.boardLeft+x,board.boardTop+y-g.getFont().getHeight()/2,Graphics.TOP|Graphics.HCENTER);
                break;
            case ballGROW:
                drawBasicBall(g);
                g.setColor(0x000000);
                g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL));
                g.drawString("G",board.boardLeft+x,board.boardTop+y-g.getFont().getHeight()/2,Graphics.TOP|Graphics.HCENTER);
                break;
            case ballSPEED:
                drawBasicBall(g);
                g.setColor(0x000000);
                g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL));
                g.drawString("S",board.boardLeft+x,board.boardTop+y-g.getFont().getHeight()/2,Graphics.TOP|Graphics.HCENTER);
                break;
        }
    } //draw()

    public void process (long time) {
        //so, we do _speed pixels per second; _time in milliseconds
        x+=(dx*time/1000);
        y+=(dy*time/1000);
        int bound = board.checkBounds(this);        
    } //process()

    public void onCollide(int bound, int edge) {
        //_edge is value must be assigned to collided coord
        switch (bound) {
            case NBoard.boundLEFT: x = edge+radius; dx = -dx; break;
            case NBoard.boundRIGHT: x = edge-radius; dx = -dx; break;
            case NBoard.boundTOP: y = edge+radius; dy = -dy; break;
            case NBoard.boundBOTTOM: y = edge-radius; dy = -dy; break;
            case NBoard.boundCARET: y = edge-radius; dy = -dy; break;
        }//switch
    }//onCollide()
}
