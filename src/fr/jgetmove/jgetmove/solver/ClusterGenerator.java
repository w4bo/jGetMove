package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;
import fr.jgetmove.jgetmove.utils.ArrayUtils;

import java.util.*;

public class ClusterGenerator implements Generator {

    private final Database defaultDatabase;
    private ArrayList<ArrayList<Integer>> clustersGenerated;
    private int minSupport, maxPattern, minTime;

    /**
     * Initialise le solveur.
     *
     * @param database
     * @param minSupport support minimal
     * @param maxPattern nombre maximal de pattern a trouvé
     * @param minTime    temps minimal
     */
    public ClusterGenerator(Database database, int minSupport, int maxPattern, int minTime) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
        this.clustersGenerated = new ArrayList<>();
        this.defaultDatabase = database;
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
     */
    @TraceMethod
    public ArrayList<ArrayList<Integer>> generate() {
        Database database = new Database(defaultDatabase);
        Debug.println("totalItem", database.getClusterIds());

        ArrayList<Integer> itemsets = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();
        ArrayList<Transaction> transactions = new ArrayList<>(defaultDatabase.getTransactions().size());

        for (Transaction transaction : defaultDatabase.getTransactions().values()) {
            transactions.add(new Transaction(transaction.getId()));
        }

        int[] numClusters = new int[1];
        numClusters[0] = 0;

        run(database, transactions, itemsets, database.getTransactionIds(), freqItemset, numClusters);

        return clustersGenerated;
    }

    /**
     * Boucle r&eacute;cursive
     * <p>
     * <pre>
     * Lcm::LcmIterNew(database, []itemsets, []transactionList, occ,
     * []freqList, []transactionsets, numItems, []itemID,
     * []timeID, [][]level2ItemID, [][]level2TimeID) {
     * </pre>
     *
     * @param database           (database, , itemID, timeID) La database &agrave; analyser
     * @param transactions       (transactionsets)
     * @param clusterIds         (itemsets) une liste representant les id
     * @param transactionIds     (transactionList)
     * @param frequentClusterIds (freqList) une liste reprensentant les clusterIds frequents
     * @param numItems           (numItems)
     */
    @TraceMethod
    //TODO Review and fix 
    private void run(Database database, ArrayList<Transaction> transactions, ArrayList<Integer> clusterIds, Set<Integer> transactionIds, ArrayList<Integer> frequentClusterIds, int[] numItems) {
        // TODO numItems est passés en paramètres :)
        Debug.println("clustersIds " + clusterIds);
        Debug.println("transactionIds " + transactionIds);
        Debug.println("frequentClusterIds " + frequentClusterIds);

        ArrayList<ArrayList<Integer>> generatedArrayOfClusters = new ArrayList<>();
        ArrayList<Set<Integer>> generatedArrayOfTimeIds = new ArrayList<>();
        //ArrayList<TreeSet<Integer>> generatedArrayOfClusterIds = new ArrayList<>();

        int[] sizeGenerated = {1};

        generateClusters(clusterIds, generatedArrayOfClusters, generatedArrayOfTimeIds, sizeGenerated);

        if (generatedArrayOfClusters.get(0).size() > 0) {
            // TODO: ca pourrait être optimisé tout ca :/
            clustersGenerated.addAll(generatedArrayOfClusters);
        }

        Debug.println("GeneratedItemsets : " + generatedArrayOfClusters);
        Debug.println("GeneratedItemId : " + defaultDatabase.getClusterIds());
        Debug.println("GeneratedTimeId : " + generatedArrayOfTimeIds);
        Debug.println("SizeGenerated : " + sizeGenerated[0]);
        Debug.println("");

        for (ArrayList<Integer> generatedClusters : generatedArrayOfClusters) {
            // TODO : PrintItemsetsNew on l'a oublié
            PrintItemsetsNew(database, generatedClusters, transactions, numItems);

            int calcurateCoreI = CalcurateCoreI(generatedClusters, frequentClusterIds);

            Debug.println("Core_i : " + calcurateCoreI);

            SortedSet<Integer> lowerBounds = lower_bound(defaultDatabase.getClusterIds(), calcurateCoreI);

            // freq_i
            ArrayList<Integer> freqClusterIds = new ArrayList<>();
            Debug.println("lower bounds : " + lowerBounds.size());

            for (int clusterId : lowerBounds) {
                Debug.println("GeneratedClusters : " + generatedClusters + " Cluster : " + database.getCluster(clusterId) + " Min Support : " + minSupport);

                Debug.println(database.getCluster(clusterId));
                if (database.getClusterTransactions(clusterId).size() >= minSupport &&
                        !generatedClusters.contains(clusterId)) {
                    freqClusterIds.add(clusterId);
                }
            }

            Debug.println("Frequent : " + freqClusterIds);

            for (int freqClusterId : freqClusterIds) {
                Set<Integer> newTransactionIds = new HashSet<>(); // newTransactionList

                if (PPCTest(generatedClusters, transactionIds, freqClusterId, newTransactionIds)) {
                    ArrayList<Integer> qSets = new ArrayList<>();

                    makeClosure(newTransactionIds, qSets, generatedClusters, freqClusterId);
                    if (maxPattern == 0 || qSets.size() <= maxPattern) {
                        Set<Integer> updatedTransactionIds = updateTransactions(transactionIds, qSets, freqClusterId);
                        // newFreqList
                        ArrayList<Integer> updatedFreqList = updateFreqList(transactionIds, qSets, frequentClusterIds, freqClusterId);

                        database = updateOccurenceDeriver(database, updatedTransactionIds);

                        // clusterIds -> qSets
                        // transactionIds -> updatedTransactionIds
                        // frequentClusterIds -> updatedFreqList
                        run(database, transactions, qSets, updatedTransactionIds, updatedFreqList, numItems);

                    }
                }
            }
        }
    }

