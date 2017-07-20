package fr.jgetmove.jgetmove.database;

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
        try {
            Input data = new Input("tests/assets/itemset_check.dat");
            Input dataTime = new Input("tests/assets/itemset_check_time_index.dat");
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

        assertEquals(0, clusterMatrix.getTransactionIds(0).size());

        assertTrue(clusterMatrix.getTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getTransactionIds(1).size());

        assertEquals(0, clusterMatrix.getTransactionIds(2).size());

        assertTrue(clusterMatrix.getTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getTransactionIds(3).size());

        assertTrue(clusterMatrix.getTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getTransactionIds(4).size());

    }
}
