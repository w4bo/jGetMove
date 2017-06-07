package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.Set;

/**
 * Objet Time contenant la liste des clusters pour un temps donné
 */
public class Time implements Comparable<Time> {
    private int id;

    private Block bloc;
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
    public int getId() {
        return id;
    }


    /**
     * @return le bloc associé au Time
     */
    public Block getBloc() {
        return bloc;
    }

    /**
     * @param bloc set le bloc associé au time
     */
    public void setBloc(Block bloc) {
        this.bloc = bloc;
    }

    /**
     * @return ensemble des cluster
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * @param clusters assigne un tableau de cluster à la hashmap
     */
    void setClusters(HashMap<Integer, Cluster> clusters) {
        this.clusters = clusters;
    }

    public Set<Integer> getClusterIds() {
        return getClusters().keySet();
    }


    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(clusters.keySet()) + "}";
    }

    public int compareTo(Time time) {

        //ascending order
        return id - time.id;

        //descending order
        //return compareQuantity - this.quantity;

    }
}
