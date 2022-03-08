/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.HashMap;
import java.util.Set;

/**
 * Represents a fixed time in the timeline.
 * <p>
 * Contains a set of clusters, contained in a block and has a unique identifier
 *
 * @author stardisblue
 * @author jframos0
 * @author Carmona-Anthony
 * @version 1.0.0
 * @since 0.1.0
 */
public class Time implements Comparable<Time>, PrettyPrint {
    private final int id;

    /**
     * HashMap representing all the clusters
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
     * @return the Hashmap of clusters which are contained in this time
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * Sets or replaces the current clusters by the given one
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
    public int compareTo(Time time) {
        return id - time.id;
    }

    @Override
    public String toPrettyString() {
        return "\n. " + id +
                "\n`-- Clusters : " + String.valueOf(clusters.keySet());
    }

    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(clusters.keySet()) + "}";
    }
}
