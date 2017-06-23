package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.*;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PathDetector implements Generator {

    private final Database defaultDatabase;
    ArrayList<ArrayList<Integer>> lvl2ClusterIds;
    ArrayList<ArrayList<Integer>> lvl2TimeIds;
    private int minSupport, maxPattern, minTime;
    private ArrayList<Path> paths;

    /**
     * Initialise le solveur.
     *
     * @param database database par defaut
     */
    public PathDetector(Database database, DefaultConfig config) {
        this.minSupport = config.getMinSupport();
        this.maxPattern = config.getMaxPattern();
        this.minTime = config.getMinTime();
        this.defaultDatabase = database;
        lvl2ClusterIds = new ArrayList<>();
        lvl2TimeIds = new ArrayList<>();
        paths = new ArrayList<>();
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
     * {@link GeneratorUtils#ppcTest(Database, TreeSet, int, Set)}
     */
    @TraceMethod(displayTitle = true)
    static boolean PPCTest(Database database, TreeSet<Integer> clusters, Set<Integer> transactionIds,
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

    static void addPathToTransaction(ClusterMatrix clusterMatrix, TreeSet<Integer> path, ArrayList<Transaction> transactions, int[] pathId) {
        //TODO : opti ce truc mais en gros ça règle le pb du surplus de transactions par itemsets
        Set<Integer> transactionOfLast = clusterMatrix.getClusterTransactionIds(path.last());
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
    ArrayList<Path> generate() {
        ClusterMatrix clusterMatrix = new ClusterMatrix(defaultDatabase);
        Debug.println("totalItem", defaultDatabase.getClusterIds(), Debug.DEBUG);

        ArrayList<Integer> path = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();
        ArrayList<Transaction> transactions = new ArrayList<>(defaultDatabase.getTransactions().size());

        for (Transaction transaction : defaultDatabase.getTransactions().values()) {
            transactions.add(new Transaction(transaction.getId()));
        }

        TreeSet<Integer> transactionIds = new TreeSet<>(defaultDatabase.getTransactionIds());


        int[] pathId = new int[1];
        pathId[0] = 0;

        run(clusterMatrix, path, transactionIds, freqItemset, pathId);
        Debug.println("Transactions", transactions, Debug.DEBUG);

        return paths;
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
     * @param clusterMatrix          (database, , itemID, timeID) La database &agrave; analyser
     * @param clusterIds             (itemsets) une liste representant les id
     * @param transactionIds         (transactionList)
     * @param clustersFrequenceCount (freqList) une liste representant les clusterIds frequents
     * @param pathId                 (pathId)
     */
    @TraceMethod(displayTitle = true)
    private void run(ClusterMatrix clusterMatrix, ArrayList<Integer> clusterIds,
                     Set<Integer> transactionIds, ArrayList<Integer> clustersFrequenceCount, int[] pathId) {
        Debug.println("clusterMatrix", clusterMatrix, Debug.DEBUG);
        Debug.println("path", clusterIds, Debug.DEBUG);
        Debug.println("transactionIds ", transactionIds, Debug.DEBUG);
        Debug.println("clustersFrequenceCount ", clustersFrequenceCount, Debug.DEBUG);

        Debug.println("Generating paths", Debug.INFO);
        ArrayList<TreeSet<Integer>> pathsClusterIds = generatePaths(clusterIds);
        Debug.println("pathsClusterIds", pathsClusterIds, Debug.DEBUG);

        for (TreeSet<Integer> pathClusterIds : pathsClusterIds) {

            // Lcm::printItemsetsNew([]itemsets, occ, []transactionsets, pathId, []timeID, [][]level2ItemID, [][]level2TimeID)
            int calcurateCoreI;

            if (pathClusterIds.size() > 0) { // code c complient
                //TODO back to minTime for itemsize
                //if (path.size() > minTime) {
                TreeSet<Integer> pathTransactions = clusterMatrix.getClusterTransactionIds(pathClusterIds.last());
                TreeSet<Integer> pathTimes = new TreeSet<>();
                for (Integer clusterId : pathClusterIds) {
                    pathTimes.add(clusterMatrix.getClusterTimeId(clusterId));
                }

                Path path = new Path(pathId[0], pathTransactions, pathClusterIds, pathTimes);
                pathId[0]++;
                paths.add(path);

                calcurateCoreI = GeneratorUtils.getDifferentFromLastCluster(clustersFrequenceCount, pathClusterIds.first());
            } else {
                calcurateCoreI = 0;
            }


            Debug.println("Core_i : " + calcurateCoreI, Debug.DEBUG);

            SortedSet<Integer> clustersTailSet = defaultDatabase.getClusterIds().tailSet(calcurateCoreI);

            // freq_i
            ArrayList<Integer> freqPath = new ArrayList<>();
            Debug.println("lower bounds", clustersTailSet, Debug.DEBUG);
            Debug.println("min Support", minSupport, Debug.DEBUG);

            for (int clusterId : clustersTailSet) {
                if (clusterMatrix.getClusterTransactionIds(clusterId).size() >= minSupport &&
                        !pathClusterIds.contains(clusterId)) {
                    freqPath.add(clusterId);
                }
            }

            Debug.println("Frequent : ", freqPath, Debug.DEBUG);

            for (int maxClusterId : freqPath) {
                Set<Integer> newTransactionIds = defaultDatabase.getFilteredTransactionIdsIfHaveCluster(transactionIds, maxClusterId);

                if (GeneratorUtils.ppcTest(defaultDatabase, pathClusterIds, maxClusterId, newTransactionIds)) {
                    ArrayList<Integer> newPathClusters = new ArrayList<>();

                    GeneratorUtils.makeClosure(defaultDatabase, newTransactionIds, newPathClusters, pathClusterIds, maxClusterId);
                    if (maxPattern == 0 || newPathClusters.size() <= maxPattern) {
                        Set<Integer> updatedTransactionIds = GeneratorUtils
                                .updateTransactions(defaultDatabase, transactionIds, newPathClusters, maxClusterId);
                        ArrayList<Integer> newclustersFrequenceCount = GeneratorUtils
                                .updateClustersFrequenceCount(defaultDatabase, transactionIds, newPathClusters, clustersFrequenceCount,
                                        maxClusterId);

                        clusterMatrix.optimizeMatrix(defaultDatabase, updatedTransactionIds);

                        run(clusterMatrix, newPathClusters, updatedTransactionIds, newclustersFrequenceCount, pathId);

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
     * @param pathClusters  (itemsets)
     * @param pathId        (numItems)
     * @deprecated
     */
    void printItemsetsNew(ClusterMatrix clusterMatrix, TreeSet<Integer> pathClusters, int[] pathId) {
        Debug.println("ItemsetSize : ", pathClusters.size(), Debug.DEBUG);
        Debug.println("MinTime : ", minTime, Debug.DEBUG);

        //TODO back to minTime for pathize
        if (pathClusters.size() > 0) { // code c complient
            //if (pathClusters.size() > minTime) {

            Set<Integer> pathTransactions = clusterMatrix.getClusterTransactionIds(pathClusters.last());
            //Parmis la liste de clusters d'un itemsets, je cherche le cluster qui a le moins de transaction et ça sera par définition les transactions de mon path
            // sert à rien, déja limité par clustermatrix
            /*for (int clusterId : path) {
                if (clusterMatrix.getClusterTransactionIds(clusterId).size() < transactionOfLast.size()) {
                    transactionOfLast = clusterMatrix.getClusterTransactionIds(clusterId);
                }
            }*/
            TreeSet<Integer> pathTimes = new TreeSet<>();
            //TODO :Block management
            //pathTimes.add(0);
            //pathClusters.add(0);

            for (Integer clusterId : pathClusters) {
                pathTimes.add(clusterMatrix.getClusterTimeId(clusterId));
            }

            Path path = new Path(pathId[0], pathTransactions, pathClusters, pathTimes);
            paths.add(path);
        }


    }

    void addPathToPathBlock(ClusterMatrix clusterMatrix, TreeSet<Integer> path) {
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
    @TraceMethod(displayTitleIfLast = true)
    ArrayList<TreeSet<Integer>> generatePaths(ArrayList<Integer> path) {
        // todo faire passer Path en parametres et non pas un clusterIds représentant l'path
        if (path.size() == 0) {
            ArrayList<TreeSet<Integer>> generatedPaths = new ArrayList<>();
            generatedPaths.add(new TreeSet<>(path));
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
            ArrayList<TreeSet<Integer>> generatedPaths = new ArrayList<>();

            generatedPaths.add(new TreeSet<>(path)); // why
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

        ArrayList<TreeSet<Integer>> tempPaths = new ArrayList<>();
        /*
         * For each clusterId in timeClusterIds[0], push path of size 1
         */
        for (Integer clusterId : timesClusterIds.get(0)) {
            TreeSet<Integer> singleton = new TreeSet<>();
            singleton.add(clusterId);
            tempPaths.add(singleton);
            Debug.println("Add Singleton", singleton, Debug.WARNING);
        }
        /*
         * For each time [1+], get the clusters associated
         */
        for (int clusterIdsIndex = 1, size = timesClusterIds.size(); clusterIdsIndex < size; ++clusterIdsIndex) {
            ArrayList<Integer> tempClusterIds = timesClusterIds.get(clusterIdsIndex);
            ArrayList<TreeSet<Integer>> results = new ArrayList<>();

            for (Set<Integer> generatedPath : tempPaths) {
                for (int item : tempClusterIds) {
                    TreeSet<Integer> result = new TreeSet<>(generatedPath);
                    result.add(item);
                    results.add(result); // but why ???
                }
            }
            tempPaths = results;
        }

        // Remove the itemsets already existing in the set of transactions
        Debug.println("Remove paths already existing", Debug.DEBUG);
        ArrayList<TreeSet<Integer>> checkedArrayOfClusterIds = new ArrayList<>(); //checkedItemsets

        boolean insertok;

        for (TreeSet<Integer> currentPath : tempPaths) {
            insertok = true;

            for (Transaction transaction : defaultDatabase.getTransactions().values()) {
                if (transaction.getClusterIds().equals(currentPath)) {
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
