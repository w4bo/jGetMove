/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.io;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class InputTest {

    private static Input input;

    @BeforeAll
    static void setUp() {

        // TODO : check exception d'initialisation
        try {
            input = new Input("src/test/assets/simple.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThrows(IOException.class, () -> input = new Input("src/test/assets/not/authorized.txt"));
    }

    @Test
    void readLine() throws IOException {
        assertEquals("0 2 4", input.readLine());
    }

    @Test
    void testToString() {
        assertEquals("src/test/assets/simple.dat", input.toString());
    }

}
