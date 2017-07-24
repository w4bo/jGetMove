package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @since 0.1.0
 * @version 1.2.0
 */
public class Itemset implements Comparable<Itemset>, PrettyPrint {

    private final int id;
    private TreeSet<Integer> clusters;
    private TreeSet<Integer> transactions;
    private TreeSet<Integer> times;

    public Itemset(final int id, final Set<Integer> transactions, final Set<Integer> clusters, final Set<Integer> times) {
        this.id = id;
        this.clusters = new TreeSet<>(clusters);
        this.transactions = new TreeSet<>(transactions);
        this.times = new TreeSet<>(times);
    }

    public TreeSet<Integer> getClusters() {
        return clusters;
    }

    public TreeSet<Integer> getTransactions() {
        return transactions;
    }

    public TreeSet<Integer> getTimes() {
        return times;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Itemset itemset) {
        if (clusters.equals(itemset.clusters) && transactions.equals(itemset.transactions)) {
            return 0;
        }

        Integer transactionComparator = compareIterators(transactions.iterator(), itemset.transactions.iterator());
        if (transactionComparator != null) return transactionComparator;

        Integer clusterComparator = compareIterators(clusters.iterator(), itemset.clusters.iterator());
        if (clusterComparator != null) return clusterComparator;

        return 0;
    }

    private Integer compareIterators(Iterator<Integer> first, Iterator<Integer> second) {
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
        return null;
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
        return "\n. id : " + id +
                "\n\t|-- Clusters : " + clusters +
                "\n\t|-- Transactions : " + transactions +
                "\n\t`-- Times : " + times;
    }

    @Override
    public String toString() {
        return toPrettyString();
    }
}
