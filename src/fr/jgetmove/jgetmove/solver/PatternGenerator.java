package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.TraceMethod;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.pattern.Pattern;
import fr.jgetmove.jgetmove.utils.GeneratorUtils;

import java.util.*;

public class PatternGenerator {

    private final DataBase defaultDataBase;
    private ArrayList<ArrayList<Integer>> clustersGenerated;
    private int minSupport, maxPattern, minTime;
    private boolean printed;
    private HashMap<Detector, ArrayList<Pattern>> motifs;

    /**
     * Initialise le solveur.
     *
     * @param dataBase   dataBase par defaut
     * @param minSupport support minimal
     * @param maxPattern nombre maximal de pattern a trouvé
     * @param minTime    temps minimal
     */
    public PatternGenerator(DataBase dataBase, int minSupport, int maxPattern, int minTime) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
        this.clustersGenerated = new ArrayList<>();
        this.defaultDataBase = dataBase;
        motifs = new HashMap<>();
        printed = false;

    }


    /**
     * Initialise le solveur.
     *
     * @param dataBase dataBase par defaut
     */
    public PatternGenerator(DataBase dataBase, DefaultConfig config) {
        this.minSupport = config.getMinSupport();
        this.maxPattern = config.getMaxPattern();
        this.minTime = config.getMinTime();
        this.clustersGenerated = new ArrayList<>();
        this.defaultDataBase = dataBase;
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
    protected void run(DataBase dataBase, ArrayList<ArrayList<Integer>> lvl2ClusterId,
                       ArrayList<ArrayList<Integer>> lvl2TimeId, Set<Detector> detectors) {


        ArrayList<Integer> itemsets = new ArrayList<>();
        ArrayList<Integer> freqList = new ArrayList<>();
        Debug.println("DataBase : " + dataBase);
        Debug.println("Run Lvl2TimeId  " + lvl2TimeId);
        Debug.println("Run Lvl2ClusterId  " + lvl2ClusterId);
        run(dataBase, itemsets, dataBase.getTransactionIds(), freqList, lvl2ClusterId, lvl2TimeId, detectors);

    }

    @TraceMethod(displayTitle = true)
    private void run(DataBase dataBase, ArrayList<Integer> itemsets, Set<Integer> transactionIds,
                     ArrayList<Integer> freqList, ArrayList<ArrayList<Integer>> lvl2ClusterId,
                     ArrayList<ArrayList<Integer>> lvl2TimeId, Set<Detector> detectors) {
        Debug.displayTitle();

        int calcurateCoreI = CalcurateCoreI(itemsets, freqList);
        SortedSet<Integer> lowerBounds = GeneratorUtils.lower_bound(dataBase.getClusterIds(), calcurateCoreI);

        //Blocks
        ArrayList<Integer> blocks = new ArrayList<>();
        for (Integer itemset : itemsets) {
            blocks.add(lvl2TimeId.get(itemset).get(0));
        }
        // freq_i
        ArrayList<Integer> freqClusterIds = new ArrayList<>();
        Debug.println("lower bounds : " + lowerBounds.size());

        for (int clusterId : lowerBounds) {

            if (dataBase.getCluster(clusterId).getTransactions().size() >= minSupport &&
                    !itemsets.contains(clusterId)) {
                freqClusterIds.add(clusterId);
            }
        }
        Debug.println("freq_i : " + freqClusterIds);

        ArrayList<Integer> newPathClusters = new ArrayList<>();
        ArrayList<Integer> newFreqList = new ArrayList<>();

        for (int freqClusterId : freqClusterIds) {
            Set<Integer> newTransactionIds = new TreeSet<>();

            int blockOfItem = lvl2TimeId.get(freqClusterId).get(0);
            if (PPCTest(dataBase, itemsets, transactionIds, freqClusterId, newTransactionIds) &&
                    !blocks.contains(blockOfItem)) {
                newPathClusters.clear();
                MakeClosure(dataBase, newTransactionIds, newPathClusters, itemsets, freqClusterId);

                if (maxPattern == 0 || newPathClusters.size() <= maxPattern) {
                    newTransactionIds.clear();

                    Set<Integer> iterTransactionIds = GeneratorUtils
                            .updateTransactions(dataBase, dataBase.getTransactionIds(), newPathClusters, freqClusterId);
                    newFreqList.clear();
                    newFreqList = GeneratorUtils
                            .updateClustersFrequenceCount(dataBase, dataBase.getTransactionIds(), newPathClusters, freqList, freqClusterId);
                    run(dataBase, newPathClusters, iterTransactionIds, newFreqList, lvl2ClusterId, lvl2TimeId, detectors);
                }
            }
        }
        if (!printed) {
            printItemsets(dataBase, itemsets, lvl2ClusterId, lvl2TimeId, detectors);
            printed = true;
        }
    }

    @TraceMethod(displayTitle = true)
    private void printItemsets(DataBase dataBase, ArrayList<Integer> itemsets,
                               ArrayList<ArrayList<Integer>> lvl2ClusterId, ArrayList<ArrayList<Integer>> lvl2TimeId,
                               Set<Detector> detectors) {
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
                    for (Detector detector : detectors) {
                        motifs.put(detector, detector.detect(defaultDataBase, timeBased, clusterBased, transactions));
                    }
                }*/

                if (timeBased.size() > minTime) {
                    for (Detector detector : detectors) {
                        if (motifs.get(detector) == null) {
                            ArrayList<Pattern> patterns = detector.detect(defaultDataBase, timeBased, clusterBased, transactions);
                            //ArrayList<Pattern> patterns = detector.detect(dataBase, timeBased, clusterBased, transactions);
                            motifs.put(detector, patterns);
                        } else {
                            motifs.get(detector).addAll(detector.detect(defaultDataBase, timeBased, clusterBased, transactions));
                            //motifs.get(detector).addAll(detector.detect(dataBase, timeBased, clusterBased, transactions));

                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    public HashMap<Detector, ArrayList<Pattern>> getResults() {
        return motifs;
    }
}
