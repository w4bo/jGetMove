/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterTest {

    private Cluster cluster;

    @BeforeAll
    static void init() {
        Debug.disable();
    }

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
