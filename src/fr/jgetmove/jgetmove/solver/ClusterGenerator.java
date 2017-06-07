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
    private ArrayList<ArrayList<Integer>> clustersGenerated;
    private int minSupport, maxPattern, minTime;
    private ArrayList<ArrayList<Integer>> lvl2ClusterIds;
    private ArrayList<ArrayList<Integer>> lvl2TimeIds;

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
        this.clustersGenerated = new ArrayList<>();
        lvl2ClusterIds = new ArrayList<>();
        lvl2TimeIds = new ArrayList<>();
    }

    /**
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
     * @deprecated use
     * {@link GeneratorUtils#makeClosure(Database, Set, ArrayList, ArrayList, int)}
     */
    @TraceMethod(displayTitle = true)
    private static void MakeClosure(Database database, Set<Integer> transactionIds, ArrayList<Integer> qSets,
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
     * {@link GeneratorUtils#ppcTest(Database, ArrayList, Set, int, Set)}
     */
    @TraceMethod(displayTitle = true)
    private static boolean PPCTest(Database database, ArrayList<Integer> clusters, Set<Integer> transactionIds,
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
    private static int CalcurateCoreI(ArrayList<Integer> clusterIds, ArrayList<Integer> frequentClusterIds) {
        return GeneratorUtils.getDifferentFromLastCluster(clusterIds, frequentClusterIds);
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
    public ClusterGeneratorResult generate() {
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

        Database database2 = new Database(transactions);

        return new ClusterGeneratorResult(database2, clustersGenerated, lvl2TimeIds,
                lvl2ClusterIds);
    }

    /**
     * Getter sur la liste des itemsets
     *
     * @return la liste des itemsets genérées
     */
    public ArrayList<ArrayList<Integer>> getClustersGenerated() {
        return clustersGenerated;
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
    private void run(ClusterMatrix clusterMatrix, ArrayList<Transaction> transactions, ArrayList<Integer> clusterIds,
                     Set<Integer> transactionIds, ArrayList<Integer> frequentClusterIds, int[] numItems) {
        Debug.println("clusterMatrix", clusterMatrix, Debug.DEBUG);
        Debug.println("clustersIds", clusterIds, Debug.DEBUG);
        Debug.println("transactionIds ", transactionIds, Debug.DEBUG);
        Debug.println("frequentClusterIds ", frequentClusterIds, Debug.DEBUG);

        ArrayList<ArrayList<Integer>> generatedArrayOfClusters = generateClusters(clusterIds);

        if (generatedArrayOfClusters.size() > 0 && generatedArrayOfClusters.get(0).size() > 0) {
            // TODO: ca pourrait être optimisé tout ca :/
            clustersGenerated.addAll(generatedArrayOfClusters);
        }

        Debug.println("GeneratedItemsets", generatedArrayOfClusters, Debug.DEBUG);
        Debug.println("GeneratedItemId", defaultDatabase.getClusterIds(), Debug.DEBUG);

        for (ArrayList<Integer> generatedClusters : generatedArrayOfClusters) {
            PrintItemsetsNew(clusterMatrix, generatedClusters, transactions, numItems);

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
                Set<Integer> newTransactionIds = new HashSet<>(); // newTransactionList

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
     * Lcm::PrintItemsetsNew([]itemsets, occ, [Transaction]transactionsets,
     * numItems, []timeID,[][]level2ItemID,
     * [][]level2TimeID)
     * </pre>
     *
     * @param clusterMatrix (occ)
     * @param clusterIds    (itemsets)
     * @param transactions  (transactionsets)
     * @param numClusters   (numItems)
     * @deprecated use {@link #printClustersNew(ClusterMatrix, ArrayList, ArrayList, int[])}
     */
    private void PrintItemsetsNew(ClusterMatrix clusterMatrix, ArrayList<Integer> clusterIds, ArrayList<Transaction> transactions,
                                  int[] numClusters) {
        printClustersNew(clusterMatrix, clusterIds, transactions, numClusters);
    }

    /**
     * <pre>
     * Lcm::PrintItemsetsNew([]itemsets, occ, [Transaction]transactionsets,
     * numItems, []timeID,[][]level2ItemID,
     * [][]level2TimeID)
     * </pre>
     *
     * @param clusterMatrix (occ)
     * @param clusterIds    (itemsets)
     * @param transactions  (transactionsets)
     * @param numClusters   (numItems)
     */
    private void printClustersNew(ClusterMatrix clusterMatrix, ArrayList<Integer> clusterIds, ArrayList<Transaction> transactions,
                                  int[] numClusters) {
        //TODO
        Debug.println("ItemsetSize : " + clusterIds.size());
        Debug.println("MinTime : " + minTime);

        if (clusterIds.size() > minTime) {
            ArrayList<Integer> timeIds = new ArrayList<>();
            ArrayList<Integer> clusterList = new ArrayList<>();
            timeIds.add(0);
            clusterList.add(0);

            for (Integer clusterId : clusterIds) {
                clusterList.add(clusterId);
                timeIds.add(clusterMatrix.getClusterTimeId(clusterId));
            }
            lvl2ClusterIds.add(clusterList);
            lvl2TimeIds.add(timeIds);

            Set<Integer> transactionOfLast =
                    clusterMatrix.getClusterTransactionIds(clusterIds.get(clusterIds.size() - 1));

            //Pour chaque transaction, on ajoute le cluster qui a l'id numItem
            for (Integer transactionId : transactionOfLast) {
                transactions.get(transactionId).add(new Cluster(numClusters[0]));
            }

            numClusters[0]++;
        }
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
     * @param database          (occurence)
     * @param newTransactionIds (transactionList)
     * @deprecated use {@link ClusterMatrix#optimizeMatrix(Database, Set)}
     */
    private void updateOccurenceDeriver(ClusterMatrix database, Set<Integer> newTransactionIds) {
        database.optimizeMatrix(defaultDatabase, newTransactionIds);
    }

    /**
     * <pre>
     * Lcm::GenerateItemset(Database,[]itemsets,[]itemID,[]timeID,[][]generatedItemsets, [][]generatedtimeID,[][]generateditemID,sizeGenerated)
     * </pre>
     *
     * @param clusterIds (itemsets) une liste representant les clusterId
     */
    private ArrayList<ArrayList<Integer>> generateClusters(ArrayList<Integer> clusterIds) {
        // todo aplatir generated* dans cette fonction : raison, non utilisées

        ArrayList<ArrayList<Integer>> generatedArrayOfClusters = new ArrayList<>();

        if (clusterIds.size() == 0) {
            generatedArrayOfClusters.add(clusterIds);
            return generatedArrayOfClusters;
        }

        boolean isMonoCluster = true; // numberSameTime

        int lastTime = 0;
        // liste des temps qui n'ont qu'un seul cluster
        for (int i = 0; i < clusterIds.size(); ++i) {
            int clusterId = clusterIds.get(i);
            lastTime = defaultDatabase.getClusterTimeId(clusterId);

            if (defaultDatabase.getClusterTime(clusterId).getClusters().size() > 1) {
                isMonoCluster = false;
            }

            /*if (i != clusterIds.size() - 1) {
                int nextClusterId = clusterIds.get(i + 1);
                if (defaultDatabase.getClusterTimeId(clusterId)
                        == defaultDatabase.getClusterTimeId(nextClusterId)) {
                    isMonoCluster = false;
                }
            }*/
        }

        if (isMonoCluster) {
            generatedArrayOfClusters.add(clusterIds); // why
            //generatedArrayOfTimeIds.add(times); // whyyy
            //generatedArrayOfClusterIds.add(database.getClusterIds()); // but why ???
            return generatedArrayOfClusters;
        }

        //Manage MultiClustering

        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>(); // PosDates

        for (int i = 1; i <= lastTime; i++) {
            ArrayList<Integer> tempClusterIds = new ArrayList<>();

            for (int clusterId : clusterIds) {
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
