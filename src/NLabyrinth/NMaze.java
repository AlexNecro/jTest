/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NLabyrinth;

import java.util.Vector;

/**
 *
 * @author Necro
 */
class NWayPoint {
    private static int lastid=0;
    public int id;
    public int x;//current x
    public int y;//current y
    public int length;//way length
    public boolean isCompleted;//is this way is ended

    NWayPoint(NWayPoint wpt) {
        lastid++;
        this.id = lastid;
        x = wpt.x;
        y = wpt.y;
        length = wpt.length;
        isCompleted = wpt.isCompleted;
    }
    NWayPoint(int x, int y) {
        lastid++;
        this.id = lastid;
        this.x = x;
        this.y = y;
        length = 0;
        isCompleted = false;
    }
}//NWayPoint

public class NMaze {
    protected Vector waypoints = new Vector();
    private NBoard board;
    //private Random random = new Random();
    //private NWayPoint[] waypoints;
    public int startX, startY, endX, endY;//this is result
    public int step;//current step, for reference
    public boolean isOrphan = false;

    protected int mapWidth = 8;//in tiles
    protected int mapHeight = 8;
    protected int[] map;
    private boolean isMapGenerated = false;
    /*protected int[] map = {//test suite:
        1,0,1,1, 1,0,0,0,
        1,0,1,0, 1,1,1,0,
        1,1,1,0, 1,0,1,1,
        0,0,1,1, 1,1,0,1,

        1,1,1,0, 0,1,0,1,
        1,0,1,1, 1,1,0,1,
        1,1,1,0, 0,0,1,1,
        0,0,1,0, 1,1,1,0
    };*/

    //tile passability
    public static final int passLeft = 0x1;
    public static final int passTop = 0x2;
    public static final int passRight = 0x4;
    public static final int passBottom = 0x8;
    //tile visual types:
    public static final int tileHH = 1;//horizontal
    public static final int tileVV = 2;//vertical
    public static final int tileLB = 3;//left to bottom
    public static final int tileRB = 4;//right to bottom
    public static final int tileLT = 5;//left to top
    public static final int tileRT = 6;//right to top
    public static final int tileHT = 7;//horiz+top
    public static final int tileHB = 8;//horiz+bottom
    public static final int tileVL = 9;//vert+left
    public static final int tileVR = 10;//vert+right
    public static final int tileXX = 11;//4-way corner
    public static final int tileT0 = 12;//top to dead end
    public static final int tileB0 = 13;//bottom to dead end
    public static final int tileL0 = 14;//left to dead end
    public static final int tileR0 = 15;//right to dead end
    public static final int tile00 = 16;//empty; must highest index

    //each tile has moveablity determined by types of squares at the edges
    //pass2tile maps from passability to tile??; from 0000b to 1111b
    //bit order: BRTL
    private static final int[] pass2tile = {
        tile00/*0000*/, tileL0/*0001*/, tileT0/*0010*/, tileLT/*0011*/,
        tileR0/*0100*/, tileHH/*0101*/, tileRT/*0110*/, tileHT/*0111*/,
        tileB0/*1000*/, tileLB/*1001*/, tileVV/*1010*/, tileVL/*1011*/,
        tileRB/*1100*/, tileHB/*1101*/, tileVR/*1110*/, tileXX/*1111*/};

    public NMaze(NBoard _board, int width, int height) {
        mapWidth = width;
        mapHeight = height;
        isMapGenerated = false;
        board = _board;
        map = new int[width*height];
    }//NMaze()

    public int getVal(int x, int y) {
        if (x<0 || y<0 || x>=mapWidth || y>=mapHeight) return 0;
        return map[x+y*mapHeight];
    }//getVal()

    public void genStart() {//starts generation of new maze
        for (int y=0;y<mapHeight;y++)
            for (int x=0;x<mapWidth;x++) {
                map[y*mapHeight + x] = 0;
            }
        endX = board.random.nextInt(mapWidth);
        endY = board.random.nextInt(mapHeight);
        startX = endX;
        startY = endY;
        isMapGenerated = false;
        isOrphan = false;
        step = 0;
        waypoints.removeAllElements();
        waypoints.addElement(new NWayPoint(endX,endY));        
    }//genStart()

