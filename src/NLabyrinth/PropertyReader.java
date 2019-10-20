/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package NLabyrinth;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
/**
 *
 * @author palexandr
 */
public class PropertyReader {
    NBoard board;
    /**
     * Имя файла
     */
    private String fileName;
    /**
     * Хранилище свойсв
     */
    private Hashtable properties;

    /**
     *
     * @param Название файла
     */
    public PropertyReader(NBoard board, String fileName) {
        this.board = board;
        this.fileName = "/" + fileName;
        properties = new Hashtable();
        //this.midletClass = midletClass;
    }
    /**
     *
     * @return Считанные свойства
     */
    public final Hashtable readProperties() {
        this.readUnicodeFile();
        return properties;
    }
    /**
     *
     */
    private final void readUnicodeFile() {
        InputStream is = null;
        InputStreamReader isr = null;
        try {

            is = getClass().getResourceAsStream(fileName);
            if (is == null) {
                throw new Exception("File '" + fileName + "' does not exist");
            }
            isr = new InputStreamReader(is);
            LineReader lineReader = new LineReader(isr);
            String line = lineReader.readLine();            
            while (line != null) {
                this.parseProperty(line);
                line = lineReader.readLine();
            }
        } catch (Exception ex) {
            //board.addText(ex.toString());
            System.out.println(ex);
        }
    }
    /**
     *
     * @param Линия текста
     */
    private final void parseProperty(final String line) {
        int lineLength = line.length();
        StringBuffer bfs = new StringBuffer();
        String token = "";
        final int SEPARATOR = 2;
        final int NORMAL = 3;
        int lastChar = NORMAL;
        String key = "";
        String value = "";
        for (int i = 0; i < lineLength; i++) {
            lastChar = NORMAL;
            char c = line.charAt(i);
            if (c == '#') {
                return;
            }

            if (c == '@') {
                lastChar = SEPARATOR;
            }
            if (c == '=') {
                token = bfs.toString();
                key = token;
                lastChar = SEPARATOR;
            }
            if (c == '$') {
                token = bfs.toString();
                value = token;
                key = key.toLowerCase();
                getProperties().put(key, value);
                lastChar = SEPARATOR;
            }
            if (lastChar != SEPARATOR) {
                bfs.append(c);
            } else {
                bfs = new StringBuffer();
            }
        }
    }
    /**
     *
     * @return
     */
    public Hashtable getProperties() {
        return properties;
    }
}