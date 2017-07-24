/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.HashMap;
import java.util.Set;

/**
 * Represents a fixed time in the timeline, contains a set of clusters, contained in a block and has a unique identifier
 * <p>
 * Time is managed by DataBase
 *
 * @version 1.0.0
 * @since 0.1.0
 */
public class Time implements Comparable<Time>, PrettyPrint {
    private final int id;

    /**
     * Hashmap containing the list of clusters (idCluster -> Cluster)
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
