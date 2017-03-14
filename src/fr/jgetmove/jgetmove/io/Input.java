package fr.jgetmove.jgetmove.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Input {

    private BufferedReader reader;

    /**
     * @param filePath
     */
    public Input(String filePath) {

        String filePath1 = filePath;

        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return
     * @throws IOException
     */
    public String readLine() throws IOException {
        return reader.readLine();
    }
}
