/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NLabyrinth;

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.TiledLayer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
//import javax.microedition.lcdui.Font;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.game.LayerManager;

/**
 *
 * @author Necro
 */

public class NBoard extends GameCanvas implements Runnable {
    protected int Score=0;
    protected int Level=1;
    protected NMaze maze;
    protected Vector objects = new Vector();//first object is always player, 2nd - level gate (exit), 3rd - level entrance
    public static final int objectPlayer = 10;
    public static final int objectLevelGate = 20;
    public static final int objectWall = 30;//simple static non-passable sprite
    public static final int objectGhost = 40;//simple static passable sprite
    public static final int objectGem = 50;//object that we want collect
    public static final int objectMob = 60;//object that moves itself; it can kill
    public static final int objectSpawn = 70;//object that generates mobs
    public static final int objectBomb = 80;//object that harms mobs & player after few seconds it was placed

    private long timePrevious = 0;
    private long tick;
    private static final int TICK_MIN = 200;
    private static final int MAX_MSGS = 15;    
    private int     stage = stageUNDEF;//game stage

    public Random random = new Random();//everybody must use this object
    protected LayerManager layermanager = new LayerManager();
    protected NResourceManager resourcemanager;
    protected TiledLayer tileset;
    protected Image imgWin;
    protected Image imgLose;
    
    protected int tileSize = 32;
    protected int mapOriginX = 0;//in pixels, from upper-left screen corner
    protected int mapOriginY = 0;
    protected int speed=16;//movement speed

    public static final int stageUNDEF = 0;
    public static final int stageINIT = 100;
    public static final int stageGAME = 200;
    public static final int stageWIN = 300;
    public static final int stageLOSE = 400;

    private int tileOffset = 0;//must be inc by 24 to get other tileset    

    public int boardLeft;
    public int boardRight;
    public int boardTop;
    public int boardBottom;

    private int     msgnum = 0;
    private Vector  messages = new Vector();

    public NBoard() {
        super(true);
        resourcemanager = new NResourceManager(this);        
        maze = new NMaze(this, 4,4);
        
        imgWin = resourcemanager.getImage("win");
        imgLose = resourcemanager.getImage("lose");
        return;
    }//Nboard()

    public void restart() {
        stage=stageUNDEF;
        msgnum=0;
        messages.removeAllElements();
        if (Level<2) {Score=0;Level=1;}
    }//restart()

    public void dump()
    {//debug
        String str = new String();
        for (int y=0;y<maze.mapHeight;y++){
            str="";
            for (int x=0;x<maze.mapWidth;x++) {
                str+=Integer.toString(maze.getVal(x,y));
            }
            addText(str);
        }
    }

    private void movePlayer(int dir) {
        NGameObject obj = (NGameObject)objects.elementAt(0);//player
        switch(dir) {
            case NMaze.passLeft:
                if ((maze.getPassability(obj.x,obj.y,1)&NMaze.passLeft) == NMaze.passLeft)
                    obj.moveBy(-1,0);
                break;
            case NMaze.passRight:
                if ((maze.getPassability(obj.x,obj.y,1)&NMaze.passRight) == NMaze.passRight)
                    obj.moveBy(+1,0);
                break;
            case NMaze.passTop:
                if ((maze.getPassability(obj.x,obj.y,1)&NMaze.passTop) == NMaze.passTop)
                    obj.moveBy(0,-1);
                break;
            case NMaze.passBottom:
                if ((maze.getPassability(obj.x,obj.y,1)&NMaze.passBottom) == NMaze.passBottom)
                    obj.moveBy(0,+1);
                break;
        }//switch dir
        mapOriginX = (boardRight-boardLeft)/2 - obj.x*tileSize;
        mapOriginY = (boardBottom-boardTop)/2 - obj.y*tileSize;
    }//movePlayer()


    private void checkUserInput() {
        int keyStates = getKeyStates();
        if (stage == stageGAME) {
            if ((keyStates & LEFT_PRESSED) != 0) {
                movePlayer(NMaze.passLeft);
            } else if ((keyStates & RIGHT_PRESSED) != 0) {
                movePlayer(NMaze.passRight);
            } else if ((keyStates & UP_PRESSED) != 0) {
                movePlayer(NMaze.passTop);
            } else if ((keyStates & DOWN_PRESSED) != 0) {
                movePlayer(NMaze.passBottom);
            } else if ((keyStates & FIRE_PRESSED) != 0) {
                ((NGameObject)objects.elementAt(0)).Attack(null);
            }
        } else if (stage == stageWIN){
            if ((keyStates & FIRE_PRESSED) != 0)
                restart();
        }
    }//checkUserInput()

    public void reloadTileset(int direction) {
        //direction = -1 or 1
        tileOffset += direction*24;
        addText("theme changed ("+Long.toString(tileOffset)+")");
        for (int y=0;y<maze.mapHeight;y++)
            for (int x=0;x<maze.mapWidth;x++) {
                tileset.setCell(x, y, tileset.getCell(x,y)+tileOffset);
                }        
    }//reloadTileset()

