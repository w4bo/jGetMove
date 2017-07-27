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

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Class representing an itemset
 * <p>
 * Contains a set of clusters, transactions and times.
 *
 * @author stardisblue
 * @author jframos0
 * @author Carmona-Anthony
 * @version 1.2.0
 * @implSpec All of the attributes are stored as {@link TreeSet}
 * @since 0.1.0
 */
public class Itemset implements Comparable<Itemset>, PrettyPrint {

    /**
     * All the clusters' id of the itemset.
     */
    private TreeSet<Integer> clusters;
    /**
     * The transactions' id of the itemset
     */
    private TreeSet<Integer> transactions;
    /**
     * The times' id of the itemset
     */
    private TreeSet<Integer> times;

    /**
     * Default constructer.
     *
     * @param transactions set containing the transactions of the itemset
     * @param clusters     set containing the clusters of the itemset
     * @param times        set containing the times of the itemset
     * @implSpec all the sets are copied when added to the itemset
     */
    public Itemset(Collection<Integer> transactions, Collection<Integer> clusters, Collection<Integer> times) {
        this.clusters = new TreeSet<>(clusters);
        this.transactions = new TreeSet<>(transactions);
        this.times = new TreeSet<>(times);
    }

    /**
     * @return all the clusters of the itemset as a {@link TreeSet}
     */
    public TreeSet<Integer> getClusters() {
        return clusters;
    }

    /**
     * @return all the transactions of the itemset as a {@link TreeSet}
     */
    public TreeSet<Integer> getTransactions() {
        return transactions;
    }

    /**
     * @return all the times of the itemset as a {@link TreeSet}
     */
    public TreeSet<Integer> getTimes() {
        return times;
    }

    @Override
    public int compareTo(Itemset itemset) {
        if (clusters.equals(itemset.clusters) && transactions.equals(itemset.transactions)) {
            return 0;
        }

        int transactionComparator = compareIterators(transactions.iterator(), itemset.transactions.iterator());
        if (transactionComparator != 0) return transactionComparator;

        int clusterComparator = compareIterators(clusters.iterator(), itemset.clusters.iterator());
        if (clusterComparator != 0) return clusterComparator;

        return 0;
    }

    /**
     * Compares two iterators.
     * <p>
     * Iterates simultaneously over the iterators and returns 1 if the first has a higher value or -1 if the second one has. If all the values are equals. Checks if the first is longer. Otherwise, returns null.
     *
     * @param first  iterator
     * @param second iterator
     * @return return 1 if the first has a higher value than the second or if is longer and all values are equals. returns -1 if the second has a higher value or if is longer and all the precedent values are equal. returns 0 otherwise.
     */
    private int compareIterators(Iterator<Integer> first, Iterator<Integer> second) {
        while (first.hasNext() && second.hasNext()) {
            int transactionId = first.next();
            int pTransactionId = second.next();

            if (transactionId != pTransactionId) {
                return transactionId - pTransactionId;
            }
        }

        if (first.hasNext()) {
            return 1;
        } else if (second.hasNext()) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Itemset))
            return false;


        Itemset i = (Itemset) o;

        return clusters.equals(i.clusters) && transactions.equals(i.transactions);
    }


    @Override
    public String toPrettyString() {
        return "\n\t|-- Clusters : " + clusters +
                "\n\t|-- Transactions : " + transactions +
                "\n\t`-- Times : " + times;
    }

    /**
     * @return a string representing the object.
     * @implSpec uses {@link #toPrettyString()}
     */
    @Override
    public String toString() {
        return toPrettyString();
    }
}
