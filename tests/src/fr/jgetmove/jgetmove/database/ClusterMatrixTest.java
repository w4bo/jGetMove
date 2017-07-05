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
        assertEquals(1, clusterMatrix.getClusterTimeId(0));
        assertEquals(1, clusterMatrix.getClusterTimeId(1));
        assertEquals(2, clusterMatrix.getClusterTimeId(2));
        assertEquals(2, clusterMatrix.getClusterTimeId(3));
        assertEquals(3, clusterMatrix.getClusterTimeId(4));
    }

    @Test
    void getClusterTransactionIds() {
        assertTrue(clusterMatrix.getClusterTransactionIds(0).contains(0));
        assertEquals(1, clusterMatrix.getClusterTransactionIds(0).size());

        assertTrue(clusterMatrix.getClusterTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getClusterTransactionIds(1).size());

        assertTrue(clusterMatrix.getClusterTransactionIds(0).contains(0));
        assertEquals(1, clusterMatrix.getClusterTransactionIds(2).size());

        assertTrue(clusterMatrix.getClusterTransactionIds(1).contains(1));

        assertEquals(1, clusterMatrix.getClusterTransactionIds(3).size());

        assertTrue(clusterMatrix.getClusterTransactionIds(0).contains(0));
        assertTrue(clusterMatrix.getClusterTransactionIds(1).contains(1));
        assertEquals(2, clusterMatrix.getClusterTransactionIds(4).size());
    }

    @Test
    void optimizeMatrix() {
        ClusterMatrix clusterMatrix = new ClusterMatrix(dataBase);

        TreeSet<Integer> set = new TreeSet<>();
        set.add(1);
        clusterMatrix.optimizeMatrix(dataBase, set);

        assertEquals(0, clusterMatrix.getClusterTransactionIds(0).size());

        assertTrue(clusterMatrix.getClusterTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getClusterTransactionIds(1).size());

        assertEquals(0, clusterMatrix.getClusterTransactionIds(2).size());

        assertTrue(clusterMatrix.getClusterTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getClusterTransactionIds(3).size());

        assertTrue(clusterMatrix.getClusterTransactionIds(1).contains(1));
        assertEquals(1, clusterMatrix.getClusterTransactionIds(4).size());

    }
}
