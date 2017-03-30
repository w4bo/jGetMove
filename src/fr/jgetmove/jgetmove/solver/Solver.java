package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;

import java.util.*;

public class Solver implements ISolver {

    private int minSupport, maxPattern, minTime;

    /**
     * Initialise le solveur.
     *
     * @param minSupport support minimal
     * @param maxPattern nombre maximal de pattern a trouvé
     * @param minTime    temps minimal
     */
    public Solver(int minSupport, int maxPattern, int minTime) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
    }

    /**
     * Gets the lower_bound of an array
     *
     * @param array find the lower_bound from
     * @param key   the item to compare
     * @return an array beginning from key
     * @deprecated use {@link TreeSet#tailSet(Object)}
     */
    private static SortedSet<Integer> lower_bound(TreeSet<Integer> array, int key) {
        return array.tailSet(key);
    }

    /**
     * Initialise le solver à partir d'une base de données
     * <p>
     * <pre>
     * Lcm::RunLcmNew(database, []transactionsets,
     * numItems, []itemID, []timeID,
     * [][]level2ItemID, [][]level2TimeID)
     * </pre>
     *
     * @param database la base de données à analyser
     */
    public void init(Database database) {
        Debug.println("totalItem : " + database.getClusterIds());

        ArrayList<Integer> itemsets = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();

        run(database, itemsets, database.getTransactionIds(), freqItemset);
    }

    /**
     * Boucle récursive
     * <p>
     * <pre>
     * Lcm::LcmIterNew(database, []itemsets, []transactionList, occ,
     * []freqList, []transactionsets, numItems, []itemID,
     * []timeID, [][]level2ItemID, [][]level2TimeID) {
     * </pre>
     *
     * @param database           (database, transactionsets, itemID, timeID) La database à analyser
     * @param clusterIds         (itemsets) une liste representant les id
     * @param transactionIds     (transactionList)
     * @param frequentClusterIds (transactionsets) une liste reprensentant les clusterIds frequents
     */
    private void run(Database database, ArrayList<Integer> clusterIds, Set<Integer> transactionIds, ArrayList<Integer> frequentClusterIds) {

        ArrayList<ArrayList<Integer>> generatedClusterArrays = new ArrayList<>();
        ArrayList<Set<Integer>> generatedTimeIds = new ArrayList<>();
        ArrayList<TreeSet<Integer>> generatedClusterIds = new ArrayList<>();

        int[] sizeGenerated = {1};

        generateClusters(database, clusterIds, generatedClusterArrays, generatedTimeIds, generatedClusterIds, sizeGenerated);

        Debug.println("");
        Debug.println("LCM Iter New");
        Debug.println("GeneratedItemsets : " + generatedClusterArrays);
        Debug.println("GeneratedItemId : " + generatedClusterIds);
        Debug.println("GeneratedTimeId : " + generatedTimeIds);
        Debug.println("SizeGenerated : " + sizeGenerated[0]);
        Debug.println("");

        for (ArrayList<Integer> generatedClusters : generatedClusterArrays) {
            // TODO : PrintItemsetsNew on l'a oublié

            int calcurateCoreI = CalcurateCoreI(generatedClusters, frequentClusterIds);
            Debug.println("Core_i : " + calcurateCoreI);

            SortedSet<Integer> lowerBounds = lower_bound(database.getClusterIds(), calcurateCoreI);
            ArrayList<Integer> freqClusterIds = new ArrayList<>();

            for (int clusterId : lowerBounds) {

                if ((database.getCluster(clusterId).getTransactions().size()) >= minSupport &&
                        !generatedClusters.contains(clusterId)) {
                    freqClusterIds.add(clusterId);
                }

                ArrayList<Integer> qSets = new ArrayList<>();

                for (int freqClusterId : freqClusterIds) {
                    Debug.println("freqClusterId : " + freqClusterIds);
                    Set<Integer> newTransactionIds = new HashSet<>();

                    if (PPCTest(database, generatedClusters, transactionIds, freqClusterId, newTransactionIds)) {
                        qSets.clear();

                        MakeClosure(database, newTransactionIds, qSets, generatedClusters, freqClusterId);
                        if (maxPattern == 0 || qSets.size() <= maxPattern) {
                            ArrayList<Integer> newFreqList;

                            Set<Integer> iterTransactionIds = updateTransactions(database, transactionIds, qSets, freqClusterId);
                            newFreqList = updateFreqList(database, transactionIds, qSets, frequentClusterIds, freqClusterId);

                            //updateOccurenceDeriver(database, iterTransactionIds);
                            // clusterIds -> qSets
                            // transactionIds -> newTransactionIds
                            // freqList -> newFreqlist
                            run(database, qSets, iterTransactionIds, newFreqList);

                        }
                    }
                }
            }
        }
    }

    /**
     * @param database
     * @param transactionIds
     * @param qSets
     * @param frequentClusters
     * @param freqClusterId
     * @return
     */
    private ArrayList<Integer> updateFreqList(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets,
                                              ArrayList<Integer> frequentClusters, int freqClusterId) {
        //On ajoute les frequences des itemsets de qSets
        ArrayList<Integer> newFrequentClusters = new ArrayList<>();

        int clusterId = 0;
        if (frequentClusters.size() > 0) {
            for (; clusterId < qSets.size(); clusterId++) {
                if (qSets.get(clusterId) >= freqClusterId) break;
                newFrequentClusters.add(frequentClusters.get(clusterId));
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
                    lastList.add(previousList.get(previousList.get(transactionId)));
                }
            }
            newFrequentClusters.add(freqCount);
            previousList = lastList;
        }

        return newFrequentClusters;
    }

    private void updateOccurenceDeriver(Database database, Set<Integer> newTransactionIds) {
        //TODO
        for (int transactionId : newTransactionIds) {
            Transaction transaction = database.getTransaction(transactionId);

            for (int clusterId : transaction.getClusterIds()) {
                transaction.getClusters().put(clusterId, database.getCluster(clusterId));
                Cluster cluster = database.getCluster(clusterId);
                cluster.getTransactions().put(transactionId, transaction);
            }
        }

    }

    private Set<Integer> updateTransactions(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets, int freqClusterId) {
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

    private boolean PPCTest(Database database, ArrayList<Integer> generatedItemset, Set<Integer> transactionIds, int freqClusterId, Set<Integer> newTransactionIds) {
        // CalcTransactionList
        for (int transactionId : transactionIds) {
            Transaction transaction = database.getTransaction(transactionId);
            if (transaction.getClusterIds().contains(freqClusterId)) {
                newTransactionIds.add(transactionId);
            }
        }

        for (int clusterId = 0; clusterId < freqClusterId; clusterId++) {
            if (!(generatedItemset.contains(clusterId)) &&
                    CheckItemInclusion(database, newTransactionIds, clusterId)) {
                return false;
            }
        }
        /*for (int transactionId : database.getTransactionIds()) {
            // contains est plus rapide que binary_search
            
        }*/
        return true;
    }

    /**
     * <pre>
     * Lcm::CalcurateCoreI(database, itemsets, freqList)
     * </pre>
     *
     * @param clusterIds         (itemsets)
     * @param frequentClusterIds (freqList)
     * @return l'avant dernier élement du frequentClusterIds, si frequentClusterIds est trop petit, renvoie le premier element de clusterIds, renvoi 0 si clusterIds est vide;
     */
    private int CalcurateCoreI(ArrayList<Integer> clusterIds, ArrayList<Integer> frequentClusterIds) {
        if (clusterIds.size() > 0) {
            if (frequentClusterIds.size() > 1) {
                return frequentClusterIds.get(frequentClusterIds.size() - 2);
            }

            return clusterIds.get(0);
        }
        return 0;
    }

    private void MakeClosure(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets, ArrayList<Integer> itemset, int freq) {
        Debug.println("");
        Debug.println("Make Closure");
        Debug.println("transactionIds : " + transactionIds);
        Debug.println("qSets : " + qSets);
        Debug.println("itemset : " + itemset);
        Debug.println("freq : " + freq);
        Debug.println("");

        for (int clusterId = 0; clusterId < itemset.size() && itemset.get(clusterId) < freq; clusterId++) {
            qSets.add(itemset.get(clusterId));
        }

        //qSets.addAll(itemset);

        qSets.add(freq);

        SortedSet<Integer> lowerBoundSet = lower_bound(database.getClusterIds(), freq + 1);

        for (int clusterId : lowerBoundSet) {
            if (CheckItemInclusion(database, transactionIds, clusterId)) {
                qSets.add(clusterId);
            }
        }
        Debug.println("qSets : " + qSets);
    }


    /**
     * CheckItemInclusion(Database,transactionlist,item)
     * Check whether clusterId is included in any of the transactions pointed to transactions
     *
     * @param database       (database) la base de données
     * @param transactionIds (transactionList) la liste des transactions
     * @param clusterId      (item) clusterId to find
     * @return true if the clusterId is in one of the transactions
     * @deprecated not expressive naming use {@link #clusterInTransactions(Database, Set, int)}
     */
    private boolean CheckItemInclusion(Database database, Set<Integer> transactionIds, int clusterId) {
        return clusterInTransactions(database, transactionIds, clusterId);
    }

    /**
     * Verifie si le cluster est inclus dans toute la liste des transactions
     * <p>
     * Dans GetMove :
     * <pre>
     * Lcm::CheckItemInclusion(Database,transactionList,item)
     * </pre>
     *
     * @param database       (database) la base de données
     * @param transactionIds (transactionList) la liste des transactions
     * @param clusterId      (item) le cluster à trouver
     * @return vrai si le cluster est présent dans toute les transactions de la liste
     */
    private boolean clusterInTransactions(Database database, Set<Integer> transactionIds, int clusterId) {
        for (int transactionId : transactionIds) {
            if (!database.getTransaction(transactionId).getClusterIds().contains(clusterId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <pre>
     * Lcm::GenerateItemset(Database,[]itemsets,[]itemID,[]timeID,[][]generatedItemsets, [][]generatedtimeID,[][]generateditemID,sizeGenerated)
     * </pre>
     *
     * @param database                   (database, itemID, timeID) la database a analyser
     * @param clusterIds                 (itemsets) une liste representant les clusterId
     * @param generatedArrayOfClusters   (generateItemsets) une liste reprensentant les itemsetsGenerees
     * @param generatedArrayOfTimeIds    (generatedtimeID) une liste reprensentant les tempsGenerees
     * @param generatedArrayOfClusterIds (generateditemID) une liste reprensentant les itemGenerees
     */
    private void generateClusters(Database database, ArrayList<Integer> clusterIds,
                                  ArrayList<ArrayList<Integer>> generatedArrayOfClusters,
                                  ArrayList<Set<Integer>> generatedArrayOfTimeIds,
                                  ArrayList<TreeSet<Integer>> generatedArrayOfClusterIds,
                                  int[] sizeGenerated) {
        // todo aplatir generated* dans cette fonction : raison, non utilisées
        if (clusterIds.size() == 0) {
            sizeGenerated[0] = 0;
            generatedArrayOfClusters.add(clusterIds);
            return;
        }
        //pk 2d generated* ???

        generatedArrayOfClusters.clear();

        Set<Integer> times = new HashSet<>(); // listOfDates
        boolean isMonoCluster = true; // numberSameTime
        int lastTime;

        // liste des temps qui n'ont qu'un seul cluster
        for (int i = 0; i < clusterIds.size(); ++i) {
            int clusterId = clusterIds.get(i);
            times.add(database.getClusterTimeId(clusterId));

            if (i != clusterIds.size() - 1) {
                int nextClusterId = clusterIds.get(i + 1);

                if (database.getClusterTimeId(clusterId)
                        == database.getClusterTimeId(nextClusterId)) {
                    isMonoCluster = false;
                }
            }
        }

        if (isMonoCluster) {
            generatedArrayOfClusters.add(clusterIds); // why
            generatedArrayOfTimeIds.add(times); // whyyy
            generatedArrayOfClusterIds.add(database.getClusterIds()); // but why ???
            return;
        }

        //Manage MultiClustering

        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>(); // PosDates

        for (int timeId : times) {
            ArrayList<Integer> tempClusterIds = new ArrayList<>();

            for (int clusterId : clusterIds) {
                if (database.getClusterTimeId(clusterId) == timeId) {
                    tempClusterIds.add(clusterId);
                }
            }

            timesClusterIds.add(tempClusterIds);
        }

        //sizeGenerated stands for the number of potential itemsets to generate
        Debug.println("sizeGenerated stands for the number of potential itemsets to generate");
        sizeGenerated[0] = 1;

        for (ArrayList<Integer> temp : timesClusterIds) {
            sizeGenerated[0] *= temp.size();
        }

        //initialise the set of generated itemsets
        Debug.println("initialise the set of generated itemsets");


        for (Integer clusterId : timesClusterIds.get(0)) {
            ArrayList<Integer> singleton = new ArrayList<>(1);
            singleton.add(clusterId);
            generatedArrayOfClusters.add(singleton);
            Debug.println("Add Singleton : " + singleton);
        }

        for (int clusterIdsIndex = 1, size = timesClusterIds.size(); clusterIdsIndex < size; ++clusterIdsIndex) {
            ArrayList<Integer> tempClusterIds = timesClusterIds.get(clusterIdsIndex);
            ArrayList<ArrayList<Integer>> results = new ArrayList<>();

            for (ArrayList<Integer> generatedItems : generatedArrayOfClusters) {
                for (int item : tempClusterIds) {
                    ArrayList<Integer> result = new ArrayList<>(generatedItems);
                    result.add(item);
                    results.add(result); // but why ???
                }
            }
            generatedArrayOfClusters = results;
        }

        // Remove the itemsets already existing in the set of transactions
        Debug.println("Remove Itemsets already existing");
        ArrayList<ArrayList<Integer>> checkedArrayOfClusterIds = new ArrayList<>(); //checkedItemsets

        boolean insertok;
        int nbClusters = 0;

        for (ArrayList<Integer> currentClusterIds : generatedArrayOfClusters) {
            insertok = true;

            for (Transaction transaction : database.getTransactions().values()) {

                if (compareSetAndList(transaction.getClusterIds(), currentClusterIds)) {
                    insertok = false;
                    break;
                }
            }

            if (insertok) {
                checkedArrayOfClusterIds.add(currentClusterIds);
                nbClusters++;
            }
        }

        sizeGenerated[0] = nbClusters;

        // updating list of dates
        Debug.println("updating list of dates");
        times.clear();


        for (ArrayList<Integer> checkedClusterIds : checkedArrayOfClusterIds) {
            for (int checkedClusterId : checkedClusterIds) {
                if (clusterIds.contains(checkedClusterId)) {
                    times.add(database.getClusterTimeId(checkedClusterId));
                }
            }
        }

        generatedArrayOfClusters.clear();
        generatedArrayOfClusters = checkedArrayOfClusterIds; // why
        generatedArrayOfTimeIds.add(times); // whyy
        generatedArrayOfClusterIds.add(database.getClusterIds()); // but whyy ?

    }

    /**
     * Retourne vrai si les deux tableaux sont identiques
     *
     * @param set  le Set a comparer
     * @param list la liste a comparer
     * @return vrai si les deux sont identiques
     */
    private boolean compareSetAndList(Set<Integer> set, List<Integer> list) {
        // meilleure optimisation possible : http://stackoverflow.com/a/1075817
        TreeSet<Integer> treeSet = new TreeSet<>(set);
        TreeSet<Integer> treeList = new TreeSet<>(list);

        return treeSet.equals(treeList);
    }
}
