package fr.jgetmove.jgetmove.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Créé le fichier .json à partir d'une chaîne de caractère générée dans Database
 */

public class Output {
    private String filePath;
    private BufferedWriter writer;

    /**
     * @param filePath le chemin vers le fichier à ecrire
     * @throws IOException
     */
    public Output(String filePath) throws IOException {
        this.filePath = filePath;
        writer = new BufferedWriter(new FileWriter(filePath));
    }


    /**
     * @throws IOException si il y a une erreur I/O
     * @see BufferedReader#writeline
     */
    public void write(String line) throws IOException {
        writer.write(line);
        close();
    }

    public void close() throws IOException {
        writer.close();
    }

    @Override
    public String toString() {
        return filePath;
    }
}
