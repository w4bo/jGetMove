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

        for (Cluster cluster : base.getClusters().values()) {
            clusterMatrix.optimizeMatrix(base, cluster.getTransactions().keySet());

            SortedSet<Integer> headClusters = base.getClusterIds().headSet(cluster.getId());
            HashSet<HashSet<Integer>> headClusterTransactions = new HashSet<>(headClusters.size());

            boolean pass = false;
            int currentClusterSize = cluster.getTransactions().size();

            // TODO : transform to iterator to avoid creating headclusters and tailclusters
            for (int clusterId : headClusters) {
                HashSet<Integer> clusterTransactions = clusterMatrix.getTransactionIds(clusterId);

                if (clusterTransactions.size() == currentClusterSize) {
                    pass = true;
                    break;
                }

                headClusterTransactions.add(clusterTransactions);
            }

            if (pass || currentClusterSize < minSupport) {
                continue;
            }
            Debug.printTitle("Current Cluster:" + cluster.getId(), Debug.DEBUG);
            Debug.println("ClusterMatrix", clusterMatrix, Debug.DEBUG);

            SortedSet<Integer> tailClusters = base.getClusterIds().tailSet(cluster.getId());
            HashMap<HashSet<Integer>, TreeSet<Integer>> tailClusterTransaction = new HashMap<>(tailClusters.size());

            for (int clusterId : tailClusters) {
                HashSet<Integer> transactions = clusterMatrix.getTransactionIds(clusterId);

                if (transactions.size() < minSupport) {
                    continue;
                }

                pass = false;
                for (HashSet<Integer> headTransactions : headClusterTransactions) {
                    if (headTransactions.containsAll(transactions)) {
                        pass = true;
                    }
                }
                if (pass) {
                    continue;
                }

                tailClusterTransaction.computeIfAbsent(transactions, key -> new TreeSet<>()).add(clusterId);
            }

            Debug.println("tailClusterTransaction", tailClusterTransaction, Debug.DEBUG);

            for (Map.Entry<HashSet<Integer>, TreeSet<Integer>> entry : tailClusterTransaction.entrySet()) {
                HashSet<Integer> transactions = entry.getKey();
                TreeSet<Integer> clusters = entry.getValue();

                for (Map.Entry<HashSet<Integer>, TreeSet<Integer>> entryIter : tailClusterTransaction.entrySet()) {
                    HashSet<Integer> transactionsIter = entryIter.getKey();
                    TreeSet<Integer> clustersIter = entryIter.getValue();

                    if (transactions.size() < transactionsIter.size() && transactionsIter.containsAll(transactions)) {
                        clusters.addAll(clustersIter);
                    }
                }
            }

            Debug.println("tailClusterTransaction", tailClusterTransaction, Debug.DEBUG);

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
    void saveItemset(ClusterMatrix clusterMatrix, TreeSet<Integer> itemsetClusters, HashSet<Integer> itemsetTransactions) {
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
