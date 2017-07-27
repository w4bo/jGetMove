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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterMatrixTest {
    private static DataBase dataBase;
    private static ClusterMatrix clusterMatrix;

    @BeforeAll
    static void init() {
        Debug.disable();
        try {
            Input data = new Input("tests/assets/simple.dat");
            Input dataTime = new Input("tests/assets/simple_time_index.dat");
            dataBase = new DataBase(data, dataTime);

            clusterMatrix = new ClusterMatrix(dataBase);
        } catch (MalformedTimeIndexException | IOException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getClusterTimeId() {
        assertEquals(1, clusterMatrix.getTimeId(0));
        assertEquals(1, clusterMatrix.getTimeId(1));
        assertEquals(2, clusterMatrix.getTimeId(2));
        assertEquals(2, clusterMatrix.getTimeId(3));
        assertEquals(3, clusterMatrix.getTimeId(4));
    }

    @Test
    void getClusterTransactionIds() {
        assertTrue(clusterMatrix.getTransactionIds(0).contains(0));
        assertEquals(1, clusterMatrix.getTransactionIds(0).size());

        assertTrue(clusterMatrix.getTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getTransactionIds(1).size());

        assertTrue(clusterMatrix.getTransactionIds(0).contains(0));
        assertEquals(1, clusterMatrix.getTransactionIds(2).size());

        assertTrue(clusterMatrix.getTransactionIds(1).contains(1));

        assertEquals(1, clusterMatrix.getTransactionIds(3).size());

        assertTrue(clusterMatrix.getTransactionIds(0).contains(0));
        assertTrue(clusterMatrix.getTransactionIds(1).contains(1));
        assertEquals(2, clusterMatrix.getTransactionIds(4).size());
    }

    @Test
    void optimizeMatrix() {
        ClusterMatrix clusterMatrix = new ClusterMatrix(dataBase);

        TreeSet<Integer> set = new TreeSet<>();
        set.add(1);
        clusterMatrix.optimizeMatrix(dataBase, set);

        assertTrue(clusterMatrix.getTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getTransactionIds(1).size());

    }
}
