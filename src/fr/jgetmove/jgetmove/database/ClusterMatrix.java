package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.*;

/**
 * Used in ItemsetDetector, holds a {@link Cluster}-{@link Time} matrix and a {@link Cluster}-{@link Transaction} matrix
 */
public class ClusterMatrix implements PrettyPrint {

    private SortedSet<Integer> clusterIds;
    private HashMap<Integer, Integer> clusterTimeMatrix;
    private HashMap<Integer, HashSet<Integer>> clusterTransactionsMatrix;


    /**
     * Initializes the cluster by tacking the relations of the block as a reference
     *
     * @param base Initial base reference
     */
    public ClusterMatrix(Base base) {
        clusterIds = new TreeSet<>();
        clusterTimeMatrix = new HashMap<>(base.getClusterIds().size());
        clusterTransactionsMatrix = new HashMap<>(base.getClusterIds().size());

        for (Transaction transaction : base.getTransactions().values()) {
            for (int clusterId : transaction.getClusterIds()) {
                if (!clusterTransactionsMatrix.containsKey(clusterId)) {
                    clusterIds.add(clusterId);
                    clusterTransactionsMatrix.put(clusterId, new HashSet<>());
                    clusterTimeMatrix.put(clusterId, base.getClusterTimeId(clusterId));
                }

                clusterTransactionsMatrix.get(clusterId).add(transaction.getId());
            }
        }
    }

    /**
     * @param clusterId the identifier of the cluster
     * @return the time id of the given cluster
     */
    public int getTimeId(int clusterId) {
        return clusterTimeMatrix.get(clusterId);
    }

    /**
     * @param clusterId cluster's identifier
     * @return returns all the transactions of a cluster as a treeset
     */
    public HashSet<Integer> getTransactionIds(int clusterId) {
        return clusterTransactionsMatrix.get(clusterId);
    }

    public SortedSet<Integer> getClusterIds() {
        return clusterIds;
    }

    /**
     * Rebinds the cluster-transaction matrix by removing the transactions not present in transactionIds and adding the one which are
     * <p>
     * <pre>
     * Lcm::UpdateOccurenceDeriver(const DataBase &database, const vector<int> &transactionList, ClusterMatrix &occurence)
     * </pre>
     *
     * @param base           Reference database to retrieve the bindings
     * @param transactionIds Transactions which need to be present in the matrix
     */
    public void optimizeMatrix(Base base, Set<Integer> transactionIds) {
        clusterTransactionsMatrix.clear();
        clusterIds.clear();
        for (int transactionId : transactionIds) {
            Transaction transaction = base.getTransaction(transactionId);
            TreeSet<Integer> clusterIds = transaction.getClusterIds();
            for (int clusterId : clusterIds) {
                if (!clusterTransactionsMatrix.containsKey(clusterId)) {
                    this.clusterIds.add(clusterId);
                    clusterTransactionsMatrix.put(clusterId, new HashSet<>());
                    clusterTimeMatrix.put(clusterId, base.getClusterTimeId(clusterId));
                }

                clusterTransactionsMatrix.get(clusterId).add(transactionId);
            }
        }
    }

    @Override
    public String toPrettyString() {
        return "\n|-- ClusterMatrix :" + clusterTransactionsMatrix +
                "\n`-- TimeMatrix :" + clusterTimeMatrix;
    }

    @Override
    public String toString() {
        return Debug.indent(toPrettyString());
    }
}
