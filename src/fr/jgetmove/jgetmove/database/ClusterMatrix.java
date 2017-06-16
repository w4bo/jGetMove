package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Used in ItemsetDetector, holds a {@link Cluster}-{@link Time} matrix and a {@link Cluster}-{@link Transaction} matrix
 */
public class ClusterMatrix {

    private HashMap<Integer, Integer> clusterTimeMatrix;
    private HashMap<Integer, TreeSet<Integer>> clusterTransactionsMatrix;


    /**
     * Initializes the cluster by tacking the relations of the database as a reference
     *
     * @param database Initial matrix reference
     */
    public ClusterMatrix(Database database) {
        clusterTimeMatrix = new HashMap<>();
        clusterTransactionsMatrix = new HashMap<>();

        for (Transaction transaction : database.getTransactions().values()) {
            for (int clusterId : transaction.getClusterIds()) {

                if (!clusterTransactionsMatrix.containsKey(clusterId)) {
                    clusterTransactionsMatrix.put(clusterId, new TreeSet<>());
                    clusterTimeMatrix.put(clusterId, database.getClusterTimeId(clusterId));
                }

                clusterTransactionsMatrix.get(clusterId).add(transaction.getId());
            }
        }
    }

    /**
     * @param clusterId the identifier of the cluster
     * @return the time id of the given cluster
     */
    public int getClusterTimeId(int clusterId) {
        return clusterTimeMatrix.get(clusterId);
    }

    /**
     * @param clusterId cluster's identifier
     * @return returns all the transactions of a cluster as a treeset
     */
    public TreeSet<Integer> getClusterTransactionIds(int clusterId) {
        return clusterTransactionsMatrix.get(clusterId);
    }

    /**
     * Rebinds the cluster-transaction matrix by removing the transactions not present in transactionIds and adding the one which are
     *
     * @param database       Reference database to retrieve the bindings
     * @param transactionIds Transactions which need to be present in the matrix
     */
    public void optimizeMatrix(Database database, Set<Integer> transactionIds) {
        clusterTransactionsMatrix.forEach((clusterId, transactions) -> transactions.clear());
        for (int transactionId : transactionIds) {
            Transaction transaction = database.getTransaction(transactionId);
            Set<Integer> clusterIds = transaction.getClusterIds();

            for (int clusterId : clusterIds) {
                if (!clusterTransactionsMatrix.containsKey(clusterId)) {
                    clusterTransactionsMatrix.put(clusterId, new TreeSet<>());
                    clusterTimeMatrix.put(clusterId, database.getClusterTimeId(clusterId));
                }

                clusterTransactionsMatrix.get(clusterId).add(transactionId);
            }
        }
    }

    @Override
    public String toString() {
        String str = "\n|-- ClusterMatrix :" + clusterTransactionsMatrix;
        str += "\n`-- TimeMatrix :" + clusterTimeMatrix.values();
        return str;
    }

}
