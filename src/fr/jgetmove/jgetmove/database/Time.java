package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.Set;

/**
 * Represents a fixed time in the timeline, contains a set of clusters, contained in a block and has a unique identifier
 * <p>
 * Time is managed by DataBase
 */
public class Time implements Comparable<Time> {
    private int id;

    /**
     * The block in which the time is, can be null
     */
    private BlockBase bloc;
    /**
     * Hashmap containing the list of clusters (idCluster -> Cluster)
     * Hashmap contenant la liste des clusters idCluster=>Cluster
     */
    private HashMap<Integer, Cluster> clusters;

    /**
     * @param id identifier
     */
    public Time(int id) {
        this.id = id;
        clusters = new HashMap<>();
    }

    /**
     * @param cluster adds the cluster to the time
     */
    void add(Cluster cluster) {
        this.clusters.put(cluster.getId(), cluster);
    }

    /**
     * @return id identifier
     */
    public int getId() {
        return id;
    }


    /**
     * @return the <tt>BlockBase</tt> which contains this <tt>Time</tt>
     */
    public BlockBase getBloc() {
        return bloc;
    }

    /**
     * @param bloc sets the containing <tt>BlockBase</tt>
     */
    public void setBloc(BlockBase bloc) {
        this.bloc = bloc;
    }

    /**
     * @return the Hashmap of clusters which are contained in this time
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * Sets and replaces the current clusters by the given one
     *
     * @param clusters new clusters of the time
     */
    void setClusters(HashMap<Integer, Cluster> clusters) {
        this.clusters = clusters;
    }

    /**
     * @return only returns the clusters' id contained in this time
     */
    public Set<Integer> getClusterIds() {
        return getClusters().keySet();
    }


    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(clusters.keySet()) + "}";
    }

    @Override
    public int compareTo(Time time) {
        return id - time.id;
    }
}
