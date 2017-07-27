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

import fr.jgetmove.jgetmove.debug.Debug;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterTest {

    private Cluster cluster;

    @BeforeAll
    void init() {
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
