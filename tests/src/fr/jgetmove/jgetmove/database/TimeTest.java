/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
