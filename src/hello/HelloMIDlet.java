package hello;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class HelloMIDlet extends MIDlet implements CommandListener {

    private Command exitCmd = new Command ("Exit", Command.EXIT, 5);
    private Command cmdRestart = new Command ("Restart", Command.SCREEN, 1);
    private Command cmd1Cmd = new Command ("Speed+", Command.SCREEN, 2);
    private Command cmd2Cmd = new Command ("Speed-", Command.SCREEN, 3);
    //private Display display;     // The display for this MIDlet
    private NBoard board = null;
    private Thread myThread = null;

    public HelloMIDlet() {
        
    }

    public void startApp() {
        startGame();
    }

    public void startGame() {
        if (board == null)
            board = new NBoard ();
        else
            board.restart();
        board.addCommand (exitCmd);
        board.addCommand (cmdRestart);
        board.addCommand (cmd1Cmd);
        board.addCommand (cmd2Cmd);
        board.setCommandListener (this);

        Display.getDisplay (this).setCurrent (board);
        try {
            // Start in its own thread
            if (myThread == null)
                myThread = new Thread (board);
            //ensure the thread will work after pause
            //canvas.setDestroyed (false);
            myThread.start ();            
        } catch (Error e) {
            destroyApp (false);
            notifyDestroyed ();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCmd) {
            destroyApp(false);
            notifyDestroyed();
        } else if (c == cmd1Cmd) {
            board.incSpeed();
        } else if (c == cmd2Cmd) {
            board.decSpeed();
        } else if (c == cmdRestart) {
            startGame();
        }

    }

}
