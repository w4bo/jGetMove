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
 * Charge le fichier passé en paramètres.
 * Peut être lu grâce à readLine
 */
public class Input {

    private String filePath;
    private BufferedReader reader;

    /**
     * @param filePath le chemin vers le fichier à lire
     */
    public Input(String filePath) throws FileNotFoundException {
        this.filePath = filePath;
        reader = new BufferedReader(new FileReader(filePath));

    }

    /**
     * @return un String contenant toute la ligne, excluant les caractères de fin de ligne
     * @throws IOException si il y a une erreur I/O
     * @see BufferedReader#readLine
     */
    public String readLine() throws IOException {
        return reader.readLine();
    }

    @Override
    public String toString() {
        return filePath;
    }
}
