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
    public Input(String filePath) {
        this.filePath = filePath;

        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
