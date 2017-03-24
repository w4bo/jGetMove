package fr.jgetmove.jgetmove.solver;

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
        ArrayList<Integer> freqList = new ArrayList<>();

        Set<Integer> transactionIds = database.getTransactionIds();

        LcmIterNew(database, itemsets, transactionIds, freqList);
    }

    /**
     * @param database       La database à analyser
     * @param itemset        une liste representant les id/itemsets
     * @param transactionIds
     * @param freqItemset    une liste reprensentant less clusters frequents
     */
    private void LcmIterNew(Database database, ArrayList<Integer> itemset, Set<Integer> transactionIds, ArrayList<Integer> freqItemset) {

        ArrayList<ArrayList<Integer>> generatedItemsets = new ArrayList<>();
        ArrayList<Set<Integer>> generatedTimeIds = new ArrayList<>();
        ArrayList<TreeSet<Integer>> generatedClusterIds = new ArrayList<>();

        sizeGenerated = 1;

        generateItemset(database, itemset, generatedItemsets, generatedTimeIds, generatedClusterIds);

        System.out.println("GeneratedItemsets : " + generatedItemsets);
        System.out.println("GeneratedItemId : " + generatedClusterIds);
        System.out.println("GeneratedTimeId : " + generatedTimeIds);
        System.out.println("SizeGenerated : " + sizeGenerated);

        for (ArrayList<Integer> generatedItemset : generatedItemsets) {
            System.out.println("Test");
            int calcurateCoreI = CalcurateCoreI(generatedItemset, freqItemset);
            System.out.println("Core_i : " + calcurateCoreI);

            SortedSet<Integer> lowerBoundSet = lower_bound(database.getClusterIds(), calcurateCoreI);
            ArrayList<Integer> freqClusterIds = new ArrayList<>();

            for (int clusterId : lowerBoundSet) {
                System.out.println("Total Item : " + database.getClusters());
                System.out.println("Index : " + clusterId);
                System.out.println("Transaction : " + database.getTransactions());

                if (!((database.getCluster(clusterId).getTransactions().size()) >= minSupport) &&
                        Collections.binarySearch(generatedItemset, clusterId) == 0) {
                    freqClusterIds.add(clusterId);
                }

                Set<Integer> newTransactionIds = new HashSet<>();
                ArrayList<Integer> qSets = new ArrayList<>();
                ArrayList<Integer> newFreqList = new ArrayList<>();

                for (int freqClusterId : freqClusterIds) {
                    newTransactionIds.clear();

                    if (PPCTest(database, generatedItemset, transactionIds, freqClusterId, newTransactionIds)) {

                        qSets.clear();

                        MakeClosure(database, newTransactionIds, qSets, generatedItemset, freqClusterId);

                        if (maxPattern == 0 || qSets.size() <= maxPattern) {
                            newTransactionIds.clear();
                            updateTransactions(database, transactionIds, qSets, freqClusterId, newTransactionIds);
                            updateOccurenceDeriver(database, newTransactionIds);
                            newFreqList.clear();
                            updateFreqList(database, transactionIds, qSets, freqItemset, freqClusterId, newFreqList);
                            // itemset -> qSets
                            // transactionIds -> newTransactionIds
                            // freqList -> newFreqlist
                            LcmIterNew(database, qSets, newTransactionIds, newFreqList);
                        }
                    }
                }
            }
        }
    }

    private void updateFreqList(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets, ArrayList<Integer> freqItemset, int freqClusterId, ArrayList<Integer> newFreqList) {
        // TODO Auto-generated method stub

    }

    private void updateOccurenceDeriver(Database database, Set<Integer> newTransactionIds) {
        // TODO Auto-generated method stub

    }

    private void updateTransactions(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets, int freqClusterId, Set<Integer> newTransactionIds) {

        for (int transactionId : transactionIds) {
            Transaction transaction = database.getTransaction(transactionId);
            boolean canAdd = true;
            for (int qSetItem : qSets) {
                if (qSetItem >= freqClusterId && !transaction.getClusterIds().contains(qSetItem)) {
                    canAdd = false;
                }
            }

            if (canAdd) {
                newTransactionIds.add(transactionId);
            }
        }

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

        for (Integer item : itemset) {
            qSets.add(item);
        }

        qSets.add(freq);

        SortedSet<Integer> lowerBoundSet = lower_bound(database.getClusterIds(), freq + 1);

        for (int clusterId : lowerBoundSet) {
            if (CheckItemInclusion(database, transactionIds, clusterId)) {
                qSets.add(clusterId);
            }
        }
    }


    /**
     * CheckItemInclusion
     * Check whether clusterId is included in the transactions pointed to transactions
     *
     * @param database       la base de données
     * @param transactionIds la liste des transactions
     * @param clusterId      clusterId to find
     * @return
     */
    private boolean CheckItemInclusion(Database database, Set<Integer> transactionIds, int clusterId) {
        for (int transactionId : transactionIds) {
            if (database.getTransaction(transactionId).getClusterIds().contains(clusterId)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param database            la database a analyser
     * @param itemset             une liste representant les id/itemsets
     * @param generatedItemsets   une liste reprensentant les itemsetsGenerees
     * @param generatedTimeIds    une liste reprensentant les tempsGenerees
     * @param generatedClusterIds une liste reprensentant les itemGenerees
     */
    private void generateItemset(Database database, ArrayList<Integer> itemset,
                                 ArrayList<ArrayList<Integer>> generatedItemsets,
                                 ArrayList<Set<Integer>> generatedTimeIds,
                                 ArrayList<TreeSet<Integer>> generatedClusterIds) {

        if (itemset.size() == 0) {
            sizeGenerated = 0;
            generatedItemsets.add(itemset);
            return;
        }

        generatedItemsets.clear();

        TreeSet<Integer> timeIds = database.getTimeIds();
        Set<Integer> listOfTimes = new HashSet<>();
        boolean monoCluster = true;

        // liste des temps qui n'ont qu'un seul cluster
        for (int itemIndex = 0; itemIndex < itemset.size(); ++itemIndex) {
            int item = itemset.get(itemIndex);
            listOfTimes.add(database.getCluster(item).getTimeId());

            if (itemIndex != itemset.size() - 1) {
                int nextItem = itemset.get(itemIndex + 1);
                if (database.getCluster(item).getTimeId() == database.getCluster(nextItem).getTimeId()) {
                    monoCluster = false;
                    break;
                }
            }
        }


        if (monoCluster) {
            generatedTimeIds.add(listOfTimes);
            generatedClusterIds.add(database.getClusterIds());
            return;
        }

        //Manage MultiClustering
        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>();

        for (int timeId : listOfTimes) {
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
            generatedItemsets.add(singleton);
            System.out.println("Add Singleton : " + singleton);
        }

        for (int timeId = 1; timeId < timesClusterIds.size(); ++timeId) {
            ArrayList<Integer> clusterIds = timesClusterIds.get(timeId);
            ArrayList<ArrayList<Integer>> results = new ArrayList<>();

            for (ArrayList<Integer> generatedItems : generatedItemsets) {
                for (int item : clusterIds) {
                    ArrayList<Integer> result = new ArrayList<>(generatedItems);
                    result.add(item);
                    results.add(result);
                }
            }

            System.out.println("Test : " + results);
            generatedItemsets = results;
        }

        // Remove the itemsets already existing in the set of transactions
        System.out.println("Remove Itemsets already existing");
        ArrayList<ArrayList<Integer>> checkedItemsets = new ArrayList<>();

        boolean insertok;

        for (ArrayList<Integer> generatedItemset : generatedItemsets) {
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
        listOfTimes.clear();

        for (ArrayList<Integer> checkedItemset : checkedItemsets) {
            for (int checkedItem : checkedItemset) {
                if (itemset.contains(checkedItem)) {
                    listOfTimes.add(database.getCluster(itemset.get(checkedItem)).getTimeId());
                }
            }
        }

        generatedItemsets.clear();
        generatedItemsets = checkedItemsets;
        generatedTimeIds.add(listOfTimes);
        generatedClusterIds.add(database.getClusterIds());
    }
}
