/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.*;
import java.util.Map.Entry;

/**
 * Class containing all the basic data.
 * <p>
 * All the data is binded once, note that adding or rebinding data in here is expensive. Use {@link ClusterMatrix} for a dynamic database.
 *
 * @author stardisblue
 * @version 1.1.0
 * @see ClusterMatrix
 * @since 0.2.0
 */
public class Base implements PrettyPrint {

    /**
     * Hosts all the Times : {@code HashMap<timeId,Time>}
     */
    protected HashMap<Integer, Time> times;
    /**
     * Hosts all the Clusters : {@code HashMap<clusterId,Cluster>}
     */
    protected HashMap<Integer, Cluster> clusters;
    /**
     * Hosts all the Transactions : {@code HashMap<transactionId,Transaction>}
     */
    protected HashMap<Integer, Transaction> transactions;

    /**
     * Hosts all the times' id in a {@link TreeSet}
     */
    private TreeSet<Integer> timeIds;
    /**
     * Hosts all the clusters' id in a {@link TreeSet}
     */
    private TreeSet<Integer> clusterIds;

    /**
     * Hosts all the transactions' id in a {@link TreeSet}
     */
    private TreeSet<Integer> transactionIds;

    /**
     * Initializes an empty Base
     */
    public Base() {
        clusters = new HashMap<>();
        transactions = new HashMap<>();
        times = new HashMap<>();

        clusterIds = new TreeSet<>();
        transactionIds = new TreeSet<>();
        timeIds = new TreeSet<>();
    }

    /**
     * Fills and binds the Base using the parameters
     *
     * @param clustersTransactions link between clusters and transactions 1:x relation
     * @param clustersTime         link between clusters and its time 1:1 relation
     */
    public Base(HashMap<Integer, Set<Integer>> clustersTransactions, HashMap<Integer, Integer> clustersTime) {
        this();

        for (Entry<Integer, Set<Integer>> entry : clustersTransactions.entrySet()) {
            Cluster cluster = getOrCreateCluster(entry.getKey());
            for (int transactionId : entry.getValue()) {
                Transaction transaction = getOrCreateTransaction(transactionId);

                cluster.add(transaction);
                transaction.add(cluster);
            }
        }

        for (Entry<Integer, Integer> entry : clustersTime.entrySet()) {
            Cluster cluster = getOrCreateCluster(entry.getKey());
            Time time = getOrCreateTime(entry.getValue());

            cluster.setTime(time);
            time.add(cluster);
        }
    }

    /**
     * Adds the cluster to the base
     *
     * @param cluster cluster to add
     */
    public void add(Cluster cluster) {
        clusters.put(cluster.getId(), cluster);
        clusterIds.add(cluster.getId());
    }

    /**
     * Adds the transaction to the base
     *
     * @param transaction transaction to add
     */
    public void add(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        transactionIds.add(transaction.getId());
    }

    /**
     * Adds the time to the base
     *
     * @param time time to add
     */
    public void add(Time time) {
        times.put(time.getId(), time);
        timeIds.add(time.getId());
    }

    /**
     * Checks whether the clusterId is present, if not creates  one. Returns the cluster found/created.
     *
     * @param clusterId id to check
     * @return created or found cluster.
     */
    public Cluster getOrCreateCluster(final int clusterId) {
        Cluster cluster = this.getCluster(clusterId);

        if (cluster == null) {
            cluster = new Cluster(clusterId);
            this.add(cluster);
        }

        return cluster;
    }


    /**
     * Check whether the timeId is present, if not creates one. Returns the time found/created.
     *
     * @param timeId id to check
     * @return created or found time
     */
    public Time getOrCreateTime(final int timeId) {
        Time time = this.getTime(timeId);

        if (time == null) {
            time = new Time(timeId);
            this.add(time);
        }

        return time;
    }

    /**
     * Check whether the transactionId is present, if not creates one. Returns the transaction found/created.
     *
     * @param transactionId id to check
     * @return created or found transaction
     */
    public Transaction getOrCreateTransaction(final int transactionId) {
        Transaction transaction = this.getTransaction(transactionId);

        if (transaction == null) {
            transaction = new Transaction(transactionId);
            this.add(transaction);
        }
        return transaction;
    }

    /**
     * Returns the {@link Cluster} having this id, {@code null} otherwise
     *
     * @param clusterId id of the cluster
     * @return the corresponding {@link Cluster} or {@code null}
     * @see HashMap#get(Object)
     */
    public Cluster getCluster(final int clusterId) {
        return clusters.get(clusterId);
    }

