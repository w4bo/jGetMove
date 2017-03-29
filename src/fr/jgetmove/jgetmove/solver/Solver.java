package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Transaction;

import java.util.*;

public class Solver implements ISolver {

    private int minSupport, maxPattern, minTime;

    private int sizeGenerated;

    /**
     * @param minSupport support minimal
     * @param maxPattern nombre maximal de pattern a trouv�
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
     *
     * @param database la base de données à analyser
     */
    public void initLcm(Database database) {
        System.out.println("totalItem : " + database.getClusterIds());

        ArrayList<Integer> itemsets = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();

        Set<Integer> transactionIds = database.getTransactionIds();

        LcmIterNew(database, itemsets, transactionIds, freqItemset);
    }

    /**
     * @param database       La database à analyser
     * @param itemset        une liste representant les id/itemsets
     * @param transactionIds
     * @param freqItemset    une liste reprensentant les clusters frequents
     */
    private void LcmIterNew(Database database, ArrayList<Integer> itemset, Set<Integer> transactionIds, ArrayList<Integer> freqItemset) {

        ArrayList<ArrayList<Integer>> generatedClusterArrays = new ArrayList<>();
        ArrayList<Set<Integer>> generatedTimeIds = new ArrayList<>();
        ArrayList<TreeSet<Integer>> generatedClusterIds = new ArrayList<>();

        sizeGenerated = 1;

        generateItemset(database, itemset, generatedClusterArrays, generatedTimeIds, generatedClusterIds);

        System.out.println("");
        System.out.println("LCM Iter New");
        System.out.println("GeneratedItemsets : " + generatedClusterArrays);
        System.out.println("GeneratedItemId : " + generatedClusterIds);
        System.out.println("GeneratedTimeId : " + generatedTimeIds);
        System.out.println("SizeGenerated : " + sizeGenerated);
        System.out.println("");

        for (ArrayList<Integer> generatedClusters : generatedClusterArrays) {
            int calcurateCoreI = CalcurateCoreI(generatedClusters, freqItemset);
            System.out.println("Core_i : " + calcurateCoreI);

            SortedSet<Integer> lowerBounds = lower_bound(database.getClusterIds(), calcurateCoreI);
            ArrayList<Integer> freqClusterIds = new ArrayList<>();

            for (int clusterId : lowerBounds) {
                /*System.out.println("Total Item : " + database.getClusters());
                System.out.println("Index : " + clusterId);
                System.out.println("Transaction : " + database.getTransactions());*/

                if ((database.getCluster(clusterId).getTransactions().size()) >= minSupport &&
                        !generatedClusters.contains(clusterId)) {
                    freqClusterIds.add(clusterId);
                }

                ArrayList<Integer> qSets = new ArrayList<>();

                for (int freqClusterId : freqClusterIds) {
                    Set<Integer> newTransactionIds = new HashSet<>();

                    if (PPCTest(database, generatedClusters, transactionIds, freqClusterId, newTransactionIds)) {
                        qSets.clear();

                        MakeClosure(database, newTransactionIds, qSets, generatedClusters, freqClusterId);

                        if (maxPattern == 0 || qSets.size() <= maxPattern) {
                            ArrayList<Integer> newFreqList;

                            Set<Integer> iterTransactionIds = updateTransactions(database, transactionIds, qSets, freqClusterId);
                            newFreqList = updateFreqList(database, transactionIds, qSets, freqItemset, freqClusterId);

                            //updateOccurenceDeriver(database, iterTransactionIds);
                            // itemset -> qSets
                            // transactionIds -> newTransactionIds
                            // freqList -> newFreqlist
                            LcmIterNew(database, qSets, iterTransactionIds, newFreqList);

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
        System.out.println("newTransactionIds " + newTransactionIds);
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

        for (int transactionId : database.getTransactionIds()) {
            // contains est plus rapide que binary_search
            if (!generatedItemset.contains(transactionId) &&
                    CheckItemInclusion(database, newTransactionIds, transactionId)) {
                return false;
            }
        }
        return true;
    }

    private int CalcurateCoreI(ArrayList<Integer> itemset, ArrayList<Integer> freqItemset) {
        if (itemset.size() > 0) {
            if (freqItemset.size() > 1) {
                return freqItemset.get(freqItemset.size() - 2);
            }

            return itemset.get(0);
        }
        return 0;
    }

    private void MakeClosure(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets, ArrayList<Integer> itemset, int freq) {
        System.out.println("");
        System.out.println("Make Closure");
        System.out.println("transactionIds : " + transactionIds);
        System.out.println("qSets : " + qSets);
        System.out.println("itemset : " + itemset);
        System.out.println("freq : " + freq);
        System.out.println("");

        qSets.addAll(itemset);

        qSets.add(freq);

        SortedSet<Integer> lowerBoundSet = lower_bound(database.getClusterIds(), freq + 1);

        for (int clusterId : lowerBoundSet) {
            if (CheckItemInclusion(database, transactionIds, clusterId)) {
                qSets.add(clusterId);
            }
        }
        System.out.println("qSets : " + qSets);
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
     * @param database               (database, itemID, timeID) la database a analyser
     * @param itemset                (itemsets) une liste representant les id/itemsets
     * @param generatedClusterArrays (generateItemsets) une liste reprensentant les itemsetsGenerees
     * @param generatedTimeIds       (generatedtimeID) une liste reprensentant les tempsGenerees
     * @param generatedClusterIds    (generateditemID) une liste reprensentant les itemGenerees
     */
    private void generateItemset(Database database, ArrayList<Integer> itemset,
                                 ArrayList<ArrayList<Integer>> generatedClusterArrays,
                                 ArrayList<Set<Integer>> generatedTimeIds,
                                 ArrayList<TreeSet<Integer>> generatedClusterIds) {
        if (itemset.size() == 0) {
            sizeGenerated = 0;
            generatedClusterArrays.add(itemset);
            return;
        }
        //TODO

        generatedClusterArrays.clear();

        Set<Integer> times = new HashSet<>();
        boolean monoCluster = true;

        // liste des temps qui n'ont qu'un seul cluster
        for (int itemIndex = 0; itemIndex < itemset.size(); ++itemIndex) {
            int item = itemset.get(itemIndex);

            times.add(database.getCluster(item).getTimeId());

            if (itemIndex != itemset.size() - 1) {
                int nextItem = itemset.get(itemIndex + 1);

                if (database.getCluster(item).getTimeId() == database.getCluster(nextItem).getTimeId()) {
                    monoCluster = false;
                }
            }
        }

        if (monoCluster) {
            generatedClusterArrays.add(itemset);
            generatedTimeIds.add(times);
            generatedClusterIds.add(database.getClusterIds());
            return;
        }

        //Manage MultiClustering
        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>();

        for (int timeId : times) {
            ArrayList<Integer> clusterIds = new ArrayList<>();

            for (int item : itemset) {
                if (database.getCluster(item).getTimeId() == timeId) {
                    clusterIds.add(item);
                }
            }

            timesClusterIds.add(clusterIds);
        }

        //sizeGenerated stands for the number of potential itemsets to generate
        System.out.println("sizeGenerated stands for the number of potential itemsets to generate");
        sizeGenerated = 1;

        for (ArrayList<Integer> clusterIds : timesClusterIds) {
            sizeGenerated *= clusterIds.size();
        }

        //initialise the set of generated itemsets
        System.out.println("initialise the set of generated itemsets");


        for (Integer clusterId : timesClusterIds.get(0)) {
            ArrayList<Integer> singleton = new ArrayList<>(1);
            singleton.add(clusterId);
            generatedClusterArrays.add(singleton);
            System.out.println("Add Singleton : " + singleton);
        }

        for (int timeId = 1; timeId < timesClusterIds.size(); ++timeId) {
            ArrayList<Integer> clusterIds = timesClusterIds.get(timeId);
            ArrayList<ArrayList<Integer>> results = new ArrayList<>();

            for (ArrayList<Integer> generatedItems : generatedClusterArrays) {
                for (int item : clusterIds) {
                    ArrayList<Integer> result = new ArrayList<>(generatedItems);
                    result.add(item);
                    results.add(result);
                }
            }

            System.out.println("Test : " + results);
            generatedClusterArrays = results;
        }

        // Remove the itemsets already existing in the set of transactions
        System.out.println("Remove Itemsets already existing");
        ArrayList<ArrayList<Integer>> checkedItemsets = new ArrayList<>();

        boolean insertok;

        for (ArrayList<Integer> generatedItemset : generatedClusterArrays) {
            insertok = true;

            for (Transaction transaction : database.getTransactions().values()) {
                if (transaction.getClusterIds().containsAll(generatedItemset) &&
                        generatedItemset.containsAll(transaction.getClusterIds())) {
                    insertok = false;
                    break;
                }
            }

            if (insertok) {
                checkedItemsets.add(generatedItemset);
            }
        }

        System.out.println("GeneratedItemsets : " + checkedItemsets);

        // updating list of dates
        System.out.println("updating list of dates");
        times.clear();

        for (ArrayList<Integer> checkedItemset : checkedItemsets) {
            for (int checkedItem : checkedItemset) {
                if (itemset.contains(checkedItem)) {
                    times.add(database.getCluster(itemset.get(checkedItem)).getTimeId());
                }
            }
        }

        generatedClusterArrays.clear();
        generatedClusterArrays = checkedItemsets;
        generatedTimeIds.add(times);
        generatedClusterIds.add(database.getClusterIds());
    }
}
