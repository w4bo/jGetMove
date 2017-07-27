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
