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
    public int compareTo(Itemset p) {
        if (clusters.equals(p.clusters) && transactions.equals(p.transactions)) {
            return 0;
        }

        //  if (clusters.size() == p.clusters.size()) {
        Iterator<Integer> iterator = clusters.iterator();
        Iterator<Integer> pIterator = p.clusters.iterator();

        while (iterator.hasNext() && pIterator.hasNext()) {
            int clusterId = iterator.next();
            int pClusterId = pIterator.next();

            if (clusterId != pClusterId) {
                return clusterId - pClusterId;
            }
        }

        if (iterator.hasNext()) {
            return 1;
        } else if (pIterator.hasNext()) {
            return 0;
        }

        Iterator<Integer> transactionIt = transactions.iterator();
        Iterator<Integer> pTransactionIt = p.transactions.iterator();

        while (transactionIt.hasNext() && pTransactionIt.hasNext()) {
            int transactionId = transactionIt.next();
            int pTransactionId = pTransactionIt.next();

            if (transactionId != pTransactionId) {
                return transactionId - pTransactionId;
            }
        }

        if (transactionIt.hasNext()) {
            return 1;
        } else if (pIterator.hasNext()) {
            return 0;
        }

        return 0;
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
