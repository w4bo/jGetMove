package fr.jgetmove.jgetmove.utils;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
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

        assertEquals(4, GeneratorUtils.getDifferentFromLastItem(frequent, clusterIds));
        assertEquals(1, GeneratorUtils.getDifferentFromLastItem(sameFrequent, clusterIds));
        //assertEquals(0, GeneratorUtils.getDifferentFromLastItem(sameFrequent, 0));
    }

    @Test
    void makeClosure() {
        try {
            DataBase dataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));

            TreeSet<Integer> transactionIds = new TreeSet<>();
            transactionIds.add(0);

            ArrayList<Integer> qSets;
            TreeSet<Integer> itemset = new TreeSet<>();

            qSets = GeneratorUtils.makeClosure(dataBase, transactionIds, itemset, 0);

            ArrayList<Integer> qSetsCheck = new ArrayList<>();
            qSetsCheck.add(0);
            qSetsCheck.add(2);
            qSetsCheck.add(4);

            assertEquals(qSetsCheck, qSets);


            transactionIds.clear();
            transactionIds.add(1);

            qSets.clear();
            itemset.clear();

            qSets = GeneratorUtils.makeClosure(dataBase, transactionIds, itemset, 1);

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

            qSets = GeneratorUtils.makeClosure(dataBase, transactionIds, itemset, 4);

            qSetsCheck.clear();
            qSetsCheck.add(4);
            assertEquals(qSetsCheck, qSets);

        } catch (IOException | MalformedTimeIndexException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }

    @Test
    void updateTransactions() {

        try {
            DataBase dataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));
            Set<Integer> transactionIds = new TreeSet<>();
            transactionIds.add(0);
            transactionIds.add(1);

            ArrayList<Integer> qSets = new ArrayList<>();
            qSets.add(0);
            qSets.add(2);
            qSets.add(4);

            Set<Integer> updated = GeneratorUtils.updateTransactions(dataBase, transactionIds, qSets, 0);

            assertEquals(1, updated.size());
            assertTrue(updated.contains(0));


            qSets.clear();
            qSets.add(1);
            qSets.add(3);
            qSets.add(4);

            updated = GeneratorUtils.updateTransactions(dataBase, transactionIds, qSets, 1);

            assertEquals(1, updated.size());
            assertTrue(updated.contains(1));


            qSets.clear();
            qSets.add(4);

            updated = GeneratorUtils.updateTransactions(dataBase, transactionIds, qSets, 1);

            assertEquals(2, updated.size());
            assertTrue(updated.contains(0));
            assertTrue(updated.contains(1));

        } catch (IOException | ClusterNotExistException | MalformedTimeIndexException e) {
            e.printStackTrace();
        }

    }

    @Test
    void updateFreqList() {
        DataBase dataBase = null;
        try {
            dataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));
        } catch (IOException | ClusterNotExistException | MalformedTimeIndexException e) {
            e.printStackTrace();
        }
        Set<Integer> transactionIds = new TreeSet<>();
        transactionIds.add(0);
        transactionIds.add(1);

        ArrayList<Integer> qSets = new ArrayList<>();
        qSets.add(0);
        qSets.add(2);
        qSets.add(4);

        ArrayList<Integer> frequentClusters = new ArrayList<>();

        ArrayList<Integer> updated = GeneratorUtils.updateClustersFrequenceCount(dataBase, transactionIds, qSets, frequentClusters, 0);

        assertEquals(3, updated.size());
        assertEquals(1, (int) updated.get(0));
        assertEquals(1, (int) updated.get(1));
        assertEquals(1, (int) updated.get(2));


        qSets.clear();
        qSets.add(1);
        qSets.add(3);
        qSets.add(4);

        updated = GeneratorUtils.updateClustersFrequenceCount(dataBase, transactionIds, qSets, frequentClusters, 1);

        assertEquals(3, updated.size());
        assertEquals(1, (int) updated.get(0));
        assertEquals(1, (int) updated.get(1));
        assertEquals(1, (int) updated.get(2));

        qSets.clear();
        qSets.add(4);
        updated = GeneratorUtils.updateClustersFrequenceCount(dataBase, transactionIds, qSets, frequentClusters, 4);

        assertEquals(1, updated.size());
        assertEquals(2, (int) updated.get(0));

    }

    /**
     * @see DataBase#areTransactionsInCluster(Set, int)
     */
    @Test
    @Disabled
    void checkItemInclusion() {
        // already done in database
    }

    @Test
    void ppcTest() {
        try {
            DataBase dataBase = new DataBase(new Input("tests/assets/itemset_check.dat"),
                    new Input("tests/assets/itemset_check_time_index.dat"));

            TreeSet<Integer> itemset = new TreeSet<>();
            itemset.add(0);
            itemset.add(1);

            TreeSet<Integer> transactionIds = new TreeSet<>();
            transactionIds.add(0);
            transactionIds.add(1);

            assertTrue(GeneratorUtils.ppcTest(dataBase, itemset, transactionIds, 2));
            assertFalse(GeneratorUtils.ppcTest(dataBase, itemset, transactionIds, 5));


            dataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));

            itemset = new TreeSet<>();

            transactionIds = new TreeSet<>();
            transactionIds.add(0);
            transactionIds.add(1);

            // new ppcTest structure
            Set<Integer> filteredTransactionIds = dataBase.getFilteredTransactionIdsIfHaveCluster(transactionIds, 0);
            boolean generatorUtils = GeneratorUtils.ppcTest(dataBase, itemset, filteredTransactionIds, 0);

            assertEquals(generatorUtils, true);
        } catch (IOException | MalformedTimeIndexException | ClusterNotExistException e) {
            e.printStackTrace();
        }

    }

}
