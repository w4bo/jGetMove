/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Class representing a Transaction.
 * <p>
 * Contains all the clusters containing the Transaction.
 *
 * @author stardisblue
 * @author jframos0
 * @author Carmona-Anthony
 * @version 1.0.0
 * @implSpec Contains a {@link HashMap} representing the clusters as well as a {@link TreeSet} ordering all the clusters' id.
 * @since 0.1.0
 */
public class Transaction implements Comparable<Transaction>, PrettyPrint {
    /**
     * identifier
     */
    private int id;

    /**
     * Contains all the clusters of the transaction.
     */
    private HashMap<Integer, Cluster> clusters;
    /**
     * Contains all the clusters' id of the transaction.
     */
    private TreeSet<Integer> clusterIds;

    /**
     * @param id transaction identifier
     */
    public Transaction(int id) {
        this.id = id;
        this.clusters = new HashMap<>();
        this.clusterIds = new TreeSet<>();
    }

    /**
     * @param id       transaction identifier
     * @param clusters array of clusters
     */
    public Transaction(int id, Cluster[] clusters) {
        this.id = id;

        for (Cluster cluster : clusters) {
            add(cluster);
        }
    }

    /**
     * @param id       transaction identifier
     * @param clusters clusters to add
     */
    public Transaction(int id, HashMap<Integer, Cluster> clusters) {
        this.id = id;
        this.clusters = clusters;
    }

    /**
     * Adds to cluster to this transaction.
     *
     * @param cluster cluster to add
     */
    public void add(Cluster cluster) {
        clusters.put(cluster.getId(), cluster);
        clusterIds.add(cluster.getId());
    }

    /**
     * @return id of the transaction
     */
    public int getId() {
        return id;
    }

    /**
     * @return the clusters containing this transaction as a {@link HashMap}
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * @return the clusters' id as a {@link TreeSet}
     */
    public TreeSet<Integer> getClusterIds() {
        return clusterIds;
    }

    @Override
    public int compareTo(Transaction transaction) {
        return id - transaction.id;
    }

    @Override
    public String toPrettyString() {
        return "\n. " + id +
                "\n`-- Clusters : " + String.valueOf(clusterIds);
    }

    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(clusterIds) + "}";
    }
}
