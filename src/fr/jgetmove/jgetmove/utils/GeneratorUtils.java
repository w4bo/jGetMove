package fr.jgetmove.jgetmove.utils;

import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;

import java.util.*;

/**
 * @version 1.0.0
 * @since 0.1.0
 */
public class GeneratorUtils {
    /**
     * <pre>
     * Lcm::CalcurateCoreI(database, itemsets, freqList)
     * </pre>
     *
     * @param frequentClusterIds (freqList) !not empty
     * @param clusterIds         default value to return if frequentclusterIds are the same
     * @return le dernier élement different du dernier clusterId de frequentClusterIds, si frequentClusterIds est trop petit, renvoie le premier element de clusterIds, sinon renvoi 0 si clusterIds est vide
     */
    public static int getDifferentFromLastItem(ArrayList<Integer> frequentClusterIds, ArrayList<Integer> clusterIds) {
        int last = frequentClusterIds.get(frequentClusterIds.size() - 1);

        for (int i = frequentClusterIds.size() - 1; i >= 0; i--) {
            if (last != frequentClusterIds.get(i))
                return clusterIds.get(i);
        }

        return clusterIds.get(0);
    }

    /**
     * Renvoie la valeur qui englobe le plus de transactions
     * <p>
     * <pre>
     * Lcm::MakeClosure(const DataBase &database, vector<int> &transactionList,
     * vector<int> &q_sets, vector<int> &itemsets,
     * int item)
     * </pre>
     *
     * @param base             (database)
     * @param transactionIds   (transactionList)
     * @param itemset          (itemsets)
     * @param currentClusterId (item)
     */
    @TraceMethod(displayTitle = true)
    public static ArrayList<Integer> makeClosure(Base base, Set<Integer> transactionIds,
                                                 TreeSet<Integer> itemset, int currentClusterId) {
        Debug.println("transactionIds", transactionIds, Debug.DEBUG);
        ArrayList<Integer> newItemset = new ArrayList<>();
        Debug.println("itemset", itemset, Debug.DEBUG);
        Debug.println("currentClusterId", currentClusterId, Debug.DEBUG);

        // all the clusters in the itemset have these
        /*for (int clusterId : itemset) {
            if (clusterId < currentClusterId) {
                newItemset.add(clusterId);
            }
        }*/

        newItemset.addAll(itemset.headSet(currentClusterId));

        // this also has these
        newItemset.add(currentClusterId);

        SortedSet<Integer> lowerBoundSet = base.getClusterIds().tailSet(currentClusterId + 1);
        // the future have this only if the transactions are in the clusterId
        for (int clusterId : lowerBoundSet) {
            if (base.areTransactionsInCluster(transactionIds, clusterId)) {
                newItemset.add(clusterId);
            }
        }

        return newItemset;
    }

    /**
     * Returns an updated list of transactions which are present in the clusters. These clusters need to be greater than needToCheckFromClusterId. If they are lower, they are not checked and the transaction is automatically added.
     * <p>
     * <pre>
     * Lcm::UpdateTransactionList(const DataBase &database, const vector<int> &transactionList, const vector<int> &q_sets, int item, vector<int> &newTransactionList)
     * </pre>
     *
     * @param base                     (database)
     * @param transactionIds           (transactionList) the transactions which are checked
     * @param itemset                  (q_sets) an array containing the clusterIds which are checked
     * @param needToCheckFromClusterId (item) the minimal clusterId from where we need to check if the transaction is contained in the cluster
     * @return (newTransactionList)
     */
    public static Set<Integer> updateTransactions(Base base, Set<Integer> transactionIds,
                                                  ArrayList<Integer> itemset, int needToCheckFromClusterId) {
        Set<Integer> newTransactionIds = new HashSet<>();

        // foreach transaction in transactionIds
        for (int transactionId : transactionIds) {
            Transaction transaction = base.getTransaction(transactionId);
            boolean canAdd = true;

            for (int itemsetClusterId : itemset) {// foreach cluster of the itemset
                if (itemsetClusterId >= needToCheckFromClusterId && !transaction.getClusterIds().contains(itemsetClusterId)) {
                    // if the cluster is greater than the needToCheckFromClusterId AND the transaction is not in it
                    // then we can't add it, otherwise (if the itemsetClusterId is lower then no problem it works ?

                    canAdd = false;
                    break;
                }
            }
            // if the itemset has a greater clusterIds and if the transaction isn't in it then we don't add it ...
            // otherwise open bar :D
            if (canAdd) { // then we can add this transaction as it respects the protocol :D
                newTransactionIds.add(transactionId);
            }
        }

        return newTransactionIds;
    }

