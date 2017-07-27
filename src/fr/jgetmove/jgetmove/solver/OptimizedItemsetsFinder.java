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
import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;

import java.util.*;

/**
 * An optimised linear version of the LCM algorithm.
 * <p>
 * The copy, add and edit operations have been minised.
 * <p>
 * It's recommended to use this algorithm as it is the fatest.
 *
 * @author stardisblue
 * @version 1.0.0
 * @since 1.0.0
 */
public class OptimizedItemsetsFinder extends BasicItemsetsFinder {
    /**
     * Default constructor
     *
     * @param config options for the algorithms
     */
    public OptimizedItemsetsFinder(Config config) {
        super(config);
    }

    @Override
    @TraceMethod
    public ArrayList<Itemset> generate(Base base, int minTime) {
        Debug.println("Base", base, Debug.DEBUG);
        Debug.println("n° of Clusters", base.getClusterIds().size(), Debug.INFO);
        Debug.println("n° of Transactions", base.getTransactionIds().size(), Debug.INFO);
        Debug.println("n° of Times", base.getTimeIds().size(), Debug.INFO);


        ClusterMatrix clusterMatrix = new ClusterMatrix(base);


        // Overwriting minTime to avoid problems with blocks
        this.minTime = minTime;
        // important if has multiple blocks it needs to be cleaned.
        this.itemsets = new ArrayList<>(clusterMatrix.getClusterIds().size());

        run(base, clusterMatrix);

        Debug.println("Itemsets", itemsets, Debug.DEBUG);
        Debug.println("n° of Itemsets", itemsets.size(), Debug.INFO);
        itemsets.trimToSize();
        return itemsets;
    }

    /**
     * Core method containing the algorithm in charge of detecting the itemsets
     *
     * @param base          database
     * @param clusterMatrix dynamic database
     * @implSpec Iterates over clusters. detect if the possible itemsets from this cluster were not done before. if not, try to expand them by detecting when the transactions of the itemset are englobed by each other.
     * Once all the possible combinations are done, we need to be sure that an itemset doesn't have two clusters on the same time, so we do a cartesian product. the result is then saved as an itemset.
     * <p>
     * complexity:  <code>c<sup>2</sup>(i + t) + c&times;i</code> (worst case) otherwise <code>c(log(c)&times;(i + t) + i)</code> (c clusters, t transactions, i itemsets)
     */
    private void run(Base base, ClusterMatrix clusterMatrix) {
        HashSet<Set<Integer>> doneTransactionSets = new HashSet<>(base.getClusters().size());

        for (Cluster cluster : base.getClusters().values()) {
            // checking if the transactions of the cluster correponds to an already done set of transactions
            if (doneTransactionSets.contains(cluster.getTransactions().keySet())) continue;

            // optimises the dynamic database for the transactions of this cluster
            clusterMatrix.optimizeMatrix(base, cluster.getTransactions().keySet());

            Debug.printTitle("Current Cluster:" + cluster.getId(), Debug.DEBUG);
            Debug.println("ClusterMatrix", clusterMatrix, Debug.DEBUG);

            // initialising the itemset container
            HashMap<HashSet<Integer>, HashSet<Integer>> transactionsClustersItemset = getTransactionsAndClustersOfItemsets(clusterMatrix, doneTransactionSets);
            // saving buffer
            doneTransactionSets.addAll(transactionsClustersItemset.keySet());

            Debug.println("transactionsClustersItemset", transactionsClustersItemset, Debug.DEBUG);

            // expanding the itemset while checking the transaction sets that englobe each other
            addEnglobingClusters(clusterMatrix, transactionsClustersItemset);

            Debug.println("transactionsClustersItemset Updated", transactionsClustersItemset, Debug.DEBUG);

            // once expanded, we proceed to linearise them across the times
            for (Map.Entry<HashSet<Integer>, HashSet<Integer>> itemsetData : transactionsClustersItemset.entrySet()) {
                ArrayList<HashSet<Integer>> itemsets = generateItemsets(base, itemsetData.getValue());

                Debug.println("itemsets", itemsets, Debug.DEBUG);

                // foreach formed itemset we create the itemset
                for (HashSet<Integer> itemsetClusterIds : itemsets) {
                    if (itemsetClusterIds.size() > minTime) {
                        saveItemset(clusterMatrix, itemsetClusterIds, itemsetData.getKey());
                    }
                }
            }
        }
    }

    /**
     * Adds all the clusters which englobe an itemset. For each itemset
     *
     * @param clusterMatrix               dynamic database
     * @param transactionsClustersItemset the data of itemsets
     * @implSpec complexity :<code>i&times;c</code> worst case, otherwise <code>i&times;log(c)</code> (i itemsets, c clusters)
     */
    private void addEnglobingClusters(ClusterMatrix clusterMatrix, HashMap<HashSet<Integer>, HashSet<Integer>> transactionsClustersItemset) {
        for (Map.Entry<HashSet<Integer>, HashSet<Integer>> itemsetData : transactionsClustersItemset.entrySet()) {
            HashSet<Integer> transactions = itemsetData.getKey();
            HashSet<Integer> clusters = itemsetData.getValue();

            for (int clusterId : clusterMatrix.getClusterIds()) {
                HashSet<Integer> transactionsIter = clusterMatrix.getTransactionIds(clusterId);
                if (transactionsIter.size() > transactions.size() && transactionsIter.containsAll(transactions)) {
                    clusters.add(clusterId);
                }
            }
        }
    }

    /**
     * returns future data of an itemset.
     *
     * @param clusterMatrix       dynamic database
     * @param doneTransactionSets the transactionsSets of existing itemsets
     * @return future data of an itemset.
     * @implSpec complexity : <code>c</code> (worst case), <code>log(c)</code> (c cluster)
     */
    private HashMap<HashSet<Integer>, HashSet<Integer>> getTransactionsAndClustersOfItemsets(ClusterMatrix clusterMatrix, HashSet<Set<Integer>> doneTransactionSets) {
        HashMap<HashSet<Integer>, HashSet<Integer>> transactionsClustersItemset = new HashMap<>(clusterMatrix.getClusterIds().size());

        for (int clusterId : clusterMatrix.getClusterIds()) {
            HashSet<Integer> transactions = clusterMatrix.getTransactionIds(clusterId);

            // basic check
            if (transactions.size() < minSupport) continue;

            // checking if it has already been done before
            if (doneTransactionSets.contains(transactions)) continue;

            // adding the cluster to an itemset identified by transactions
            transactionsClustersItemset.computeIfAbsent(transactions, key -> new HashSet<>()).add(clusterId);
        }
        return transactionsClustersItemset;
    }

    /**
     * Creates and adds the itemset. Will return <tt>true</tt> if the itemset wasn't already saved.
     *
     * @param clusterMatrix   will be used to retrieve the list of transactions and times
     * @param itemsetClusters clusters of the itemset
     * @see Itemset#compareTo(Itemset) to understand how the verification is made.
     */
    private void saveItemset(ClusterMatrix clusterMatrix, HashSet<Integer> itemsetClusters, HashSet<Integer> itemsetTransactions) {
        // then the itemset is possible
        HashSet<Integer> itemsetTimes = new HashSet<>();
        for (Integer clusterId : itemsetClusters) {
            itemsetTimes.add(clusterMatrix.getTimeId(clusterId));
        }
        // so we add it to the final list
        Itemset itemset = new Itemset(itemsetTransactions, itemsetClusters, itemsetTimes);

        this.itemsets.add(itemset);
    }
}
