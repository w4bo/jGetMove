package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.Set;

/**
 * Object contenant l'ensemble des clusters dans lequel il est présent.
 */
public class Transaction {
    /**
     * id de la transaction
     */
    private int id;

    /**
     * hashmap contenant (idCluster => Cluster)
     */
    private HashMap<Integer, Cluster> clusters;

    /**
     * @param id identifiant de la transaction
     */
    Transaction(int id) {
        this.id = id;
        this.clusters = new HashMap<>();
    }

    /**
     * @param id       identifiant de la transaction
     * @param clusters ensemble des clusters de la transaction
     */
    Transaction(int id, Cluster[] clusters) {
        this.id = id;

        for (Cluster cluster : clusters) {
            add(cluster);
        }
    }

    /**
     * @param id       identifiant de la transaction
     * @param clusters ensemble des clusters de la transaction
     */
    Transaction(int id, HashMap<Integer, Cluster> clusters) {
        this.id = id;
        this.clusters = clusters;
    }

    /**
     * @param cluster ajoute le cluster à la transaction
     */
    public void add(Cluster cluster) {
        clusters.put(cluster.getId(), cluster);
    }

    /**
     * @return id de la transaction
     */
    public int getId() {
        return id;
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
        return getClusters().keySet();
    }

    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(clusters.keySet()) + "}";
    }
}
