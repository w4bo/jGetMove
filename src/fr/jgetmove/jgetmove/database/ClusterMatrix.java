package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class ClusterMatrix {

    HashMap<Integer, Integer> clusterTimeMatrix;
    HashMap<Integer, TreeSet<Integer>> clusterTransactionsMatrix;

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
     * @param clusterId identifiant du cluster
     * @return le cluster ayant le clusterId
     */
    public TreeSet<Integer> getCluster(int clusterId) {
        return clusterTransactionsMatrix.get(clusterId);
    }

    public int getClusterTimeId(int clusterId) {
        return clusterTimeMatrix.get(clusterId);
    }

    /**
     * @param clusterId identifiant du cluster
     * @return l'ensemble des transactions du cluster
     */
    public TreeSet<Integer> getClusterTransactionIds(int clusterId) {
        return clusterTransactionsMatrix.get(clusterId);
    }

    public void optimizeMatrix(Database defaultDatabase, Set<Integer> transactionIds) {
        for (int transactionId : transactionIds) {
            Transaction transaction = defaultDatabase.getTransaction(transactionId);
            Set<Integer> clusterIds = transaction.getClusterIds();

            for (int clusterId : clusterIds) {
                TreeSet<Integer> transactions = this.getCluster(clusterId);

                if (transactions == null) {
                    clusterTransactionsMatrix.put(clusterId, new TreeSet<>());
                    clusterTimeMatrix.put(clusterId, defaultDatabase.getClusterTimeId(clusterId));
                }
                clusterTransactionsMatrix.get(clusterId).add(transactionId);
            }
        }
    }
}
