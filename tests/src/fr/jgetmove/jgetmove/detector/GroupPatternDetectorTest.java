package fr.jgetmove.jgetmove.detector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by jeffou on 21/06/17.
 */
public class GroupPatternDetectorTest {

    private static GroupPatternDetector groupPatternDetector;

    @BeforeEach
    void setUp(){
        groupPatternDetector = new GroupPatternDetector(1,0);
    }

    /*@Test
    void getInstance(){
        ConvoyDetector test = null;
        assertEquals(test,test.getInstance(1));
        assertEquals(closedSwarmDetector,test.getInstance(1));
        assertEquals(closedSwarmDetector,closedSwarmDetector.getInstance(1));
    }*/

    @Test
    void detect(){
        assertEquals(null,groupPatternDetector.detect(null,null,null,null));
    }
}
