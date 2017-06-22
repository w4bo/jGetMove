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

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
                    && database.isClusterInTransactions(newTransactionIds, clusterId)) {
                return false;
            }
        }
        return true;
    }

    static void addPathToTransaction(ClusterMatrix clusterMatrix, ArrayList<Integer> path, ArrayList<Transaction> transactions, int[] pathId) {
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
        // TODO :on ajoute pour chaque transaction le path auquel il appartient :D
        for (Integer transactionId : transactionOfLast) {
            transactions.get(transactionId).add(new Cluster(pathId[0]));
        }

        pathId[0]++;
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
     * @param path               (itemsets) une liste representant les id
     * @param transactionIds     (transactionList)
     * @param frequentClusterIds (freqList) une liste representant les clusterIds frequents
     * @param numItems           (numItems)
     */
    @TraceMethod(displayTitle = true)
    private void run(ClusterMatrix clusterMatrix, ArrayList<Transaction> transactions, ArrayList<Integer> path,
                     Set<Integer> transactionIds, ArrayList<Integer> frequentClusterIds, int[] numItems) {
        Debug.println("clusterMatrix", clusterMatrix, Debug.DEBUG);
        Debug.println("transactionIds ", transactionIds, Debug.DEBUG);
        Debug.println("frequentClusterIds ", frequentClusterIds, Debug.DEBUG);

        ArrayList<ArrayList<Integer>> generatedPaths = generatePaths(path);

        Debug.println("path", path, Debug.DEBUG);
        Debug.println("generatedPaths", generatedPaths, Debug.DEBUG);

        for (ArrayList<Integer> generatedPath : generatedPaths) {
            Debug.println("minTime : ", minTime, Debug.DEBUG);

            // Lcm::printItemsetsNew([]itemsets, occ, []transactionsets, numItems, []timeID, [][]level2ItemID, [][]level2TimeID)

            if (path.size() > 0) { // code c complient
                //TODO back to minTime for itemsize
                //if (path.size() > minTime) {
                addPathToPathBlock(clusterMatrix, generatedPath);
                addPathToTransaction(clusterMatrix, generatedPath, transactions, numItems);
            }

            int calcurateCoreI = GeneratorUtils.getDifferentFromLastCluster(generatedPath, frequentClusterIds);

            Debug.println("Core_i : " + calcurateCoreI, Debug.DEBUG);

            SortedSet<Integer> lowerBounds = defaultDatabase.getClusterIds().tailSet(calcurateCoreI);

            // freq_i
            ArrayList<Integer> freqPath = new ArrayList<>();
            Debug.println("lower bounds", lowerBounds.size(), Debug.DEBUG);

            for (int clusterId : lowerBounds) {
                Debug.println("clusterId", clusterId, Debug.DEBUG);
                Debug.println("min Support", minSupport, Debug.DEBUG);

                if (clusterMatrix.getClusterTransactionIds(clusterId).size() >= minSupport &&
                        !generatedPath.contains(clusterId)) {
                    freqPath.add(clusterId);
                }
            }

            Debug.println("Frequent : ", freqPath, Debug.DEBUG);

            for (int freqClusterId : freqPath) {
                Set<Integer> newTransactionIds = defaultDatabase.getFilteredTransactionIdsIfHaveCluster(transactionIds, freqClusterId);

                if (GeneratorUtils.ppcTest(defaultDatabase, generatedPath, freqClusterId, newTransactionIds)) {
                    ArrayList<Integer> qSets = new ArrayList<>();

                    GeneratorUtils.makeClosure(defaultDatabase, newTransactionIds, qSets, generatedPath, freqClusterId);
                    if (maxPattern == 0 || qSets.size() <= maxPattern) {
                        Set<Integer> updatedTransactionIds = GeneratorUtils
                                .updateTransactions(defaultDatabase, transactionIds, qSets, freqClusterId);
                        ArrayList<Integer> updatedFreqList = GeneratorUtils
                                .updateFreqList(defaultDatabase, transactionIds, qSets, frequentClusterIds,
                                        freqClusterId);

                        clusterMatrix.optimizeMatrix(defaultDatabase, updatedTransactionIds);

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
     * @deprecated
     */
    void printItemsetsNew(ClusterMatrix clusterMatrix, ArrayList<Integer> path, ArrayList<Transaction> transactions,
                          int[] numItemset) {
        Debug.println("ItemsetSize : ", path.size(), Debug.DEBUG);
        Debug.println("MinTime : ", minTime, Debug.DEBUG);

        //TODO back to minTime for itemsize
        if (path.size() > 0) { // code c complient
            //if (path.size() > minTime) {
            addPathToPathBlock(clusterMatrix, path);
            addPathToTransaction(clusterMatrix, path, transactions, numItemset);
        }
    }

    void addPathToPathBlock(ClusterMatrix clusterMatrix, ArrayList<Integer> path) {
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
     * <pre>
     * Lcm::GenerateItemset(Database,[]itemsets,[]itemID,[]timeID,[][]generatedItemsets, [][]generatedtimeID,[][]generateditemID,sizeGenerated)
     * </pre>
     *
     * @param path (itemsets) une liste representant les clusterId
     */
    ArrayList<ArrayList<Integer>> generatePaths(ArrayList<Integer> path) {
        // todo faire passer Itemset en parametres et non pas un clusterIds représentant l'path


        if (path.size() == 0) {
            ArrayList<ArrayList<Integer>> generatedPaths = new ArrayList<>();
            generatedPaths.add(path);
            return generatedPaths;
        }

        boolean oneTimePerCluster = true; // numberSameTime

        int lastTime = 0;
        // liste des temps qui n'ont qu'un seul cluster
        for (int i = 0; i < path.size(); ++i) {
            int clusterId = path.get(i);
            lastTime = defaultDatabase.getClusterTimeId(clusterId);

            if (i != path.size() - 1) {
                int nextClusterId = path.get(i + 1);
                if (defaultDatabase.getClusterTimeId(clusterId)
                        == defaultDatabase.getClusterTimeId(nextClusterId)) {
                    oneTimePerCluster = false;
                }
            }
        }

        if (oneTimePerCluster) {
            ArrayList<ArrayList<Integer>> generatedPaths = new ArrayList<>();

            generatedPaths.add(path); // why
            //generatedArrayOfTimeIds.add(times); // whyyy
            //generatedArrayOfClusterIds.add(database.getClusterIds()); // but why ???
            return generatedPaths;
        }

        //Manage MultiClustering

        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>(); // PosDates

        for (int i = 1; i <= lastTime; i++) {
            ArrayList<Integer> tempPath = new ArrayList<>();

            for (int clusterId : path) {
                if (defaultDatabase.getClusterTimeId(clusterId) == i) {
                    tempPath.add(clusterId);
                }
            }

            timesClusterIds.add(tempPath);
        }

        //sizeGenerated stands for the number of potential itemsets to generate
        Debug.println("sizeGenerated stands for the number of potential itemsets to generate", Debug.DEBUG);

        //initialise the set of generated itemsets
        Debug.println("initialise the set of generated itemsets", Debug.DEBUG);

        ArrayList<ArrayList<Integer>> tempPaths = new ArrayList<>();
        /*
         * For each clusterId in timeClusterIds[0], push path of size 1
         */
        for (Integer clusterId : timesClusterIds.get(0)) {
            ArrayList<Integer> singleton = new ArrayList<>(1);
            singleton.add(clusterId);
            tempPaths.add(singleton);
            Debug.println("Add Singleton", singleton, Debug.WARNING);
        }
        /*
         * For each time [1+], get the clusters associated
         */
        for (int clusterIdsIndex = 1, size = timesClusterIds.size(); clusterIdsIndex < size; ++clusterIdsIndex) {
            ArrayList<Integer> tempClusterIds = timesClusterIds.get(clusterIdsIndex);
            ArrayList<ArrayList<Integer>> results = new ArrayList<>();

            for (ArrayList<Integer> generatedPath : tempPaths) {
                for (int item : tempClusterIds) {
                    ArrayList<Integer> result = new ArrayList<>(generatedPath);
                    result.add(item);
                    results.add(result); // but why ???
                }
            }
            tempPaths = results;
        }

        // Remove the itemsets already existing in the set of transactions
        Debug.println("Remove paths already existing", Debug.DEBUG);
        ArrayList<ArrayList<Integer>> checkedArrayOfClusterIds = new ArrayList<>(); //checkedItemsets

        boolean insertok;

        for (ArrayList<Integer> currentPath : tempPaths) {
            insertok = true;

            for (Transaction transaction : defaultDatabase.getTransactions().values()) {
                if (ArrayUtils.isSame(transaction.getClusterIds(), currentPath)) {
                    insertok = false;
                    break;
                }
            }

            if (insertok) {
                checkedArrayOfClusterIds.add(currentPath);
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

        //ArrayList<ArrayList<Integer>> generatedPaths = new ArrayList<>();
        //generatedPaths.addAll(checkedArrayOfClusterIds); // why
        //generatedArrayOfTimeIds.add(times); // whyy
        //generatedArrayOfClusterIds.add(database.getClusterIds()); // but whyy ?
        return checkedArrayOfClusterIds;
    }
}
