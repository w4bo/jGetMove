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
            output = new Output("tests/assets/output_test.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThrows(IOException.class, () -> output = new Output("tests/assets/not/authorized.txt"));
    }

    @Test
    void testToString() {
        assertEquals("tests/assets/output_test.txt", output.toString());
    }

}
