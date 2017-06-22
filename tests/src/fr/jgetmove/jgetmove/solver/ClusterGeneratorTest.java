package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterGeneratorTest {
// TODO finish complexDatabase tests

    private static Database simpleDatabase;
    private static Database complexDatabase;
    private static DefaultConfig config;

    @BeforeAll
    static void init() throws IOException, MalformedTimeIndexException, ClusterNotExistException {
        simpleDatabase = new Database(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"), 0);
        config = new DefaultConfig(1, 0, 1, 0);
        complexDatabase = new Database(new Input("tests/assets/complex.dat"), new Input("tests/assets/complex_time_index.dat"), 0);
    }

    @Test
    void ppcTest() {

        try {
            Database database = new Database(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"), 0);

            ArrayList<Integer> itemset = new ArrayList<>();

            TreeSet<Integer> transactionIds = new TreeSet<>();
            transactionIds.add(0);
            transactionIds.add(1);

            // new ppcTest structure
            Set<Integer> filteredTransactionIds = database.getFilteredTransactionIdsIfHaveCluster(transactionIds, 0);
            boolean generatorUtils = GeneratorUtils.ppcTest(database, itemset, 0, filteredTransactionIds);

            // current ppcTest structure
            TreeSet<Integer> clusterGeneratorTransactionIds = new TreeSet<>();
            boolean clusterGenerator = ClusterGenerator.PPCTest(database, itemset, transactionIds, 0, clusterGeneratorTransactionIds);

            // new == old
            assertEquals(filteredTransactionIds, clusterGeneratorTransactionIds);
            assertEquals(generatorUtils, clusterGenerator);

            // old is correct
            assertEquals(1, clusterGeneratorTransactionIds.size());
            assertTrue(clusterGeneratorTransactionIds.contains(0));


            filteredTransactionIds = database.getFilteredTransactionIdsIfHaveCluster(transactionIds, 4);
            generatorUtils = GeneratorUtils.ppcTest(database, itemset, 4, filteredTransactionIds);

            // current ppcTest structure
            clusterGeneratorTransactionIds.clear();
            clusterGenerator = ClusterGenerator.PPCTest(database, itemset, transactionIds, 4, clusterGeneratorTransactionIds);

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
        ClusterGenerator clusterGenerator = new ClusterGenerator(simpleDatabase, config);

        ClusterGeneratorResult results = clusterGenerator.generate();

        assertEquals(3, results.getLvl2ClusterIds().size());
        assertEquals(3, results.getLvl2TimeIds().size());
    }

    @Test
    void generateComplex() {

        ClusterGenerator clusterGenerator = new ClusterGenerator(complexDatabase, config);

        ClusterGeneratorResult results = clusterGenerator.generate();

        assertEquals(23, results.getLvl2ClusterIds().size());
        assertEquals(23, results.getLvl2TimeIds().size());
    }

    @Test
    void getClustersGenerated() {
        //TODO
    }

    @Test
    void run() {
        //TODO
    }

    @Test
    void printItemsetsNew() {
        //there is 3 iterations for this testcase
        // for the 3 of them only itemset and transaction changes

        int[] numCluster = new int[]{0};
        ArrayList<Integer> itemset = new ArrayList<>();
        itemset.add(0);
        itemset.add(2);
        itemset.add(4);

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(0));
        transactions.add(new Transaction(1));

        TreeSet<Integer> transactionIds = new TreeSet<>();
        transactionIds.add(0);

        ClusterMatrix clusterMatrix = new ClusterMatrix(simpleDatabase);

        ClusterGenerator clusterGenerator = new ClusterGenerator(simpleDatabase, config);
        clusterMatrix.optimizeMatrix(simpleDatabase, transactionIds);

        clusterGenerator.printItemsetsNew(clusterMatrix, itemset, transactions, numCluster);

        assertTrue(transactions.get(0).getClusterIds().contains(0));
        assertEquals(1, numCluster[0]);

        assertEquals(1, clusterGenerator.lvl2ClusterIds.size());
        assertEquals(0, clusterGenerator.lvl2ClusterIds.get(0).get(0).intValue());
        assertEquals(0, clusterGenerator.lvl2ClusterIds.get(0).get(1).intValue());
        assertEquals(2, clusterGenerator.lvl2ClusterIds.get(0).get(2).intValue());
        assertEquals(4, clusterGenerator.lvl2ClusterIds.get(0).get(3).intValue());
        assertEquals(1, clusterGenerator.lvl2TimeIds.size());
        assertEquals(0, clusterGenerator.lvl2TimeIds.get(0).get(0).intValue());
        assertEquals(1, clusterGenerator.lvl2TimeIds.get(0).get(1).intValue());
        assertEquals(2, clusterGenerator.lvl2TimeIds.get(0).get(2).intValue());
        assertEquals(3, clusterGenerator.lvl2TimeIds.get(0).get(3).intValue());

        itemset.clear();
        itemset.add(1);
        itemset.add(3);
        itemset.add(4);

        transactionIds.clear();
        transactionIds.add(1);

        clusterMatrix.optimizeMatrix(simpleDatabase, transactionIds);
        clusterGenerator.printItemsetsNew(clusterMatrix, itemset, transactions, numCluster);

        assertTrue(transactions.get(1).getClusterIds().contains(1));
        assertEquals(2, numCluster[0]);

        assertEquals(2, clusterGenerator.lvl2ClusterIds.size());
        assertEquals(0, clusterGenerator.lvl2ClusterIds.get(1).get(0).intValue());
        assertEquals(1, clusterGenerator.lvl2ClusterIds.get(1).get(1).intValue());
        assertEquals(3, clusterGenerator.lvl2ClusterIds.get(1).get(2).intValue());
        assertEquals(4, clusterGenerator.lvl2ClusterIds.get(1).get(3).intValue());
        assertEquals(2, clusterGenerator.lvl2TimeIds.size());
        assertEquals(0, clusterGenerator.lvl2TimeIds.get(1).get(0).intValue());
        assertEquals(1, clusterGenerator.lvl2TimeIds.get(1).get(1).intValue());
        assertEquals(2, clusterGenerator.lvl2TimeIds.get(1).get(2).intValue());
        assertEquals(3, clusterGenerator.lvl2TimeIds.get(1).get(3).intValue());


        itemset.clear();
        itemset.add(4);

        transactionIds.clear();
        transactionIds.add(0);
        transactionIds.add(1);

        clusterMatrix.optimizeMatrix(simpleDatabase, transactionIds);
        clusterGenerator.printItemsetsNew(clusterMatrix, itemset, transactions, numCluster);


        assertTrue(transactions.get(0).getClusterIds().contains(2));
        assertTrue(transactions.get(1).getClusterIds().contains(2));
        assertEquals(3, numCluster[0]);

        assertEquals(3, clusterGenerator.lvl2ClusterIds.size());
        assertEquals(0, clusterGenerator.lvl2ClusterIds.get(2).get(0).intValue());
        assertEquals(4, clusterGenerator.lvl2ClusterIds.get(2).get(1).intValue());
        assertEquals(3, clusterGenerator.lvl2TimeIds.size());
        assertEquals(0, clusterGenerator.lvl2TimeIds.get(2).get(0).intValue());
        assertEquals(3, clusterGenerator.lvl2TimeIds.get(2).get(1).intValue());
    }

    @Test
    void generateClusters() {

        // Testing with singlepath itemset

        ClusterGenerator clusterGenerator = new ClusterGenerator(simpleDatabase, config);

        ArrayList<ArrayList<Integer>> generatedItemsets = clusterGenerator.generateItemsets(new ArrayList<>());

        Debug.println("generatedItemset", generatedItemsets, Debug.DEBUG);

        assertEquals(1, generatedItemsets.size());
        assertEquals(0, generatedItemsets.get(0).size());

        ArrayList<Integer> itemset = new ArrayList<>();
        itemset.add(0);
        itemset.add(2);
        itemset.add(4);

        generatedItemsets = clusterGenerator.generateItemsets(itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(3, generatedItemsets.get(0).size());

        itemset.clear();
        itemset.add(1);
        itemset.add(3);
        itemset.add(4);

        generatedItemsets = clusterGenerator.generateItemsets(itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(3, generatedItemsets.get(0).size());

        itemset.clear();
        itemset.add(4);

        generatedItemsets = clusterGenerator.generateItemsets(itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(1, generatedItemsets.get(0).size());

    }


    @Test
    @Disabled
    void calcurateCoreI() {
        // already done in Database.getDifferentFromLastCluster()
    }

    @Test
    @Disabled
    void makeClosure() {
        // Already done in GenerateUtils.makeClosure
    }

    @Test
    @Disabled
    void updateOccurenceDeriver() {
        // already done in ClusterMatrix.optimizeMatrix
    }
}
