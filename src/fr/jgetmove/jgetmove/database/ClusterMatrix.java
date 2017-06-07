package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class ClusterMatrix {

    private HashMap<Integer, Integer> clusterTimeMatrix;
    private HashMap<Integer, TreeSet<Integer>> clusterTransactionsMatrix;

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
                if (!clusterTransactionsMatrix.containsKey(clusterId)) {
                    clusterTransactionsMatrix.put(clusterId, new TreeSet<>());
                    clusterTimeMatrix.put(clusterId, defaultDatabase.getClusterTimeId(clusterId));
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
