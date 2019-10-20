/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NLabyrinth;
import javax.microedition.lcdui.game.Sprite;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *
 * @author Necro
 */
public class NSprite extends Sprite{
    private NBoard board;
    //Sprite  sprite;
    String  name;    
    public int  angle;
    public int  action;
    //animated actions:
    public static final int actMove=0;
    public static final int actAttack=1;
    public static final int actDie=2;
    public static final int actCorp=3;
    //angles:
    public static final int angleLeft=0;
    public static final int angleRight=1;
    public static final int angleUp=2;
    public static final int angleDown=3;
    // action,angle,frame
    private int[][][] animation = null;
    private boolean isScripted = false;//has it animation .conf?

    public NSprite(NBoard board, String name,  int width, int height) {
        //loads name.png and name.conf
        super(board.resourcemanager.getImage(name), width, height);
        this.board = board;
        Hashtable hash = board.resourcemanager.getConf(name);
        //this is the way: new int[4][][];
        animation = new int[4][4][4];//all anims, all angles, 4 frames
        Enumeration keys = hash.keys();
        Object key=null;
        String act, angl, frames;
        int iact, iangl, iframe, ind, lastind;
        while (keys.hasMoreElements()) {
            isScripted = true;
            key = keys.nextElement();
            act = ((String)key).substring(0, ((String)key).indexOf("."));
            angl = ((String)key).substring(((String)key).indexOf(".")+1);
            if (act.equals("move")) iact = actMove;
            else if (act.equals("attack")) iact = actAttack;
            else if (act.equals("die")) iact = actDie;
            else if (act.equals("corp")) iact = actCorp;
            else {
                board.addText("unsup:"+act);
                continue;
            }

            if (angl.equals("left")) iangl = angleLeft;
            else if (angl.equals("right")) iangl = angleRight;
            else if (angl.equals("up")) iangl = angleUp;
            else if (angl.equals("down")) iangl = angleDown;
            else {
                board.addText("unsup:"+angl);
                continue;
            }

            frames = (String)hash.get(key);
            lastind = 0;
            iframe = 0;
            while ((ind=frames.indexOf(",", lastind)) > -1) {
                if (iact<0 || iangl<0 || iframe<0 || iact>3 || iangl>3 || iframe>3) {
                    board.addText("err:"+Integer.toString(iact)+","+Integer.toString(iangl)+","+Integer.toString(iframe));
                    continue;
                }
                try {
                    animation[iact][iangl][iframe] = Integer.parseInt(frames.substring(lastind,ind));
                } catch(Exception e) {
                }
//                board.addText(frames+"["+Integer.toString(iframe)+"]("+Integer.toString(lastind)+","+Integer.toString(ind)+")="+frames.substring(lastind,ind));
                iframe++;
                lastind=ind+1;
            }
            try {
                animation[iact][iangl][iframe] = Integer.parseInt(frames.substring(lastind));
           } catch(Exception e) {
           }
//           board.addText(frames+"["+Integer.toString(iframe)+"]("+Integer.toString(lastind)+","+Integer.toString(ind)+")="+frames.substring(lastind));
        }
    }

    public boolean setAction(int act) {
        if (act<actMove || act>actCorp) return false;
        this.action = act;
        if (isScripted) setFrameSequence(animation[action][angle]);
        return false;
    }//setAction()

    public boolean setAngle(int ang) {
        if (ang<angleLeft || ang>angleDown) return false;
        this.angle = ang;
        if (isScripted) setFrameSequence(animation[action][angle]);
        return false;
    }//setAction()
}
