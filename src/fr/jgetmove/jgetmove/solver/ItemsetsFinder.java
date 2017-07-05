package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ItemsetsFinder {

    private int minSupport;
    private int maxPattern;
    private TreeSet<Itemset> itemsets;
    private int minTime;

    public ItemsetsFinder(DefaultConfig config) {
        minSupport = config.getMinSupport();
        maxPattern = config.getMaxPattern();
        itemsets = new TreeSet<>();
    }

    /**
     * Generates all the itemsets possible from toItemsetize clusters.
     * <p>
     * There can be more than one itemset possible if and only if two clusters of toItemsetize are on the same time.
     * <p>
     * <pre>
     * Lcm::GenerateItemset(DataBase,[]itemsets,[]itemID,[]timeID,[][]generatedItemsets, [][]generatedtimeID,[][]generateditemID,sizeGenerated)
     * </pre>
     *
     * @param base         (database)
     * @param toItemsetize (itemsets) une liste representant les clusterId
     */
    @TraceMethod(displayTitle = true)
    static ArrayList<TreeSet<Integer>> generateItemsets(Base base, ArrayList<Integer> toItemsetize) {
        Debug.println("toItemsetize", toItemsetize, Debug.DEBUG);

        if (toItemsetize.size() == 0) {
            ArrayList<TreeSet<Integer>> generatedItemsets = new ArrayList<>();
            generatedItemsets.add(new TreeSet<>(toItemsetize));
            return generatedItemsets;
        }

        boolean oneTimePerCluster = true; // numberSameTime

        int lastTime = 0;
        // liste des temps qui n'ont qu'un seul cluster
        for (int i = 0; i < toItemsetize.size(); ++i) {
            int clusterId = toItemsetize.get(i);
            lastTime = base.getClusterTimeId(clusterId);

            if (i != toItemsetize.size() - 1) {
                int nextClusterId = toItemsetize.get(i + 1);
                if (base.getClusterTimeId(clusterId) == base.getClusterTimeId(nextClusterId)) {
                    oneTimePerCluster = false;
                }
            }
        }

        if (oneTimePerCluster) {
            ArrayList<TreeSet<Integer>> generatedPaths = new ArrayList<>(1);
            generatedPaths.add(new TreeSet<>(toItemsetize));
            return generatedPaths;
        }

        //Manage MultiClustering
        Debug.println("MultiClustering", Debug.DEBUG);


        ArrayList<ArrayList<Integer>> timesClusterIds = new ArrayList<>(); // PosDates

        for (int i = 1; i <= lastTime; i++) {
            ArrayList<Integer> tempPath = new ArrayList<>();

            for (int clusterId : toItemsetize) {
                if (base.getClusterTimeId(clusterId) == i) {
                    tempPath.add(clusterId);
                }
            }

            timesClusterIds.add(tempPath);
        }

        ArrayList<TreeSet<Integer>> tempItemsets = new ArrayList<>();

        /*
         * For each clusterId in timeClusterIds[0], push path of size 1
         */
        for (Integer clusterId : timesClusterIds.get(0)) {
            TreeSet<Integer> singleton = new TreeSet<>();
            singleton.add(clusterId);
            tempItemsets.add(singleton);
        }

        /*
         * For each time [1+], get the clusters associated
         */
        for (int clusterIdsIndex = 1, size = timesClusterIds.size(); clusterIdsIndex < size; ++clusterIdsIndex) {
            ArrayList<Integer> tempClusterIds = timesClusterIds.get(clusterIdsIndex);
            ArrayList<TreeSet<Integer>> results = new ArrayList<>();

            for (Set<Integer> generatedPath : tempItemsets) {
                for (int item : tempClusterIds) {
                    TreeSet<Integer> result = new TreeSet<>(generatedPath);
                    result.add(item);
                    results.add(result); // but why ???
                }
            }
            tempItemsets = results;
        }

        // Remove the itemsets already existing in the set of transactions
        ArrayList<TreeSet<Integer>> checkedArrayOfClusterIds = new ArrayList<>();//checkedItemsets

        boolean insertok;

        for (TreeSet<Integer> currentItemset : tempItemsets) {
            insertok = true;

            for (Transaction transaction : base.getTransactions().values()) {
                if (transaction.getClusterIds().equals(currentItemset)) {
                    insertok = false;
                    break;
                }
            }

            if (insertok) {
                checkedArrayOfClusterIds.add(currentItemset);
            }
        }
        return checkedArrayOfClusterIds;
    }

    /**
     * Runs the algorithm on the given base. It returns the itemsets found.
     * <p>
     * <pre>
     * Lcm::RunLcmNew(database, []transactionsets,
     * numItems, []itemID, []timeID,
     * [][]level2ItemID, [][]level2TimeID)
     * </pre>
     */
    @TraceMethod(displayTitle = true, title = "Finding itemsets")
    ArrayList<Itemset> generate(Base base, final int minTime) {
        Debug.println("Base", base, Debug.DEBUG);
        Debug.println("n° of Clusters", base.getClusterIds().size(), Debug.INFO);
        Debug.println("n° of Transactions", base.getTransactionIds().size(), Debug.INFO);
        Debug.println("n° of Times", base.getTimeIds().size(), Debug.INFO);


        ClusterMatrix clusterMatrix = new ClusterMatrix(base);

        ArrayList<Integer> itemset = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();
        TreeSet<Integer> transactionIds = new TreeSet<>(base.getTransactionIds());


        int[] itemsetId = new int[1];
        itemsetId[0] = 0;

        // Overwriting minTime to avoid problems with blocks
        this.minTime = minTime;
        // important if has multiple blocks it needs to be cleaned.
        this.itemsets.clear();

        run(base, clusterMatrix, itemset, transactionIds, freqItemset, itemsetId);

        Debug.println("Itemsets", itemsets, Debug.DEBUG);
        Debug.println("n° of Itemsets", itemsets.size(), Debug.INFO);
        return new ArrayList<>(itemsets);
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
        Debug.println("ClusterMatrix", clusterMatrix, Debug.DEBUG);
        Debug.println("Transactions ", transactionIds, Debug.DEBUG);
        Debug.println("ClustersFrequenceCount", clustersFrequenceCount, Debug.DEBUG);

        ArrayList<TreeSet<Integer>> itemsetArrayList = generateItemsets(base, toItemsetize);

        Debug.println("Itemsets", itemsetArrayList, Debug.DEBUG);

        for (TreeSet<Integer> itemsetTreeSet : itemsetArrayList) {
            int calcurateCoreI;

            //if (itemsetTreeSet.size() > 0) { // code c complient
            if (itemsetTreeSet.size() > minTime) {

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

            // still wondering what corei is used for ...
            Debug.println("Core i : " + calcurateCoreI, Debug.DEBUG);

            SortedSet<Integer> clustersTailSet = base.getClusterIds().tailSet(calcurateCoreI);

            ArrayList<Integer> freqItemset = new ArrayList<>();

            for (int clusterId : clustersTailSet) {
                if (clusterMatrix.getClusterTransactionIds(clusterId).size() >= minSupport &&
                        !itemsetTreeSet.contains(clusterId)) {
                    freqItemset.add(clusterId);
                }
            }

            // freq_i
            Debug.println("ClusterTailSet", clustersTailSet, Debug.DEBUG);
            Debug.println("Frequent", freqItemset, Debug.DEBUG);

            for (int maxClusterId : freqItemset) {
                Set<Integer> newTransactionIds = base.getFilteredTransactionIdsIfHaveCluster(transactionIds, maxClusterId);

                if (GeneratorUtils.ppcTest(base, itemsetTreeSet, maxClusterId, newTransactionIds)) {
                    ArrayList<Integer> newItemsetClusters = GeneratorUtils.makeClosure(base, newTransactionIds, itemsetTreeSet, maxClusterId);
                    Debug.println("NewItemsetClusters", newItemsetClusters, Debug.DEBUG);

                    if (maxPattern == 0 || newItemsetClusters.size() <= maxPattern) {
                        Set<Integer> updatedTransactionIds = GeneratorUtils
                                .updateTransactions(base, transactionIds, newItemsetClusters, maxClusterId);
                        ArrayList<Integer> newclustersFrequenceCount = GeneratorUtils
                                .updateClustersFrequenceCount(base, transactionIds, newItemsetClusters, clustersFrequenceCount,
                                        maxClusterId);

                        clusterMatrix.optimizeMatrix(base, updatedTransactionIds);

                        run(base, clusterMatrix, newItemsetClusters, updatedTransactionIds, newclustersFrequenceCount, itemsetId);

                    }
                }
            }
        }
    }
}
