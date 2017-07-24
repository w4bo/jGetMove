package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;

import java.util.*;

/**
 * @version 1.0.0
 * @since 0.2.0
 */
public class BlockMerger {
    private final int minSupport;
    protected TreeSet<Itemset> itemsets;


    /**
     * Initialise le solveur.
     */
    public BlockMerger(DefaultConfig config) {
        minSupport = config.getMinSupport();
        itemsets = new TreeSet<>();
    }

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

            // getting all the clusters folowing the current one (included)
            SortedSet<Integer> tailClusters = clusterMatrix.getClusterIds().tailSet(currentclusterId);
            // setting up all the detected clusters to add to the itemset
            HashMap<HashSet<Integer>, TreeSet<Integer>> detectedItemsets = new HashMap<>(tailClusters.size());

            // determining all the transactions that contains the most englobing number of clusters for a transactionSet
            HashMap<Integer, HashMap<HashSet<Integer>, Integer>> minimalClusterForTransaction = new HashMap<>(nbOfTimes);

            // minimalClusterForTransaction setup
            HashSet<HashSet<Integer>> doneForNextIteration = new HashSet<>(clusterMatrix.getClusterIds().size());
            for (int clusterId : tailClusters) {
                HashSet<Integer> transactions = clusterMatrix.getTransactionIds(clusterId);

                if (transactions.size() == 0) continue;
                if (doneTransactions.contains(transactions)) continue;
                doneForNextIteration.add(transactions);

                Integer timeId = clusterMatrix.getTimeId(clusterId);

                Integer minClusterId = minimalClusterForTransaction.computeIfAbsent(timeId,
                        t -> new HashMap<>(currentClusterSize * currentClusterSize)).get(transactions);

                if (minClusterId == null ||
                        base.getClusterTransactions(clusterId).size() < base.getClusterTransactions(minClusterId).size()) {
                    minClusterId = clusterId;
                }

                minimalClusterForTransaction.get(timeId).put(transactions, minClusterId);
            }
            doneTransactions.addAll(doneForNextIteration);


            Debug.println("minimalClusterForTransaction", minimalClusterForTransaction, Debug.DEBUG);

            for (Map.Entry<Integer, HashMap<HashSet<Integer>, Integer>> entry : minimalClusterForTransaction.entrySet()) {
                for (Map.Entry<HashSet<Integer>, Integer> entryCopy : entry.getValue().entrySet()) {
                    detectedItemsets.computeIfAbsent(entryCopy.getKey(), key -> new TreeSet<>()).add(entryCopy.getValue());
                }

                for (Map.Entry<Integer, HashMap<HashSet<Integer>, Integer>> entryIter : minimalClusterForTransaction.entrySet()) {
                    int timeIdIter = entryIter.getKey();
                    // if it's the same time : ignore it
                    if (entry.getKey() == timeIdIter) continue;

                    for (HashSet<Integer> transactions : entry.getValue().keySet()) {
                        HashMap<HashSet<Integer>, Integer> transactionsClusterIter = entryIter.getValue();

                        // if the transactionSet is already present on this time
                        if (transactionsClusterIter.get(transactions) != null) continue;

                        HashSet<Integer> minimalEnglobing = new HashSet<>();
                        int minimalCluster = -1; // random
                        for (Map.Entry<HashSet<Integer>, Integer> englobingEntry : entryIter.getValue().entrySet()) {
                            HashSet<Integer> englobingTransactions = englobingEntry.getKey();

                            if (englobingTransactions.size() <= transactions.size()) continue;

                            if (englobingTransactions.containsAll(transactions)) {
                                /// wohoo gold
                                if (englobingTransactions.size() == transactions.size() + 1) {
                                    minimalCluster = englobingEntry.getValue();
                                    break;
                                }

                                if (englobingTransactions.size() < minimalEnglobing.size() || minimalCluster == -1) {
                                    minimalEnglobing = englobingTransactions;
                                    minimalCluster = englobingEntry.getValue();
                                }
                            }
                        }
                        if (minimalCluster != -1) {
                            detectedItemsets.computeIfAbsent(transactions, key -> new TreeSet<>()).add(minimalCluster);
                        }
                    }
                }
            }

            Debug.println("detectedItemsets", detectedItemsets, Debug.DEBUG);

            for (Map.Entry<HashSet<Integer>, TreeSet<Integer>> itemsetClusters : detectedItemsets.entrySet()) {
                saveItemset(clusterMatrix, itemsetClusters.getValue(), itemsetClusters.getKey());
            }
        }

        Debug.println("Itemsets", itemsets, Debug.DEBUG);
        Debug.println("n° of Itemsets", itemsets.size(), Debug.INFO);
        return new ArrayList<>(itemsets);
    }

    /**
     * Creates and adds the itemset. Will return <tt>true</tt> if the itemset wasn't already saved.
     *
     * @param clusterMatrix   will be used to retrieve the list of transactions and times
     * @param itemsetClusters clusters of the itemset
     * @see Itemset#compareTo(Itemset) to understand how the verification is made.
     */
    private void saveItemset(ClusterMatrix clusterMatrix, TreeSet<Integer> itemsetClusters, HashSet<Integer> itemsetTransactions) {
        // then the itemset is possible
        TreeSet<Integer> itemsetTimes = new TreeSet<>();
        for (Integer clusterId : itemsetClusters) {
            itemsetTimes.add(clusterMatrix.getTimeId(clusterId));
        }
        // so we add it to the final list
        Itemset itemset = new Itemset(itemsets.size(), itemsetTransactions, itemsetClusters, itemsetTimes);

        this.itemsets.add(itemset);
    }
}
