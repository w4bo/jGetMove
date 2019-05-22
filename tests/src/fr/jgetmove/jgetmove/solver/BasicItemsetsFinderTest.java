/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class BasicItemsetsFinderTest {

    @Test
    void generateItemsets() throws IOException, MalformedTimeIndexException, ClusterNotExistException {
        Debug.disable();
        DataBase simpleDataBase = new DataBase(new Input("tests/assets/simple.dat"), new Input("tests/assets/simple_time_index.dat"));
        // Testing with singlepath itemset
        ArrayList<HashSet<Integer>> generatedItemsets = BasicItemsetsFinder.generateItemsets(simpleDataBase, new HashSet<>());

        Debug.println("generatedItemset", generatedItemsets, Debug.DEBUG);

        assertEquals(1, generatedItemsets.size());
        assertEquals(0, generatedItemsets.get(0).size());

        HashSet<Integer> itemset = new HashSet<>();
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
}
