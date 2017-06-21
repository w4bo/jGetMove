package fr.jgetmove.jgetmove.detector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConvoyDetectorTest {


    @Test
    void getInstance() {
        assertEquals(ConvoyDetector.getInstance(1), ConvoyDetector.getInstance(1));
    }

    @Test
    void detect() {
        // TODO
    }
}
