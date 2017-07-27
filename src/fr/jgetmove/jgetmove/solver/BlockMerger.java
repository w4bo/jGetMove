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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Used to merge Blocks between each others. Has some custom logic compared to ItemsetsFinder but share most of the algorithm
 *
 * @author stardisblue
 * @version 1.1.0
 * @since 0.2.0
 */
public class BlockMerger {
    private final int minSupport;
    protected ArrayList<Itemset> itemsets;


    /**
     * Default constructor
     */
    public BlockMerger(Config config) {
        minSupport = config.getMinSupport();
        itemsets = new ArrayList<>();
    }

    /**
     * Main method, has a lot in common with {@link OptimizedItemsetsFinder} but doesn't do cartesian product on clusters. On the other hand, will try to detect the smallest englobing set of transaction on the other times.
     *
     * @param base database
     * @return detected possible mergings
     */
    @TraceMethod
    ArrayList<Itemset> generate(Base base) {
        final int nbOfTimes = base.getTimeIds().size();
        Debug.println("Base", base, Debug.DEBUG);
        Debug.println("n° of Clusters", base.getClusterIds().size(), Debug.INFO);
        Debug.println("n° of Transactions", base.getTransactionIds().size(), Debug.INFO);
        Debug.println("n° of Times", nbOfTimes, Debug.INFO);

        ClusterMatrix clusterMatrix = new ClusterMatrix(base);

        // main loop for each cluster
        HashSet<HashSet<Integer>> doneTransactions = new HashSet<>(base.getClusters().size());
        for (Cluster cluster : base.getClusters().values()) {
            clusterMatrix.optimizeMatrix(base, cluster.getTransactions().keySet());
            final int currentclusterId = cluster.getId();

            HashSet<Integer> currentTransactions = clusterMatrix.getTransactionIds(currentclusterId);
            final int currentClusterSize = currentTransactions.size();

            if (doneTransactions.contains(currentTransactions)) continue;
            if (currentClusterSize < minSupport) continue;

            Debug.printTitle("Current Cluster:" + currentclusterId, Debug.DEBUG);
            Debug.println("ClusterMatrix", clusterMatrix, Debug.DEBUG);
            Debug.println("DoneTransactions", doneTransactions, Debug.DEBUG);

            // determining all the transactions that contains the most englobing number of clusters for a transactionSet
            HashMap<Integer, HashMap<HashSet<Integer>, Integer>> minimalClusterForTransaction = new HashMap<>(nbOfTimes);

            HashSet<HashSet<Integer>> doneForNextIteration = new HashSet<>(clusterMatrix.getClusterIds().size());
            for (int clusterId : clusterMatrix.getClusterIds()) {
                if (clusterId < currentclusterId) continue; // if the clusterId is before the actual cluster

                HashSet<Integer> transactions = clusterMatrix.getTransactionIds(clusterId);

                if (transactions.isEmpty()) continue;
                if (doneTransactions.contains(transactions)) continue;

                doneForNextIteration.add(transactions);

                final int timeId = clusterMatrix.getTimeId(clusterId);

                Integer minClusterId = minimalClusterForTransaction.computeIfAbsent(timeId,
                        t -> new HashMap<>(currentClusterSize)).get(transactions);

                if (minClusterId == null ||
                        base.getClusterTransactions(clusterId).size() < base.getClusterTransactions(minClusterId).size()) {
                    minClusterId = clusterId;
                }

                minimalClusterForTransaction.get(timeId).put(transactions, minClusterId);
            }
            doneTransactions.addAll(doneForNextIteration);

            Debug.println("minimalClusterForTransaction", minimalClusterForTransaction, Debug.DEBUG);

            // setting up all the detected clusters to add to the itemset
            HashMap<HashSet<Integer>, HashSet<Integer>> detectedItemsets = detectAndMergeEnglobedItemsets(minimalClusterForTransaction, doneForNextIteration.size());

            Debug.println("detectedItemsets", detectedItemsets, Debug.DEBUG);

            // and we save everything
            for (Map.Entry<HashSet<Integer>, HashSet<Integer>> itemsetClusters : detectedItemsets.entrySet()) {
                saveItemset(clusterMatrix, itemsetClusters.getValue(), itemsetClusters.getKey());
            }
        }

        Debug.println("Itemsets", itemsets, Debug.DEBUG);
        Debug.println("n° of Itemsets", itemsets.size(), Debug.INFO);
        return itemsets;
    }

