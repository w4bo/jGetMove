package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Object contenant l'ensemble des clusters dans lequel il est présent.
 */
public class Transaction implements Comparable<Transaction>, PrettyPrint {
    /**
     * id de la transaction
     */
    private int id;

    /**
     * hashmap contenant (idCluster => Cluster)
     */
    private HashMap<Integer, Cluster> clusters;
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
     * @param clusters set of cluster to add
     */
    public Transaction(int id, HashMap<Integer, Cluster> clusters) {
        this.id = id;
        this.clusters = clusters;
    }

    /**
     * @param cluster ajoute le cluster à la transaction
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
     * @return the Cluster HashMap
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * @return the Clusters' id as a TreeSet
     */
    public TreeSet<Integer> getClusterIds() {
        return clusterIds;
    }

    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(clusterIds) + "}";
    }

    @Override
    public String toPrettyString() {
        return "\n. " + id +
                "\n`-- Clusters : " + String.valueOf(clusterIds);
    }

    @Override
    public int compareTo(Transaction transaction) {
        return id - transaction.id;
    }
}