    /**
     * Returns the {@link Time} having this id, {@code null} otherwise
     *
     * @param timeId id of the time
     * @return the corresponding {@link Time} or {@code null}
     * @see HashMap#get(Object)
     */
    public Time getTime(final int timeId) {
        return times.get(timeId);
    }

    /**
     * Returns the {@link Transaction} having this id, {@code null} otherwise
     *
     * @param transactionId id of the transaction
     * @return the corresponding {@link Transaction} or {@code null}
     * @see HashMap#get(Object)
     */
    public Transaction getTransaction(final int transactionId) {
        return transactions.get(transactionId);
    }

    /**
     * @return all the clusters of the base in form of a {@link HashMap}
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * @return all the times of the base in form of a {@link HashMap}
     */
    public HashMap<Integer, Time> getTimes() {
        return times;
    }

    /**
     * @return all the transactions of the base in form of a {@link HashMap}
     */
    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }


    /**
     * @return all the clusterIds as a {@link TreeSet}
     */
    public TreeSet<Integer> getClusterIds() {
        return clusterIds;
    }

    /**
     * @return all the timeIds as a {@link TreeSet}
     */
    public TreeSet<Integer> getTimeIds() {
        return timeIds;
    }

    /**
     * @return all the transactionIds as a {@link TreeSet}
     */
    public TreeSet<Integer> getTransactionIds() {
        return transactionIds;
    }


    /**
     * Retrieves all the transactions of a specific cluster.
     * <p>
     * Shortcut for <code>getCluster(clusterId).getTransactions()</code>
     *
     * @param clusterId id of the {@link Cluster}
     * @return all the transaction of the cluster as a {@link HashMap}
     */
    public HashMap<Integer, Transaction> getClusterTransactions(final int clusterId) {
        return getCluster(clusterId).getTransactions();
    }


    /**
     * Retrieves all the clusters of a specific transaction.
     * <p>
     * Shortcut for <code>getTransaction(transactionId).getClusters()</code>
     *
     * @param transactionId id of the {@link Transaction}
     * @return all the clusters of the transaction as a {@link HashMap}
     */
    public HashMap<Integer, Cluster> getTransactionClusters(final int transactionId) {
        return getTransaction(transactionId).getClusters();
    }

    /**
     * Checks and returns a list of transactions from <tt>transactionIds</tt> which are contained in the cluster (<tt>clusterId</tt>)
     *
     * @param transactionIds list of transactions to check from
     * @param clusterId      the cluster to filter them
     * @return a list of transactionIds
     * @implSpec iterates over the set and checks if the transaction is contained in the cluster using {@link #getClusterTransactions(int)}.{@link HashMap#containsKey(Object) containsKey(Object)} . If so, the transaction is added to the return set.
     */
    public Set<Integer> getFilteredTransactionIdsIfHaveCluster(final Set<Integer> transactionIds, final int clusterId) {
        Set<Integer> filteredTransactionIds = new HashSet<>();

        for (int transactionId : transactionIds) {
            if (this.getClusterTransactions(clusterId).containsKey(transactionId)) {
                filteredTransactionIds.add(transactionId);
            }
        }
        return filteredTransactionIds;
    }

    /**
     * Checks whether all the transactions are contained in the cluster.
     * <p>
     * Shortcut for {@link #getClusterTransactions(int) getClusterTransactions(clusterId)}.{@link HashMap#keySet() keySet()}.{@link Set#containsAll(Collection) containsAll(transactionIds)}
     * <p>
     * Dans GetMove :
     * <pre>
     * Lcm::CheckItemInclusion(DataBase,transactionList,item)
     * </pre>
     *
     * @param transactionIds (transactionList) set of transactionIds
     * @param clusterId      (item) the concerned cluster
     * @return <code>true</code> of the set is contained in the cluster
     */
    public boolean areTransactionsInCluster(final Set<Integer> transactionIds, final int clusterId) {
        return getClusterTransactions(clusterId).keySet().containsAll(transactionIds);
    }

    /**
     * Returns the timeId of the cluster
     * <p>
     * Shortcut for {@link #getCluster(int) getCluster(clusterId)}.{@link Cluster#getTimeId() getTimeId()}
     *
     * @param clusterId id of the cluster
     * @return the timeId of the cluster
     */
    public int getClusterTimeId(final int clusterId) {
        return getCluster(clusterId).getTimeId();
    }

    @Override
    public String toPrettyString() {
        return "\n|-- Clusters :" + String.valueOf(clusters.values()) +
                "\n|-- Transactions :" + String.valueOf(transactions.values()) +
                "\n`-- Times :" + String.valueOf(times.values());
    }

    /**
     * @return A string representing the object
     * @implSpec indents the output of {@link #toPrettyString()}
     */
    @Override
    public String toString() {
        return Debug.indent(toPrettyString());
    }
}