    /**
     * <pre>
     * Lcm::UpdateFreqList(const DataBase &database, const vector<int> &transactionList, const vector<int> &gsub, vector<int> &freqList, int freq, vector<int> &newFreq)
     * </pre>
     *
     * @param base                      (database)
     * @param transactionIds            (transactionList)
     * @param newItemsetClusters        (gsub)
     * @param oldClustersFrequenceCount (freqList)
     * @param maxClusterId              (freq)
     * @return (newFreq)
     */
    public static ArrayList<Integer> updateClustersFrequenceCount(Base base, Set<Integer> transactionIds,
                                                                  ArrayList<Integer> newItemsetClusters,
                                                                  ArrayList<Integer> oldClustersFrequenceCount, int maxClusterId) {
        //On ajoute les frequences des itemsets de newItemsetClusters
        ArrayList<Integer> clustersFrequenceCount = new ArrayList<>();

        int clusterIndex = 0;

        if (oldClustersFrequenceCount.size() > 0) {

            for (; clusterIndex < newItemsetClusters.size(); clusterIndex++) {
                if (newItemsetClusters.get(clusterIndex) >= maxClusterId) break;
                clustersFrequenceCount.add(oldClustersFrequenceCount.get(clusterIndex));
            }
        }
        // newList = database.getTransactionsId()
        // pour chaque item de Qset
        //      pour chaque transaction de newList
        //          si la transaction contient item
        //              increment compteur
        //              ajouter transaction dans newnewList
        //      ajouter compteur a newFreqlist
        //      newlist = newnewlist
        ArrayList<Integer> currentTransactionCount = new ArrayList<>(transactionIds);

        for (; clusterIndex < newItemsetClusters.size(); clusterIndex++) {
            ArrayList<Integer> transactionsCount = new ArrayList<>();

            int transactionCount = 0;

            for (int transactionId : currentTransactionCount) {
                Transaction transaction = base.getTransaction(transactionId);

                if (transaction.getClusterIds().contains(newItemsetClusters.get(clusterIndex))) {
                    ++transactionCount;
                    transactionsCount.add(transactionId);
                }
            }
            clustersFrequenceCount.add(transactionCount);
            currentTransactionCount = transactionsCount;
        }

        return clustersFrequenceCount;
    }

    /**
     * Checks if the itemset is the most englobing one. Has all the clusters that have these transaction
     * <p>
     * clusters lower than the maxClusterId
     * <p>
     * <p>
     * <pre>
     * Lcm::PpcTest(database, []itemsets, []transactionList, item, []newTransactionList)
     * </pre>
     *
     * @param itemset        (itemsets) itemset to check
     * @param maxClusterId   (item) upperbound of the clusterId
     * @param transactionIds (newTransactionList) transactions to check
     * @return vrai si ppctest est réussi
     */
    public static boolean ppcTest(Base base, TreeSet<Integer> itemset, Set<Integer> transactionIds, final int maxClusterId) {
        // checks if the itemset is the biggest one in the range.
        for (int clusterId = base.getClusterIds().first(); clusterId < maxClusterId; clusterId++) {
            if (!itemset.contains(clusterId) && base.areTransactionsInCluster(transactionIds, clusterId)) {
                return false;
            }
        }

        return true;
    }

}
