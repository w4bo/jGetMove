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
 *
 * @since 1.0.0
 * @version 1.0.0
 */
public class OptimizedItemsetsFinder extends ItemsetsFinder {
    public OptimizedItemsetsFinder(DefaultConfig config) {
        super(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TraceMethod
    ArrayList<Itemset> generate(Base base, int minTime) {
        Debug.println("Base", base, Debug.DEBUG);
        Debug.println("n° of Clusters", base.getClusterIds().size(), Debug.INFO);
        Debug.println("n° of Transactions", base.getTransactionIds().size(), Debug.INFO);
        Debug.println("n° of Times", base.getTimeIds().size(), Debug.INFO);


        ClusterMatrix clusterMatrix = new ClusterMatrix(base);


        // Overwriting minTime to avoid problems with blocks
        this.minTime = minTime;
        // important if has multiple blocks it needs to be cleaned.
        this.itemsets.clear();

        HashSet<Set<Integer>> doneTransactionSets = new HashSet<>(base.getClusters().size());

        for (Cluster cluster : base.getClusters().values()) {
            // checking wether the transactions of the current cluster are (a part of/the same as) the transations of headClusters
            if (doneTransactionSets.contains(cluster.getTransactions().keySet())) continue;

            clusterMatrix.optimizeMatrix(base, cluster.getTransactions().keySet());

            Debug.printTitle("Current Cluster:" + cluster.getId(), Debug.DEBUG);
            Debug.println("ClusterMatrix", clusterMatrix, Debug.DEBUG);

            HashMap<HashSet<Integer>, TreeSet<Integer>> tailClusterTransaction = new HashMap<>(clusterMatrix.getClusterIds().size());

            HashSet<HashSet<Integer>> doneForNextIteration = new HashSet<>(clusterMatrix.getClusterIds().size());
            for (int clusterId : clusterMatrix.getClusterIds()) {
                HashSet<Integer> transactions = clusterMatrix.getTransactionIds(clusterId);

                if (transactions.size() < minSupport) continue;
                if (doneTransactionSets.contains(transactions)) continue;
                doneForNextIteration.add(transactions);

                tailClusterTransaction.computeIfAbsent(transactions, key -> new TreeSet<>()).add(clusterId);
            }
            doneTransactionSets.addAll(doneForNextIteration);

            Debug.println("TailClusterTransaction", tailClusterTransaction, Debug.DEBUG);

            for (Map.Entry<HashSet<Integer>, TreeSet<Integer>> entry : tailClusterTransaction.entrySet()) {
                HashSet<Integer> transactions = entry.getKey();
                TreeSet<Integer> clusters = entry.getValue();

                for (int clusterId : clusterMatrix.getClusterIds()) {
                    HashSet<Integer> transactionsIter = clusterMatrix.getTransactionIds(clusterId);
                    if (transactionsIter.size() > transactions.size() && transactionsIter.containsAll(transactions)) {
                        clusters.add(clusterId);
                    }
                }
            }

            Debug.println("TailClusterTransactions Updated", tailClusterTransaction, Debug.DEBUG);

            for (Map.Entry<HashSet<Integer>, TreeSet<Integer>> itemsetClusters : tailClusterTransaction.entrySet()) {
                ArrayList<TreeSet<Integer>> itemsets = generateItemsets(base, new ArrayList<>(itemsetClusters.getValue()));

                Debug.println("itemsets", itemsets, Debug.DEBUG);

                for (TreeSet<Integer> itemsetClusterIds : itemsets) {
                    if (itemsetClusterIds.size() > minTime) {
                        saveItemset(clusterMatrix, itemsetClusterIds, itemsetClusters.getKey());
                    }
                }
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
