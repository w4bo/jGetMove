package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class Solver implements ISolver {

    private int minSupport, maxPattern, minTime;
    private Set<Integer> totalItem;

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

    private static int lower_bound(ArrayList<Integer> array, int key) {
        int lenght = array.size();
        int low = 0;
        int high = lenght - 1;
        int mid = (low + high) / 2;

        while (true) {

            if ((array.get(mid) <= key)) {
                low = mid + 1;
                if (high < low)
                    return mid < (lenght - 1) ? mid + 1 : -1;
            } else {
                high = mid - 1;
                if (high < low)
                    return mid;
            }
            mid = (low + high) / 2;
        }
    }

    /**
     * Initialise le solver à partir d'une base de données
     *
     * @param database la base de données à analyser
     */
    public void initLcm(Database database) {

        totalItem = database.getItemset();
        System.out.println("totalItem : " + totalItem);

        ArrayList<Integer> itemsets = new ArrayList<>();
        ArrayList<Integer> freqList = new ArrayList<>();

        LcmIterNew(database, itemsets, freqList);
    }

    /**
     * @param database La database à analyser
     * @param itemsets une liste representant les id/itemsets
     * @param freqList une liste reprensentant less clusters frequents
     */
    private void LcmIterNew(Database database, ArrayList<Integer> itemsets, ArrayList<Integer> freqList) {

        ArrayList<ArrayList<Integer>> generatedItemsets = new ArrayList<>();
        ArrayList<ArrayList<Integer>> generatedTimeId = new ArrayList<>();
        ArrayList<ArrayList<Integer>> generatedItemId = new ArrayList<>();

        sizeGenerated = 1;

        GenerateItemset(database, itemsets, generatedItemsets, generatedTimeId, generatedItemId);

        System.out.println("GeneratedItemsets : " + generatedItemsets);
        System.out.println("GeneratedItemId : " + generatedItemId);
        System.out.println("GeneratedTimeId : " + generatedTimeId);
        System.out.println("SizeGenerated : " + sizeGenerated);

        for (ArrayList<Integer> generatedItemset : generatedItemsets) {
            System.out.println("Test");
            int core_i = CalcurateCoreI(database, generatedItemset, freqList);
            System.out.println("Core_i : " + core_i);

            int index = lower_bound(new ArrayList<>(totalItem), core_i);

            ArrayList<Integer> freq_i = new ArrayList<>();

            for (int i = index; i < totalItem.size(); i++) {
                System.out.println("Total Item : " + totalItem);
                System.out.println("Index : " + i);
                System.out.println("Transaction : " + database.getTransactions());

                if (!((database.getClusters().get(i).getTransactions().size()) >= minSupport) &&
                        Collections.binarySearch(generatedItemset, i) == 0) {
                    freq_i.add(i);
                }

                ArrayList<Integer> newTransactionList = new ArrayList<>();
                ArrayList<Integer> q_sets = new ArrayList<>();
                ArrayList<Integer> newFreqList = new ArrayList<>();

                for (int j = 0; j < freq_i.size(); j++) {
                    newTransactionList.clear();

                    //if (PPCTest)

                    q_sets.clear();

                    MakeClosure(database, newTransactionList, q_sets, generatedItemset, j);
                }
            }
        }
    }

    private int CalcurateCoreI(Database database, ArrayList<Integer> itemsets, ArrayList<Integer> freqList) {
        // TODO Auto-generated method stub
        int nbTransactions = database.getTransactions().size();
        Set<Integer> tempo;

        for (int i = 0; i < nbTransactions; i++) {
            tempo = database.getTransaction(i).getClusterIds();
        }

        if (itemsets.size() > 0) {
            int current = freqList.get(freqList.size() - 1);

            for (int i = freqList.size() - 1; i >= 0; i--) {
                if (current != freqList.get(i)) {
                    return freqList.get(i);
                }
            }

            return itemsets.get(0);
        }
        return 0;
    }

    private void MakeClosure(Database database, ArrayList<Integer> transactionList, ArrayList<Integer> q_sets, ArrayList<Integer> itemSet, int freq) {

        for (Integer item : itemSet) {
            q_sets.add(item);
        }

        q_sets.add(freq);

        int index = lower_bound(new ArrayList<>(totalItem), freq + 1);

        for (int i = index; i < totalItem.size(); i++) {
            if (CheckItemInclusion(database, transactionList, i)) {
                q_sets.add(i);
            }
        }
    }


    /**
     * CheckItemInclusion
     * Check whether item is included in the transactions pointed to transactionList
     *
     * @param database        la base de données
     * @param transactionList la liste des transactions
     * @param item            item to find
     * @return
     */
    private boolean CheckItemInclusion(Database database, ArrayList<Integer> transactionList, int item) {
        // TODO Auto-generated method stub

        for (int transactionId : transactionList) {
            if (!database.getTransaction(transactionId).getClusterIds().contains(item)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param database          la database a analyser
     * @param itemsets          une liste representant les id/itemsets
     * @param generatedItemsets une liste reprensentant les itemsetsGenerees
     * @param generatedTimeId   une liste reprensentant les tempsGenerees
     * @param generatedItemId   une liste reprensentant les itemGenerees
     */
    private void GenerateItemset(Database database, ArrayList<Integer> itemsets,
                                 ArrayList<ArrayList<Integer>> generatedItemsets, ArrayList<ArrayList<Integer>> generatedTimeId,
                                 ArrayList<ArrayList<Integer>> generatedItemId) {

        if (itemsets.size() == 0) {
            sizeGenerated = 0;
            generatedItemsets.add(itemsets);
            return;
        }

        Integer[] timeIds = database.getTimeIds().toArray(new Integer[database.getTimeIds().size()]);
        ArrayList<Integer> clusterIds = new ArrayList<>(database.getClusterIds());
        ArrayList<Integer> listOfDates = new ArrayList<>();

        int numberSameTime = 0;
        int lastTime = 0;

        for (int i = 0; i < itemsets.size(); i++) {
            listOfDates.add(timeIds[itemsets.get(i)]);

            if (i != itemsets.size() - 1) {
                if (timeIds[itemsets.get(i)] == timeIds[itemsets.get(i + 1)]) {
                    numberSameTime++;
                }
            }
            lastTime = timeIds[itemsets.get(i)];
        }
        generatedItemsets.clear();

        if (numberSameTime == 0) {
            generatedTimeId.add(listOfDates);
            generatedItemId.add(clusterIds);
        }

        //Manage MultiClustering
        ArrayList<ArrayList<Integer>> posDates = new ArrayList<>();

        for (int i = 1; i < lastTime; i++) {
            ArrayList<Integer> row = new ArrayList<>();

            for (int j = 0; j < itemsets.size(); j++) {
                if (timeIds[itemsets.get(i)] == i) {
                    row.add(itemsets.get(i));
                }
            }

            posDates.add(row);
        }

        //sizeGenerated stands for the number of potential itemsets to generate
        System.out.println("sizeGenerated stands for the number of potential itemsets to generate");
        sizeGenerated = 1;

        for (ArrayList<Integer> posDate : posDates) {
            sizeGenerated *= posDate.size();
        }

        //initialise the set of generated itemsets
        System.out.println("initialise the set of generated itemsets");

        for (int itemsetIndex = 0; itemsetIndex < posDates.size(); ++itemsetIndex) {
            ArrayList<Integer> itemset = posDates.get(itemsetIndex);
            ArrayList<Integer> singleton = new ArrayList<>();

            if (itemsetIndex == 0) {
                for (Integer iterator : itemset) {
                    singleton.add(iterator);
                    System.out.println("Add Singleton : " + singleton);
                    generatedItemsets.add(singleton);
                }

            } else {
                //OUI
                ArrayList<ArrayList<Integer>> newResults = new ArrayList<>();
                ArrayList<Integer> result = new ArrayList<>();

                for (ArrayList<Integer> iterator : generatedItemsets) {
                    for (Integer itItem : itemset) {
                        result = iterator;
                        result.add(itItem);
                        newResults.add(result);
                    }
                }

                System.out.println("Test : " + result);
                generatedItemsets = newResults;
            }
        }
        // Remove the itemsets already existing in the set of transactions

        System.out.println("Remove Itemsets already existing");

        Set<Integer> tempo;
        ArrayList<ArrayList<Integer>> CheckedItemsets = new ArrayList<>();
        ArrayList<Integer> currentItemsets = new ArrayList<>();
        int nb = database.getNumberOfTransaction();
        int nbitemsets = 0;
        boolean insertok;

        for (ArrayList<Integer> generatedItemset : generatedItemsets) {
            insertok = true;
            currentItemsets.clear();
            currentItemsets = generatedItemset;

            for (int i = 0; i < nb; i++) {
                tempo = database.getTransaction(i).getClusterIds();
                if (tempo == generatedItemset) {
                    insertok = false;
                    break;
                }
            }

            if (insertok) {
                CheckedItemsets.add(currentItemsets);
                nbitemsets++;
            }
        }

        sizeGenerated = nbitemsets;
        generatedItemsets.clear();
        generatedItemsets = CheckedItemsets;
        System.out.println("GeneratedItemsets : " + CheckedItemsets);

        // updating list of dates
        System.out.println("updating list of dates");
        listOfDates.clear();

        for (int l = 0; l < sizeGenerated; l++) {
            for (int u = 0; u < generatedItemsets.get(l).size(); u++) {
                for (int itemset : itemsets) {
                    if (generatedItemsets.get(l).get(u) == itemset) {
                        listOfDates.add(timeIds[itemset]);
                    }
                }
            }
        }

        generatedTimeId.add(listOfDates);
        generatedItemId.add(clusterIds);
    }
}
