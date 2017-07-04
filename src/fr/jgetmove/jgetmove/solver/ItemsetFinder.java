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

public class ItemsetFinder {

    private int minSupport, maxPattern, minTime;
    private TreeSet<Itemset> itemsets;

    public ItemsetFinder(DefaultConfig config) {
        this.minSupport = config.getMinSupport();
        this.maxPattern = config.getMaxPattern();
        this.minTime = config.getMinTime();
        itemsets = new TreeSet<>();
    }

    /**
     * Teste si p(i-1) == q(i-1)
     * <p>
     * <pre>
     * Lcm::PpcTest(dataBase, []itemsets, []transactionList, item, []newTransactionList)
     * </pre>
     *
     * @param clusters          (itemsets)
     * @param transactionIds    (transactionList)
     * @param freqClusterId     (item)
     * @param newTransactionIds (newTransactionList)
     * @return vrai si ppctest est réussi
     * @deprecated use
     * {@link DataBase#getFilteredTransactionIdsIfHaveCluster(Set, int)}
     * and
     * {@link GeneratorUtils#ppcTest(Base, TreeSet, int, Set)}
     */
    @Deprecated
    static boolean PPCTest(DataBase dataBase, TreeSet<Integer> clusters, Set<Integer> transactionIds,
                           int freqClusterId, Set<Integer> newTransactionIds) {
        // CalcTransactionList
        for (int transactionId : transactionIds) {
            Transaction transaction = dataBase.getTransaction(transactionId);

            if (transaction.getClusterIds().contains(freqClusterId)) {
                newTransactionIds.add(transactionId);
            }
        }
        for (int clusterId = 0; clusterId < freqClusterId; clusterId++) {
            if (!clusters.contains(clusterId)
                    && dataBase.isClusterInTransactions(newTransactionIds, clusterId)) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    static void addPathToTransaction(ClusterMatrix clusterMatrix, TreeSet<Integer> itemset, ArrayList<Transaction> transactions, int[] pathId) {
        //TODO : opti ce truc mais en gros ça règle le pb du surplus de transactions par itemsets
        Set<Integer> transactionOfLast = clusterMatrix.getClusterTransactionIds(itemset.last());
        //Parmis la liste de clusters d'un itemsets, je cherche le cluster qui a le moins de transaction et ça sera par définition les transactions de mon itemset
        // sert à rien, déja limité par clustermatrix
            /*for (int clusterId : itemset) {
                if (clusterMatrix.getClusterTransactionIds(clusterId).size() < transactionOfLast.size()) {
                    transactionOfLast = clusterMatrix.getClusterTransactionIds(clusterId);
                }
            }*/

        //Pour chaque transaction, on ajoute le cluster qui a l'id numItem
        // TODO :on ajoute pour chaque transaction le itemset auquel il appartient :D
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
    @TraceMethod(displayTitle = true, title = "Finding itemsets")
    TreeSet<Itemset> generate(Base base) {
        Debug.println("n° clusters", base.getClusterIds().size(), Debug.INFO);
        Debug.println("n° transactions", base.getTransactionIds().size(), Debug.INFO);
        Debug.println("n° times", base.getTimeIds().size(), Debug.INFO);

        Debug.println("Finding itemsets from", base, Debug.DEBUG);

        ClusterMatrix clusterMatrix = new ClusterMatrix(base);

        ArrayList<Integer> itemset = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();
        TreeSet<Integer> transactionIds = new TreeSet<>(base.getTransactionIds());


        int[] pathId = new int[1];
        pathId[0] = 0;

        run(base, clusterMatrix, itemset, transactionIds, freqItemset, pathId);

        Debug.println("n° itemsets found", itemsets.size(), Debug.INFO);
        Debug.println("itemsets", itemsets, Debug.DEBUG);


        return itemsets;
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
     * @param toItemsetize           (itemsets) une liste representant les id
     * @param transactionIds         (transactionList)
     * @param clustersFrequenceCount (freqList) une liste representant les clusterIds frequents
     * @param itemsetId              (pathId)
     */
    @TraceMethod(displayTitle = true)
    private void run(Base base, ClusterMatrix clusterMatrix, ArrayList<Integer> toItemsetize,
                     Set<Integer> transactionIds, ArrayList<Integer> clustersFrequenceCount, int[] itemsetId) {
        Debug.println("clusterMatrix", clusterMatrix, Debug.DEBUG);
        Debug.println("itemset", toItemsetize, Debug.DEBUG);
        Debug.println("transactionIds ", transactionIds, Debug.DEBUG);
        Debug.println("clustersFrequenceCount ", clustersFrequenceCount, Debug.DEBUG);

        ArrayList<TreeSet<Integer>> itemsetArrayList = generateItemsets(base, toItemsetize);

        Debug.println("itemsets", itemsetArrayList, Debug.DEBUG);

        for (TreeSet<Integer> itemsetTreeSet : itemsetArrayList) {

            // Lcm::printItemsetsNew([]itemsets, occ, []transactionsets, itemsetId, []timeID, [][]level2ItemID, [][]level2TimeID)
            int calcurateCoreI;

            if (itemsetTreeSet.size() > 0) { // code c complient
                //if (itemset.size() > minTime) {

                TreeSet<Integer> pathTransactions = clusterMatrix.getClusterTransactionIds(itemsetTreeSet.last());

                TreeSet<Integer> pathTimes = new TreeSet<>();
                for (Integer clusterId : itemsetTreeSet) {
                    pathTimes.add(clusterMatrix.getClusterTimeId(clusterId));
                }
                Itemset itemset = new Itemset(itemsetId[0], pathTransactions, itemsetTreeSet, pathTimes);

                if (this.itemsets.add(itemset)) {
                    itemsetId[0]++;
                }

                calcurateCoreI = GeneratorUtils.getDifferentFromLastCluster(clustersFrequenceCount, itemsetTreeSet.first());
            } else {
                calcurateCoreI = 0;
            }


            Debug.println("Core_i : " + calcurateCoreI, Debug.DEBUG);

            SortedSet<Integer> clustersTailSet = base.getClusterIds().tailSet(calcurateCoreI);

            // freq_i
            ArrayList<Integer> freqItemset = new ArrayList<>();
            Debug.println("lower bounds", clustersTailSet, Debug.DEBUG);
            Debug.println("min Support", minSupport, Debug.DEBUG);

            for (int clusterId : clustersTailSet) {
                if (clusterMatrix.getClusterTransactionIds(clusterId).size() >= minSupport &&
                        !itemsetTreeSet.contains(clusterId)) {
                    freqItemset.add(clusterId);
                }
            }

            Debug.println("Frequent : ", freqItemset, Debug.DEBUG);

            for (int maxClusterId : freqItemset) {
                Set<Integer> newTransactionIds = base.getFilteredTransactionIdsIfHaveCluster(transactionIds, maxClusterId);

                if (GeneratorUtils.ppcTest(base, itemsetTreeSet, maxClusterId, newTransactionIds)) {
                    ArrayList<Integer> newPathClusters = new ArrayList<>();

                    GeneratorUtils.makeClosure(base, newTransactionIds, newPathClusters, itemsetTreeSet, maxClusterId);
                    if (maxPattern == 0 || newPathClusters.size() <= maxPattern) {
                        Set<Integer> updatedTransactionIds = GeneratorUtils
                                .updateTransactions(base, transactionIds, newPathClusters, maxClusterId);
                        ArrayList<Integer> newclustersFrequenceCount = GeneratorUtils
                                .updateClustersFrequenceCount(base, transactionIds, newPathClusters, clustersFrequenceCount,
                                        maxClusterId);

                        clusterMatrix.optimizeMatrix(base, updatedTransactionIds);

                        run(base, clusterMatrix, newPathClusters, updatedTransactionIds, newclustersFrequenceCount, itemsetId);

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
            //Parmis la liste de clusters d'un itemsets, je cherche le cluster qui a le moins de transaction et ça sera par définition les transactions de mon itemset
            // sert à rien, déja limité par clustermatrix
            /*for (int clusterId : itemset) {
                if (clusterMatrix.getClusterTransactionIds(clusterId).size() < transactionOfLast.size()) {
                    transactionOfLast = clusterMatrix.getClusterTransactionIds(clusterId);
                }
            }*/
            TreeSet<Integer> pathTimes = new TreeSet<>();
            //TODO :BlockBase management
            //pathTimes.add(0);
            //pathClusters.add(0);

            for (Integer clusterId : pathClusters) {
                pathTimes.add(clusterMatrix.getClusterTimeId(clusterId));
            }

            Itemset itemset = new Itemset(pathId[0], pathTransactions, pathClusters, pathTimes);
            itemsets.add(itemset);
        }


    }

    /**
     * <pre>
     * Lcm::GenerateItemset(DataBase,[]itemsets,[]itemID,[]timeID,[][]generatedItemsets, [][]generatedtimeID,[][]generateditemID,sizeGenerated)
     * </pre>
     *
     * @param base (database)
     * @param path (itemsets) une liste representant les clusterId
     */
    @TraceMethod(displayTitleIfLast = true)
    ArrayList<TreeSet<Integer>> generateItemsets(Base base, ArrayList<Integer> path) {
        // todo faire passer Itemset en parametres et non pas un clusterIds représentant l'path
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
            lastTime = base.getClusterTimeId(clusterId);

            if (i != path.size() - 1) {
                int nextClusterId = path.get(i + 1);
                if (base.getClusterTimeId(clusterId) == base.getClusterTimeId(nextClusterId)) {
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
                if (base.getClusterTimeId(clusterId) == i) {
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
        Debug.println("Remove itemsets already existing", Debug.DEBUG);
        ArrayList<TreeSet<Integer>> checkedArrayOfClusterIds = new ArrayList<>(); //checkedItemsets

        boolean insertok;

        for (TreeSet<Integer> currentPath : tempPaths) {
            insertok = true;

            for (Transaction transaction : base.getTransactions().values()) {
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
                    times.add(defaultDataBase.getClusterTimeId(checkedClusterId));
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
