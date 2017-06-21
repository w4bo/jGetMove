package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterGeneratorTest {
    @Test
    @Disabled
    void makeClosure() {
        // Already done in GenerateUtils.makeClosure

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
    @Disabled
    void calcurateCoreI() {
        // already done in Database.getDifferentFromLastCluster()
    }

    @Test
    void generate() {
        //TODO
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
        //TODO
    }

    @Test
    @Disabled
    void updateOccurenceDeriver() {
        // already done in ClusterMatrix.optimizeMatrix
    }

    @Test
    void generateClusters() {

        // Testing with singlepath itemset
        try {
            Database database = new Database(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"), 0);
            DefaultConfig config = new DefaultConfig(1, 0, 1, 0);

            ClusterGenerator clusterGenerator = new ClusterGenerator(database, config);

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

        } catch (IOException | ClusterNotExistException | MalformedTimeIndexException e) {
            e.printStackTrace();
        }
    }


}
