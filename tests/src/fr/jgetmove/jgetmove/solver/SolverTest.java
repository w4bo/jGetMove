/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolverTest {


    private static DataBase simpleDataBase;
    private static DataBase complexDataBase;
    private static Solver solver;

    @BeforeAll
    static void init() throws IOException, MalformedTimeIndexException, ClusterNotExistException {
        Debug.disable();
        simpleDataBase = new DataBase(new Input("tests/assets/simple.dat"), new Input("tests/assets/simple_time_index.dat"));
        Config config = new Config(1, 0, 1, 0, 0);
        complexDataBase = new DataBase(new Input("tests/assets/basic.dat"), new Input("tests/assets/basic_time_index.dat"));

        ItemsetsFinder itemsetsFinder = new OptimizedItemsetsFinder(config);
        BlockMerger blockMerger = new BlockMerger(config);
        solver = new Solver(itemsetsFinder, blockMerger, new HashSet<>(), new HashSet<>(), config);
    }

    @Test
    void findItemsets() {
        findItemsetsSimple();
        findItemsetsComplex();
        findItemsetsBlocs();
    }

    private void findItemsetsSimple() {
        String foundItemsets = "[[\n" +
                "\t|-- Clusters : [0, 2, 4]\n\t|-- Transactions : [0]\n\t`-- Times : [1, 2, 3], \n" +
                "\t|-- Clusters : [1, 3, 4]\n\t|-- Transactions : [1]\n\t`-- Times : [1, 2, 3]]]";

        assertEquals(foundItemsets, solver.findItemsets(simpleDataBase).toString());
    }

    private void findItemsetsComplex() {
        String foundItemsets = "[[\n" +
                "\t|-- Clusters : [0, 2, 6, 9, 15, 19, 22]\n\t|-- Transactions : [0]\n\t`-- Times : [1, 2, 4, 5, 7, 9, 10], \n" +
                "\t|-- Clusters : [0, 2, 5, 10, 12, 13, 19, 23]\n\t|-- Transactions : [1]\n\t`-- Times : [1, 2, 3, 5, 6, 7, 9, 10], \n" +
                "\t|-- Clusters : [0, 2, 19]\n\t|-- Transactions : [0, 1]\n\t`-- Times : [1, 2, 9], \n" +
                "\t|-- Clusters : [0, 22]\n\t|-- Transactions : [0, 2]\n\t`-- Times : [1, 10], \n" +
                "\t|-- Clusters : [0, 3, 5, 7, 8, 14, 18, 20, 22]\n\t|-- Transactions : [2]\n\t`-- Times : [1, 2, 3, 4, 5, 7, 8, 9, 10], \n" +
                "\t|-- Clusters : [0, 5]\n\t|-- Transactions : [1, 2]\n\t`-- Times : [1, 3], \n" +
                "\t|-- Clusters : [1, 3, 5, 11, 12, 16, 20, 22]\n\t|-- Transactions : [3]\n\t`-- Times : [1, 2, 3, 5, 6, 7, 9, 10], \n" +
                "\t|-- Clusters : [1, 4, 7, 17]\n\t|-- Transactions : [4]\n\t`-- Times : [1, 2, 4, 7], \n" +
                "\t|-- Clusters : [1, 4, 7, 11, 16, 21, 23]\n\t|-- Transactions : [5]\n\t`-- Times : [1, 2, 4, 5, 7, 9, 10], \n" +
                "\t|-- Clusters : [1, 11, 16]\n\t|-- Transactions : [3, 5]\n\t`-- Times : [1, 5, 7], \n" +
                "\t|-- Clusters : [1, 4, 7]\n\t|-- Transactions : [4, 5]\n\t`-- Times : [1, 2, 4], \n" +
                "\t|-- Clusters : [3, 5, 20, 22]\n\t|-- Transactions : [2, 3]\n\t`-- Times : [2, 3, 9, 10], \n" +
                "\t|-- Clusters : [5, 12]\n\t|-- Transactions : [1, 3]\n\t`-- Times : [3, 6]]]";

        assertEquals(foundItemsets, solver.findItemsets(complexDataBase).toString());
    }

    private void findItemsetsBlocs() {
        Config config = new Config(1, 0, 1, 5, 0);
        ItemsetsFinder itemsetsFinder = new OptimizedItemsetsFinder(config);
        BlockMerger blockMerger = new BlockMerger(config);
        Solver solver = new Solver(itemsetsFinder, blockMerger, new HashSet<>(), new HashSet<>(), config);

        String foundItemsets = "[[\n" +
                "\t|-- Clusters : [0, 2, 6, 9]\n\t|-- Transactions : [0]\n\t`-- Times : [1, 2, 4, 5], \n" +
                "\t|-- Clusters : [0, 2, 5, 10]\n\t|-- Transactions : [1]\n\t`-- Times : [1, 2, 3, 5], \n" +
                "\t|-- Clusters : [0, 2]\n\t|-- Transactions : [0, 1]\n\t`-- Times : [1, 2], \n" +
                "\t|-- Clusters : [0, 3, 5, 7, 8]\n\t|-- Transactions : [2]\n\t`-- Times : [1, 2, 3, 4, 5], \n" +
                "\t|-- Clusters : [0, 5]\n\t|-- Transactions : [1, 2]\n\t`-- Times : [1, 3], \n" +
                "\t|-- Clusters : [0]\n\t|-- Transactions : [0, 1, 2]\n\t`-- Times : [1], \n" +
                "\t|-- Clusters : [1, 11]\n\t|-- Transactions : [3, 5]\n\t`-- Times : [1, 5], \n" +
                "\t|-- Clusters : [1, 4, 7]\n\t|-- Transactions : [4, 5]\n\t`-- Times : [1, 2, 4], \n" +
                "\t|-- Clusters : [1, 3, 5, 11]\n\t|-- Transactions : [3]\n\t`-- Times : [1, 2, 3, 5], \n" +
                "\t|-- Clusters : [1]\n\t|-- Transactions : [3, 4, 5]\n\t`-- Times : [1], \n" +
                "\t|-- Clusters : [3, 5]\n\t|-- Transactions : [2, 3]\n\t`-- Times : [2, 3], \n" +
                "\t|-- Clusters : [5]\n\t|-- Transactions : [1, 2, 3]\n\t`-- Times : [3], \n" +
                "\t|-- Clusters : [7]\n\t|-- Transactions : [2, 4, 5]\n\t`-- Times : [4], \n" +
                "\t|-- Clusters : [1, 4, 7, 11]\n\t|-- Transactions : [5]\n\t`-- Times : [1, 2, 4, 5]], " +
                "[\n" +
                "\t|-- Clusters : [16]\n\t|-- Transactions : [3, 5]\n\t`-- Times : [7], \n" +
                "\t|-- Clusters : [12, 16, 20, 22]\n\t|-- Transactions : [3]\n\t`-- Times : [6, 7, 9, 10], \n" +
                "\t|-- Clusters : [16, 21, 23]\n\t|-- Transactions : [5]\n\t`-- Times : [7, 9, 10], \n" +
                "\t|-- Clusters : [17]\n\t|-- Transactions : [4]\n\t`-- Times : [7], \n" +
                "\t|-- Clusters : [14, 18, 20, 22]\n\t|-- Transactions : [2]\n\t`-- Times : [7, 8, 9, 10], \n" +
                "\t|-- Clusters : [15, 19, 22]\n\t|-- Transactions : [0]\n\t`-- Times : [7, 9, 10], \n" +
                "\t|-- Clusters : [19]\n\t|-- Transactions : [0, 1]\n\t`-- Times : [9], \n" +
                "\t|-- Clusters : [12, 13, 19, 23]\n\t|-- Transactions : [1]\n\t`-- Times : [6, 7, 9, 10], \n" +
                "\t|-- Clusters : [20, 22]\n\t|-- Transactions : [2, 3]\n\t`-- Times : [9, 10], \n" +
                "\t|-- Clusters : [22]\n\t|-- Transactions : [0, 2, 3]\n\t`-- Times : [10], \n" +
                "\t|-- Clusters : [23]\n\t|-- Transactions : [1, 5]\n\t`-- Times : [10], \n" +
                "\t|-- Clusters : [12]\n\t|-- Transactions : [1, 3]\n\t`-- Times : [6]]]";
        assertEquals(foundItemsets, solver.findItemsets(complexDataBase).toString());
    }

    @Test
    void mergeBlocks() {
        ArrayList<ArrayList<Itemset>> base = solver.findItemsets(complexDataBase);
        TreeSet<Itemset> expected = new TreeSet<>(base.get(0));
        for (int blockSize = 0; blockSize < complexDataBase.getTimeIds().size(); blockSize++) {
            Config config = new Config(1, 0, 1, blockSize, 0);
            ItemsetsFinder itemsetsFinder = new OptimizedItemsetsFinder(config);
            BlockMerger blockMerger = new BlockMerger(config);
            Solver solverBlock = new Solver(itemsetsFinder, blockMerger, new HashSet<>(), new HashSet<>(), config);

            assertEquals(expected, new TreeSet<>(solverBlock.mergeBlocks(solverBlock.findItemsets(complexDataBase))));
        }
    }

    @Test
    void detectPatterns() {

    }
}
