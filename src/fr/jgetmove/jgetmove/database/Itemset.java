package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.ArrayList;
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

    Itemset(final int id) {
        this.id = id;
        clusters = new TreeSet<>();
        transactions = new TreeSet<>();
        times = new TreeSet<>();
    }

    public Itemset(final int id, final Set<Integer> transactions, final Set<Integer> clusters, final Set<Integer> times) {
        this.id = id;
        this.clusters = new TreeSet<>(clusters);
        this.transactions = new TreeSet<>(transactions);
        this.times = new TreeSet<>(times);
    }

    public void add(final int clusterId, final int timeId, final ArrayList<Integer> transactions) {
        if (timeId > times.last()) {
            this.transactions = new TreeSet<>(transactions);
        }

        times.add(timeId);
        clusters.add(clusterId);
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
        if (clusters.equals(p.clusters) && transactions.equals(p.transactions) && times.equals(p.times)) {
            return 0;
        }

        return id - p.id;
    }
}