    private int genWayPoint(NWayPoint wpt) {
        map[wpt.x + wpt.y*mapHeight] = 1;
        if (wpt.isCompleted == true) {
            wpt.isCompleted = true;
            return 0;
        }
        //board.addText(Integer.toString(wpt.id)+"."+Integer.toString(wpt.length)+"> "+Integer.toString(endX)+","+Integer.toString(endY));
        int dir = getDir(wpt.x,wpt.y);
        step++;
        if (dir!=0) {
            wpt.length++;
            switch (dir) {
                case passLeft: wpt.x--; map[wpt.x + wpt.y*mapHeight] = 1; break;
                case passRight: wpt.x++; map[wpt.x + wpt.y*mapHeight] = 1; break;
                case passTop: wpt.y--; map[wpt.x + wpt.y*mapHeight] = 1; break;
                case passBottom: wpt.y++; map[wpt.x + wpt.y*mapHeight] = 1; break;
                default:
                    board.addText(Integer.toString(wpt.id)+"."+Integer.toString(wpt.length)+"> ERR STOP");
                    wpt.isCompleted = true;
                    return 0;
            }//switch dir
            //try add one more point:
            dir = getDir(wpt.x,wpt.y)*board.random.nextInt(2);// *random.nextInteger(2)
            if (dir!=0) waypoints.addElement(new NWayPoint(wpt));
            return 1;
        } else {
            wpt.isCompleted = true;
            board.addText(Integer.toString(wpt.id)+"."+Integer.toString(wpt.length)+"> END");
            return 0;
        }//if dir
    }
    public int genStep() {//generates next point(s)
        int pts = 0;
        NWayPoint wpt;
        for (int i=0;i<waypoints.size();i++) {
            wpt = (NWayPoint) waypoints.elementAt(i);
            if (!wpt.isCompleted)
                pts+=genWayPoint(wpt);
        }
        int maxlen = 0;
        for (int i=0;i<waypoints.size();i++) {
            wpt = (NWayPoint) waypoints.elementAt(i);
            if (wpt.length>maxlen) {
                endX = wpt.x;
                endY = wpt.y;
                maxlen = wpt.length;
            }
        }
        //after entire maze generated, we can measure path lengths between all
        //ending points and set start and end points to ends of largest path.
        //we're guaranteed all points are accessible
        //but because we'll compare lengths stored int pts, we're not guaranteed
        //that shorter paths between pts is not exists
        if (pts==0)
            isMapGenerated=true;
        return pts;
    }//genStep()

    public boolean genIsCompleted() {
        return isMapGenerated;
    }//genIsCompleted()

    public int getPassability(int x, int y, int passable) {
        //passable=1 tests for passability, passable=0 tests for inpassability
        int pass =0;
        if (x<0 || y<0 || x>=mapWidth || y>=mapHeight) return pass;

        if ((x>0) && (map[(x-1)+y*mapHeight] == passable)) pass = pass|passLeft;
        if ((x<(mapWidth-1)) && (map[(x+1)+y*mapHeight] == passable)) pass = pass|passRight;
        if ((y>0) && (map[(x)+(y-1)*mapHeight] == passable)) pass = pass|passTop;
        if ((y<(mapHeight-1)) && (map[(x)+(y+1)*mapHeight] == passable)) pass = pass|passBottom;

        if (!isOrphan && step>1 && !isMapGenerated && pass==0 && passable==1 && map[x+y*mapHeight]==1) {
            board.addText("[Orphan:"+Integer.toString(x)+","+Integer.toString(y)+"]");
            isOrphan = true;            
        }
        return pass;
    }//getPassability()

    public int getTileVisualType(int x, int y) {
        //tile itself can't be passed:
        try {
            if (map[x+y*mapHeight] == 0) return tile00;

            //so we constructed passability mask
            return pass2tile[getPassability(x,y,1)];
        } catch (Exception e) {
            board.addText(Long.toString(x)+","+Long.toString(y)+" "+e.toString());
            return tile00;
        }
    }

    public int  getDir(int x, int y) {
        //returns possible random direction from specified point
        //calculate number of possibilities:
        int pass = getPassability(x,y,0);
        int passnumber = 0;
        if ((pass&passLeft) == passLeft)
            if (getPassability(x-1,y,1) == passRight)
                passnumber+=1;
        if ((pass&passTop) == passTop)
            if (getPassability(x,y-1,1) == passBottom)
                passnumber+=1;
        if ((pass&passRight) == passRight)
            if (getPassability(x+1,y,1) == passLeft)
                passnumber+=1;
        if ((pass&passBottom) == passBottom)
            if (getPassability(x,y+1,1) == passTop)
                passnumber+=1;

        int rand = 0;
        if (passnumber == 0) {
            board.addText("[BRTL]"+Integer.toString(passnumber)+"/"+Integer.toString(pass,2)+" ("+Integer.toString(x)+","+Integer.toString(y)+")");
            return 0;
        } else {
            rand = board.random.nextInt(passnumber)+1;
        }


        if (rand==1) {
            //we test choosen point: it must have only one road attached to it, our current
            if ((pass&passLeft) == passLeft && getPassability(x-1,y,1) == passRight) return passLeft;
            if ((pass&passTop) == passTop  && getPassability(x,y-1,1) == passBottom) return passTop;
            if ((pass&passRight) == passRight && getPassability(x+1,y,1) == passLeft) return passRight;
            if ((pass&passBottom) == passBottom && getPassability(x,y+1,1) == passTop) return passBottom;
        }
        if (rand==2) {
            if ((pass&passTop) == passTop  && getPassability(x,y-1,1) == passBottom) return passTop;
            if ((pass&passRight) == passRight && getPassability(x+1,y,1) == passLeft) return passRight;
            if ((pass&passBottom) == passBottom && getPassability(x,y+1,1) == passTop) return passBottom;
        }
        if (rand==3) {
            if ((pass&passRight) == passRight && getPassability(x+1,y,1) == passLeft) return passRight;
            if ((pass&passBottom) == passBottom && getPassability(x,y+1,1) == passTop) return passBottom;
        }
        if (rand==4) {
            if ((pass&passBottom) == passBottom && getPassability(x,y+1,1) == passTop) return passBottom;
        }
        //addText("[x]"+Integer.toString(passnumber)+"/"+Integer.toString(pass)+" ("+Integer.toString(x)+","+Integer.toString(y)+")");
        return 0;
    }//getDir()
}
