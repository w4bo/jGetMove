package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.ClusterMatrix;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;

import java.util.*;

/**
 * Finds the itemsets
 *
 * @version 1.1.0
 * @since 0.1.0
 */
public class ItemsetsFinder {

    protected int minSupport;
    protected int maxPattern;
    protected TreeSet<Itemset> itemsets;
    protected int minTime;

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
            generatedItemsets.add(new TreeSet<>());
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
    @TraceMethod
    ArrayList<Itemset> generate(Base base, final int minTime) {
        Debug.println("Base", base, Debug.DEBUG);
        Debug.println("n° of Clusters", base.getClusterIds().size(), Debug.INFO);
        Debug.println("n° of Transactions", base.getTransactionIds().size(), Debug.INFO);
        Debug.println("n° of Times", base.getTimeIds().size(), Debug.INFO);


        ClusterMatrix clusterMatrix = new ClusterMatrix(base);

        ArrayList<Integer> itemset = new ArrayList<>();
        ArrayList<Integer> freqItemset = new ArrayList<>();
        TreeSet<Integer> transactionIds = new TreeSet<>(base.getTransactionIds());

        // Overwriting minTime to avoid problems with blocks
        this.minTime = minTime;
        // important if has multiple blocks it needs to be cleaned.
        this.itemsets.clear();

        run(base, clusterMatrix, itemset, transactionIds, freqItemset);

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
     * @param clusterIds             (itemsets) une liste representant les id
     * @param transactionIds         (transactionList)
     * @param clustersFrequenceCount (freqList) une liste representant les clusterIds frequents
     */
    @TraceMethod(displayTitle = true)
    void run(Base base, ClusterMatrix clusterMatrix, ArrayList<Integer> clusterIds,
             Set<Integer> transactionIds, ArrayList<Integer> clustersFrequenceCount) {
        Debug.println("ClusterMatrix", clusterMatrix, Debug.DEBUG);
        Debug.println("Transactions", transactionIds, Debug.DEBUG);
        Debug.println("ClusterIds", clusterIds, Debug.DEBUG);
        Debug.println("ClustersFrequenceCount", clustersFrequenceCount, Debug.DEBUG);

        // generating all the possible itemsets from the list of clusters
        ArrayList<TreeSet<Integer>> itemsets = generateItemsets(base, clusterIds);

        Debug.println("Itemsets", itemsets, Debug.DEBUG);

        // foreach possible itemsets
        for (TreeSet<Integer> itemsetClusters : itemsets) {
            Debug.println("Itemset", itemsetClusters, Debug.DEBUG);

            int calcurateCoreI;

            // if the itemset is not empty :)
            if (itemsetClusters.size() > minTime) {
                saveItemset(clusterMatrix, itemsetClusters);

                // we calcurates its calcuration ?
                calcurateCoreI = GeneratorUtils.getDifferentFromLastItem(clustersFrequenceCount, clusterIds);

            } else { // if it's empty we can't calcurate it :(
                calcurateCoreI = 0;
            }

            // still wondering what corei is used for ... (and this calcurating stuff is for, maybe for YoRHa)
            Debug.println("Core i : " + calcurateCoreI, Debug.DEBUG);

            // knowing the coreI of this itemset, we restrain the search space by getting all the clusters over it (including coreI)
            SortedSet<Integer> clustersTailSet = base.getClusterIds().tailSet(calcurateCoreI);

            // once we have this, we need to find the possible future clusters of the future itemsets
            ArrayList<Integer> maxClusterIdsOfItemsets = new ArrayList<>();

            // if the cluster is not in the already added in the itemset
            // and has more than the minimal amount of transactions
            for (int clusterId : clustersTailSet) {
                if (!clusterIds.contains(clusterId) && clusterMatrix.getTransactionIds(clusterId).size() >= minSupport) {
                    maxClusterIdsOfItemsets.add(clusterId);
                }
            }

            // freq_i
            Debug.println("ClusterTailSet", clustersTailSet, Debug.DEBUG);
            // we have our future possible clusters that can be in a future itemset
            Debug.println("maxClusterIdsOfItemsets", maxClusterIdsOfItemsets, Debug.DEBUG);

            // once we have them, we test for each of them
            for (int maxClusterId : maxClusterIdsOfItemsets) {
                // we retrieve the new transactions from the database
                Set<Integer> newTransactionIds = base.getFilteredTransactionIdsIfHaveCluster(transactionIds, maxClusterId);

                Debug.println("NewTransactionIds", newTransactionIds, Debug.DEBUG);

                if (GeneratorUtils.ppcTest(base, itemsetClusters, newTransactionIds, maxClusterId)) {
                    // if the itemset is the most englobing itemset possible from clusterId 0 ->maxClusterId
                    ArrayList<Integer> futureClusterIds = GeneratorUtils.makeClosure(base, newTransactionIds, itemsetClusters, maxClusterId);
                    // then we try to extend it to a larger itemset

                    Debug.println("futureClusterIds", futureClusterIds, Debug.DEBUG);

                    if (maxPattern == 0 || futureClusterIds.size() <= maxPattern) {
                        Set<Integer> futureTransactionIds = GeneratorUtils
                                .updateTransactions(base, transactionIds, futureClusterIds, maxClusterId);
                        ArrayList<Integer> futureClustersFrequenceCount = GeneratorUtils
                                .updateClustersFrequenceCount(base, transactionIds, futureClusterIds, clustersFrequenceCount,
                                        maxClusterId);

                        clusterMatrix.optimizeMatrix(base, futureTransactionIds);

                        run(base, clusterMatrix, futureClusterIds, futureTransactionIds, futureClustersFrequenceCount);

                    }
                }
            }
        }
    }

    /**
     * Creates and adds the itemset. Will return <tt>true</tt> if the itemset wasn't already saved.
     *
     * @param clusterMatrix   will be used to retrieve the list of transactions and times
     * @param itemsetClusters clusters of the itemset
     * @return <tt>true</tt> if the saved itemset is'nt already added, will return <tt>false</tt> if the itemset was already saved.
     * @see Itemset#compareTo(Itemset) to understand how the verification is made.
     */
    boolean saveItemset(ClusterMatrix clusterMatrix, TreeSet<Integer> itemsetClusters) {
        // then the itemset is possible
        HashSet<Integer> itemsetTransactions = clusterMatrix.getTransactionIds(itemsetClusters.last());

        TreeSet<Integer> itemsetTimes = new TreeSet<>();
        for (Integer clusterId : itemsetClusters) {
            itemsetTimes.add(clusterMatrix.getTimeId(clusterId));
        }
        // so we add it to the final list
        Itemset itemset = new Itemset(itemsets.size(), itemsetTransactions, itemsetClusters, itemsetTimes);

        // unless he's already in there. if so we don't add it
        return this.itemsets.add(itemset);
    }
}
