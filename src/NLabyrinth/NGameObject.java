/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NLabyrinth;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.MediaException;
/**
 *
 * @author Necro
 */
public class NGameObject {//this is not pure abstract class, so it can handle some types of entities
    private NBoard board;
    protected int x,y;//direction of movement is determined by sprite.angle
    protected int mx=0,my=0;//additional coords, for smooth movements
    protected int type;
    protected NSprite sprite=null;
    protected int Lives=3;
    protected long interval;//milliseconds per one move
    protected long lasttick=0;

    public NGameObject(NBoard board, int type, int x, int y) {
        //descendants must tune sprite, or owner must
        this.x = x;
        this.y = y;
        this.board = board;
        this.type = type;
        switch (type) {
            case NBoard.objectPlayer:                
                sprite = new NSprite(board,"kolobok",board.tileSize,board.tileSize);
                sprite.setAction(NSprite.actMove);
                sprite.setAngle(NSprite.angleLeft);
                board.layermanager.insert(sprite,0);
                break;
            case NBoard.objectLevelGate:
                sprite = new NSprite(board,"arrow_green",board.tileSize,board.tileSize);
                sprite.defineReferencePixel(0, board.tileSize/2);
                board.layermanager.insert(sprite,0);
                break;
            case NBoard.objectSpawn:
                interval=15000;
                sprite = new NSprite(board,"spawn",board.tileSize,board.tileSize);
                board.layermanager.insert(sprite,0);
                break;
            case NBoard.objectBomb:
                interval=3000;
                sprite = new NSprite(board,"bomb",board.tileSize,board.tileSize);
                sprite.setAction(NSprite.actMove);
                sprite.setAngle(NSprite.angleLeft);
                board.layermanager.insert(sprite,0);
                break;
        }
    }//NGameObject()

    public NGameObject(NBoard board, int type, int x, int y, int width, int height, String spritename) {
        this.x = x;
        this.y = y;
        this.board = board;
        this.type = type;
        interval=3000;
        sprite = new NSprite(board,spritename,width,height);
        sprite.setAction(NSprite.actMove);
        sprite.setAngle(NSprite.angleLeft);
        board.layermanager.insert(sprite,0);
    }//NGameObject()

    protected void deinit() {
        //deinitializes object
        board.layermanager.remove(sprite);
        type = 0;
        //sprite = null;
    }

    public boolean isPassable() {
        if (type==NBoard.objectGhost) {
            return true;
        }
        if (type==NBoard.objectMob && (sprite.action==NSprite.actCorp || sprite.action==NSprite.actDie)) {
            return true;
        }
        return false;
    }//isPassable()

    public boolean isActive() {//for mobs
        if (type!=NBoard.objectMob) return false;
        if (sprite.action==NSprite.actCorp || sprite.action==NSprite.actDie) {
            return false;
        }
        return true;
    }

    public boolean Die(NGameObject attacker) {//mobs & player - under attack
        switch (type) {
            case NBoard.objectPlayer:
                Lives--;
                board.Score-=3;
                if (Lives<=0) return true;
                break;
            case NBoard.objectMob:
                if (sprite.action == NSprite.actMove || sprite.action == NSprite.actAttack) {
                    sprite.setAction(NSprite.actDie);
                    board.Score++;
                }
                return true;
        }//switch
        return false;
    }//Attack()

    public boolean Attack(NGameObject obj) {//for mobs
        if (type==NBoard.objectMob) {
            if (sprite.action==NSprite.actCorp || sprite.action==NSprite.actDie) {
                return false;
            }
            int dx = obj.x-x;
            int dy = obj.y-y;
            if (dx<0) sprite.setAngle(NSprite.angleLeft);
            if (dx>0) sprite.setAngle(NSprite.angleRight);
            if (dy<0) sprite.setAngle(NSprite.angleUp);
            if (dy>0) sprite.setAngle(NSprite.angleDown);
            sprite.setAction(NSprite.actAttack);
            try {
                javax.microedition.media.Manager.playTone(80, 100, 50);
            } catch (MediaException ex) {
                ex.printStackTrace();
            }
            obj.Die(this);
            return true;
        } else if (type==NBoard.objectPlayer) {
            if (board.getObjectCount(NBoard.objectBomb)<2) {
                try {
                    javax.microedition.media.Manager.playTone(69, 100, 50);
                } catch (MediaException ex) {
                    ex.printStackTrace();
                }
                board.objects.addElement(new NGameObject(board, NBoard.objectBomb, x, y));
            }
            return true;
        }
        return false;
    }

    public void moveBy(int dx, int dy) {
        if (dx<0) sprite.setAngle(NSprite.angleLeft);
        if (dx>0) sprite.setAngle(NSprite.angleRight);
        if (dy<0) sprite.setAngle(NSprite.angleUp);
        if (dy>0) sprite.setAngle(NSprite.angleDown);
        x+=dx;
        y+=dy;        
        board.testCollide(this);
    }//moveBy()

