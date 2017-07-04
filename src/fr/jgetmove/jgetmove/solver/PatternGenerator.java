package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.*;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.detector.SingleDetector;
import fr.jgetmove.jgetmove.pattern.Pattern;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;

import java.util.*;

public class PatternGenerator {

    private int minSupport, maxPattern, minTime;
    private boolean printed;
    private HashMap<SingleDetector, ArrayList<Pattern>> motifs;

    /**
     * Initialise le solveur.
     *
     * @param minSupport support minimal
     * @param maxPattern nombre maximal de pattern a trouvé
     * @param minTime    temps minimal
     */
    public PatternGenerator(int minSupport, int maxPattern, int minTime) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
        motifs = new HashMap<>();
        printed = false;

    }


    /**
     * Initialise le solveur.
     */
    public PatternGenerator(DefaultConfig config) {
        this.minSupport = config.getMinSupport();
        this.maxPattern = config.getMaxPattern();
        this.minTime = config.getMinTime();
        motifs = new HashMap<>();
        printed = false;

    }


    /**
     * <pre>
     * Lcm::MakeClosure(const DataBase &dataBase, vector<int> &transactionList,
     * vector<int> &q_sets, vector<int> &itemsets,
     * int item)
     * </pre>
     *
     * @param dataBase       (database)
     * @param transactionIds (transactionList)
     * @param qSets          (q_sets)
     * @param itemset        (itemsets)
     * @param freq           (item)
     * @deprecated use {@link GeneratorUtils#makeClosure(Base, Set, ArrayList, Collection, int)}
     */
    @TraceMethod(displayTitle = true)
    private static void MakeClosure(DataBase dataBase, Set<Integer> transactionIds, ArrayList<Integer> qSets,
                                    ArrayList<Integer> itemset, int freq) {
        GeneratorUtils.makeClosure(dataBase, transactionIds, qSets, itemset, freq);
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
     * @deprecated use {@link DataBase#getFilteredTransactionIdsIfHaveCluster(Set, int)} and {@link GeneratorUtils#ppcTest(Base, TreeSet, int, Set)}
     */
    @TraceMethod(displayTitle = true)
    private static boolean PPCTest(DataBase dataBase, ArrayList<Integer> clusters, Set<Integer> transactionIds,
                                   int freqClusterId, Set<Integer> newTransactionIds) {
        // CalcTransactionList
        for (int transactionId : transactionIds) {
            Transaction transaction = dataBase.getTransaction(transactionId);

            if (transaction.getClusterIds().contains(freqClusterId)) {
                newTransactionIds.add(transactionId);
            }
        }

        for (int clusterId = 0; clusterId < freqClusterId; clusterId++) {
            if (!clusters.contains(clusterId) && GeneratorUtils
                    .CheckItemInclusion(dataBase, newTransactionIds, clusterId)) {
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
     * @deprecated use {@link GeneratorUtils#getDifferentFromLastCluster(ArrayList, int)}
     */
    private static int CalcurateCoreI(ArrayList<Integer> clusterIds, ArrayList<Integer> frequentClusterIds) {
        return GeneratorUtils.getDifferentFromLastCluster(frequentClusterIds, clusterIds.get(0));
    }

    /**
     */
    void generate(Base base, ArrayList<ItemsetsOfBlock> itemsetsOfBlocks) {
        ClusterMatrix clusterMatrix = new ClusterMatrix(base);

        ArrayList<Integer> itemsets = new ArrayList<>();
        ArrayList<Integer> clustersFrequenceCount = new ArrayList<>();
        TreeSet<Integer> transactionIds = new TreeSet<>(base.getTransactionIds());
        Debug.println("ItemsetsOfBlock", itemsets, Debug.DEBUG);
        //run(base, clusterMatrix, itemsets, transactionIds, clustersFrequenceCount, itemsetsOfBlocks);

    }

    @TraceMethod(displayTitle = true)
    private void run(DataBase dataBase, ClusterMatrix clusterMatrix, ArrayList<Integer> itemset, Set<Integer> transactionIds,
                     ArrayList<Integer> clustersFrequenceCount, ArrayList<ArrayList<Integer>> lvl2ClusterId,
                     ArrayList<ArrayList<Integer>> lvl2TimeId, Set<Detector> detectors) {
                /*TODO private void run(Base base, ClusterMatrix clusterMatrix, ArrayList<Integer> itemset, Set<Integer> transactionIds,
                     ArrayList<Integer> clustersFrequenceCount, ArrayList<ItemsetsOfBlock> itemsetsOfBlocks) {*/
        Debug.displayTitle();

        int calcurateCoreI;
        if (itemset.size() > 0) {
            calcurateCoreI = GeneratorUtils.getDifferentFromLastCluster(clustersFrequenceCount, itemset.get(0));
        } else {
            calcurateCoreI = 0;
        }

        SortedSet<Integer> clustersTailSet = dataBase.getClusterIds().tailSet(calcurateCoreI);

        //Blocks
        //TODO /*
        ArrayList<Integer> blocks = new ArrayList<>();
        for (Integer clusterId : itemset) {
            blocks.add(lvl2TimeId.get(clusterId).get(0));
        }
        //*/
        // freq_i
        ArrayList<Integer> freqItemset = new ArrayList<>();
        Debug.println("lower bounds", clustersTailSet, Debug.DEBUG);
        Debug.println("min Support", minSupport, Debug.DEBUG);

        for (int clusterId : clustersTailSet) {
            if (clusterMatrix.getClusterTransactionIds(clusterId).size() >= minSupport &&
                    !itemset.contains(clusterId)) {
                freqItemset.add(clusterId);
            }
        }

        Debug.println("frequent : " + freqItemset);

        ArrayList<Integer> newPathClusters = new ArrayList<>();
        ArrayList<Integer> newFreqList = new ArrayList<>();

        for (int freqClusterId : freqItemset) {
            Set<Integer> newTransactionIds = new TreeSet<>();

            int blockOfItem = lvl2TimeId.get(freqClusterId).get(0);
            if (PPCTest(dataBase, itemset, transactionIds, freqClusterId, newTransactionIds) &&
                    !blocks.contains(blockOfItem)) {
                newPathClusters.clear();
                MakeClosure(dataBase, newTransactionIds, newPathClusters, itemset, freqClusterId);

                if (maxPattern == 0 || newPathClusters.size() <= maxPattern) {
                    newTransactionIds.clear();

                    Set<Integer> iterTransactionIds = GeneratorUtils
                            .updateTransactions(dataBase, dataBase.getTransactionIds(), newPathClusters, freqClusterId);
                    newFreqList.clear();
                    newFreqList = GeneratorUtils
                            .updateClustersFrequenceCount(dataBase, dataBase.getTransactionIds(), newPathClusters, clustersFrequenceCount, freqClusterId);
                    //TODO run(dataBase, newPathClusters, iterTransactionIds, newFreqList, lvl2ClusterId, lvl2TimeId, detectors);
                }
            }
        }
        if (!printed) {
            printItemsets(dataBase, itemset, lvl2ClusterId, lvl2TimeId, detectors);
            printed = true;
        }
    }

    @TraceMethod(displayTitle = true)
    private void printItemsets(DataBase dataBase, ArrayList<Integer> itemsets,
                               ArrayList<ArrayList<Integer>> lvl2ClusterId, ArrayList<ArrayList<Integer>> lvl2TimeId,
                               Set<Detector> singleDetectors) {
        Debug.println("Database2" + dataBase);
        Debug.println("itemsets : " + itemsets);
        Debug.println("lvl2TimeId : " + lvl2TimeId);
        Debug.println("lvl2ClusterId : " + lvl2ClusterId);
        if (itemsets.size() > 0) {

            for (int i = 0; i < dataBase.getClusters().size(); i++) {

                Set<Integer> timeBased = new TreeSet<>();
                Set<Integer> clusterBased = new TreeSet<>();

                Collection<Transaction> transactions = dataBase.getCluster(i).getTransactions().values();
                Debug.println("TEST", transactions, Debug.DEBUG);
                for (int j = 1; j < lvl2TimeId.get(i).size(); j++) {
                    timeBased.add(lvl2TimeId.get(i).get(j));
                }

                for (int j = 1; j < lvl2ClusterId.get(i).size(); j++) {
                    clusterBased.add(lvl2ClusterId.get(i).get(j));
                }

                /*if (timeBased.size() > minTime) {
                    for (SingleDetector detector : singleDetectors) {
                        motifs.put(detector, detector.detect(defaultDataBase, timeBased, clusterBased, transactions));
                    }
                }*/

                if (timeBased.size() > minTime) {
                    for (Detector singleDetector : singleDetectors) {
                        if (motifs.get(singleDetector) == null) {
                            // TODO ArrayList<Pattern> patterns = singleDetector.detect(defaultDataBase, timeBased, clusterBased, transactions);
                            //ArrayList<Pattern> patterns = singleDetector.detect(dataBase, timeBased, clusterBased, transactions);
                            // TODO motifs.put(singleDetector, patterns);
                        } else {
                            //TODO motifs.get(singleDetector).addAll(singleDetector.detect(defaultDataBase, timeBased, clusterBased, transactions));
                            //motifs.get(singleDetector).addAll(singleDetector.detect(dataBase, timeBased, clusterBased, transactions));

                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    public HashMap<SingleDetector, ArrayList<Pattern>> getResults() {
        return motifs;
    }
}
