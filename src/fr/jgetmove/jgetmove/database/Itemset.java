package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;
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
    public String toString() {
        return Debug.indent(toPrettyString());
    }

    @Override
    public String toPrettyString() {
        return "\n. id : " + id +
                "\n|-- Clusters : " + clusters +
                "\n|-- Transactions : " + transactions +
                "\n`-- Times : " + times;
    }


    @Override
    public int compareTo(Itemset p) {
        if (clusters.equals(p.clusters) && transactions.equals(p.transactions)) {
            return 0;
        }
        Iterator<Integer> clusterIt = clusters.iterator();
        Iterator<Integer> pClusterIt = p.clusters.iterator();
        while (clusterIt.hasNext() && pClusterIt.hasNext()) {
            int clusterId = clusterIt.next();
            int pClusterId = pClusterIt.next();
            if (clusterId == pClusterId) {
                continue;
            }

            return clusterId - pClusterId;
        }

        if (clusterIt.hasNext()) {
            return 1;
        } else if (pClusterIt.hasNext()) {
            return -1;
        }

        Iterator<Integer> transactionIt = transactions.iterator();
        Iterator<Integer> pTransactionIt = p.transactions.iterator();
        while (transactionIt.hasNext() && pTransactionIt.hasNext()) {
            int transactionId = transactionIt.next();
            int pTransactionId = pTransactionIt.next();
            if (transactionId == pTransactionId) {
                continue;
            }

            return transactionId - pTransactionId;
        }

        if (transactionIt.hasNext()) {
            return 1;
        } else if (pTransactionIt.hasNext()) {
            return -1;
        }

        return 0;
    }
}