    private void checkAngle() {
        //changes movement direction, if it's needed
        if (type!=NBoard.objectMob || sprite.action==NSprite.actDie || sprite.action==NSprite.actCorp) return;
        switch (sprite.angle) {
            case NSprite.angleLeft:
                if (!board.isWall(x-1, y));
                else {sprite.setAngle(board.random.nextInt(4));checkAngle();}                
                break;
            case NSprite.angleRight:
                if (!board.isWall(x+1, y));
                else {sprite.setAngle(board.random.nextInt(4));checkAngle();}                
                break;
            case NSprite.angleUp:
                if (!board.isWall(x, y-1));
                else {sprite.setAngle(board.random.nextInt(4));checkAngle();}                
                break;
            case NSprite.angleDown:
                if (!board.isWall(x, y+1));
                else {sprite.setAngle(board.random.nextInt(4));checkAngle();}                
        }//switch angle
            return;
    }//checkAngle()

    public void process(long tick) {
        //there's game logic
        if (lasttick==0) {lasttick=tick; return;}
        if (type==NBoard.objectMob) {
            if (sprite.action==NSprite.actDie) {
                if ((tick-lasttick)>=interval) {
                    sprite.setAction(NSprite.actCorp);
                }
                return;
            }
            if (sprite.action==NSprite.actCorp) {
                if ((tick-lasttick)>=interval*5) {                    
                    board.removeObject(this);
                }
                return;
            }
            checkAngle();            
            if ((tick-lasttick)<interval) {
                //smoothing
                switch (sprite.angle) {
                case NSprite.angleLeft:                    
                    mx = -(int)((board.tileSize*(tick-lasttick))/interval);
                    break;
                case NSprite.angleRight:
                    mx = +(int)((board.tileSize*(tick-lasttick))/interval);
                    break;
                case NSprite.angleUp:
                    my = -(int)((board.tileSize)*(tick-lasttick)/interval);
                    break;
                case NSprite.angleDown:
                    my = +(int)((board.tileSize)*(tick-lasttick)/interval);
                    break;
                }//switch angle
                return;
            }
            mx=0;
            my=0;
            lasttick = tick;
            switch (sprite.angle) {
                case NSprite.angleLeft:
                    moveBy(-1,0);                    
                    break;
                case NSprite.angleRight:
                    moveBy(+1,0);                    
                    break;
                case NSprite.angleUp:
                    moveBy(0,-1);                    
                    break;
                case NSprite.angleDown:
                    moveBy(0,+1);
                    break;
            }//switch angle
            NGameObject obj=board.testNear(this);
            if (obj==null) {
                sprite.setAction(NSprite.actMove);
            } else {
                Attack(obj);
            }//if obj
        }//if type==objectMob

        else if (type==NBoard.objectSpawn) {
            if ((tick-lasttick)<interval) return;
            lasttick = tick;
            if (board.getObjectCount(NBoard.objectMob)<(board.maze.mapWidth*board.maze.mapHeight)/16) {
                board.objects.addElement(new NGameObject(board, NBoard.objectMob, x, y, board.tileSize,board.tileSize,"mob1"));
            }
        }

        else if (type==NBoard.objectBomb) {
            if (sprite.action == NSprite.actAttack) {
                try {
                    javax.microedition.media.Manager.playTone(50, 100, 100);
                    javax.microedition.media.Manager.playTone(55, 100, 100);
                } catch (MediaException ex) {
                    ex.printStackTrace();
                }
                NGameObject test;
                for (int i=0;i<board.objects.size();i++) {
                    test = (NGameObject) board.objects.elementAt(i);
                    if (((x-test.x)==1 && y==test.y) ||
                        ((y-test.y)==1 && x==test.x) ||
                        ((x-test.x)==-1 && y==test.y) ||
                        ((y-test.y)==-1 && x==test.x) ||
                        (y==test.y && x==test.x)){
                            if (test.type == NBoard.objectMob || test.type == NBoard.objectPlayer) {
                                test.Die(this);}
                    }//if x...
                }//for i

                if ((tick-lasttick)>=interval/3){
                    lasttick = tick;
                    board.removeObject(this);
                }
            } else if (sprite.action == NSprite.actMove) {
                //explode
                if ((tick-lasttick)>=interval){
                    lasttick = tick;
                    sprite.setAction(NSprite.actAttack);
                }
            }
        }
    }//process()

    public void draw(Graphics g) {
        //there's
        if (sprite==null) return;
        sprite.nextFrame();
        sprite.setRefPixelPosition(board.mapOriginX+x*board.tileSize+mx, board.mapOriginY+y*board.tileSize+my);
        //sprites will be drawn through layermanager
    }//draw()
}
