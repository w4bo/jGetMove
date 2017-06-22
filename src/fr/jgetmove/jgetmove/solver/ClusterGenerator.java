package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;
import fr.jgetmove.jgetmove.utils.ArrayUtils;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;

import java.util.*;

public class ClusterGenerator implements Generator {

    private final Database defaultDatabase;
    ArrayList<ArrayList<Integer>> lvl2ClusterIds;
    ArrayList<ArrayList<Integer>> lvl2TimeIds;
    private int minSupport, maxPattern, minTime;

    /**
     * Initialise le solveur.
     *
     * @param database database par defaut
     */
    public ClusterGenerator(Database database, DefaultConfig config) {
        this.minSupport = config.getMinSupport();
        this.maxPattern = config.getMaxPattern();
        this.minTime = config.getMinTime();
        this.defaultDatabase = database;
        lvl2ClusterIds = new ArrayList<>();
        lvl2TimeIds = new ArrayList<>();
    }

    /**
     * <pre>
     * Lcm::MakeClosure(const Database &database, vector&lt;int&gt; &transactionList,
     * vector&lt;int&gt; &q_sets, vector&lt;int&gt; &itemsets,
     * int item)
     * </pre>
     *
     * @param database       (database)
     * @param transactionIds (transactionList)
     * @param qSets          (q_sets)
     * @param itemset        (itemsets)
     * @param freq           (item)
     * @deprecated use
     * {@link GeneratorUtils#makeClosure(Database, Set, ArrayList, ArrayList, int)}
     */
    @TraceMethod(displayTitle = true)
    static void MakeClosure(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets,
                            ArrayList<Integer> itemset, int freq) {
        GeneratorUtils.makeClosure(database, transactionIds, qSets, itemset, freq);
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
     * @return vrai si ppctest est réussi
     * @deprecated use
     * {@link Database#getFilteredTransactionIdsIfHaveCluster(Set, int)}
     * and
     * {@link GeneratorUtils#ppcTest(Database, ArrayList, int, Set)}
     */
    @TraceMethod(displayTitle = true)
    static boolean PPCTest(Database database, ArrayList<Integer> clusters, Set<Integer> transactionIds,
                           int freqClusterId, Set<Integer> newTransactionIds) {
        // CalcTransactionList
        for (int transactionId : transactionIds) {
            Transaction transaction = database.getTransaction(transactionId);

            if (transaction.getClusterIds().contains(freqClusterId)) {
                newTransactionIds.add(transactionId);
            }
        }
        for (int clusterId = 0; clusterId < freqClusterId; clusterId++) {
            if (!clusters.contains(clusterId)
                    && GeneratorUtils.CheckItemInclusion(database, newTransactionIds, clusterId)) {
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
     * @return le dernier élement different du dernier clusterId de
     * frequentClusterIds, si frequentClusterIds est trop petit, renvoie
     * le premier element de clusterIds, sinon renvoi 0 si clusterIds
     * est vide
     * @deprecated use
     * {@link GeneratorUtils#getDifferentFromLastCluster(ArrayList, ArrayList)}
     */
    static int CalcurateCoreI(ArrayList<Integer> clusterIds, ArrayList<Integer> frequentClusterIds) {
        return GeneratorUtils.getDifferentFromLastCluster(clusterIds, frequentClusterIds);
    }

    private static void addTransactionToItemset(ClusterMatrix clusterMatrix, ArrayList<Integer> path, ArrayList<Transaction> transactions, int[] numItemset) {
        //TODO : opti ce truc mais en gros ça règle le pb du surplus de transactions par itemsets
        Set<Integer> transactionOfLast = clusterMatrix.getClusterTransactionIds(path.get(path.size() - 1));
        //Parmis la liste de clusters d'un itemsets, je cherche le cluster qui a le moins de transaction et ça sera par définition les transactions de mon path
        // sert à rien, déja limité par clustermatrix
            /*for (int clusterId : path) {
                if (clusterMatrix.getClusterTransactionIds(clusterId).size() < transactionOfLast.size()) {
                    transactionOfLast = clusterMatrix.getClusterTransactionIds(clusterId);
                }
            }*/

        //Pour chaque transaction, on ajoute le cluster qui a l'id numItem
        // TODO :on ajoute pour chaque transaction l'path auquel il appartient :D
        for (Integer transactionId : transactionOfLast) {
            transactions.get(transactionId).add(new Cluster(numItemset[0]));
        }

        numItemset[0]++;
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
    ClusterGeneratorResult generate() {
        ClusterMatrix clusterMatrix = new ClusterMatrix(defaultDatabase);
        Debug.println("totalItem", defaultDatabase.getClusterIds(), Debug.DEBUG);

        ArrayList<Integer> itemsets = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();
        ArrayList<Transaction> transactions = new ArrayList<>(defaultDatabase.getTransactions().size());

        for (Transaction transaction : defaultDatabase.getTransactions().values()) {
            transactions.add(new Transaction(transaction.getId()));
        }

        TreeSet<Integer> transactionIds = new TreeSet<>(defaultDatabase.getTransactionIds());


        int[] numClusters = new int[1];
        numClusters[0] = 0;

        run(clusterMatrix, transactions, itemsets, transactionIds, freqItemset, numClusters);
        Debug.println("Transactions", transactions, Debug.DEBUG);

        // Database 2 devient PathBlock
        Database database2 = new Database(transactions);

        return new ClusterGeneratorResult(database2, lvl2TimeIds,
                lvl2ClusterIds);
    }

    /**
     * Boucle r&eacute;cursive
     * <p>
     * <p>
     * <pre>
     * Lcm::LcmIterNew(database, []itemsets, []transactionList, occ,
     * []freqList, []transactionsets, numItems, []itemID,
     * []timeID, [][]level2ItemID, [][]level2TimeID) {
     * </pre>
     *
     * @param clusterMatrix      (database, , itemID, timeID) La database &agrave; analyser
     * @param transactions       (transactionsets)
     * @param clusterIds         (itemsets) une liste representant les id
     * @param transactionIds     (transactionList)
     * @param frequentClusterIds (freqList) une liste representant les clusterIds frequents
     * @param numItems           (numItems)
     */
    @TraceMethod(displayTitle = true)
    void run(ClusterMatrix clusterMatrix, ArrayList<Transaction> transactions, ArrayList<Integer> clusterIds,
             Set<Integer> transactionIds, ArrayList<Integer> frequentClusterIds, int[] numItems) {
        Debug.println("clusterMatrix", clusterMatrix, Debug.DEBUG);
        Debug.println("clustersIds", clusterIds, Debug.DEBUG);
        Debug.println("transactionIds ", transactionIds, Debug.DEBUG);
        Debug.println("frequentClusterIds ", frequentClusterIds, Debug.DEBUG);

        ArrayList<ArrayList<Integer>> generatedArrayOfClusters = generateItemsets(clusterIds);

        Debug.println("GeneratedItemsets", generatedArrayOfClusters, Debug.DEBUG);
        Debug.println("GeneratedItemId", defaultDatabase.getClusterIds(), Debug.DEBUG);

        for (ArrayList<Integer> generatedClusters : generatedArrayOfClusters) {
            printItemsetsNew(clusterMatrix, generatedClusters, transactions, numItems);

            int calcurateCoreI = CalcurateCoreI(generatedClusters, frequentClusterIds);

            Debug.println("Core_i : " + calcurateCoreI, Debug.DEBUG);

            SortedSet<Integer> lowerBounds = GeneratorUtils
                    .lower_bound(defaultDatabase.getClusterIds(), calcurateCoreI);

            // freq_i
            ArrayList<Integer> freqClusterIds = new ArrayList<>();
            Debug.println("lower bounds", lowerBounds.size(), Debug.DEBUG);

            for (int clusterId : lowerBounds) {
                Debug.println("GeneratedClusters", generatedClusters, Debug.DEBUG);
                Debug.println(" ClusterId", clusterId, Debug.DEBUG);
                Debug.println("Min Support", minSupport, Debug.DEBUG);

                if (clusterMatrix.getClusterTransactionIds(clusterId).size() >= minSupport &&
                        !generatedClusters.contains(clusterId)) {
                    freqClusterIds.add(clusterId);
                }
            }

            Debug.println("Frequent : " + freqClusterIds);

            for (int freqClusterId : freqClusterIds) {
                //Set<Integer> newTransactionIds = defaultDatabase.getFilteredTransactionIdsIfHaveCluster(transactionIds, freqClusterId);
                Set<Integer> newTransactionIds = new HashSet<>(); // newTransactionList
                // if(GeneratorUtils.ppcTest(defaultDatabase, generatedClusters, freqClusterId, newTransactionIds)){
                if (PPCTest(defaultDatabase, generatedClusters, transactionIds, freqClusterId, newTransactionIds)) {
                    ArrayList<Integer> qSets = new ArrayList<>();

                    MakeClosure(defaultDatabase, newTransactionIds, qSets, generatedClusters, freqClusterId);
                    if (maxPattern == 0 || qSets.size() <= maxPattern) {
                        Set<Integer> updatedTransactionIds = GeneratorUtils
                                .updateTransactions(defaultDatabase, transactionIds, qSets, freqClusterId);
                        // newFreqList
                        ArrayList<Integer> updatedFreqList = GeneratorUtils
                                .updateFreqList(defaultDatabase, transactionIds, qSets, frequentClusterIds,
                                        freqClusterId);

                        updateOccurenceDeriver(clusterMatrix, updatedTransactionIds);

                        // clusterIds -> qSets
                        // transactionIds -> updatedTransactionIds
                        // frequentClusterIds -> updatedFreqList
                        run(clusterMatrix, transactions, qSets, updatedTransactionIds, updatedFreqList, numItems);

                    }
                }
            }
        }
    }

    /**
     * <pre>
     * Lcm::printItemsetsNew([]itemsets, occ, []transactionsets, numItems, []timeID, [][]level2ItemID, [][]level2TimeID)
     * </pre>
     *
     * @param clusterMatrix (occ)
     * @param path          (itemsets)
     * @param transactions  (transactionsets)
     * @param numItemset    (numItems)
     */
    void printItemsetsNew(ClusterMatrix clusterMatrix, ArrayList<Integer> path, ArrayList<Transaction> transactions,
                          int[] numItemset) {
        Debug.println("ItemsetSize : ", path.size(), Debug.DEBUG);
        Debug.println("MinTime : ", minTime, Debug.DEBUG);

        //TODO back to minTime for itemsize
        if (path.size() > 0) { // code c complient
            //if (path.size() > minTime) {
            addPathToBlockPath(clusterMatrix, path);
            addTransactionToItemset(clusterMatrix, path, transactions, numItemset);
        }
    }

    private void addPathToBlockPath(ClusterMatrix clusterMatrix, ArrayList<Integer> path) {
        ArrayList<Integer> timeIds = new ArrayList<>();
        ArrayList<Integer> clusterList = new ArrayList<>();
        timeIds.add(0);
        clusterList.add(0);

        for (Integer clusterId : path) {
            clusterList.add(clusterId);
            timeIds.add(clusterMatrix.getClusterTimeId(clusterId));
        }
        lvl2ClusterIds.add(clusterList);
        lvl2TimeIds.add(timeIds);
    }

    /**
     * Supprime et recrée les associations {@link Cluster} <=> {@link Transaction} en fonction des transactions à prendre en compte
     * <p>
     * <pre>
     * Lcm::UpdateOccurenceDeriver(const Database &database, const vector<int> &transactionList, ClusterMatrix &occurence)
     * </pre>
     * <p>
     * defaultDatabase (database)
     *
     * @param clusterMatrix     (occurence)
     * @param newTransactionIds (transactionList)
     * @deprecated use {@link ClusterMatrix#optimizeMatrix(Database, Set)}
     */
    void updateOccurenceDeriver(ClusterMatrix clusterMatrix, Set<Integer> newTransactionIds) {
        clusterMatrix.optimizeMatrix(defaultDatabase, newTransactionIds);
    }

    /**
     * <pre>
     * Lcm::GenerateItemset(Database,[]itemsets,[]itemID,[]timeID,[][]generatedItemsets, [][]generatedtimeID,[][]generateditemID,sizeGenerated)
     * </pre>
     *
     * @param itemset (itemsets) une liste representant les clusterId
     */
    ArrayList<ArrayList<Integer>> generateItemsets(ArrayList<Integer> itemset) {
        // todo aplatir generated* dans cette fonction : raison, non utilisées
        // todo faire passer Itemset en parametres et non pas un clusterIds représentant l'itemset

        ArrayList<ArrayList<Integer>> generatedArrayOfClusters = new ArrayList<>();

        if (itemset.size() == 0) {
            generatedArrayOfClusters.add(itemset);
            return generatedArrayOfClusters;
        }

        boolean oneTimePerCluster = true; // numberSameTime

        int lastTime = 0;
        // liste des temps qui n'ont qu'un seul cluster
        for (int i = 0; i < itemset.size(); ++i) {
            int clusterId = itemset.get(i);
            lastTime = defaultDatabase.getClusterTimeId(clusterId);

            if (i != itemset.size() - 1) {
                int nextClusterId = itemset.get(i + 1);
                if (defaultDatabase.getClusterTimeId(clusterId)
                        == defaultDatabase.getClusterTimeId(nextClusterId)) {
                    oneTimePerCluster = false;
                }
            }
        }

        if (oneTimePerCluster) {
            generatedArrayOfClusters.add(itemset); // why
            //generatedArrayOfTimeIds.add(times); // whyyy
            //generatedArrayOfClusterIds.add(database.getClusterIds()); // but why ???
            return generatedArrayOfClusters;
        }

        //Manage MultiClustering

        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>(); // PosDates

        for (int i = 1; i <= lastTime; i++) {
            ArrayList<Integer> tempClusterIds = new ArrayList<>();

            for (int clusterId : itemset) {
                if (defaultDatabase.getClusterTimeId(clusterId) == i) {
                    tempClusterIds.add(clusterId);
                }
            }

            timesClusterIds.add(tempClusterIds);
        }

        //sizeGenerated stands for the number of potential itemsets to generate
        Debug.println("sizeGenerated stands for the number of potential itemsets to generate");

        //initialise the set of generated itemsets
        Debug.println("initialise the set of generated itemsets");

        ArrayList<ArrayList<Integer>> tempArrayOfClusters = new ArrayList<>();
        /*
         * For each clusterId in timeClusterIds[0], push itemset of size 1
         */
        for (Integer clusterId : timesClusterIds.get(0)) {
            ArrayList<Integer> singleton = new ArrayList<>(1);
            singleton.add(clusterId);
            tempArrayOfClusters.add(singleton);
            Debug.println("Add Singleton", singleton, Debug.WARNING);
        }
        /*
         * For each time [1+], get the clusters associated
         */
        for (int clusterIdsIndex = 1, size = timesClusterIds.size(); clusterIdsIndex < size; ++clusterIdsIndex) {
            ArrayList<Integer> tempClusterIds = timesClusterIds.get(clusterIdsIndex);
            ArrayList<ArrayList<Integer>> results = new ArrayList<>();

            for (ArrayList<Integer> generatedItems : tempArrayOfClusters) {
                for (int item : tempClusterIds) {
                    ArrayList<Integer> result = new ArrayList<>(generatedItems);
                    result.add(item);
                    results.add(result); // but why ???
                }
            }
            tempArrayOfClusters = results;
        }

        // Remove the itemsets already existing in the set of transactions
        Debug.println("Remove Itemsets already existing");
        ArrayList<ArrayList<Integer>> checkedArrayOfClusterIds = new ArrayList<>(); //checkedItemsets

        boolean insertok;

        for (ArrayList<Integer> currentClusterIds : tempArrayOfClusters) {
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

        // updating list of dates
        //Debug.println("updating list of dates");
        /*times.clear();


        for (ArrayList<Integer> checkedClusterIds : checkedArrayOfClusterIds) {
            for (int checkedClusterId : checkedClusterIds) {
                if (clusterIds.contains(checkedClusterId)) {
                    times.add(defaultDatabase.getClusterTimeId(checkedClusterId));
                }
            }
        }*/

        generatedArrayOfClusters.clear();
        generatedArrayOfClusters.addAll(checkedArrayOfClusterIds); // why
        //generatedArrayOfTimeIds.add(times); // whyy
        //generatedArrayOfClusterIds.add(database.getClusterIds()); // but whyy ?
        return generatedArrayOfClusters;
    }
}
