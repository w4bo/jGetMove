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


class OutputTest {
    private static Output output;

    @BeforeAll
    static void setUp() {

        // TODO : check exception d'initialisation
        try {
            output = new Output("src/test/assets/output_test.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThrows(IOException.class, () -> output = new Output("src/test/assets/not/authorized.txt"));
    }

    @Test
    void testToString() {
        assertEquals("src/test/assets/output_test.txt", output.toString());
    }

}
