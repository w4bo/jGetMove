package fr.jgetmove.jgetmove.database;

import java.util.HashMap;

/**
 * Objet Time contenant la liste des clusters pour un temps donné
 */
public class Time {
    private int id;
    /**
     * Hashmap contenant la liste des clusters idCluster=>Cluster
     */
    private HashMap<Integer, Cluster> clusters;

    /**
     * @param id identifiant du temps
     */
    Time(int id) {
        this.id = id;
        clusters = new HashMap<>();
    }

    /**
     * @param cluster ajoute une cluster à la liste des clusters
     */
    void add(Cluster cluster) {
        this.clusters.put(cluster.getId(), cluster);
    }

    /**
     * @return id du temps
     */
    int getId() {
        return id;
    }

    /**
     * @return ensemble des cluster
     */
    HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * @param clusters assigne un tableau de cluster à la hashmap
     */
    void setClusters(HashMap<Integer, Cluster> clusters) {
        this.clusters = clusters;
    }


    @Override
    public String toString() {
        return String.valueOf(clusters.keySet());
    }
}
