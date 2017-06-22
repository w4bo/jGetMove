package fr.jgetmove.jgetmove.database;

import java.util.HashMap;

/**
 * Groups a set of clusters in a specific time
 */
public class Cluster {

    private int id;

    /**
     * HashMap of all the transactions of the cluster idTransaction => {@link Transaction}
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
     * @param transaction to add to the cluster
     */
    public void add(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    /**
     * @return cluster identifier
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
     * @return the cluster identifier
     */
    public int getTimeId() {
        return time.getId();
    }

    /**
     * @return transaction transaction in the cluster (HashMap [idTransaction => Transaction])
     */
    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(transactions.keySet()) + "}";
    }

}
