/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NCrowd;

import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
//import javax.microedition.media.Manager;
//import javax.microedition.media.MediaException;
//import javax.microedition.media.Player;
//import javax.microedition.media.control.ToneControl;

/**
 *
 * @author Necro
 */
public class NBoard extends Canvas implements Runnable
{
    public static final int boundLEFT = 100;
    public static final int boundRIGHT = 200;
    public static final int boundTOP = 300;
    public static final int boundBOTTOM = 400;
    public static final int boundCARET = 500;

    public static final int stageUNDEF = 0;
    public static final int stageGAME = 100;
    public static final int stageWIN = 200;
    public static final int stageLOSE = 300;

    private int     msgnum = 0;
    private Vector  elements = new Vector();
    private Vector  balls = new Vector();
    private NCaret  caret = new NCaret(this);
    private int     stage = stageUNDEF;//game stage
    private long    counter = 0;
    private long    maxballs = 0;//max balls seen on the screen
    private long    timePrevious = 0;    
    private Random random = new Random();
    private static final int TICK_MIN = 50;
    

    public int boardLeft;
    public int boardRight;
    public int boardTop;
    public int boardBottom;

    public NBoard() {        
    }//NCanvas()

    public void restart() {
        elements.removeAllElements();
        balls.removeAllElements();
        stage=stageUNDEF;
    }//restart()

    private void initBoard(Graphics g)
    {
        if (stage!=stageUNDEF) return;
        timePrevious = System.currentTimeMillis();
        boardLeft = 0;
        boardRight = g.getClipWidth();
        boardTop = 0;
        boardBottom = g.getClipHeight();
        NBall ball;
        for (int i=0; i<10; i++) {
            spawnBall(NBall.ballREGULAR,null);
        }
        caret.x= (boardRight - boardLeft)/2;
        caret.y= (boardBottom - boardTop) - caret.height;
        caret.width = (boardRight - boardLeft)/6;
        stage = stageGAME;
    }//initBoard()

    public void spawnBall(int balltype, NBall parent)
    {
        NBall ball = new NBall(this);
        ball.radius = (boardRight - boardLeft)/20;
        ball.type = balltype;
        switch (balltype) {
            case NBall.ballSPAWN:
                ball.radius = (int)((float)ball.radius*1.7);
                break;
            case NBall.ballGROW:
                ball.radius = (int)((float)ball.radius*1.7);
                break;
            case NBall.ballSPEED:
                ball.radius = (int)((float)ball.radius*1.7);
                break;
            case NBall.ballSHRINK:
                ball.radius = (int)((float)ball.radius*1.1);
                break;
            case NBall.ballKILL:
                ball.radius = (int)((float)ball.radius*1.1);
                break;
        }
        if (parent==null) {
            ball.color = (128+random.nextInt(128))*256*256 + (128+random.nextInt(256))*256 + (128+random.nextInt(256));
            ball.speed = random.nextInt(ball.radius*7)+ball.radius*2;
            if (random.nextInt(2)>0)
                ball.dx = ball.speed;
            else
                ball.dx = -ball.speed;
            ball.dy = - ball.speed;
            ball.x = (int)((float) (boardRight - boardLeft)*random.nextFloat());
            ball.y = (int)((float) (boardBottom - boardTop)*random.nextFloat());            
        } else {
            ball.color = parent.color;
            ball.speed = parent.speed;
            ball.dx = -parent.dx;
            ball.dy = parent.dy;
            ball.x = parent.x;
            ball.y = parent.y;
        }
        balls.addElement(ball);
    }//spawnBall()

