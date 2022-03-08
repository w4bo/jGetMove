/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.HashMap;

/**
 * Class representing a cluster.
 * <p>
 * Contains a set of transactions and the time in which the cluster is located
 *
 * @author stardisblue
 * @author jframos0
 * @author Carmona-Anthony
 * @version 1.0.0
 * @since 0.1.0
 */
public class Cluster implements PrettyPrint {

    private int id;

    /**
     * HashMap representing all the transactions of the cluster.
     */
    private HashMap<Integer, Transaction> transactions;

    /**
     * The {@link Time} where the cluster is
     */
    private Time time;

    /**
     * Initializes the cluster  with it's identifier
     *
     * @param id cluster's identifier
     */
    public Cluster(int id) {
        this.id = id;
        transactions = new HashMap<>();
    }

    /**
     * Adds the transaction to the cluster
     *
     * @param transaction to add
     */
    public void add(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    /**
     * @return cluster's identifier
     */
    public int getId() {
        return id;
    }

    /**
     * @return returns the {@link Time} of the cluster
     */
    public Time getTime() {
        return time;
    }

    /**
     * @param time sets the time of the cluster
     */
    public void setTime(Time time) {
        this.time = time;
    }

    /**
     * @return the time's id of the cluster
     */
    public int getTimeId() {
        return time.getId();
    }

    /**
     * @return all the transactions of the cluster as a {@code HashMap<transactionId, Transaction>}
     */
    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toPrettyString() {
        return "\n. " + id +
                "\n|-- Time : " + time.getId() +
                "\n`-- Transactions : " + String.valueOf(transactions.keySet());
    }

    /**
     * @return a string representing the cluster
     */
    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(transactions.keySet()) + "}";
    }
}
