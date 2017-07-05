package fr.jgetmove.jgetmove.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterTest {

    private Cluster cluster;

    @BeforeEach
    void setUp() {
        cluster = new Cluster(1);
    }

    @Test
    void addAndGetTransactions() {
        Transaction transaction1 = new Transaction(1);
        Transaction transaction2 = new Transaction(2);
        cluster.add(transaction1);


        assertTrue(cluster.getTransactions().containsKey(transaction1.getId()));
        assertTrue(cluster.getTransactions().containsValue(transaction1));

        assertEquals(1, cluster.getTransactions().size());

        cluster.add(transaction2);
        assertTrue(cluster.getTransactions().containsKey(transaction2.getId()));
        assertTrue(cluster.getTransactions().containsValue(transaction2));

        assertEquals(2, cluster.getTransactions().size());
    }

    @Test
    void getId() {
        assertEquals(1, cluster.getId());
    }

    @Test
    void setAndGetTime() {
        Time time = new Time(1);

        cluster.setTime(time);
        assertEquals(time, cluster.getTime());
        assertEquals(cluster.getTime().getId(), cluster.getTimeId());
    }

    @Test
    void getTimeId() {
    }

}