    /**
     * Search on each time the one wich cluster englobes the best this set of transaction.
     * <p>
     * The purpose is to detect, if there is no exact equivalence of transaction sets across times, the one wich contains the most clusters relative to this transaction set (to this itemset).
     *
     * @param minimalClusterForTransaction complex structure containing all the information necessery to detect the least englobing sets
     * @param numberOfDetectedItemsets     sets the maximum number of possible itemsets
     * @return itemset content to use to create an itemset
     */
    private HashMap<HashSet<Integer>, HashSet<Integer>> detectAndMergeEnglobedItemsets(HashMap<Integer, HashMap<HashSet<Integer>, Integer>> minimalClusterForTransaction, final int numberOfDetectedItemsets) {
        HashMap<HashSet<Integer>, HashSet<Integer>> detectedItemsets = new HashMap<>(numberOfDetectedItemsets);

        for (Map.Entry<Integer, HashMap<HashSet<Integer>, Integer>> entry : minimalClusterForTransaction.entrySet()) {
            for (Map.Entry<HashSet<Integer>, Integer> entryCopy : entry.getValue().entrySet()) {
                // filling up with the basic equivalence (the  transaction of the cluster is the same)
                detectedItemsets.computeIfAbsent(entryCopy.getKey(), key -> new HashSet<>()).add(entryCopy.getValue());
            }

            // filling detectedItemsets while searching for the smallest set englobing all the transaction for each time (they are already present in other itemset so we need to check that)
            for (Map.Entry<Integer, HashMap<HashSet<Integer>, Integer>> entryIter : minimalClusterForTransaction.entrySet()) {
                int timeIdIter = entryIter.getKey();
                // if it's the same time : ignore it
                if (entry.getKey() == timeIdIter) continue;

                for (HashSet<Integer> transactions : entry.getValue().keySet()) {
                    HashMap<HashSet<Integer>, Integer> transactionsClusterIter = entryIter.getValue();

                    // if the transactionSet is already present on this time (we already added it)
                    if (transactionsClusterIter.get(transactions) != null) continue;

                    int minimalEnglobing = Integer.MAX_VALUE;
                    int minimalCluster = -1; // error value
                    for (Map.Entry<HashSet<Integer>, Integer> englobingEntry : entryIter.getValue().entrySet()) {
                        HashSet<Integer> englobingTransactions = englobingEntry.getKey();

                        // the englobing need to be smaller
                        if (englobingTransactions.size() <= transactions.size()
                                || (minimalEnglobing <= englobingTransactions.size())) continue;

                        if (englobingTransactions.containsAll(transactions)) {
                            /// wohoo gold micro optimisation if it has exactly one transaction of difference
                            // we dont need to search for anything else
                            if (englobingTransactions.size() == transactions.size() + 1) {
                                minimalCluster = englobingEntry.getValue();
                                break;
                            }

                            // else we set it as the smallest one
                            minimalEnglobing = englobingTransactions.size();
                            minimalCluster = englobingEntry.getValue();
                        }
                    }

                    // if we found one in this time (block)
                    if (minimalCluster != -1) {
                        detectedItemsets.computeIfAbsent(transactions, key -> new HashSet<>()).add(minimalCluster);
                    }
                }
            }
        }

        return detectedItemsets;
    }

    /**
     * Creates and adds the itemset.
     *
     * @param clusterMatrix   will be used to retrieve the list of transactions and times
     * @param itemsetClusters clusters of the itemset
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
