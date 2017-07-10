package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
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
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemsetsFinderTest {
// TODO finish complexDataBase tests

    private static DataBase simpleDataBase;
    private static DataBase complexDataBase;
    private static DefaultConfig config;

    @BeforeAll
    static void init() throws IOException, MalformedTimeIndexException, ClusterNotExistException {
        simpleDataBase = new DataBase(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"));
        config = new DefaultConfig(1, 0, 1, 0, 0);
        complexDataBase = new DataBase(new Input("tests/assets/complex.dat"), new Input("tests/assets/complex_time_index.dat"));
    }

    @Test
    void generateBasic() {
        ItemsetsFinder itemsetsFinder = new ItemsetsFinder(config);

        ArrayList<Itemset> results = itemsetsFinder.generate(simpleDataBase, config.getBlockSize());

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

        ItemsetsFinder itemsetsFinder = new ItemsetsFinder(config);

        ArrayList<Itemset> results = itemsetsFinder.generate(complexDataBase, config.getBlockSize());
        assertEquals(19, results.size());
        // assertEquals("[[0], [0, 2, 19], [0, 2, 5, 10, 12, 13, 19, 23], [0, 2, 6, 9, 15, 19, 22], [0, 3, 5, 7, 8, 14, 18, 20, 22], [0, 5], [0, 22], [1], [1, 3, 5, 11, 12, 16, 20, 22], [1, 4, 7], [1, 4, 7, 11, 16, 21, 23], [1, 4, 7, 17], [1, 11, 16], [3, 5, 20, 22], [5], [5, 12], [7], [22], [23]]", results.toString());
        //assertEquals("[[0, 1], [0, 1, 2, 9], [0, 1, 2, 3, 5, 6, 7, 9, 10], [0, 1, 2, 4, 5, 7, 9, 10], [0, 1, 2, 3, 4, 5, 7, 8, 9, 10], [0, 1, 3], [0, 1, 2, 3, 4, 5, 7, 8, 9, 10], [0, 1, 10], [0, 1, 2, 3, 4, 5, 7, 8, 9, 10], [0, 1], [0, 1, 2, 3, 5, 6, 7, 9, 10], [0, 1, 2, 4], [0, 1, 2, 4, 5, 7, 9, 10], [0, 1, 2, 4, 7], [0, 1, 5, 7], [0, 1, 2, 3, 5, 6, 7, 9, 10], [0, 1, 2, 4, 5, 7, 9, 10], [0, 2, 3, 9, 10], [0, 3], [0, 3, 6], [0, 4], [0, 10], [0, 10]]", results.getLvl2TimeIds().toString());

    }

    @Test
    @Disabled
    void generateMultiCluster() {
        ItemsetsFinder itemsetsFinder = new ItemsetsFinder(config);

        try {
            DataBase multiDataBase = new DataBase(new Input("tests/assets/multi_cluster.dat"), new Input("tests/assets/multi_cluster_time_index.dat"));
            ArrayList<Itemset> results = itemsetsFinder.generate(multiDataBase, config.getBlockSize());
        } catch (IOException | ClusterNotExistException | MalformedTimeIndexException e) {
            e.printStackTrace();
        }

    }

    @Test
    void generateClusters() {

        // Testing with singlepath itemset

        ItemsetsFinder itemsetsFinder = new ItemsetsFinder(config);

        ArrayList<TreeSet<Integer>> generatedItemsets = ItemsetsFinder.generateItemsets(simpleDataBase, new ArrayList<>());

        Debug.println("generatedItemset", generatedItemsets, Debug.DEBUG);

        assertEquals(1, generatedItemsets.size());
        assertEquals(0, generatedItemsets.get(0).size());

        ArrayList<Integer> itemset = new ArrayList<>();
        itemset.add(0);
        itemset.add(2);
        itemset.add(4);

        generatedItemsets = ItemsetsFinder.generateItemsets(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(3, generatedItemsets.get(0).size());

        itemset.clear();
        itemset.add(1);
        itemset.add(3);
        itemset.add(4);

        generatedItemsets = ItemsetsFinder.generateItemsets(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(3, generatedItemsets.get(0).size());

        itemset.clear();
        itemset.add(4);

        generatedItemsets = ItemsetsFinder.generateItemsets(simpleDataBase, itemset);

        assertEquals(1, generatedItemsets.size());
        assertEquals(1, generatedItemsets.get(0).size());

    }
}
