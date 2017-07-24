/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.jgetmove.jgetmove.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeTest {
    private Time time;

    @BeforeEach
    void setUp() {
        time = new Time(1);
    }

    @Test
    void addAndGetClusters() {
        Cluster cluster1 = new Cluster(1);
        Cluster cluster2 = new Cluster(2);
        time.add(cluster1);


        assertTrue(time.getClusters().containsKey(cluster1.getId()));
        assertTrue(time.getClusters().containsValue(cluster1));
        assertTrue(time.getClusterIds().contains(cluster1.getId()));

        assertEquals(1, time.getClusters().size());
        assertEquals(1, time.getClusterIds().size());

        time.add(cluster2);
        assertTrue(time.getClusters().containsKey(cluster2.getId()));
        assertTrue(time.getClusters().containsValue(cluster2));
        assertTrue(time.getClusterIds().contains(cluster2.getId()));

        assertEquals(2, time.getClusters().size());
        assertEquals(2, time.getClusterIds().size());
    }

    @Test
    void getId() {
        assertEquals(1, time.getId());
    }


    @Test
    void setClusters() {
        Cluster cluster1 = new Cluster(1);
        Cluster cluster2 = new Cluster(2);
        time.add(cluster1);
        time.add(cluster2);
        HashMap<Integer, Cluster> clusters = new HashMap<>(time.getClusters());
        time.setClusters(clusters);


        assertTrue(time.getClusters().containsKey(cluster1.getId()));
        assertTrue(time.getClusters().containsValue(cluster1));
        assertTrue(time.getClusterIds().contains(cluster1.getId()));

        assertTrue(time.getClusters().containsKey(cluster2.getId()));
        assertTrue(time.getClusters().containsValue(cluster2));
        assertTrue(time.getClusterIds().contains(cluster2.getId()));

        assertEquals(2, time.getClusters().size());
        assertEquals(2, time.getClusterIds().size());

    }


    @Test
    void compareTo() {
        Time less = new Time(0);
        Time more = new Time(2);

        assertEquals(-1, time.compareTo(more));
        assertEquals(1, time.compareTo(less));
    }
}