    private void initBoard(Graphics g)
    {
        if (stage!=stageUNDEF) return;
        timePrevious = System.currentTimeMillis();
        boardLeft = 0;
        boardRight = g.getClipWidth();
        boardTop = 0;
        boardBottom = g.getClipHeight();
        stage = stageINIT;

        maze = new NMaze(this, Level*4,Level*4);
        for (int i=0;i<objects.size();i++)
            ((NGameObject)objects.elementAt(i)).deinit();
        objects.removeAllElements();
        objects.addElement(new NGameObject(this, objectPlayer, 0, 0));//<--- there is it!
        objects.addElement(new NGameObject(this, objectLevelGate, 0, 0));
        objects.addElement(new NGameObject(this, objectGhost, 0, 0, tileSize,tileSize,"arrow_red"));        

        if (tileset!=null)
            layermanager.remove(tileset);
        tileset = new TiledLayer(maze.mapWidth, maze.mapHeight, resourcemanager.getImage("lab_tileset"), tileSize, tileSize);
        layermanager.append(tileset);
        
        addText("("+Integer.toString(maze.startX)+","+Integer.toString(maze.startY)+") - ("+Integer.toString(maze.endX)+","+Integer.toString(maze.endY)+")");
        maze.genStart();
    }//initBoard()

    protected void removeObject(NGameObject obj) {
        obj.deinit();
        objects.removeElement(obj);
    }//removeObject()

    protected NGameObject getObject(int i) {
        return ((NGameObject)objects.elementAt(i));
    }//getObject(int)

    public int getObjectCount(int type) {
        int count = 0;
        for (int i=0;i<objects.size();i++)
            if (getObject(i).type == type) count++;
        return count;
    }//getObjectCount();

    private void drawInit(Graphics g) {        
        g.setColor(0xaf00af);
        g.fillRect(0, 0, getWidth(), getHeight());
        tileset.setPosition(mapOriginX, mapOriginY);
        for (int i=0;i<objects.size();i++) {            
            getObject(i).draw(g);
        }        
        layermanager.paint(g,0,0);
        drawMessages(g);        
    }

    private void drawGame(Graphics g) {        
        NGameObject obj;
        g.setColor(0xaf00af);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (int i=0;i<objects.size();i++) {
            getObject(i).draw(g);
        }
        obj = getObject(0);
        mapOriginX = (boardRight-boardLeft)/2 - obj.x*tileSize;
        mapOriginY = (boardBottom-boardTop)/2 - obj.y*tileSize;
        tileset.setPosition(mapOriginX, mapOriginY);
        layermanager.paint(g,0,0);
        g.setColor(0x808080);
        g.drawString("NLabyrinth - Necro, 2011",0,0,Graphics.TOP|Graphics.LEFT);
        g.drawString("Lvl: "+Long.toString(Level)+" Lives: "+Long.toString(getObject(0).Lives)+"; "+Integer.toString(maze.mapWidth)+"x"+Integer.toString(maze.mapWidth)+" * "+Integer.toString(Score),0,g.getFont().getHeight(),Graphics.TOP|Graphics.LEFT);
        drawMessages(g);
    }

    protected void drawEndGame(Graphics g) {
        g.setColor(0xaffffff);
        g.fillRect(0, 0, g.getClipWidth(), g.getClipHeight());
        if (stage == stageWIN)
            g.drawImage(imgWin, g.getClipWidth()/2 - imgWin.getWidth()/2, g.getClipHeight()/2 - imgWin.getHeight()/2, 0);
        else
            g.drawImage(imgLose, g.getClipWidth()/2 - imgWin.getWidth()/2, g.getClipHeight()/2 - imgWin.getHeight()/2, 0);
        //drawMessages(g);
    }//drawWin()

    private void drawMessages(Graphics g) {
        g.setColor(0x808080);        
        for (int i=0;i<messages.size();i++)
            g.drawString(messages.elementAt(i).toString(),0,(i+2)*g.getFont().getHeight(),Graphics.TOP|Graphics.LEFT);
    }

    private void processgame() {        
        for (int i=0;i<objects.size();i++) {
            getObject(i).process(timePrevious);
        }
        if (getObject(0).Lives<=0) {
            Level--;
            stage = stageLOSE;}
    }//processgame()

    protected boolean isWall(int x, int y) {
        if (maze.getVal(x, y)==0) return true;
        return false;
    }//isWall()

