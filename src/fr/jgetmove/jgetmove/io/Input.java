/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
