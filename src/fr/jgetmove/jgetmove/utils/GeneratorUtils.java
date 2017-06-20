package fr.jgetmove.jgetmove.utils;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;

import java.util.*;

public class GeneratorUtils {
    /**
     * <pre>
     * Lcm::CalcurateCoreI(database, itemsets, freqList)
     * </pre>
     *
     * @param clusterIds         (itemsets)
     * @param frequentClusterIds (freqList) !not empty
     * @return le dernier élement different du dernier clusterId de frequentClusterIds, si frequentClusterIds est trop petit, renvoie le premier element de clusterIds, sinon renvoi 0 si clusterIds est vide
     */
    public static int getDifferentFromLastCluster(ArrayList<Integer> clusterIds,
                                                  ArrayList<Integer> frequentClusterIds) {
        if (clusterIds.size() > 0) {
            int current = frequentClusterIds.get(frequentClusterIds.size() - 1);

            for (int i = frequentClusterIds.size() - 1; i >= 0; i--) {
                if (current != frequentClusterIds.get(i))
                    return frequentClusterIds.get(i);
            }

            return clusterIds.get(0);
        }
        return 0;
    }

    /**
     * Renvoie la valeur qui englobe le plus de transactions
     * <p>
     * <pre>
     * Lcm::MakeClosure(const Database &database, vector<int> &transactionList,
     * vector<int> &q_sets, vector<int> &itemsets,
     * int item)
     * </pre>
     *
     * @param database       (database)
     * @param transactionIds (transactionList)
     * @param qSets          (q_sets)
     * @param itemset        (itemsets)
     * @param freq           (item)
     */
    @TraceMethod(displayTitle = true)
    public static void makeClosure(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets,
                                   ArrayList<Integer> itemset, int freq) {
        Debug.println("transactionIds : " + transactionIds);
        Debug.println("qSets : " + qSets);
        Debug.println("itemset : " + itemset);
        Debug.println("freq : " + freq);
        Debug.println("");

        for (int clusterIndex = 0; clusterIndex < itemset.size() && itemset.get(clusterIndex) < freq; clusterIndex++) {
            qSets.add(itemset.get(clusterIndex));
        }

        //qSets.addAll(itemset);

        qSets.add(freq);

        SortedSet<Integer> lowerBoundSet = GeneratorUtils.lower_bound(database.getClusterIds(), freq + 1);

        for (int clusterId : lowerBoundSet) {
            if (GeneratorUtils.CheckItemInclusion(database, transactionIds, clusterId)) {
                qSets.add(clusterId);
            }
        }
        Debug.println("qSets : " + qSets);
    }

    /**
     * <pre>
     * Lcm::UpdateTransactionList(const Database &database, const vector<int> &transactionList, const vector<int> &q_sets, int item, vector<int> &newTransactionList)
     * </pre>
     *
     * @param database       (database)
     * @param transactionIds (transactionList)
     * @param qSets          (q_sets)
     * @param freqClusterId  (item)
     * @return (newTransactionList)
     */
    public static Set<Integer> updateTransactions(Database database, Set<Integer> transactionIds,
                                                  ArrayList<Integer> qSets, int freqClusterId) {
        Set<Integer> newTransactionIds = new HashSet<>();

        for (int transactionId : transactionIds) {
            Transaction transaction = database.getTransaction(transactionId);
            boolean canAdd = true;

            for (int qSetClusterId : qSets) {
                if (qSetClusterId >= freqClusterId && !transaction.getClusterIds().contains(qSetClusterId)) {
                    canAdd = false;
                }
            }
            if (canAdd) {
                newTransactionIds.add(transactionId);
            }
        }

        return newTransactionIds;
    }

    /**
     * <pre>
     * Lcm::UpdateFreqList(const Database &database, const vector<int> &transactionList, const vector<int> &gsub, vector<int> &freqList, int freq, vector<int> &newFreq)
     * </pre>
     *
     * @param database         (database)
     * @param transactionIds   (transactionList)
     * @param qSets            (gsub)
     * @param frequentClusters (freqList)
     * @param freqClusterId    (freq)
     * @return (newFreq)
     */
    public static ArrayList<Integer> updateFreqList(Database database, Set<Integer> transactionIds,
                                                    ArrayList<Integer> qSets,
                                                    ArrayList<Integer> frequentClusters, int freqClusterId) {
        //On ajoute les frequences des itemsets de qSets
        ArrayList<Integer> updatedFrequentClusters = new ArrayList<>();

        int clusterId = 0;
        if (frequentClusters.size() > 0) {
            for (; clusterId < qSets.size(); clusterId++) {
                if (qSets.get(clusterId) >= freqClusterId) break;
                updatedFrequentClusters.add(frequentClusters.get(clusterId));
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
        ArrayList<Integer> previousList = new ArrayList<>(transactionIds);

        for (; clusterId < qSets.size(); clusterId++) {
            ArrayList<Integer> lastList = new ArrayList<>();

            int freqCount = 0;

            for (int transactionId : previousList) {
                Transaction transaction = database.getTransaction(transactionId);

                if (transaction.getClusterIds().contains(qSets.get(clusterId))) {
                    freqCount++;
                    lastList.add(transactionId);
                }
            }
            updatedFrequentClusters.add(freqCount);
            previousList = lastList;
        }

        return updatedFrequentClusters;
    }

    /**
     * Gets the lower_bound of an array
     *
     * @param array find the lower_bound from
     * @param key   the item to compare
     * @return an array beginning from key
     * @deprecated use {@link TreeSet#tailSet(Object)}
     */
    public static SortedSet<Integer> lower_bound(TreeSet<Integer> array, int key) {
        return array.tailSet(key);
    }

    /**
     * CheckItemInclusion(Database,transactionlist,item)
     * Check whether clusterId is included in any of the transactions pointed to transactions
     *
     * @param database       (database)
     * @param transactionIds (transactionList) la liste des transactions
     * @param clusterId      (item) clusterId to find
     * @return true if the clusterId is in one of the transactions
     * @deprecated not expressive naming use {@link Database#isClusterInTransactions(Set, int)}
     */
    public static boolean CheckItemInclusion(Database database, Set<Integer> transactionIds, int clusterId) {
        return database.isClusterInTransactions(transactionIds, clusterId);
    }

    /**
     * Teste si p(i-1) == q(i-1)
     * <p>
     * <pre>
     * Lcm::PpcTest(database, []itemsets, []transactionList, item, []newTransactionList)
     * </pre>
     *
     * @param itemset           (itemsets)
     * @param maxClusterId     (item)
     * @param transactionIds (newTransactionList)
     * @return vrai si ppctest est réussi
     */
    public static boolean ppcTest(Database database, ArrayList<Integer> itemset, int maxClusterId, Set<Integer> transactionIds) {
        for (int clusterId = 0; clusterId < maxClusterId; clusterId++) {
            if (!itemset.contains(clusterId) && CheckItemInclusion(database, transactionIds, clusterId)) {
                return false;
            }
        }
        return true;
    }

}
