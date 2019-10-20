/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NLabyrinth;

import java.io.Reader;
/**
 *
 * @author palexandr
 */
public class LineReader {
    /**
     *
     */
    private Reader r;
    /**
     *
     * @param reader
     */
    public LineReader(Reader reader) {
        this.r = reader;
    }
    /**
     *
     * @return
     */
    public final String readLine() {
        String s = "";
        try {
            int c = r.read();
            if (c == -1) {
                return null;
            }
            while (c != -1 && c != '\n' && c != '\r') {
                s += (char) c;
                c = r.read();
            }            
        } catch (Exception e) {            
        }
        return s;
    }
}