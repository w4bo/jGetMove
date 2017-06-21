package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.pattern.Pattern;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by jeffou on 21/06/17.
 */
public class ConvoyDetectorTest {

    private static ConvoyDetector convoyDetector;

    @BeforeEach
    void setUp(){
        convoyDetector = new ConvoyDetector(1);
    }

    @Test
    void getInstance(){
        ConvoyDetector test = null;
        assertEquals(test,test.getInstance(1));
        assertEquals(convoyDetector,test.getInstance(1));
        assertEquals(convoyDetector,convoyDetector.getInstance(1));
    }

    @Test
    void detect(){
        assertEquals(null,convoyDetector.detect(null,null,null,null));
    }
}
