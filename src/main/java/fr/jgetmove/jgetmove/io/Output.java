/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Small simple {@link BufferedWriter} wrapper
 *
 * @author stardisblue
 * @version 1.0.0
 * @since 0.1.0
 */
public class Output {
    private String filePath;
    private BufferedWriter writer;

    /**
     * @param filePath path to the file to write into
     * @throws IOException if an I/O error occurs
     */
    public Output(String filePath) throws IOException {
        this.filePath = filePath;
        writer = new BufferedWriter(new FileWriter(filePath));
    }


    /**
     * writes a string
     *
     * @throws IOException if an I/O error occurs
     * @see BufferedWriter#write(String)
     */
    public void write(String line) throws IOException {
        writer.write(line);
        close();
    }

    /**
     * Wrapper for closing writer
     *
     * @throws IOException if an I/O error occurs
     * @see BufferedWriter#close()
     */
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public String toString() {
        return filePath;
    }
}
