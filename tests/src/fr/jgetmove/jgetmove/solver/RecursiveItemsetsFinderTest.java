/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.Config;
import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveItemsetsFinderTest {

    private static DataBase simpleDataBase;
    private static DataBase complexDataBase;
    private static Config config;

    @BeforeAll
    static void init() throws IOException, MalformedTimeIndexException, ClusterNotExistException {
        simpleDataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));
        config = new Config(1, 0, 1, 0, 0);
        complexDataBase = new DataBase(new Input("tests/assets/complex.dat"), new Input("tests/assets/complex_time_index.dat"));
    }

    @Test
    void generateBasic() {
        RecursiveItemsetsFinder recursiveItemsetsFinder = new RecursiveItemsetsFinder(config);

        TreeSet<Itemset> results = recursiveItemsetsFinder.generate(simpleDataBase, config.getBlockSize());

        assertEquals(3, results.size());
        assertEquals("[\n" +
                "\t|-- Clusters : [0, 2, 4]\n\t|-- Transactions : [0]\n\t`-- Times : [1, 2, 3], \n" +
                "\t|-- Clusters : [4]\n\t|-- Transactions : [0, 1]\n\t`-- Times : [3], \n" +
                "\t|-- Clusters : [1, 3, 4]\n\t|-- Transactions : [1]\n\t`-- Times : [1, 2, 3]]", results.toString());
    }

    @Test
    void generateComplex() {
        String complexResult = "[\n\t|-- Clusters : [0, 2, 6, 9, 15, 19, 22]\n\t|-- Transactions : [0]\n\t`-- Times : [1, 2, 4, 5, 7, 9, 10], \n" +
                "\t|-- Clusters : [0, 2, 19]\n\t|-- Transactions : [0, 1]\n\t`-- Times : [1, 2, 9], \n" +
                "\t|-- Clusters : [0]\n\t|-- Transactions : [0, 1, 2]\n\t`-- Times : [1], \n" +
                "\t|-- Clusters : [0, 22]\n\t|-- Transactions : [0, 2]\n\t`-- Times : [1, 10], \n" +
                "\t|-- Clusters : [22]\n\t|-- Transactions : [0, 2, 3]\n\t`-- Times : [10], \n" +
                "\t|-- Clusters : [0, 2, 5, 10, 12, 13, 19, 23]\n\t|-- Transactions : [1]\n\t`-- Times : [1, 2, 3, 5, 6, 7, 9, 10], \n" +
                "\t|-- Clusters : [0, 5]\n\t|-- Transactions : [1, 2]\n\t`-- Times : [1, 3], \n" +
                "\t|-- Clusters : [5]\n\t|-- Transactions : [1, 2, 3]\n\t`-- Times : [3], \n" +
                "\t|-- Clusters : [5, 12]\n\t|-- Transactions : [1, 3]\n\t`-- Times : [3, 6], \n" +
                "\t|-- Clusters : [23]\n\t|-- Transactions : [1, 5]\n\t`-- Times : [10], \n" +
                "\t|-- Clusters : [0, 3, 5, 7, 8, 14, 18, 20, 22]\n\t|-- Transactions : [2]\n\t`-- Times : [1, 2, 3, 4, 5, 7, 8, 9, 10], \n" +
                "\t|-- Clusters : [3, 5, 20, 22]\n\t|-- Transactions : [2, 3]\n\t`-- Times : [2, 3, 9, 10], \n" +
                "\t|-- Clusters : [7]\n\t|-- Transactions : [2, 4, 5]\n\t`-- Times : [4], \n" +
                "\t|-- Clusters : [1, 3, 5, 11, 12, 16, 20, 22]\n\t|-- Transactions : [3]\n\t`-- Times : [1, 2, 3, 5, 6, 7, 9, 10], \n" +
                "\t|-- Clusters : [1]\n\t|-- Transactions : [3, 4, 5]\n\t`-- Times : [1], \n" +
                "\t|-- Clusters : [1, 11, 16]\n\t|-- Transactions : [3, 5]\n\t`-- Times : [1, 5, 7], \n" +
                "\t|-- Clusters : [1, 4, 7, 17]\n\t|-- Transactions : [4]\n\t`-- Times : [1, 2, 4, 7], \n" +
                "\t|-- Clusters : [1, 4, 7]\n\t|-- Transactions : [4, 5]\n\t`-- Times : [1, 2, 4], \n" +
                "\t|-- Clusters : [1, 4, 7, 11, 16, 21, 23]\n\t|-- Transactions : [5]\n\t`-- Times : [1, 2, 4, 5, 7, 9, 10]]";

        RecursiveItemsetsFinder recursiveItemsetsFinder = new RecursiveItemsetsFinder(config);

        TreeSet<Itemset> results = recursiveItemsetsFinder.generate(complexDataBase, config.getBlockSize());
        assertEquals(19, results.size());
        assertEquals(complexResult, results.toString());
    }

    @Test
    void generateItemsets() {
        // Testing with singlepath itemset
        ArrayList<TreeSet<Integer>> generatedItemsets = BasicItemsetsFinder.generateItemsets(simpleDataBase, new TreeSet<>());

        Debug.println("generatedItemset", generatedItemsets, Debug.DEBUG);

        assertEquals(1, generatedItemsets.size());
        assertEquals(0, generatedItemsets.get(0).size());

        TreeSet<Integer> itemset = new TreeSet<>();
        itemset.add(0);
        itemset.add(2);
        itemset.add(4);

        generatedItemsets = BasicItemsetsFinder.generateItemsets(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(3, generatedItemsets.get(0).size());

        itemset.clear();
        itemset.add(1);
        itemset.add(3);
        itemset.add(4);

        generatedItemsets = BasicItemsetsFinder.generateItemsets(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(3, generatedItemsets.get(0).size());

        itemset.clear();
        itemset.add(4);

        generatedItemsets = BasicItemsetsFinder.generateItemsets(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(1, generatedItemsets.get(0).size());

    }

    @Test
    void getDifferentFromLastCluster() {
        ArrayList<Integer> clusterIds = new ArrayList<>();
        clusterIds.add(1);
        clusterIds.add(2);
        clusterIds.add(3);
        clusterIds.add(4);
        clusterIds.add(5);
        clusterIds.add(6);

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

        assertEquals(5, RecursiveItemsetsFinder.getDifferentFromLastItem(frequent, clusterIds));
        assertEquals(1, RecursiveItemsetsFinder.getDifferentFromLastItem(sameFrequent, clusterIds));
        //assertEquals(0, GeneratorUtils.getDifferentFromLastItem(sameFrequent, 0));
    }

    @Test
    void makeClosure() {
        TreeSet<Integer> transactionIds = new TreeSet<>();
        transactionIds.add(0);

        ArrayList<Integer> qSets;
        TreeSet<Integer> itemset = new TreeSet<>();

        qSets = RecursiveItemsetsFinder.makeClosure(simpleDataBase, transactionIds, itemset, 0);

        ArrayList<Integer> qSetsCheck = new ArrayList<>();
        qSetsCheck.add(0);
        qSetsCheck.add(2);
        qSetsCheck.add(4);

        assertEquals(qSetsCheck, qSets);


        transactionIds.clear();
        transactionIds.add(1);

        qSets.clear();
        itemset.clear();

        qSets = RecursiveItemsetsFinder.makeClosure(simpleDataBase, transactionIds, itemset, 1);

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

        qSets = RecursiveItemsetsFinder.makeClosure(simpleDataBase, transactionIds, itemset, 4);

        qSetsCheck.clear();
        qSetsCheck.add(4);
        assertEquals(qSetsCheck, qSets);
    }

    @Test
    void updateTransactions() {
        Set<Integer> transactionIds = new TreeSet<>();
        transactionIds.add(0);
        transactionIds.add(1);

        ArrayList<Integer> qSets = new ArrayList<>();
        qSets.add(0);
        qSets.add(2);
        qSets.add(4);

        Set<Integer> updated = RecursiveItemsetsFinder.updateTransactions(simpleDataBase, transactionIds, qSets, 0);

        assertEquals(1, updated.size());
        assertTrue(updated.contains(0));


        qSets.clear();
        qSets.add(1);
        qSets.add(3);
        qSets.add(4);

        updated = RecursiveItemsetsFinder.updateTransactions(simpleDataBase, transactionIds, qSets, 1);

        assertEquals(1, updated.size());
        assertTrue(updated.contains(1));


        qSets.clear();
        qSets.add(4);

        updated = RecursiveItemsetsFinder.updateTransactions(simpleDataBase, transactionIds, qSets, 1);

        assertEquals(2, updated.size());
        assertTrue(updated.contains(0));
        assertTrue(updated.contains(1));
    }

    @Test
    void updateFreqList() {
        Set<Integer> transactionIds = new TreeSet<>();
        transactionIds.add(0);
        transactionIds.add(1);

        ArrayList<Integer> qSets = new ArrayList<>();
        qSets.add(0);
        qSets.add(2);
        qSets.add(4);

        ArrayList<Integer> frequentClusters = new ArrayList<>();

        ArrayList<Integer> updated = RecursiveItemsetsFinder.updateClustersFrequenceCount(simpleDataBase, transactionIds, qSets, frequentClusters, 0);

        assertEquals(3, updated.size());
        assertEquals(1, (int) updated.get(0));
        assertEquals(1, (int) updated.get(1));
        assertEquals(1, (int) updated.get(2));


        qSets.clear();
        qSets.add(1);
        qSets.add(3);
        qSets.add(4);

        updated = RecursiveItemsetsFinder.updateClustersFrequenceCount(simpleDataBase, transactionIds, qSets, frequentClusters, 1);

        assertEquals(3, updated.size());
        assertEquals(1, (int) updated.get(0));
        assertEquals(1, (int) updated.get(1));
        assertEquals(1, (int) updated.get(2));

        qSets.clear();
        qSets.add(4);
        updated = RecursiveItemsetsFinder.updateClustersFrequenceCount(simpleDataBase, transactionIds, qSets, frequentClusters, 4);

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

            TreeSet<Integer> itemset = new TreeSet<>();
            itemset.add(0);
            itemset.add(1);

            TreeSet<Integer> transactionIds = new TreeSet<>();
            transactionIds.add(0);
            transactionIds.add(1);

            assertTrue(RecursiveItemsetsFinder.ppcTest(simpleDataBase, itemset, transactionIds, 2));
            assertFalse(RecursiveItemsetsFinder.ppcTest(simpleDataBase, itemset, transactionIds, 5));


            simpleDataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));

            itemset = new TreeSet<>();

            transactionIds = new TreeSet<>();
            transactionIds.add(0);
            transactionIds.add(1);

            // new ppcTest structure
            Set<Integer> filteredTransactionIds = simpleDataBase.getFilteredTransactionIdsIfHaveCluster(transactionIds, 0);
            boolean generatorUtils = RecursiveItemsetsFinder.ppcTest(simpleDataBase, itemset, filteredTransactionIds, 0);

            assertEquals(generatorUtils, true);
        } catch (IOException | MalformedTimeIndexException | ClusterNotExistException e) {
            e.printStackTrace();
        }

    }
}