    public int checkBounds(NBall _ball)
    {
        if (System.currentTimeMillis() - _ball.birth < 1000) return 0;
        if ((_ball.x-_ball.radius) <= boardLeft) {_ball.onCollide(boundLEFT,boardLeft);}
        if ((_ball.x+_ball.radius) >= boardRight) {_ball.onCollide(boundRIGHT,boardRight);}
        if ((_ball.y-_ball.radius) <= boardTop) {_ball.onCollide(boundTOP,boardTop);}
        //if ((_ball.y+_ball.radius) >= boardBottom) {_ball.onCollide(boundBOTTOM,boardBottom);}
        if ((_ball.y+_ball.radius) >= boardBottom) {balls.removeElement(_ball);}
        //now test it against caret                
        if (_ball.x > (caret.x-caret.width/2) &&
                _ball.x < (caret.x+caret.width/2) &&
                (_ball.y+_ball.radius) >= (caret.y-caret.height/2))
        {
            _ball.onCollide(boundCARET, caret.y-caret.height/2);//this is to detect strike
           // NBall ball;
            switch (_ball.type) {
                case NBall.ballSPAWN:
                    balls.removeElement(_ball);
                    for (int i=0; i<5; i++) {
                       spawnBall(NBall.ballREGULAR, null);
                    }
                    break;
                case NBall.ballGROW:
                    balls.removeElement(_ball);                    
                    caret.width+=(boardRight - boardLeft)/18;
                    addText("you grow!");
                    if (caret.width>=(boardRight - boardLeft)) {
                        //oops!!!
                        caret.width = (boardRight - boardLeft)/6;
                        addText("overgrown!");
                    }
                    break;
                case NBall.ballSPEED:
                    balls.removeElement(_ball);
                    incSpeed();
                    addText("speed up!");
                    break;
                case NBall.ballSHRINK:
                    balls.removeElement(_ball);
                    if (caret.width > (boardRight - boardLeft)/6) {
                        caret.width -= (boardRight - boardLeft)/12;
                        addText("shrink!");
                    }
                    break;
                case NBall.ballKILL:
                    balls.removeElement(_ball);
                    int size = balls.size();
                    for (int i=0;i<size/3;i++)
                        balls.removeElementAt(0);
                    addText("victims: "+Long.toString(size/3));
                    break;
                default:                    
                    int rand = random.nextInt(16);
                    if (rand==1) {                        
                        spawnBall(NBall.ballSPAWN, _ball);
                        addText("spawn");
                    } else if (rand==2) {                        
                        spawnBall(NBall.ballGROW, _ball);
                        addText("grow");
                    } else if (rand==3) {                        
                        spawnBall(NBall.ballSPEED, _ball);
                        addText("speed");
                    } else if (rand==4) {                        
                        spawnBall(NBall.ballSHRINK, _ball);
                        addText("shrink");
                    } else if (rand==5) {                        
                        spawnBall(NBall.ballKILL, _ball);
                        addText("kill");
                    } else {
                        spawnBall(NBall.ballREGULAR, _ball);
                    }                    
                    break;
            }//switch()
        }        
        
        return 0;
    }

    public int checkBounds(NCaret _caret)
    {
        if ((_caret.x) <= boardLeft) {_caret.x = boardLeft; return boundLEFT;}
        if ((_caret.x) >= boardRight) {_caret.x = boardRight; return boundRIGHT;}
        return 0;
    }

    public void incSpeed() {
        if (stage!=stageGAME) return;
        NBall ball;
        for (int i=0;i<balls.size();i++) {
            ball = (NBall)balls.elementAt(i);
            ball.setSpeed(ball.speed+10);
        }
    }

    public void decSpeed() {
        if (stage!=stageGAME) return;
        NBall ball;
        for (int i=0;i<balls.size();i++) {
            ball = (NBall)balls.elementAt(i);
            ball.setSpeed(ball.speed-10);
        }
    }

    public void paint (Graphics g) {
        switch (stage) {
            case stageUNDEF:
                initBoard(g);
                break;
            case stageGAME:
                paintGame(g);
                break;
            case stageWIN:
                paintWin(g);
                break;
            case stageLOSE:
                paintLose(g);
                break;
        };//switch
    }//paint()
    
