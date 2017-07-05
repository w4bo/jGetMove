package fr.jgetmove.jgetmove.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TransactionTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction(1);
    }


    @Test
    void addAndGetClusters() {
        Cluster cluster1 = new Cluster(1);
        Cluster cluster2 = new Cluster(2);
        transaction.add(cluster1);


        assertTrue(transaction.getClusters().containsKey(cluster1.getId()));
        assertTrue(transaction.getClusters().containsValue(cluster1));
        assertTrue(transaction.getClusterIds().contains(cluster1.getId()));

        assertEquals(1, transaction.getClusters().size());
        assertEquals(1, transaction.getClusterIds().size());

        transaction.add(cluster2);
        assertTrue(transaction.getClusters().containsKey(cluster2.getId()));
        assertTrue(transaction.getClusters().containsValue(cluster2));
        assertTrue(transaction.getClusterIds().contains(cluster2.getId()));

        assertEquals(2, transaction.getClusters().size());
        assertEquals(2, transaction.getClusterIds().size());
    }

    @Test
    void getId() {
        assertEquals(1, transaction.getId());
    }

    @Test
    void compareTo() {
        Transaction less = new Transaction(0);
        Transaction more = new Transaction(2);

        assertEquals(-1, transaction.compareTo(more));
        assertEquals(1, transaction.compareTo(less));

    }
}
