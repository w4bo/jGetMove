package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Path;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathDetectorTest {
// TODO finish complexDataBase tests

    private static DataBase simpleDataBase;
    private static DataBase complexDataBase;
    private static DefaultConfig config;

    @BeforeAll
    static void init() throws IOException, MalformedTimeIndexException, ClusterNotExistException {
        simpleDataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));
        config = new DefaultConfig(1, 0, 1, 0);
        complexDataBase = new DataBase(new Input("tests/assets/complex.dat"), new Input("tests/assets/complex_time_index.dat"));
    }

    @Test
    void ppcTest() {

        try {
            DataBase dataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));

            TreeSet<Integer> itemset = new TreeSet<>();

            TreeSet<Integer> transactionIds = new TreeSet<>();
            transactionIds.add(0);
            transactionIds.add(1);

            // new ppcTest structure
            Set<Integer> filteredTransactionIds = dataBase.getFilteredTransactionIdsIfHaveCluster(transactionIds, 0);
            boolean generatorUtils = GeneratorUtils.ppcTest(dataBase, itemset, 0, filteredTransactionIds);

            // current ppcTest structure
            TreeSet<Integer> clusterGeneratorTransactionIds = new TreeSet<>();
            boolean clusterGenerator = PathDetector.PPCTest(dataBase, itemset, transactionIds, 0, clusterGeneratorTransactionIds);

            // new == old
            assertEquals(filteredTransactionIds, clusterGeneratorTransactionIds);
            assertEquals(generatorUtils, clusterGenerator);

            // old is correct
            assertEquals(1, clusterGeneratorTransactionIds.size());
            assertTrue(clusterGeneratorTransactionIds.contains(0));


            filteredTransactionIds = dataBase.getFilteredTransactionIdsIfHaveCluster(transactionIds, 4);
            generatorUtils = GeneratorUtils.ppcTest(dataBase, itemset, 4, filteredTransactionIds);

            // current ppcTest structure
            clusterGeneratorTransactionIds.clear();
            clusterGenerator = PathDetector.PPCTest(dataBase, itemset, transactionIds, 4, clusterGeneratorTransactionIds);

            // new == old
            assertEquals(filteredTransactionIds, clusterGeneratorTransactionIds);
            assertEquals(generatorUtils, clusterGenerator);

            // old is correct
            assertEquals(2, clusterGeneratorTransactionIds.size());
            assertTrue(clusterGeneratorTransactionIds.contains(0));
            assertTrue(clusterGeneratorTransactionIds.contains(1));

        } catch (IOException | ClusterNotExistException | MalformedTimeIndexException e) {
            e.printStackTrace();
        }
    }


    @Test
    void generateBasic() {
        PathDetector pathDetector = new PathDetector(config);

        TreeSet<Path> results = pathDetector.generate(simpleDataBase);

        assertEquals(3, results.size());

        ArrayList<ArrayList<Integer>> lvl2ClusterCheck = new ArrayList<>();

        ArrayList<Integer> path = new ArrayList<>();
        path.add(0);
        path.add(0);
        path.add(2);
        path.add(4);
        lvl2ClusterCheck.add(path);

        ArrayList<Integer> path2 = new ArrayList<>();
        path2.add(0);
        path2.add(1);
        path2.add(3);
        path2.add(4);
        lvl2ClusterCheck.add(path2);

        ArrayList<Integer> path3 = new ArrayList<>();
        path3.add(0);
        path3.add(4);
        lvl2ClusterCheck.add(path3);
        assertEquals(3, results.size());
        //assertArrayEquals(lvl2ClusterCheck.toArray(), results.toArray());


        ArrayList<ArrayList<Integer>> lvl2TimeCheck = new ArrayList<>();

        ArrayList<Integer> pathTimes = new ArrayList<>();
        pathTimes.add(0);
        pathTimes.add(1);
        pathTimes.add(2);
        pathTimes.add(3);
        lvl2TimeCheck.add(pathTimes);

        ArrayList<Integer> pathTimes2 = new ArrayList<>();
        pathTimes2.add(0);
        pathTimes2.add(1);
        pathTimes2.add(2);
        pathTimes2.add(3);
        lvl2TimeCheck.add(pathTimes2);

        ArrayList<Integer> pathTimes3 = new ArrayList<>();
        pathTimes3.add(0);
        pathTimes3.add(3);
        lvl2TimeCheck.add(pathTimes3);

        assertEquals(3, results.size());
        //assertArrayEquals(lvl2TimeCheck.toArray(), results.toArray());

    }

    @Test
    void generateComplex() {

        PathDetector pathDetector = new PathDetector(config);

        TreeSet<Path> results = pathDetector.generate(complexDataBase);
        assertEquals(19, results.size());
        // assertEquals("[[0, 0], [0, 0, 2, 19], [0, 0, 2, 5, 10, 12, 13, 19, 23], [0, 0, 2, 6, 9, 15, 19, 22], [0, 0, 3, 5, 7, 8, 14, 18, 20, 22], [0, 0, 5], [0, 0, 3, 5, 7, 8, 14, 18, 20, 22], [0, 0, 22], [0, 0, 3, 5, 7, 8, 14, 18, 20, 22], [0, 1], [0, 1, 3, 5, 11, 12, 16, 20, 22], [0, 1, 4, 7], [0, 1, 4, 7, 11, 16, 21, 23], [0, 1, 4, 7, 17], [0, 1, 11, 16], [0, 1, 3, 5, 11, 12, 16, 20, 22], [0, 1, 4, 7, 11, 16, 21, 23], [0, 3, 5, 20, 22], [0, 5], [0, 5, 12], [0, 7], [0, 22], [0, 23]]", results.toString());
        //assertEquals("[[0, 1], [0, 1, 2, 9], [0, 1, 2, 3, 5, 6, 7, 9, 10], [0, 1, 2, 4, 5, 7, 9, 10], [0, 1, 2, 3, 4, 5, 7, 8, 9, 10], [0, 1, 3], [0, 1, 2, 3, 4, 5, 7, 8, 9, 10], [0, 1, 10], [0, 1, 2, 3, 4, 5, 7, 8, 9, 10], [0, 1], [0, 1, 2, 3, 5, 6, 7, 9, 10], [0, 1, 2, 4], [0, 1, 2, 4, 5, 7, 9, 10], [0, 1, 2, 4, 7], [0, 1, 5, 7], [0, 1, 2, 3, 5, 6, 7, 9, 10], [0, 1, 2, 4, 5, 7, 9, 10], [0, 2, 3, 9, 10], [0, 3], [0, 3, 6], [0, 4], [0, 10], [0, 10]]", results.getLvl2TimeIds().toString());

    }

    @Test
    void run() {
        //TODO
    }

    @Test
    void addPathToBlockPath() {
        TreeSet<Integer> itemset = new TreeSet<>();
        itemset.add(0);
        itemset.add(2);
        itemset.add(4);

        TreeSet<Integer> transactionIds = new TreeSet<>();
        transactionIds.add(0);

        ClusterMatrix clusterMatrix = new ClusterMatrix(simpleDataBase);

        PathDetector pathDetector = new PathDetector(config);
        clusterMatrix.optimizeMatrix(simpleDataBase, transactionIds);

        pathDetector.addPathToPathBlock(clusterMatrix, itemset);

        assertEquals(1, pathDetector.lvl2ClusterIds.size());
        assertEquals(0, pathDetector.lvl2ClusterIds.get(0).get(0).intValue());
        assertEquals(0, pathDetector.lvl2ClusterIds.get(0).get(1).intValue());
        assertEquals(2, pathDetector.lvl2ClusterIds.get(0).get(2).intValue());
        assertEquals(4, pathDetector.lvl2ClusterIds.get(0).get(3).intValue());
        assertEquals(1, pathDetector.lvl2TimeIds.size());
        assertEquals(0, pathDetector.lvl2TimeIds.get(0).get(0).intValue());
        assertEquals(1, pathDetector.lvl2TimeIds.get(0).get(1).intValue());
        assertEquals(2, pathDetector.lvl2TimeIds.get(0).get(2).intValue());
        assertEquals(3, pathDetector.lvl2TimeIds.get(0).get(3).intValue());

        // ---
        itemset.clear();
        itemset.add(1);
        itemset.add(3);
        itemset.add(4);

        transactionIds.clear();
        transactionIds.add(1);

        clusterMatrix.optimizeMatrix(simpleDataBase, transactionIds);
        pathDetector.addPathToPathBlock(clusterMatrix, itemset);

        assertEquals(2, pathDetector.lvl2ClusterIds.size());
        assertEquals(0, pathDetector.lvl2ClusterIds.get(1).get(0).intValue());
        assertEquals(1, pathDetector.lvl2ClusterIds.get(1).get(1).intValue());
        assertEquals(3, pathDetector.lvl2ClusterIds.get(1).get(2).intValue());
        assertEquals(4, pathDetector.lvl2ClusterIds.get(1).get(3).intValue());
        assertEquals(2, pathDetector.lvl2TimeIds.size());
        assertEquals(0, pathDetector.lvl2TimeIds.get(1).get(0).intValue());
        assertEquals(1, pathDetector.lvl2TimeIds.get(1).get(1).intValue());
        assertEquals(2, pathDetector.lvl2TimeIds.get(1).get(2).intValue());
        assertEquals(3, pathDetector.lvl2TimeIds.get(1).get(3).intValue());

        // ---
        itemset.clear();
        itemset.add(4);

        transactionIds.clear();
        transactionIds.add(0);
        transactionIds.add(1);

        clusterMatrix.optimizeMatrix(simpleDataBase, transactionIds);
        pathDetector.addPathToPathBlock(clusterMatrix, itemset);

        assertEquals(3, pathDetector.lvl2ClusterIds.size());
        assertEquals(0, pathDetector.lvl2ClusterIds.get(2).get(0).intValue());
        assertEquals(4, pathDetector.lvl2ClusterIds.get(2).get(1).intValue());
        assertEquals(3, pathDetector.lvl2TimeIds.size());
        assertEquals(0, pathDetector.lvl2TimeIds.get(2).get(0).intValue());
        assertEquals(3, pathDetector.lvl2TimeIds.get(2).get(1).intValue());
    }

    @Test
    void addPathToTransaction() {
        //there is 3 iterations for this testcase
        // for the 3 of them only itemset and transaction changes

        int[] numCluster = new int[]{0};
        TreeSet<Integer> itemset = new TreeSet<>();
        itemset.add(0);
        itemset.add(2);
        itemset.add(4);

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(0));
        transactions.add(new Transaction(1));

        TreeSet<Integer> transactionIds = new TreeSet<>();
        transactionIds.add(0);

        ClusterMatrix clusterMatrix = new ClusterMatrix(simpleDataBase);

        clusterMatrix.optimizeMatrix(simpleDataBase, transactionIds);
        PathDetector.addPathToTransaction(clusterMatrix, itemset, transactions, numCluster);

        assertTrue(transactions.get(0).getClusterIds().contains(0));
        assertEquals(1, numCluster[0]);

        // ---
        itemset.clear();
        itemset.add(1);
        itemset.add(3);
        itemset.add(4);

        transactionIds.clear();
        transactionIds.add(1);

        clusterMatrix.optimizeMatrix(simpleDataBase, transactionIds);
        PathDetector.addPathToTransaction(clusterMatrix, itemset, transactions, numCluster);

        assertTrue(transactions.get(1).getClusterIds().contains(1));
        assertEquals(2, numCluster[0]);

        // ---
        itemset.clear();
        itemset.add(4);

        transactionIds.clear();
        transactionIds.add(0);
        transactionIds.add(1);

        clusterMatrix.optimizeMatrix(simpleDataBase, transactionIds);
        PathDetector.addPathToTransaction(clusterMatrix, itemset, transactions, numCluster);

        assertTrue(transactions.get(0).getClusterIds().contains(2));
        assertTrue(transactions.get(1).getClusterIds().contains(2));
        assertEquals(3, numCluster[0]);
    }

    @Test
    void generateClusters() {

        // Testing with singlepath itemset

        PathDetector pathDetector = new PathDetector(config);

        ArrayList<TreeSet<Integer>> generatedItemsets = pathDetector.generatePaths(simpleDataBase, new ArrayList<>());

        Debug.println("generatedItemset", generatedItemsets, Debug.DEBUG);

        assertEquals(1, generatedItemsets.size());
        assertEquals(0, generatedItemsets.get(0).size());

        ArrayList<Integer> itemset = new ArrayList<>();
        itemset.add(0);
        itemset.add(2);
        itemset.add(4);

        generatedItemsets = pathDetector.generatePaths(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(3, generatedItemsets.get(0).size());

        itemset.clear();
        itemset.add(1);
        itemset.add(3);
        itemset.add(4);

        generatedItemsets = pathDetector.generatePaths(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(3, generatedItemsets.get(0).size());

        itemset.clear();
        itemset.add(4);

        generatedItemsets = pathDetector.generatePaths(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(1, generatedItemsets.get(0).size());

    }
}