    /**
     * <pre>
     * Lcm::PrintItemsetsNew([]itemsets, occ, [Transaction]transactionsets,
     * numItems, []timeID,[][]level2ItemID,
     * [][]level2TimeID)
     * </pre>
     *
     * @param database     (occ)
     * @param clusterIds   (itemsets)
     * @param transactions (transactionsets)
     * @param numClusters  (numItems)
     * @deprecated use {@link #printClustersNew(Database, ArrayList, ArrayList, int[])}
     */
    private void PrintItemsetsNew(Database database, ArrayList<Integer> clusterIds, ArrayList<Transaction> transactions, int[] numClusters) {
        printClustersNew(database, clusterIds, transactions, numClusters);
    }

    /**
     * <pre>
     * Lcm::PrintItemsetsNew([]itemsets, occ, [Transaction]transactionsets,
     * numItems, []timeID,[][]level2ItemID,
     * [][]level2TimeID)
     * </pre>
     *
     * @param database     (occ)
     * @param clusterIds   (itemsets)
     * @param transactions (transactionsets)
     * @param numClusters  (numItems)
     */
    private void printClustersNew(Database database, ArrayList<Integer> clusterIds, ArrayList<Transaction> transactions, int[] numClusters) {
        //TODO
        Debug.println("ItemsetSize : " + clusterIds.size());
        Debug.println("MinTime : " + minTime);

        if (clusterIds.size() > minTime) {
            Set<Integer> transactionOfLast = database.getClusterTransactions(clusterIds.get(clusterIds.size() - 1)).keySet();

            //Pour chaque transaction, on ajoute le cluster qui a l'id numItem
            for (Integer transactionId : transactionOfLast) {
                transactions.get(transactionId).add(new Cluster(numClusters[0]));
            }

            numClusters[0]++;
        }
    }

