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
import java.util.TreeSet;

/**
 * Object contenant l'ensemble des clusters dans lequel il est présent.
 *
 * @version 1.0.0
 * @since 0.1.0
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
