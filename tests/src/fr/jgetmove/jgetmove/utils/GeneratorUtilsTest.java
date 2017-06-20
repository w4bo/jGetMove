package fr.jgetmove.jgetmove.utils;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorUtilsTest {
    @Test
    void getDifferentFromLastCluster() {
        ArrayList<Integer> clusterIds = new ArrayList<>();
        clusterIds.add(1);
        clusterIds.add(2);
        clusterIds.add(3);

        ArrayList<Integer> frequent = new ArrayList<>();
        frequent.add(0);
        frequent.add(1);
        frequent.add(2);
        frequent.add(3);
        frequent.add(4);
        frequent.add(5);
        frequent.add(5);

        ArrayList<Integer> sameFrequent = new ArrayList<>();
        sameFrequent.add(1);
        sameFrequent.add(1);
        sameFrequent.add(1);

        assertEquals(4, GeneratorUtils.getDifferentFromLastCluster(clusterIds, frequent));
        assertEquals(1, GeneratorUtils.getDifferentFromLastCluster(clusterIds, sameFrequent));
        assertEquals(0, GeneratorUtils.getDifferentFromLastCluster(new ArrayList<>(), sameFrequent));
    }

    @Test
    void makeClosure() {
        try {
            Database database = new Database(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"), 0);

            TreeSet<Integer> transactionIds = new TreeSet<>();
            transactionIds.add(0);

            ArrayList<Integer> qSets = new ArrayList<>();
            ArrayList<Integer> itemset = new ArrayList<>();

            GeneratorUtils.makeClosure(database, transactionIds, qSets, itemset, 0);

            ArrayList<Integer> qSetsCheck = new ArrayList<>();
            qSetsCheck.add(0);
            qSetsCheck.add(2);
            qSetsCheck.add(4);

            assertEquals(qSetsCheck, qSets);


            transactionIds.clear();
            transactionIds.add(1);

            qSets.clear();
            itemset.clear();

            GeneratorUtils.makeClosure(database, transactionIds, qSets, itemset, 1);

            qSetsCheck.clear();
            qSetsCheck.add(1);
            qSetsCheck.add(3);
            qSetsCheck.add(4);

            assertEquals(qSetsCheck, qSets);


            transactionIds.clear();
            transactionIds.add(0);
            transactionIds.add(1);

            qSets.clear();
            itemset.clear();

            GeneratorUtils.makeClosure(database, transactionIds, qSets, itemset, 4);

            qSetsCheck.clear();
            qSetsCheck.add(4);
            assertEquals(qSetsCheck, qSets);

        } catch (IOException | MalformedTimeIndexException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }

    @Test
    void updateTransactions() {
        //TODO
    }

    @Test
    void updateFreqList() {
        //TODO
    }

    @Test
    @Disabled
    void lower_bound() {

        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.add(1);
        treeSet.add(2);
        treeSet.add(3);
        treeSet.add(4);
        treeSet.add(5);
        treeSet.add(6);

        assertEquals(3, GeneratorUtils.lower_bound(treeSet, 4).size());
    }


    @Test
    @Disabled
    /**
     *  @see Database#isClusterInTransactions(Set, int)
     */
    void checkItemInclusion() {
        // already done in database
    }

    @Test
    void ppcTest() {
        try {
            Database database = new Database(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"), 0);

            ArrayList<Integer> itemset = new ArrayList<>();
            itemset.add(0);
            itemset.add(1);

            TreeSet<Integer> transactionIds = new TreeSet<>();
            transactionIds.add(0);
            transactionIds.add(1);

            assertTrue(GeneratorUtils.ppcTest(database, itemset, 2, transactionIds));
            assertFalse(GeneratorUtils.ppcTest(database, itemset, 5, transactionIds));
        } catch (IOException | MalformedTimeIndexException | ClusterNotExistException e) {
            e.printStackTrace();
        }

    }

}