    protected NGameObject testNear(NGameObject obj) {
        NGameObject test;
        for (int i=0;i<objects.size();i++) {
            test = getObject(i);
            if(test==null || test==obj) continue;
            if (((obj.x-test.x)==1 && obj.y==test.y) ||
                ((obj.y-test.y)==1 && obj.x==test.x) ||
                ((obj.x-test.x)==-1 && obj.y==test.y) ||
                ((obj.y-test.y)==-1 && obj.x==test.x)){
                //we're near... something
                if (obj.type == objectMob && test.type==objectPlayer) {
                    //addText("start attack");
                    return test;
                }
                if (test.type == objectMob && obj.type==objectPlayer) {
                    //addText("start attack");
                    return test;                    
                }
            }
        }
        return null;
    }//testNear()

    protected NGameObject testCollide(NGameObject obj) {
        //returns colliding object, if any; does actions
        NGameObject test;
        for (int i=0;i<objects.size();i++) {
            test = getObject(i);
            if(test==null || test==obj) continue;
            if (obj.x==test.x && obj.y==test.y) {
                if (obj.type == objectPlayer) {
                    switch(test.type) {
                        case objectLevelGate:
                            Score+=maze.mapWidth*maze.mapHeight/4;
                            Level++;
                            stage = stageWIN;                            
                            break;
                        case objectMob:                            
                                test.Attack(obj);                                
                            break;
                    }//switch
                } else if (obj.type == objectMob) {
                    switch(test.type) {
                        case objectPlayer:
                            test.Attack(obj);                            
                            break;
                        case objectSpawn:
                            //addText("mob vs spawn");
                            removeObject(obj);
                            break;
                    }//switch
                }
                return test;
            }//if            
        }//for i
        return null;
    }//testCollide()
    
    private void process() {        
        //all the deals is done here
        switch(stage) {
            case stageINIT:
                try {
                    NGameObject obj;
                    //move initial objects to they positions
                    obj = getObject(0);//player
                    obj.x = maze.startX;
                    obj.y = maze.startY;
                    obj = getObject(1);//exit
                    obj.x = maze.endX;
                    obj.y = maze.endY;
                    obj = getObject(2);//entrance
                    obj.x = maze.startX;
                    obj.y = maze.startY;
                    for (int y=0;y<maze.mapHeight;y++)
                        for (int x=0;x<maze.mapWidth;x++) {
                            tileset.setCell(x, y, tileOffset+maze.getTileVisualType(x,y));
                        }
                    if (maze.genStep()==0) {                        
                        layermanager.remove(tileset);
                        NWayPoint wp;
                        //generate spawns; must be approx 1 spawn per 16 tiles
                        int nspawns=0;
                        for (int i=0;i<maze.waypoints.size();i++) {
                            wp = (NWayPoint)maze.waypoints.elementAt(i);                            
                            if (!(wp.x == getObject(1).x && wp.y == getObject(1).y) &&
                                !(wp.x == getObject(0).x && wp.y == getObject(0).y) &&
                                maze.getVal(wp.x,wp.y)>0)
                            {
                                //in addition, test that it's "blind" corner
                                if (maze.getVal(wp.x-1,wp.y)+maze.getVal(wp.x+1,wp.y)+
                                    maze.getVal(wp.x,wp.y-1)+maze.getVal(wp.x,wp.y+1) == 1)
                                {
                                    nspawns++;
                                    objects.addElement(new NGameObject(this, objectSpawn, wp.x, wp.y));
                                    if (nspawns > (maze.mapWidth*maze.mapHeight)/16) break;
                                }                                    
                            }
                        }//for i                        
                        if (nspawns < (maze.mapWidth*maze.mapHeight)/16) {
                            //messages.removeAllElements();                            
                            restart();
                            return;
                        }
                        layermanager.append(tileset);
                        stage=stageGAME;
                    }
                    messages.removeAllElements();                    
                } catch(Exception e) {
                    addText(e.toString());
                }
                break;
            case stageGAME:
                processgame();
                break;
            default:
        }//switch
    }//process()

    private void drawBoard() {
        Graphics g = getGraphics();
        switch (stage) {
            case stageWIN:
                drawEndGame(g);
                break;
            case stageLOSE:
                drawEndGame(g);
                break;
            case stageUNDEF:
                initBoard(g);
                break;
            case stageGAME:
                drawGame(g);
                break;
            case stageINIT:
                drawInit(g);
                break;            
        };//switch
        flushGraphics();
    }//drawBoard()

    public void run() {        
        while(true) {
            tick = tickTime();
            checkUserInput();
            process();
            drawBoard();
        }
    }//run()


    protected void hideNotify () {
        super.hideNotify ();
    }//hideNotify ()

    protected void showNotify () {
        super.showNotify ();
    }//showNotify ()

    private long tickTime()
    //from http://www.rsdn.ru/article/java/J2MEFirstSteps.xml
    {
        tick = System.currentTimeMillis() - timePrevious;
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
        messages.addElement(Long.toString(msgnum)+". "+s);
        if (messages.size()>MAX_MSGS)
            messages.removeElementAt(0);
    }
}
