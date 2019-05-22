/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

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
