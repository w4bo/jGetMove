package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class Base {

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


    public void add(final Cluster cluster) {
        clusters.put(cluster.getId(), cluster);
        clusterIds.add(cluster.getId());
    }

    public void add(final Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        transactionIds.add(transaction.getId());
    }

    public void add(final Time time) {
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

    public Transaction getTransaction(final int transactionId) {
        return transactions.get(transactionId);
    }

    public Time getTime(int timeId) {
        return times.get(timeId);
    }

    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    public HashMap<Integer, Time> getTimes() {
        return times;
    }

    public TreeSet<Integer> getClusterIds() {
        return clusterIds;
    }

    public TreeSet<Integer> getTransactionIds() {
        return transactionIds;
    }

    public TreeSet<Integer> getTimeIds() {
        return timeIds;
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
            if (this.getCluster(clusterId) != null && this.getClusterTransactions(clusterId).containsKey(transactionId)) {
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
    public boolean isClusterInTransactions(final Set<Integer> transactionIds, final int clusterId) {
        for (int transactionId : transactionIds) {
            if (this.getCluster(clusterId) != null && !this.getClusterTransactions(clusterId).containsKey(transactionId)) {
                return false;
            }
        }
        return true;
    }

    public int getClusterTimeId(final int clusterId) {
        return clusters.get(clusterId).getTimeId();
    }

    @Override
    public String toString() {
        String str = "\n|-- Clusters :" + clusters.values();
        str += "\n|-- Transactions :" + transactions.values();
        str += "\n`-- Temps :" + times.values();
        return str;
    }
}
