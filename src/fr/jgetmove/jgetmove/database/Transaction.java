package fr.jgetmove.jgetmove.database;

import java.util.HashMap;

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
    }

    /**
     * @param id       identifiant de la transaction
     * @param clusters ensemble des clusters de la transaction
     */
    Transaction(int id, Cluster[] clusters) {
        this.id = id;

        for (Cluster cluster : clusters) {
            this.clusters.put(cluster.getId(), cluster);
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
     * @return l'ensemble des clusters de la transaction
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * @return id de la transaction
     */
    public int getId() {
        return id;
    }
}
