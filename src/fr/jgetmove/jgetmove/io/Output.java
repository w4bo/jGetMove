package fr.jgetmove.jgetmove.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Créé le fichier .json à partir d'une chaîne de caractère générée dans Database
 */

public class Output {
    private String containJson;
    private BufferedWriter writer;

    public Output(String contain) throws IOException {
        containJson = contain;
        File json = new File("assets/test.json");
        json.createNewFile();
        writer = new BufferedWriter(new FileWriter(json));
        writer.write(containJson);
        writer.close();
    }
}
