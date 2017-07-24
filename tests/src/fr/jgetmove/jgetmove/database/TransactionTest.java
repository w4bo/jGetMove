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
