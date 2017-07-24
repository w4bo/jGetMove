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
import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public abstract class BasicItemsetsFinder implements ItemsetsFinder {

    protected int minSupport;
    protected int maxPattern;
    protected TreeSet<Itemset> itemsets;
    protected int minTime;

    public BasicItemsetsFinder(Config config) {
        minSupport = config.getMinSupport();
        maxPattern = config.getMaxPattern();
        itemsets = new TreeSet<>();
    }

    /**
     * Generates all the itemsets possible from toItemsetize clusters.
     * <p>
     * There can be more than one itemset possible if and only if two clusters of toItemsetize are on the same time.
     * <p>
     * <pre>
     * Lcm::GenerateItemset(DataBase,[]itemsets,[]itemID,[]timeID,[][]generatedItemsets, [][]generatedtimeID,[][]generateditemID,sizeGenerated)
     * </pre>
     *
     * @param base         (database)
     * @param toItemsetize (itemsets) une liste representant les clusterId
     */
    @TraceMethod(displayTitle = true)
    static ArrayList<TreeSet<Integer>> generateItemsets(Base base, ArrayList<Integer> toItemsetize) {
        Debug.println("toItemsetize", toItemsetize, Debug.DEBUG);

        if (toItemsetize.size() == 0) {
            ArrayList<TreeSet<Integer>> generatedItemsets = new ArrayList<>(1);
            generatedItemsets.add(new TreeSet<>());
            return generatedItemsets;
        }

        boolean oneTimePerCluster = true; // numberSameTime

        int lastTime = 0;
        // liste des temps qui n'ont qu'un seul cluster
        for (int i = 0; i < toItemsetize.size(); ++i) {
            int clusterId = toItemsetize.get(i);
            lastTime = base.getClusterTimeId(clusterId);

            if (i != toItemsetize.size() - 1) {
                int nextClusterId = toItemsetize.get(i + 1);
                if (base.getClusterTimeId(clusterId) == base.getClusterTimeId(nextClusterId)) {
                    oneTimePerCluster = false;
                }
            }
        }

        if (oneTimePerCluster) {
            ArrayList<TreeSet<Integer>> generatedPaths = new ArrayList<>(1);
            generatedPaths.add(new TreeSet<>(toItemsetize));
            return generatedPaths;
        }

        //Manage MultiClustering
        Debug.println("MultiClustering", Debug.DEBUG);


        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>(); // PosDates

        for (int i = 1; i <= lastTime; i++) {
            ArrayList<Integer> tempPath = new ArrayList<>();

            for (int clusterId : toItemsetize) {
                if (base.getClusterTimeId(clusterId) == i) {
                    tempPath.add(clusterId);
                }
            }

            timesClusterIds.add(tempPath);
        }

        ArrayList<TreeSet<Integer>> tempItemsets = new ArrayList<>();

        /*
         * For each clusterId in timeClusterIds[0], push path of size 1
         */
        for (Integer clusterId : timesClusterIds.get(0)) {
            TreeSet<Integer> singleton = new TreeSet<>();
            singleton.add(clusterId);
            tempItemsets.add(singleton);
        }

        /*
         * For each time [1+], get the clusters associated
         */
        for (int clusterIdsIndex = 1, size = timesClusterIds.size(); clusterIdsIndex < size; ++clusterIdsIndex) {
            ArrayList<Integer> tempClusterIds = timesClusterIds.get(clusterIdsIndex);
            ArrayList<TreeSet<Integer>> results = new ArrayList<>();

            for (Set<Integer> generatedPath : tempItemsets) {
                for (int item : tempClusterIds) {
                    TreeSet<Integer> result = new TreeSet<>(generatedPath);
                    result.add(item);
                    results.add(result); // but why ???
                }
            }
            tempItemsets = results;
        }

        // Remove the itemsets already existing in the set of transactions
        ArrayList<TreeSet<Integer>> checkedArrayOfClusterIds = new ArrayList<>();//checkedItemsets

        boolean insertok;

        for (TreeSet<Integer> currentItemset : tempItemsets) {
            insertok = true;

            for (Transaction transaction : base.getTransactions().values()) {
                if (transaction.getClusterIds().equals(currentItemset)) {
                    insertok = false;
                    break;
                }
            }

            if (insertok) {
                checkedArrayOfClusterIds.add(currentItemset);
            }
        }
        return checkedArrayOfClusterIds;
    }
}
