/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.Config;
import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;

import java.util.*;

/**
 * Finds the itemsets
 *
 * @version 1.1.0
 * @since 0.1.0
 */
public class RecursiveItemsetsFinder extends BasicItemsetsFinder {
    public RecursiveItemsetsFinder(Config config) {
        super(config);
    }

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


    /**
     * Runs the algorithm on the given base. It returns the itemsets found.
     * <p>
     * <pre>
     * Lcm::RunLcmNew(database, []transactionsets,
     * numItems, []itemID, []timeID,
     * [][]level2ItemID, [][]level2TimeID)
     * </pre>
     */
    @TraceMethod
    @Override
    public ArrayList<Itemset> generate(Base base, final int minTime) {
        Debug.println("Base", base, Debug.DEBUG);
        Debug.println("n° of Clusters", base.getClusterIds().size(), Debug.INFO);
        Debug.println("n° of Transactions", base.getTransactionIds().size(), Debug.INFO);
        Debug.println("n° of Times", base.getTimeIds().size(), Debug.INFO);


        ClusterMatrix clusterMatrix = new ClusterMatrix(base);

        ArrayList<Integer> itemset = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();
        TreeSet<Integer> transactionIds = new TreeSet<>(base.getTransactionIds());

        // Overwriting minTime to avoid problems with blocks
        this.minTime = minTime;
        // important if has multiple blocks it needs to be cleaned.
        this.itemsets.clear();

        run(base, clusterMatrix, itemset, transactionIds, freqItemset);

        Debug.println("Itemsets", itemsets, Debug.DEBUG);
        Debug.println("n° of Itemsets", itemsets.size(), Debug.INFO);
        return new ArrayList<>(itemsets);
    }

    /**
     * Boucle r&eacute;cursive
     * <p>
     * <p>
     * <pre>
     * Lcm::LcmIterNew(database, []itemsets, []transactionList, occ,
     * []freqList, []transactionsets, pathId, []itemID,
     * []timeID, [][]level2ItemID, [][]level2TimeID) {
     * </pre>
     *
     * @param base                   (default database)
     * @param clusterMatrix          (database, , itemID, timeID) La database &agrave; analyser
     * @param clusterIds             (itemsets) une liste representant les id
     * @param transactionIds         (transactionList)
     * @param clustersFrequenceCount (freqList) une liste representant les clusterIds frequents
     */
    @TraceMethod(displayTitle = true)
    private void run(Base base, ClusterMatrix clusterMatrix, ArrayList<Integer> clusterIds,
                     Set<Integer> transactionIds, ArrayList<Integer> clustersFrequenceCount) {
        Debug.println("ClusterMatrix", clusterMatrix, Debug.DEBUG);
        Debug.println("Transactions", transactionIds, Debug.DEBUG);
        Debug.println("ClusterIds", clusterIds, Debug.DEBUG);
        Debug.println("ClustersFrequenceCount", clustersFrequenceCount, Debug.DEBUG);

        // generating all the possible itemsets from the list of clusters
        ArrayList<TreeSet<Integer>> itemsets = generateItemsets(base, clusterIds);

        Debug.println("Itemsets", itemsets, Debug.DEBUG);

        // foreach possible itemsets
        for (TreeSet<Integer> itemsetClusters : itemsets) {
            Debug.println("Itemset", itemsetClusters, Debug.DEBUG);

            int calcurateCoreI;

            // if the itemset is not empty :)
            if (itemsetClusters.size() > minTime) {
                saveItemset(clusterMatrix, itemsetClusters);

                // we calcurates its calcuration ?
                calcurateCoreI = getDifferentFromLastItem(clustersFrequenceCount, clusterIds);

            } else { // if it's empty we can't calcurate it :(
                calcurateCoreI = 0;
            }

            // still wondering what corei is used for ... (and this calcurating stuff is for, maybe for YoRHa)
            Debug.println("Core i : " + calcurateCoreI, Debug.DEBUG);

            // knowing the coreI of this itemset, we restrain the search space by getting all the clusters over it (including coreI)
            SortedSet<Integer> clustersTailSet = base.getClusterIds().tailSet(calcurateCoreI);

            // once we have this, we need to find the possible future clusters of the future itemsets
            ArrayList<Integer> maxClusterIdsOfItemsets = new ArrayList<>();

            // if the cluster is not in the already added in the itemset
            // and has more than the minimal amount of transactions
            for (int clusterId : clustersTailSet) {
                if (!clusterIds.contains(clusterId) && clusterMatrix.getClusterIds().contains(clusterId) && clusterMatrix.getTransactionIds(clusterId).size() >= minSupport) {
                    maxClusterIdsOfItemsets.add(clusterId);
                }
            }

            // freq_i
            Debug.println("ClusterTailSet", clustersTailSet, Debug.DEBUG);
            // we have our future possible clusters that can be in a future itemset
            Debug.println("maxClusterIdsOfItemsets", maxClusterIdsOfItemsets, Debug.DEBUG);

            // once we have them, we test for each of them
            for (int maxClusterId : maxClusterIdsOfItemsets) {
                // we retrieve the new transactions from the database
                Set<Integer> newTransactionIds = base.getFilteredTransactionIdsIfHaveCluster(transactionIds, maxClusterId);

                Debug.println("NewTransactionIds", newTransactionIds, Debug.DEBUG);

                if (ppcTest(base, itemsetClusters, newTransactionIds, maxClusterId)) {
                    // if the itemset is the most englobing itemset possible from clusterId 0 ->maxClusterId
                    ArrayList<Integer> futureClusterIds = makeClosure(base, newTransactionIds, itemsetClusters, maxClusterId);
                    // then we try to extend it to a larger itemset

                    Debug.println("futureClusterIds", futureClusterIds, Debug.DEBUG);

                    if (maxPattern == 0 || futureClusterIds.size() <= maxPattern) {
                        Set<Integer> futureTransactionIds =
                                updateTransactions(base, transactionIds, futureClusterIds, maxClusterId);
                        ArrayList<Integer> futureClustersFrequenceCount =
                                updateClustersFrequenceCount(base, transactionIds, futureClusterIds, clustersFrequenceCount,
                                        maxClusterId);

                        clusterMatrix.optimizeMatrix(base, futureTransactionIds);

                        run(base, clusterMatrix, futureClusterIds, futureTransactionIds, futureClustersFrequenceCount);

                    }
                }
            }
        }
    }

    /**
     * Creates and adds the itemset. Will return <tt>true</tt> if the itemset wasn't already saved.
     *
     * @param clusterMatrix   will be used to retrieve the list of transactions and times
     * @param itemsetClusters clusters of the itemset
     * @return <tt>true</tt> if the saved itemset is'nt already added, will return <tt>false</tt> if the itemset was already saved.
     * @see Itemset#compareTo(Itemset) to understand how the verification is made.
     */
    private boolean saveItemset(ClusterMatrix clusterMatrix, TreeSet<Integer> itemsetClusters) {
        // then the itemset is possible
        HashSet<Integer> itemsetTransactions = clusterMatrix.getTransactionIds(itemsetClusters.last());

        TreeSet<Integer> itemsetTimes = new TreeSet<>();
        for (Integer clusterId : itemsetClusters) {
            itemsetTimes.add(clusterMatrix.getTimeId(clusterId));
        }
        // so we add it to the final list
        Itemset itemset = new Itemset(itemsets.size(), itemsetTransactions, itemsetClusters, itemsetTimes);

        // unless he's already in there. if so we don't add it
        return this.itemsets.add(itemset);
    }
}
