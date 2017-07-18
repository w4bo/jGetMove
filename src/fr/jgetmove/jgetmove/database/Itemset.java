package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
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

        Iterator<Integer> transactionIt = transactions.iterator();
        Iterator<Integer> pTransactionIt = itemset.transactions.iterator();

        while (transactionIt.hasNext() && pTransactionIt.hasNext()) {
            int transactionId = transactionIt.next();
            int pTransactionId = pTransactionIt.next();

            if (transactionId != pTransactionId) {
                return transactionId - pTransactionId;
            }
        }

        if (transactionIt.hasNext()) {
            return 1;
        } else if (pTransactionIt.hasNext()) {
            return -1;
        }

        Iterator<Integer> clusterIt = clusters.iterator();
        Iterator<Integer> pClusterIt = itemset.clusters.iterator();

        while (clusterIt.hasNext() && pClusterIt.hasNext()) {
            int clusterId = clusterIt.next();
            int pClusterId = pClusterIt.next();

            if (clusterId != pClusterId) {
                return clusterId - pClusterId;
            }
        }

        if (clusterIt.hasNext()) {
            return 1;
        } else if (pClusterIt.hasNext()) {
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

        return (clusters.equals(i.clusters) && transactions.equals(i.transactions));
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