    /**
     * <pre>
     * Lcm::UpdateFreqList(const Database &database, const vector<int> &transactionList, const vector<int> &gsub, vector<int> &freqList, int freq, vector<int> &newFreq)
     * </pre>
     *
     * @param transactionIds   (transactionList)
     * @param qSets            (gsub)
     * @param frequentClusters (freqList)
     * @param freqClusterId    (freq)
     * @return (newFreq)
     */
    private ArrayList<Integer> updateFreqList(Set<Integer> transactionIds, ArrayList<Integer> qSets,
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
                Transaction transaction = defaultDatabase.getTransaction(transactionId);

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

    /**
     * <pre>
     * Lcm::UpdateOccurenceDeriver(const Database &database, const vector<int> &transactionList, OccurenceDeriver &occurence)
     * </pre>
     * <p>
     * defaultDatabase (database)
     *
     * @param database          (occurence)
     * @param newTransactionIds (transactionList)
     * @deprecated use {@link #updateDatabase(Database, Set)}
     */
    private Database updateOccurenceDeriver(Database database, Set<Integer> newTransactionIds) {
        return updateDatabase(database, newTransactionIds);
    }

    private Database updateDatabase(Database database, Set<Integer> newTransactionIds) {
        database.clear();

        for (int transactionId : newTransactionIds) {
            Transaction newtransaction = database.getTransaction(transactionId);
            Transaction transaction = defaultDatabase.getTransaction(transactionId);
            Set<Integer> clusterIds = transaction.getClusterIds();

            if (newtransaction == null) {
                newtransaction = new Transaction(transactionId);
                database.add(newtransaction);
            }

            for (int clusterId : clusterIds) {
                Cluster cluster = database.getCluster(clusterId);

                if (cluster == null) {
                    cluster = new Cluster(clusterId);
                    database.add(cluster);
                }

                cluster.add(newtransaction);
                newtransaction.add(cluster);
            }

        }

        return database;
    }

    /**
     * <pre>
     * Lcm::UpdateTransactionList(const Database &database, const vector<int> &transactionList, const vector<int> &q_sets, int item, vector<int> &newTransactionList)
     * </pre>
     *
     * @param transactionIds (transactionList)
     * @param qSets          (q_sets)
     * @param freqClusterId  (item)
     * @return (newTransactionList)
     */
    private Set<Integer> updateTransactions(Set<Integer> transactionIds, ArrayList<Integer> qSets, int freqClusterId) {
        Set<Integer> newTransactionIds = new HashSet<>();

        for (int transactionId : transactionIds) {
            Transaction transaction = defaultDatabase.getTransaction(transactionId);
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
     * Teste si p(i-1) == q(i-1)
     * <p>
     * <pre>
     * Lcm::PpcTest(database, []itemsets, []transactionList, item, []newTransactionList)
     * </pre>
     *
     * @param clusters          (itemsets)
     * @param transactionIds    (transactionList)
     * @param freqClusterId     (item)
     * @param newTransactionIds (newTransactionList)
     * @return
     */
    @TraceMethod(displayTitle = true)
    private boolean PPCTest(ArrayList<Integer> clusters, Set<Integer> transactionIds, int freqClusterId, Set<Integer> newTransactionIds) {
        // CalcTransactionList
        for (int transactionId : transactionIds) {
            Transaction transaction = defaultDatabase.getTransaction(transactionId);

            if (transaction.getClusterIds().contains(freqClusterId)) {
                newTransactionIds.add(transactionId);
            }
        }
        for (int clusterId = 0; clusterId < freqClusterId; clusterId++) {
            if (!clusters.contains(clusterId) && CheckItemInclusion(newTransactionIds, clusterId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <pre>
     * Lcm::CalcurateCoreI(database, itemsets, freqList)
     * </pre>
     *
     * @param clusterIds         (itemsets)
     * @param frequentClusterIds (freqList)
     * @return le dernier élement different du dernier clusterId de frequentClusterIds, si frequentClusterIds est trop petit, renvoie le premier element de clusterIds, sinon renvoi 0 si clusterIds est vide
     * @deprecated use {@link #getDifferentFromLastCluster(ArrayList, ArrayList)}
     */
    private int CalcurateCoreI(ArrayList<Integer> clusterIds, ArrayList<Integer> frequentClusterIds) {
        return getDifferentFromLastCluster(clusterIds, frequentClusterIds);
    }

    /**
     * <pre>
     * Lcm::CalcurateCoreI(database, itemsets, freqList)
     * </pre>
     *
     * @param clusterIds         (itemsets)
     * @param frequentClusterIds (freqList)
     * @return le dernier élement different du dernier clusterId de frequentClusterIds, si frequentClusterIds est trop petit, renvoie le premier element de clusterIds, sinon renvoi 0 si clusterIds est vide
     */
    private int getDifferentFromLastCluster(ArrayList<Integer> clusterIds, ArrayList<Integer> frequentClusterIds) {
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
     * <pre>
     * Lcm::MakeClosure(const Database &database, vector<int> &transactionList,
     * vector<int> &q_sets, vector<int> &itemsets,
     * int item)
     * </pre>
     *
     * @param transactionIds (transactionList)
     * @param qSets          (q_sets)
     * @param itemset        (itemsets)
     * @param freq           (item)
     */
    @TraceMethod(displayTitle = true)
    private void makeClosure(Set<Integer> transactionIds, ArrayList<Integer> qSets, ArrayList<Integer> itemset, int freq) {
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

        SortedSet<Integer> lowerBoundSet = lower_bound(defaultDatabase.getClusterIds(), freq + 1);

        for (int clusterId : lowerBoundSet) {
            if (CheckItemInclusion(transactionIds, clusterId)) {
                qSets.add(clusterId);
            }
        }
        Debug.println("qSets : " + qSets);
    }


    /**
     * CheckItemInclusion(Database,transactionlist,item)
     * Check whether clusterId is included in any of the transactions pointed to transactions
     *
     * @param transactionIds (transactionList) la liste des transactions
     * @param clusterId      (item) clusterId to find
     * @return true if the clusterId is in one of the transactions
     * @deprecated not expressive naming use {@link #clusterInTransactions(Set, int)}
     */
    private boolean CheckItemInclusion(Set<Integer> transactionIds, int clusterId) {
        return clusterInTransactions(transactionIds, clusterId);
    }

    /**
     * Verifie si le cluster est inclus dans toute la liste des transactions
     * <p>
     * Dans GetMove :
     * <pre>
     * Lcm::CheckItemInclusion(Database,transactionList,item)
     * </pre>
     *
     * @param transactionIds (transactionList) la liste des transactions
     * @param clusterId      (item) le cluster à trouver
     * @return vrai si le cluster est présent dans toute les transactions de la liste
     */
    private boolean clusterInTransactions(Set<Integer> transactionIds, int clusterId) {
        for (int transactionId : transactionIds) {
            if (!defaultDatabase.getTransaction(transactionId).getClusterIds().contains(clusterId)) {
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
     * @param clusterIds               (itemsets) une liste representant les clusterId
     * @param generatedArrayOfClusters (generateItemsets) une liste reprensentant les itemsetsGenerees
     * @param generatedArrayOfTimeIds  (generatedtimeID) une liste reprensentant les tempsGenerees
     */
    private void generateClusters(ArrayList<Integer> clusterIds,
                                  ArrayList<ArrayList<Integer>> generatedArrayOfClusters,
                                  ArrayList<Set<Integer>> generatedArrayOfTimeIds,
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

        // liste des temps qui n'ont qu'un seul cluster
        for (int i = 0; i < clusterIds.size(); ++i) {
            int clusterId = clusterIds.get(i);
            times.add(defaultDatabase.getClusterTimeId(clusterId));

            if (i != clusterIds.size() - 1) {
                int nextClusterId = clusterIds.get(i + 1);

                if (defaultDatabase.getClusterTimeId(clusterId)
                        == defaultDatabase.getClusterTimeId(nextClusterId)) {
                    isMonoCluster = false;
                }
            }
        }

        if (isMonoCluster) {
            generatedArrayOfClusters.add(clusterIds); // why
            generatedArrayOfTimeIds.add(times); // whyyy
            //generatedArrayOfClusterIds.add(database.getClusterIds()); // but why ???
            return;
        }

        //Manage MultiClustering

        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>(); // PosDates

        for (int timeId : times) {
            ArrayList<Integer> tempClusterIds = new ArrayList<>();

            for (int clusterId : clusterIds) {
                if (defaultDatabase.getClusterTimeId(clusterId) == timeId) {
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

        /*
         * For each clusterId in timeClusterIds[0], push itemset of size 1
         */
        for (Integer clusterId : timesClusterIds.get(0)) {
            ArrayList<Integer> singleton = new ArrayList<>(1);
            singleton.add(clusterId);
            generatedArrayOfClusters.add(singleton);
            Debug.println("Add Singleton : " + singleton);
        }
        /*
         * For each time [1+], get the clusters associated
         */
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

        for (ArrayList<Integer> currentClusterIds : generatedArrayOfClusters) {
            insertok = true;

            for (Transaction transaction : defaultDatabase.getTransactions().values()) {
                if (ArrayUtils.isSame(transaction.getClusterIds(), currentClusterIds)) {
                    insertok = false;
                    break;
                }
            }

            if (insertok) {
                checkedArrayOfClusterIds.add(currentClusterIds);
            }
        }

        sizeGenerated[0] = checkedArrayOfClusterIds.size();

        // updating list of dates
        Debug.println("updating list of dates");
        times.clear();


        for (ArrayList<Integer> checkedClusterIds : checkedArrayOfClusterIds) {
            for (int checkedClusterId : checkedClusterIds) {
                if (clusterIds.contains(checkedClusterId)) {
                    times.add(defaultDatabase.getClusterTimeId(checkedClusterId));
                }
            }
        }

        generatedArrayOfClusters.clear();
        generatedArrayOfClusters = checkedArrayOfClusterIds; // why
        generatedArrayOfTimeIds.add(times); // whyy
        //generatedArrayOfClusterIds.add(database.getClusterIds()); // but whyy ?
    }

    /**
     * Verifie si un ensemble de transaction est inclus dans un autre
     * <p>
     * TODO : Documentation
     * <pre>isIncluded</pre>
     *
     * @param a le premier ensemble qui doit être contenu dans b
     * @param b le deuxieme ensemble qui doit contenir a
     * @return 2 si c'est egal
     */
    private int isAIncludedInB(HashMap<Integer, Transaction> a, HashMap<Integer, Transaction> b) {
        if (a.size() > b.size()) {
            return 0;
        }

        if (ArrayUtils.isIncluded(a.keySet(), b.keySet())) {
            if (ArrayUtils.areEqual(a.keySet(), b.keySet())) {
                return 2;
            }
            return 1;
        }

        return 0;
    }

    /**
     * Getter sur la liste des itemsets
     *
     * @return la liste des itemsets gen�r�es
     */
    public ArrayList<ArrayList<Integer>> getClustersGenerated() {
        return clustersGenerated;
    }
}
