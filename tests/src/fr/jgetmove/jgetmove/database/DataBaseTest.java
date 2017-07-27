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
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DataBaseTest {

    private DataBase dataBase;

    @BeforeAll
    void init() {
        Debug.disable();
    }

    @BeforeEach
    void setUp() {

        // TODO : check exception d'initialisation
        try {
            dataBase = new DataBase(new Input("tests/assets/simple.dat"), new Input("tests/assets/simple_time_index.dat"));
        } catch (IOException | MalformedTimeIndexException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getAndAddClusters() {

        assertEquals(5, dataBase.getClusters().size());
        assertEquals(5, dataBase.getClusterIds().size());
        assertEquals(1, dataBase.getCluster(1).getTimeId());
        assertNull(dataBase.getCluster(5));

        Cluster cluster1 = new Cluster(5);
        dataBase.add(cluster1);

        assertEquals(cluster1, dataBase.getCluster(5));
        assertEquals(6, dataBase.getClusters().size());
        assertEquals(6, dataBase.getClusterIds().size());
    }

    @Test
    void getAndAddTimes() {

        assertEquals(3, dataBase.getTimes().size());
        assertEquals(3, dataBase.getTimeIds().size());
        assertEquals(2, dataBase.getTime(1).getClusters().size());

        assertNull(dataBase.getTime(4));

        Time time1 = new Time(4);
        dataBase.add(time1);

        assertEquals(time1, dataBase.getTime(4));
        assertEquals(4, dataBase.getTimes().size());
        assertEquals(4, dataBase.getTimeIds().size());
        assertEquals(1, dataBase.getTime(3).getClusters().size());
    }

    @Test
    void getAndAddTransactions() {

        assertEquals(2, dataBase.getTransactions().size());
        assertEquals(2, dataBase.getTransactionIds().size());
        assertEquals(3, dataBase.getTransaction(1).getClusters().size());

        Transaction transaction1 = new Transaction(2);
        dataBase.add(transaction1);

        assertEquals(3, dataBase.getTransactions().size());
        assertEquals(3, dataBase.getTransactionIds().size());
    }

    @Test
    void getClusterTransactions() {
        assertEquals(2, dataBase.getClusterTransactions(4).size());
    }

    @Test
    void toJson() {
        assertEquals("{\"links\":[{\"id\":0,\"source\":0,\"target\":2,\"value\":1,\"label\":0},{\"id\":0,\"source\":2,\"target\":4,\"value\":1,\"label\":0},{\"id\":1,\"source\":1,\"target\":3,\"value\":1,\"label\":1},{\"id\":1,\"source\":3,\"target\":4,\"value\":1,\"label\":1}],\"nodes\":[{\"id\":0,\"label\":\"0\",\"time\":1},{\"id\":1,\"label\":\"1\",\"time\":1},{\"id\":2,\"label\":\"0\",\"time\":2},{\"id\":3,\"label\":\"1\",\"time\":2},{\"id\":4,\"label\":\"0,1\",\"time\":3}]}", dataBase.toJson().build().toString());

    }

    @Test
    void isClusterInTransactions() {
        assertTrue(dataBase.areTransactionsInCluster(dataBase.getTransactionIds(), 4));
        assertFalse(dataBase.areTransactionsInCluster(dataBase.getTransactionIds(), 2));
    }

    @Test
    void getFilteredTransactionIdsIfHaveCluster() {
        assertEquals(dataBase.getTransactionIds(), dataBase.getFilteredTransactionIdsIfHaveCluster(dataBase.getTransactionIds(), 4));
        // assertEquals(0, dataBase.getFilteredTransactionIdsIfHaveCluster(dataBase.getTransactionIds(), 6).size());
    }

}
