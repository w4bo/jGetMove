/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
