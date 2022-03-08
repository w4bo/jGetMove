/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Small simple {@link BufferedReader} wrapper
 *
 * @author stardisblue
 * @version 1.0.0
 * @since 0.1.0
 */
public class Input {

    private String filePath;
    private BufferedReader reader;

    /**
     * Initializes the Class and opens a stream to the file.
     *
     * @param filePath path to file
     * @throws FileNotFoundException when the file path is incorrect
     */
    public Input(String filePath) throws FileNotFoundException {
        this.filePath = filePath;
        reader = new BufferedReader(new FileReader(filePath));

    }

    /**
     * @return a string containing all the text of a line, excluding endline characters
     * @throws IOException if an I/O error occurs
     * @see BufferedReader#readLine
     */
    public String readLine() throws IOException {
        return reader.readLine();
    }

    /**
     * Wrapper for closing the reader
     *
     * @throws IOException if an I/O error occurs
     * @see BufferedReader#close()
     */
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public String toString() {
        return filePath;
    }
}