    protected void paintGame(Graphics g) {
        g.setColor(0x00ffff);
        g.fillRoundRect(boardLeft, boardTop, boardRight, boardBottom, 10, 10);        
        //g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM));
        g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        g.setColor(0x0000ff);
        g.drawLine(boardLeft, caret.y-caret.height/2, boardRight, caret.y-caret.height/2);
        g.setColor(0xf0f0f0);
        g.drawLine(boardLeft, boardBottom, boardRight, boardBottom);                
        g.setColor(0xff00ff);
        g.drawString("Necro, "+Long.toString(counter)+","+Long.toString(balls.size()),0,0,Graphics.TOP|Graphics.LEFT);
        for (int i=0;i<elements.size();i++)
            g.drawString(elements.elementAt(i).toString(),0,(i+1)*g.getFont().getHeight(),Graphics.TOP|Graphics.LEFT);
        NBall ball;
        for (int i=0;i<balls.size();i++) {
            ball = (NBall)balls.elementAt(i);
            ball.draw(g);
        }
        caret.draw(g);
    }//paintGame()

    protected void paintWin(Graphics g) {
        g.setColor(0xffffff);
        g.fillRoundRect(boardLeft, boardTop, boardRight, boardBottom, 10, 10);
        g.setColor(0x000000);
        g.drawArc(5, 5, getWidth()-10, getHeight()-10, 0, 360);
        g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM));        
        g.drawString("YOU WIN",(boardRight-boardLeft)/2,(boardBottom-boardTop)/2,Graphics.TOP|Graphics.HCENTER);
    }//paintWin()

    protected void paintLose(Graphics g) {
        g.setColor(0xff0f0f);
        g.fillRoundRect(boardLeft, boardTop, boardRight, boardBottom, 10, 10);
        g.setColor(0x000000);
        g.drawArc(5, 5, getWidth()-10, getHeight()-10, 0, 360);
        g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM));        
        g.drawString("YOU LOSE",(boardRight-boardLeft)/2,(boardBottom-boardTop)/2,Graphics.TOP|Graphics.HCENTER);
        g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_SMALL));
        g.drawString("max balls: "+Long.toString(maxballs),(boardRight-boardLeft)/2,3*(boardBottom-boardTop)/4,Graphics.TOP|Graphics.HCENTER);
    }//paintWin()

    protected void hideNotify () {
        super.hideNotify ();
    }//hideNotify ()

    protected void showNotify () {
        super.showNotify ();
    }//showNotify ()

    public void run () {
        while (true) {            
            try {                
                switch (stage) {
                    case stageUNDEF:
                        repaint();
                        break;
                    case stageGAME:
                        counter = tickTime();
                        NBall ball;
                        for (int i=0;i<balls.size();i++) {
                            ball = (NBall)balls.elementAt(i);
                            ball.process(counter);
                        }
                        if (balls.size()>maxballs) maxballs = balls.size();
                        repaint();
                        counter++;

                        if (balls.size()>=100) stage = stageWIN;
                        if (balls.size()<=0) stage = stageLOSE;
                        break;
                    case stageWIN:
                        break;
                    case stageLOSE:
                        break;
                }//switch
            }
            catch( Exception e ){
                addText("exception");
            }
        }
    }//run

    protected void keyPressed(int keyCode)
    {
        switch(keyCode) {
            case KEY_STAR: decSpeed(); break;
            case KEY_POUND: incSpeed(); break;
            case KEY_NUM4: caret.moveLeft(); break;
            case KEY_NUM6: caret.moveRight(); break;
        }
    }//keyPressed()

    protected void keyRepeated(int keyCode)
    {
        keyPressed(keyCode);
    }//keyRepeated()

    private long tickTime()
    //from http://www.rsdn.ru/article/java/J2MEFirstSteps.xml
    {
        long tick = System.currentTimeMillis() - timePrevious;
        if (tick < TICK_MIN){
            try{
                Thread.sleep(TICK_MIN - tick);
            } catch (InterruptedException ignored) {
                //Ничего страшного, если проспим меньше положенного
            }
            tick = System.currentTimeMillis() - timePrevious;
        }
        timePrevious = System.currentTimeMillis();
        return tick;
    } //long tickTime()

    public void addText(String s) {
        msgnum++;
        elements.addElement(Long.toString(msgnum)+". "+s);
        if (elements.size()>5)
            elements.removeElementAt(0);
    }
}
