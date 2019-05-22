/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.Config;
import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Abstract Implementation of ItemsetsFinder.
 * <p>
 * Initialises some configuration values and static method useful for the algorithms
 *
 * @author stardisblue
 * @author Carmona-Anthony
 * @author jframos0
 * @version 1.0.0
 * @since 0.1.0
 */
public abstract class BasicItemsetsFinder implements ItemsetsFinder {

    protected int minSupport;
    protected int maxPattern;
    protected ArrayList<Itemset> itemsets;
    protected int minTime;

    public BasicItemsetsFinder(Config config) {
        minSupport = config.getMinSupport();
        maxPattern = config.getMaxPattern();
        itemsets = new ArrayList<>();
        minTime = config.getMinTime();
    }

    /**
     * Generates all the itemsets possible from clusters clusters.
     * <p>
     * There can be more than one itemset possible if and only if two clusters of clusters are on the same time. The principle is to create a carthesian product representing all the itemsets possible for a set of clusters
     * <p>
     * <pre>
     * Lcm::GenerateItemset(DataBase,[]itemsets,[]itemID,[]timeID,[][]generatedItemsets, [][]generatedtimeID,[][]generateditemID,sizeGenerated)
     * </pre>
     *
     * @param base     (database)
     * @param clusters (itemsets) une liste representant les clusterId
     */
    @TraceMethod(displayTitle = true)
    static ArrayList<HashSet<Integer>> generateItemsets(Base base, HashSet<Integer> clusters) {
        Debug.println("clusters", clusters, Debug.DEBUG);

        if (clusters.isEmpty()) {
            ArrayList<HashSet<Integer>> generatedItemsets = new ArrayList<>(1);
            generatedItemsets.add(new HashSet<>());
            return generatedItemsets;
        }

        boolean oneTimePerCluster = true; // numberSameTime

        int lastTime = 0;
        // liste des temps qui n'ont qu'un seul cluster
        Iterator<Integer> iterator = clusters.iterator();
        while (iterator.hasNext()) {
            int clusterId = iterator.next();
            lastTime = base.getClusterTimeId(clusterId);

            if (iterator.hasNext()) {
                int nextClusterId = iterator.next();
                if (lastTime == base.getClusterTimeId(nextClusterId)) {
                    oneTimePerCluster = false;
                }
            }
        }

        if (oneTimePerCluster) {
            ArrayList<HashSet<Integer>> generatedPaths = new ArrayList<>(1);
            generatedPaths.add(new HashSet<>(clusters));
            return generatedPaths;
        }

        //Manage MultiClustering
        Debug.println("MultiClustering", Debug.DEBUG);


        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>(); // PosDates

        for (int timeId = 1; timeId <= lastTime; timeId++) {
            ArrayList<Integer> tempPath = new ArrayList<>();

            for (int clusterId : clusters) {
                if (base.getClusterTimeId(clusterId) == timeId) {
                    tempPath.add(clusterId);
                }
            }

            timesClusterIds.add(tempPath);
        }

        ArrayList<HashSet<Integer>> tempItemsets = new ArrayList<>();

        /*
         * For each clusterId in timeClusterIds[0], push path of size 1
         */
        for (Integer clusterId : timesClusterIds.get(0)) {
            HashSet<Integer> singleton = new HashSet<>();
            singleton.add(clusterId);
            tempItemsets.add(singleton);
        }

        /*
         * For each time [1+], get the clusters associated
         */
        for (int clusterIdsIndex = 1, size = timesClusterIds.size(); clusterIdsIndex < size; ++clusterIdsIndex) {
            ArrayList<Integer> tempClusterIds = timesClusterIds.get(clusterIdsIndex);
            ArrayList<HashSet<Integer>> results = new ArrayList<>(tempClusterIds.size() * tempItemsets.size());

            for (HashSet<Integer> generatedPath : tempItemsets) {
                for (int item : tempClusterIds) {
                    HashSet<Integer> result = new HashSet<>(generatedPath);
                    result.add(item);
                    results.add(result); // but why ???
                }
            }
            tempItemsets = results;
        }

        // Remove the itemsets already existing in the set of transactions
        ArrayList<HashSet<Integer>> checkedArrayOfClusterIds = new ArrayList<>();//checkedItemsets

        boolean insertok;

        for (HashSet<Integer> currentItemset : tempItemsets) {
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
