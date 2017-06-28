package fr.jgetmove.jgetmove.utils;

import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.DataBase;
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
     * @param frequentClusterIds (freqList) !not empty
     * @param defaultValue       default value to return if frequentclusterIds are the same
     * @return le dernier élement different du dernier clusterId de frequentClusterIds, si frequentClusterIds est trop petit, renvoie le premier element de clusterIds, sinon renvoi 0 si clusterIds est vide
     */
    public static int getDifferentFromLastCluster(ArrayList<Integer> frequentClusterIds, int defaultValue) {
        int current = frequentClusterIds.get(frequentClusterIds.size() - 1);

        for (int i = frequentClusterIds.size() - 1; i >= 0; i--) {
            if (current != frequentClusterIds.get(i))
                return frequentClusterIds.get(i);
        }

        return defaultValue;
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
     * @param matrix       (database)
     * @param transactionIds (transactionList)
     * @param qSets          (q_sets)
     * @param path           (itemsets)
     * @param freq           (item)
     */
    @TraceMethod(displayTitle = true)
    public static void makeClosure(Base matrix, Set<Integer> transactionIds, ArrayList<Integer> qSets,
                                   Collection<Integer> path, int freq) {
        Debug.println("transactionIds : " + transactionIds);
        Debug.println("qSets : " + qSets);
        Debug.println("itemset : " + path);
        Debug.println("freq : " + freq);
        Debug.println("");

        for (int clusterId : path) {
            if (clusterId < freq) {
                qSets.add(clusterId);
            }
        }

        //qSets.addAll(itemset);

        qSets.add(freq);

        SortedSet<Integer> lowerBoundSet = GeneratorUtils.lower_bound(matrix.getClusterIds(), freq + 1);

        for (int clusterId : lowerBoundSet) {
            if (GeneratorUtils.CheckItemInclusion(matrix, transactionIds, clusterId)) {
                qSets.add(clusterId);
            }
        }
        Debug.println("qSets : " + qSets);
    }

    /**
     * <pre>
     * Lcm::UpdateTransactionList(const DataBase &database, const vector<int> &transactionList, const vector<int> &q_sets, int item, vector<int> &newTransactionList)
     * </pre>
     *
     * @param matrix       (database)
     * @param transactionIds (transactionList)
     * @param pathClusterIds          (q_sets)
     * @param maxClusterId  (item)
     * @return (newTransactionList)
     */
    public static Set<Integer> updateTransactions(Base matrix, Set<Integer> transactionIds,
                                                  ArrayList<Integer> pathClusterIds, int maxClusterId) {
        Set<Integer> newTransactionIds = new HashSet<>();

        for (int transactionId : transactionIds) {
            Transaction transaction = matrix.getTransaction(transactionId);
            boolean canAdd = true;

            for (int pathClusterId : pathClusterIds) {
                if (pathClusterId >= maxClusterId && !transaction.getClusterIds().contains(pathClusterId)) {
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
     * Lcm::UpdateFreqList(const DataBase &database, const vector<int> &transactionList, const vector<int> &gsub, vector<int> &freqList, int freq, vector<int> &newFreq)
     * </pre>
     *
     * @param matrix         (database)
     * @param transactionIds   (transactionList)
     * @param newPathClusters            (gsub)
     * @param oldClustersFrequenceCount (freqList)
     * @param maxClusterId    (freq)
     * @return (newFreq)
     */
    public static ArrayList<Integer> updateClustersFrequenceCount(Base matrix, Set<Integer> transactionIds,
                                                                  ArrayList<Integer> newPathClusters,
                                                                  ArrayList<Integer> oldClustersFrequenceCount, int maxClusterId) {
        //On ajoute les frequences des itemsets de newPathClusters
        ArrayList<Integer> clustersFrequenceCount = new ArrayList<>();

        int clusterIndex = 0;
        if (oldClustersFrequenceCount.size() > 0) {
            for (; clusterIndex < newPathClusters.size(); clusterIndex++) {
                if (newPathClusters.get(clusterIndex) >= maxClusterId) break;
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
        ArrayList<Integer> previousList = new ArrayList<>(transactionIds);

        for (; clusterIndex < newPathClusters.size(); clusterIndex++) {
            ArrayList<Integer> lastList = new ArrayList<>();

            int freqCount = 0;

            for (int transactionId : previousList) {
                Transaction transaction = matrix.getTransaction(transactionId);

                if (transaction.getClusterIds().contains(newPathClusters.get(clusterIndex))) {
                    freqCount++;
                    lastList.add(transactionId);
                }
            }
            clustersFrequenceCount.add(freqCount);
            previousList = lastList;
        }

        return clustersFrequenceCount;
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
     * CheckItemInclusion(DataBase,transactionlist,item)
     * Check whether clusterId is included in any of the transactions pointed to transactions
     *
     * @param matrix       (database)
     * @param transactionIds (transactionList) la liste des transactions
     * @param clusterId      (item) clusterId to find
     * @return true if the clusterId is in one of the transactions
     * @deprecated not expressive naming use {@link DataBase#isClusterInTransactions(Set, int)}
     */
    public static boolean CheckItemInclusion(Base matrix, Set<Integer> transactionIds, int clusterId) {
        return matrix.isClusterInTransactions(transactionIds, clusterId);
    }

    /**
     * Teste si p(i-1) == q(i-1)
     * <p>
     * <pre>
     * Lcm::PpcTest(database, []itemsets, []transactionList, item, []newTransactionList)
     * </pre>
     *
     * @param path           (itemsets)
     * @param maxClusterId   (item)
     * @param transactionIds (newTransactionList)
     * @return vrai si ppctest est réussi
     */
    public static boolean ppcTest(Base matrix, TreeSet<Integer> path, int maxClusterId, Set<Integer> transactionIds) {
        for (int clusterId = 0; clusterId < maxClusterId; clusterId++) {
            if (!path.contains(clusterId) && CheckItemInclusion(matrix, transactionIds, clusterId)) {
                return false;
            }
        }
        return true;
    }

}
