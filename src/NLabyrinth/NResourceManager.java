/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NLabyrinth;
import java.util.Hashtable;
import javax.microedition.lcdui.Image;
import java.io.IOException;
import java.util.Enumeration;
/**
 *
 * @author Necro
 */
public class NResourceManager {
    private NBoard board;
    private Hashtable data = new Hashtable();
    private String respath = "240x320";

    public NResourceManager(NBoard board) {
        this.board = board;
    }//NResourceManager()

    public Image getImage(String iname) {
        iname="/" + respath + "/" + iname+".png";
        if (data.containsKey(iname)) {            
            try {
                return (Image)data.get(iname);
            }catch(Exception e) {
                board.addText(""+iname);
                board.addText(e.toString());
            }
        }        
        try {            
            data.put(iname, Image.createImage(iname));
            return (Image)data.get(iname);
        } catch (IOException e) {
            board.addText(e.toString());
            return null;
        }
    }//getImage()

    public Hashtable getConf(String sname) {
        sname="/" + respath + "/" + sname+".conf";
        if (data.containsKey(sname)) {            
            try {
                return (Hashtable)data.get(sname);
            }catch(Exception e) {
                board.addText(""+sname+": "+e.toString());
            }
        }        
        PropertyReader readerProp = new PropertyReader(board,sname);
        Hashtable hash = readerProp.readProperties();
        Enumeration keys = hash.keys();
        Object key=null;
        while (keys.hasMoreElements()) {
            key = keys.nextElement();            
        }
        data.put(sname,hash);        
        return (Hashtable)data.get(sname);
    }
}
