/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NLabyrinth;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.*;

/**
 * @author Necro
 */
public class NLabyrinthMIDlet extends MIDlet implements CommandListener  {

    private NBoard board = null;
    private Thread thread = null;    

    private Command exitCmd = new Command ("Exit", Command.EXIT, 5);
    private Command cmdRestart = new Command ("Restart", Command.SCREEN, 1);
    private Command cmdNextTheme = new Command ("Next theme", Command.SCREEN, 2);
    private Command cmdPrevTheme = new Command ("Prev theme", Command.SCREEN, 3);
    private Command cmdDumpMaze = new Command ("Dump maze", Command.SCREEN, 4);


    public NLabyrinthMIDlet() {        
    }

    public void startApp() {
        board = new NBoard ();
        board.addCommand (exitCmd);
        board.addCommand (cmdRestart);
        board.addCommand (cmdNextTheme);
        board.addCommand (cmdPrevTheme);
        board.addCommand (cmdDumpMaze);
        board.setCommandListener (this);
        Display.getDisplay (this).setCurrent (board);
        try {
            // Start in its own thread
            thread = new Thread (board);
            thread.start ();
        } catch (Error e) {
            destroyApp (false);
            notifyDestroyed ();
        }
        
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        Display.getDisplay (this).setCurrent(null);
        notifyDestroyed();
    }
    
    public void startGame() {
        board.restart();
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCmd) {
            destroyApp(false);
            notifyDestroyed();
        } else if (c == cmdRestart) {
            startGame();
        } else if (c == cmdNextTheme) {
            board.reloadTileset(1);
        } else if (c == cmdPrevTheme) {
            board.reloadTileset(-1);
        } else if (c == cmdDumpMaze) {
            board.dump();
        }
    }
}