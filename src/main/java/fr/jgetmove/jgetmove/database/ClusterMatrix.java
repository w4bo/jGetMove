/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.*;

/**
 * Used as a dynamic partial database.
 * Holds a {@link Cluster}-{@link Time} matrix and a {@link Cluster}-{@link Transaction} matrix.
 *
 * @author stardisblue
 * @version 1.1.0
 * @since 0.2.0
 */
public class ClusterMatrix implements PrettyPrint {

    /**
     * Used clusters by the clusterMatrix
     */
    private SortedSet<Integer> clusterIds;
    private HashMap<Integer, Integer> clusterTimeMatrix;
    private HashMap<Integer, HashSet<Integer>> clusterTransactionsMatrix;


    /**
     * Initializes the cluster by taking the relations of the base as a reference
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
     * @param clusterId cluster's identifier
     * @return the time id of the given cluster
     */
    public int getTimeId(int clusterId) {
        return clusterTimeMatrix.get(clusterId);
    }

    /**
     * @param clusterId cluster's identifier
     * @return returns all the transactions of a cluster as a {@link HashSet}
     */
    public HashSet<Integer> getTransactionIds(int clusterId) {
        return clusterTransactionsMatrix.get(clusterId);
    }

    /**
     * @return all the clusters' id of the matrix
     */
    public SortedSet<Integer> getClusterIds() {
        return clusterIds;
    }

    /**
     * Rebinds the cluster-transaction matrix by removing the transactions not present in transactionIds and adding the one which are
     * <p>
     * {@code Lcm::UpdateOccurenceDeriver(const Database &database, const vector<int> &transactionList, OccurenceDeriver &occurence);}
     *
     * @param base           Reference base to retrieve the bindings
     * @param transactionIds Transactions which need to be present in the matrix
     * @implSpec complexity : <code>t&times;c</code> (worst case), otherwise <code>t&times;log(c)</code>(t transactionIds, c clusters of transactions).
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

    /**
     * @return A string representing the object
     * @implSpec indents the output of {@link #toPrettyString()}
     */
    @Override
    public String toString() {
        return Debug.indent(toPrettyString());
    }
}
