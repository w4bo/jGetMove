package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version 1.1.0
 * @since 0.2.0
 */
public class Base implements PrettyPrint {

    protected HashMap<Integer, Time> times;
    protected HashMap<Integer, Cluster> clusters;
    protected HashMap<Integer, Transaction> transactions;

    private TreeSet<Integer> timeIds;
    private TreeSet<Integer> clusterIds;
    private TreeSet<Integer> transactionIds;

    Base() {
        clusters = new HashMap<>();
        transactions = new HashMap<>();
        times = new HashMap<>();

        clusterIds = new TreeSet<>();
        transactionIds = new TreeSet<>();
        timeIds = new TreeSet<>();
    }

    /**
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

    public void add(Cluster cluster) {
        clusters.put(cluster.getId(), cluster);
        clusterIds.add(cluster.getId());
    }

    public void add(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        transactionIds.add(transaction.getId());
    }

    public void add(Time time) {
        times.put(time.getId(), time);
        timeIds.add(time.getId());
    }

    public Cluster getOrCreateCluster(final int clusterId) {
        Cluster cluster = this.getCluster(clusterId);

        if (cluster == null) {
            cluster = new Cluster(clusterId);
            this.add(cluster);
        }

        return cluster;
    }


    public Time getOrCreateTime(final int timeId) {
        Time time = this.getTime(timeId);

        if (time == null) {
            time = new Time(timeId);
            this.add(time);
        }

        return time;
    }

    /**
     * @param transactionId l'id de la transaction à récuperer
     * @return La transaction crée ou récuperée
     */
    public Transaction getOrCreateTransaction(final int transactionId) {
        Transaction transaction = this.getTransaction(transactionId);

        if (transaction == null) {
            transaction = new Transaction(transactionId);
            this.add(transaction);
        }
        return transaction;
    }

    public Cluster getCluster(final int clusterId) {
        return clusters.get(clusterId);
    }

    public Time getTime(final int timeId) {
        return times.get(timeId);
    }

    public Transaction getTransaction(final int transactionId) {
        return transactions.get(transactionId);
    }

    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    public HashMap<Integer, Time> getTimes() {
        return times;
    }

    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    public TreeSet<Integer> getClusterIds() {
        return clusterIds;
    }

    public TreeSet<Integer> getTimeIds() {
        return timeIds;
    }

    public TreeSet<Integer> getTransactionIds() {
        return transactionIds;
    }

    /**
     * @param clusterId identifiant du cluster
     * @return l'ensemble des transactions du cluster
     */
    public HashMap<Integer, Transaction> getClusterTransactions(final int clusterId) {
        return getCluster(clusterId).getTransactions();
    }

    /**
     * @param transactionId identifiant de la transaction
     * @return l'ensemble des clusters de la transaction
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
     * Verifie si le cluster est inclus dans toute la liste des transactions
     * <p>
     * Dans GetMove :
     * <pre>
     * Lcm::CheckItemInclusion(DataBase,transactionList,item)
     * </pre>
     *
     * @param transactionIds (transactionList) la liste des transactions
     * @param clusterId      (item) le cluster à trouver
     * @return vrai si le cluster est présent dans toute les transactions de la liste
     */
    public boolean areTransactionsInCluster(final Set<Integer> transactionIds, final int clusterId) {
        for (int transactionId : transactionIds) {
            if (!getClusterTransactions(clusterId).containsKey(transactionId)) {
                return false;
            }
        }
        return true;
    }

    public int getClusterTimeId(final int clusterId) {
        return clusters.get(clusterId).getTimeId();
    }

    @Override
    public String toPrettyString() {
        return "\n|-- Clusters :" + String.valueOf(clusters.values()) +
                "\n|-- Transactions :" + String.valueOf(transactions.values()) +
                "\n`-- Times :" + String.valueOf(times.values());
    }

    @Override
    public String toString() {
        return Debug.indent(toPrettyString());
    }
}
