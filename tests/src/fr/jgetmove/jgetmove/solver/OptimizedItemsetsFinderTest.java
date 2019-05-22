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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class OptimizedItemsetsFinderTest {

    private static DataBase simpleDataBase;
    private static DataBase complexDataBase;
    private static Config config;

    @BeforeAll
    static void init() throws IOException, MalformedTimeIndexException, ClusterNotExistException {
        Debug.disable();
        simpleDataBase = new DataBase(new Input("tests/assets/simple.dat"), new Input("tests/assets/simple_time_index.dat"));
        config = new Config(1, 0, 1, 0, 0);
        complexDataBase = new DataBase(new Input("tests/assets/basic.dat"), new Input("tests/assets/basic_time_index.dat"));
    }

    @Test
    void generateBasic() {
        OptimizedItemsetsFinder recursiveItemsetsFinder = new OptimizedItemsetsFinder(config);

        ArrayList<Itemset> results = recursiveItemsetsFinder.generate(simpleDataBase, config.getBlockSize());

        assertEquals(3, results.size());
        assertEquals("[\n" +
                "\t|-- Clusters : [0, 2, 4]\n\t|-- Transactions : [0]\n\t`-- Times : [1, 2, 3], \n" +
                "\t|-- Clusters : [1, 3, 4]\n\t|-- Transactions : [1]\n\t`-- Times : [1, 2, 3], \n" +
                "\t|-- Clusters : [4]\n\t|-- Transactions : [0, 1]\n\t`-- Times : [3]]", results.toString());
    }

    @Test
    void generateComplex() {
        String complexResult = "[\n" +
                "\t|-- Clusters : [0, 2, 6, 9, 15, 19, 22]\n\t|-- Transactions : [0]\n\t`-- Times : [1, 2, 4, 5, 7, 9, 10], \n" +
                "\t|-- Clusters : [0, 2, 5, 10, 12, 13, 19, 23]\n\t|-- Transactions : [1]\n\t`-- Times : [1, 2, 3, 5, 6, 7, 9, 10], \n" +
                "\t|-- Clusters : [0, 2, 19]\n\t|-- Transactions : [0, 1]\n\t`-- Times : [1, 2, 9], \n" +
                "\t|-- Clusters : [0, 22]\n\t|-- Transactions : [0, 2]\n\t`-- Times : [1, 10], \n" +
                "\t|-- Clusters : [0, 3, 5, 7, 8, 14, 18, 20, 22]\n\t|-- Transactions : [2]\n\t`-- Times : [1, 2, 3, 4, 5, 7, 8, 9, 10], \n" +
                "\t|-- Clusters : [0, 5]\n\t|-- Transactions : [1, 2]\n\t`-- Times : [1, 3], \n" +
                "\t|-- Clusters : [0]\n\t|-- Transactions : [0, 1, 2]\n\t`-- Times : [1], \n" +
                "\t|-- Clusters : [1, 3, 5, 11, 12, 16, 20, 22]\n\t|-- Transactions : [3]\n\t`-- Times : [1, 2, 3, 5, 6, 7, 9, 10], \n" +
                "\t|-- Clusters : [1, 4, 7, 17]\n\t|-- Transactions : [4]\n\t`-- Times : [1, 2, 4, 7], \n" +
                "\t|-- Clusters : [1, 4, 7, 11, 16, 21, 23]\n\t|-- Transactions : [5]\n\t`-- Times : [1, 2, 4, 5, 7, 9, 10], \n" +
                "\t|-- Clusters : [1, 11, 16]\n\t|-- Transactions : [3, 5]\n\t`-- Times : [1, 5, 7], \n" +
                "\t|-- Clusters : [1, 4, 7]\n\t|-- Transactions : [4, 5]\n\t`-- Times : [1, 2, 4], \n" +
                "\t|-- Clusters : [1]\n\t|-- Transactions : [3, 4, 5]\n\t`-- Times : [1], \n" +
                "\t|-- Clusters : [3, 5, 20, 22]\n\t|-- Transactions : [2, 3]\n\t`-- Times : [2, 3, 9, 10], \n" +
                "\t|-- Clusters : [5, 12]\n\t|-- Transactions : [1, 3]\n\t`-- Times : [3, 6], \n" +
                "\t|-- Clusters : [5]\n\t|-- Transactions : [1, 2, 3]\n\t`-- Times : [3], \n" +
                "\t|-- Clusters : [7]\n\t|-- Transactions : [2, 4, 5]\n\t`-- Times : [4], \n" +
                "\t|-- Clusters : [22]\n\t|-- Transactions : [0, 2, 3]\n\t`-- Times : [10], \n" +
                "\t|-- Clusters : [23]\n\t|-- Transactions : [1, 5]\n\t`-- Times : [10]]";


        OptimizedItemsetsFinder recursiveItemsetsFinder = new OptimizedItemsetsFinder(config);

        ArrayList<Itemset> results = recursiveItemsetsFinder.generate(complexDataBase, config.getBlockSize());
        assertEquals(19, results.size());
        assertEquals(complexResult, results.toString());
    }
}
