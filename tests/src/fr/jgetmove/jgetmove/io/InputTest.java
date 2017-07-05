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
            input = new Input("tests/assets/itemset_check.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThrows(IOException.class, () -> input = new Input("tests/assets/not/authorized.txt"));
    }

    @Test
    void readLine() throws IOException {
        assertEquals("0 2 4", input.readLine());
    }

    @Test
    void testToString() {
        assertEquals("tests/assets/itemset_check.dat", input.toString());
    }

}
