package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Object contenant l'ensemble des clusters dans lequel il est présent.
 */
public class Transaction implements Comparable<Transaction> {
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
     * @param id identifiant de la transaction
     */
    public Transaction(int id) {
        this.id = id;
        this.clusters = new HashMap<>();
        this.clusterIds = new TreeSet<>();
    }

    /**
     * @param id       identifiant de la transaction
     * @param clusters ensemble des clusters de la transaction
     */
    public Transaction(int id, Cluster[] clusters) {
        this.id = id;

        for (Cluster cluster : clusters) {
            add(cluster);
        }
    }

    /**
     * @param id       identifiant de la transaction
     * @param clusters ensemble des clusters de la transaction
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
     * @return id de la transaction
     */
    public int getId() {
        return id;
    }

    /**
     * Setter on id
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return l'ensemble des clusters de la transaction
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    public Cluster getCluster(int clusterId) {
        return clusters.get(clusterId);
    }

    public Set<Integer> getClusterIds() {
        return clusterIds;
    }

    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(clusterIds) + "}";
    }

    public void clear() {
        clusters.clear();
    }

    @Override
    public int compareTo(Transaction transaction) {
        return id - transaction.id;
    }
}
