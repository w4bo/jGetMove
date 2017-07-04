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
