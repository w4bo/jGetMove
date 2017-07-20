package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.debug.Debug;

import java.util.*;

public class BlockMerger extends OptimizedItemsetsFinder {
    /**
     * Initialise le solveur.
     */
    public BlockMerger(DefaultConfig config) {
        super(config);
    }

    ArrayList<Itemset> generate(Base base) {
        final int nbOfTimes = base.getTimeIds().size();
        Debug.println("Base", base, Debug.DEBUG);
        Debug.println("n° of Clusters", base.getClusterIds().size(), Debug.INFO);
        Debug.println("n° of Transactions", base.getTransactionIds().size(), Debug.INFO);
        Debug.println("n° of Times", nbOfTimes, Debug.INFO);

        ClusterMatrix clusterMatrix = new ClusterMatrix(base);

        for (Cluster cluster : base.getClusters().values()) {
            clusterMatrix.optimizeMatrix(base, cluster.getTransactions().keySet());
            final int currentclusterId = cluster.getId();

            HashSet<Integer> currentTransactions = clusterMatrix.getTransactionIds(currentclusterId);

            final int currentClusterSize = currentTransactions.size();
            if (currentClusterSize < minSupport) {
                continue;
            }

            // getting all the cluster preceding the current one
            SortedSet<Integer> headClusters = base.getClusterIds().headSet(currentclusterId);

            // getting the transactionSets which need to be ignored
            HashSet<HashSet<Integer>> doneTransactions = doneTransactionHashs(clusterMatrix, headClusters, currentClusterSize);
            // checking wether the transactions of the current cluster are (a part of/the same as) the transations of headClusters
            if (doneTransactions == null) continue;

            Debug.printTitle("Current Cluster:" + currentclusterId, Debug.DEBUG);
            Debug.println("ClusterMatrix", clusterMatrix, Debug.DEBUG);

            // getting all the clusters folowing the current one (included)
            SortedSet<Integer> tailClusters = base.getClusterIds().tailSet(currentclusterId);
            // setting up all the detected clusters to add to the itemset
            HashMap<HashSet<Integer>, TreeSet<Integer>> detectedItemsets = new HashMap<>(tailClusters.size());

            // determining all the transactions that contains the most englobing number of clusters for a transactionSet
            HashMap<Integer, HashMap<HashSet<Integer>, Integer>> minimalClusterForTransaction = new HashMap<>(nbOfTimes);

            // minimalClusterForTransaction setup
            for (int clusterId : tailClusters) {
                HashSet<Integer> transactions = clusterMatrix.getTransactionIds(clusterId);
                if (transactions.size() == 0) continue;
                if (doneTransactions.contains(transactions)) continue;

                Integer timeId = clusterMatrix.getTimeId(clusterId);

                Integer minClusterId = minimalClusterForTransaction.computeIfAbsent(timeId,
                        t -> new HashMap<>(currentClusterSize * currentClusterSize)).get(transactions);
                if (minClusterId == null ||
                        base.getClusterTransactions(clusterId).size() < base.getClusterTransactions(minClusterId).size()) {
                    minClusterId = clusterId;
                }
                minimalClusterForTransaction.get(timeId).put(transactions, minClusterId);
            }
            Debug.println("minimalClusterForTransaction", minimalClusterForTransaction, Debug.DEBUG);

            for (int clusterId : tailClusters) {
                HashSet<Integer> transactions = clusterMatrix.getTransactionIds(clusterId);
                if (transactions.size() == 0) continue;
                if (doneTransactions.contains(transactions)) continue;

                for (int timeId = 1; timeId <= nbOfTimes; timeId++) {
                    if (!minimalClusterForTransaction.containsKey(timeId))
                        minimalClusterForTransaction.put(timeId, new HashMap<>(currentClusterSize * currentClusterSize));
                    if (minimalClusterForTransaction.get(timeId).get(transactions) != null) continue;

                    HashSet<Integer> minimalEnglobing = new HashSet<>();
                    int minimalCluster = -1; // random
                    for (Map.Entry<HashSet<Integer>, Integer> englobingEntry : minimalClusterForTransaction.get(timeId).entrySet()) {
                        HashSet<Integer> englobingTransactions = englobingEntry.getKey();
                        if (englobingTransactions.size() <= transactions.size()) continue;

                        if (englobingTransactions.containsAll(transactions)) {
                            /// wohoo gold
                            if (englobingTransactions.size() == transactions.size() + 1) {
                                minimalCluster = englobingEntry.getValue();
                                break;
                            }

                            if (englobingTransactions.size() < minimalEnglobing.size() && minimalCluster != -1) {
                                minimalEnglobing = englobingTransactions;
                                minimalCluster = englobingEntry.getValue();
                            }
                        }
                    }

                    minimalClusterForTransaction.get(timeId).put(transactions, minimalCluster);
                }
            }

            Debug.println("minimalClusterForTransaction", minimalClusterForTransaction, Debug.DEBUG);

            // detecting all the itemset that can be created
            for (int clusterId : tailClusters) {
                int timeId = clusterMatrix.getTimeId(clusterId);

                HashSet<Integer> transactions = clusterMatrix.getTransactionIds(clusterId);

                // if it doesn't have enough transactions
                if (transactions.size() < minSupport) continue;

                // if it's not one which is contained in the already done one
                if (doneTransactions.contains(transactions)) continue;

                // if it's not the minimal one
                Integer minimalCluster = minimalClusterForTransaction.get(timeId).get(transactions);
                if (minimalCluster != null && minimalCluster != clusterId) continue;

                // if it's not one which is included in headTransaction (or englobed in)
                boolean inHeadClusters = false;
                for (HashSet<Integer> headTransactions : doneTransactions) {
                    if (headTransactions.containsAll(transactions)) {
                        inHeadClusters = true;
                        break;
                    }
                }
                if (inHeadClusters) {
                    continue;
                }

                // well adding it to the detected itemsets
                detectedItemsets.computeIfAbsent(transactions, key -> new TreeSet<>()).add(clusterId);
                // TODO : times of itemset
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
     * Computes all the transaction hashsets which have already been done by the iterator
     * <p>
     * All the clusters (preceding the current one) containing a part of the transactions of the current itemset have computed all the possible itemsets with these transactions, so it's useless to compute them again.
     * <p>
     * If the transactions of a clusters are (or included) in a precedent cluster, it's not useful to compute the current itemset, th funciton returns false
     *
     * @param clusterMatrix      containing the reduced database, only contains the transactions of the current cluster
     * @param currentClusterSize size of the current cluster
     * @param headClusters       contains all the clusters preceding the current one ({@link SortedSet#headSet(Object)})
     * @return all the TransactionSets wich need to be ignored or false if the transactions of the current cluster are or are a part of a cluster present in headClusters
     */
    private HashSet<HashSet<Integer>> doneTransactionHashs(ClusterMatrix clusterMatrix, SortedSet<Integer> headClusters, final int currentClusterSize) {
        HashSet<HashSet<Integer>> headClustersTransactions = new HashSet<>(headClusters.size());
        for (int clusterId : headClusters) {
            HashSet<Integer> clusterTransactions = clusterMatrix.getTransactionIds(clusterId);

            if (clusterTransactions.size() == currentClusterSize) {
                return null;
            }

            headClustersTransactions.add(clusterTransactions);
        }
        return headClustersTransactions;
    }
}
