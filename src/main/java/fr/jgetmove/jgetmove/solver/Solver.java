/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.Config;
import fr.jgetmove.jgetmove.database.*;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.detector.MultiDetector;
import fr.jgetmove.jgetmove.detector.SingleDetector;
import fr.jgetmove.jgetmove.pattern.Pattern;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * Manager that handles all the core logic of the application.
 * <p>
 * It's used to call {@link ItemsetsFinder} to detect all the itemsets per block
 * <p>
 * It's used to call {@link BlockMerger} to merge them in a single array of itemsets used for detecting different patterns with their given detectors.
 *
 * @author stardisblue
 * @author Carmona-Anthony
 * @version 2.0.0
 * @since 0.1.0
 */
public class Solver implements PrettyPrint {

    /**
     * Config shared accross the application
     */
    private final Config config;

    /**
     * ItemsetsFinder that will handle the creation of itemsets
     */
    private ItemsetsFinder itemsetsFinder;

    /**
     * blockMerger will handle the fusion of the blocks
     */
    private BlockMerger blockMerger;

    /**
     * List of detectors called foreach itemset
     *
     * @see SingleDetector#detect(DataBase, Itemset)
     */
    private Set<SingleDetector> singleDetectors;

    /**
     * List of detectors called once, passing all the itemsets
     *
     * @see MultiDetector#detect(DataBase, Collection)
     */
    private Set<MultiDetector> multiDetectors;


    /**
     * Prepares the solver with all the elements used to detect patterns.
     *
     * @param itemsetsFinder  DI, has for function to find all the itemsets of the database
     * @param blockMerger     DI, has for funtion to merge all the blocks created by the itemsetsFinder
     * @param singleDetectors DI, initializes all the singleDetectors to use
     * @param multiDetectors  DI, initializes all the multiDetectors to use
     * @param config          configuration
     */
    public Solver(ItemsetsFinder itemsetsFinder, BlockMerger blockMerger,
                  Set<SingleDetector> singleDetectors, Set<MultiDetector> multiDetectors, Config config) {
        this.itemsetsFinder = itemsetsFinder;
        this.blockMerger = blockMerger;
        this.singleDetectors = singleDetectors;
        this.multiDetectors = multiDetectors;
        this.config = config;
    }

    /**
     * Finds all the itemsets in the database.
     * <p>
     * Breaks the task by blocks (a given time interval {@link Config#blockSize} ) and returns an array of blocks containing their respective itemsets.
     * <p>
     * If the {@link Config#blockSize} is 0, then a single block is returned, containing all the itemset of the database.
     *
     * @param dataBase database
     * @return ArrayList of blocks containing it's id and all the itemsets detected in the block
     */
    public ArrayList<ArrayList<Itemset>> findItemsets(DataBase dataBase) {
        Debug.printTitle("Itemsets Finder", Debug.INFO);

        ArrayList<ArrayList<Itemset>> blockItemsets = new ArrayList<>();

        if (config.getBlockSize() > 0) { // if blockSize is set
            Base base;
            Iterator<Integer> lastTime = dataBase.getTimeIds().iterator();

            while ((base = createBlock(dataBase, lastTime)) != null) {
                // if there is more than one block, min time is ignored for the glory of mankind (or because it could break the block fusion later on).
                // generating the itemsets
                ArrayList<Itemset> itemsets = itemsetsFinder.generate(base, 0);
                // putting the itemsets in the block
                blockItemsets.add(itemsets);
            }
        } else { // if blockSize is not set
            // One block for all of this
            ArrayList<Itemset> result = itemsetsFinder.generate(dataBase, config.getMinTime());
            // aaand putting the itemsets in the first block
            blockItemsets.add(result);
        }

        Debug.println("Blocks", blockItemsets, Debug.DEBUG);
        Debug.println("n° of Blocks", blockItemsets.size(), Debug.INFO);
        blockItemsets.trimToSize();
        return blockItemsets;
    }

    /**
     * Creates and returns an new {@link Base} if and only if there it's possible to create another {@link Base} from the {@link DataBase}.
     * <p>
     * If the lastTime iterator is
     *
     * @param dataBase to create the Blocks from
     * @param lastTime the time from where the block need to begin
     * @return null or the created Block
     */
    private Base createBlock(DataBase dataBase, Iterator<Integer> lastTime) {
        Base base = null;

        if (lastTime.hasNext()) {
            base = new Base();

            int counter = 0;

            while (lastTime.hasNext() && counter < config.getBlockSize()) {
                // adds a time in the base
                Integer timeId = lastTime.next();
                Time blockTime = new Time(timeId);
                base.add(blockTime);

                for (Cluster cluster : dataBase.getTime(timeId).getClusters().values()) {
                    // adds a (possibly new) cluster in the base and links it with the correct time
                    Cluster blockCluster = base.getOrCreateCluster(cluster.getId());
                    blockCluster.setTime(blockTime);

                    for (int transactionId : cluster.getTransactions().keySet()) {
                        // get all the transactions of the cluster, adds them in the base and links it with cluster
                        Transaction blockTransaction = base.getOrCreateTransaction(transactionId);

                        blockCluster.add(blockTransaction);
                        blockTransaction.add(blockCluster);
                    }
                }
                ++counter;
            }
        }


        return base;
    }

    /**
     * Call BlockMerger and launches the pattern detection system
     *
     * @param dataBase database
     * @param itemsets itemsets to detect pattern from
     * @return a HashMap SingleDetector &rarr; ArrayList&lt; Motif &gt;
     */
    public HashMap<Detector, ArrayList<Pattern>> detectPatterns(DataBase dataBase, ArrayList<Itemset> itemsets) {
        Debug.printTitle("Detecting Patterns", Debug.INFO);
        HashMap<Detector, ArrayList<Pattern>> patterns = new HashMap<>(singleDetectors.size() + multiDetectors.size());
        //patternGenerator.generate(dataBase, results);


        for (SingleDetector singleDetector : singleDetectors) {
            patterns.put(singleDetector, new ArrayList<>());
            ArrayList<Pattern> detectorPatterns = patterns.get(singleDetector);
            for (Itemset itemset : itemsets) {
                detectorPatterns.addAll(singleDetector.detect(dataBase, itemset));
            }

            Debug.println(singleDetector.toString(), detectorPatterns, Debug.DEBUG);
            Debug.println(singleDetector.toString(), detectorPatterns.size() + " patterns found", Debug.INFO);

        }

        for (MultiDetector multiDetector : multiDetectors) {
            patterns.put(multiDetector, multiDetector.detect(dataBase, itemsets));

            Debug.println(multiDetector.toString(), patterns.get(multiDetector), Debug.DEBUG);
            Debug.println(multiDetector.toString(), patterns.get(multiDetector).size() + " patterns found", Debug.INFO);
        }

        return patterns;
    }

    /**
     * Here we will be merging itemsets with each others across blocks so the basic principle is that we will be invoking itemsetsFinder on a different level.
     * the equivalence are done like this
     * block &rarr; time
     * itemset &rarr; cluster
     * transaction &rarr; transaction
     *
     * @param results array of blocks and their itemsets
     * @return the resulting list of itemsets
     */
    public ArrayList<Itemset> mergeBlocks(ArrayList<ArrayList<Itemset>> results) {
        Debug.printTitle("Block Merging", Debug.INFO);

        HashMap<Integer, Integer> clustersTime = new HashMap<>();
        HashMap<Integer, Set<Integer>> clustersTransactions = new HashMap<>();
        HashMap<Integer, Itemset> equivalenceTable = new HashMap<>();

        // custom clusterId(itemsetId) because the times(blocks) are independent with each other and itemsetId begins at 0 for each block
        int clusterId = 0;
        int blockIndex = 0;
        int resultsSize = results.size();

        while (blockIndex < resultsSize) {
            int timeId = blockIndex + 1;
            for (Itemset itemset : results.get(blockIndex)) {
                clustersTime.put(clusterId, timeId);
                clustersTransactions.put(clusterId, itemset.getTransactions());
                equivalenceTable.put(clusterId, itemset);
                ++clusterId;
            }
            blockIndex++;
        }

        Base itemsetBase = new Base(clustersTransactions, clustersTime);
        ArrayList<Itemset> supersets = blockMerger.generate(itemsetBase);

        // now we know which itemsets are together (each cluster of the superset is an itemset) we need to retrieve them with the equivalence table and flatten the results il a single list of itemsets

        ArrayList<Itemset> itemsets = new ArrayList<>();
        for (Itemset superset : supersets) {
            Set<Integer> mergedClusters = new HashSet<>();
            Set<Integer> mergedTimes = new HashSet<>();
            Set<Integer> mergedTransactions = new HashSet<>();
            for (int mappedItemsetIndex : superset.getClusters()) {
                Itemset itemset = equivalenceTable.get(mappedItemsetIndex);

                mergedClusters.addAll(itemset.getClusters());
                mergedTimes.addAll(itemset.getTimes());
                mergedTransactions.addAll(superset.getTransactions());

            }
            if (mergedClusters.size() > config.getMinTime()) {
                Itemset itemset = new Itemset(mergedTransactions, mergedClusters, mergedTimes);
                itemsets.add(itemset);
            }
        }

        Debug.println("Itemsets", itemsets, Debug.DEBUG);
        Debug.println("n° of itemsets", itemsets.size(), Debug.INFO);
        return itemsets;
    }

    /**
     * {@link SingleDetector#detect(DataBase, Itemset)} is called foreach itemset.
     *
     * @param singleDetector adds this detector to the list of detectors to detect
     */
    public void add(SingleDetector singleDetector) {
        singleDetectors.add(singleDetector);
    }

    /**
     * {@link MultiDetector#detect(DataBase, Collection)} is called for all Itemsets.
     *
     * @param multiDetector adds this detector to the list of detectors to detect
     */
    public void add(MultiDetector multiDetector) {
        multiDetectors.add(multiDetector);
    }

    /**
     * @param singleDetector removes this detector from the list of detectors to detect
     */
    public void remove(SingleDetector singleDetector) {
        singleDetectors.remove(singleDetector);
    }

    /**
     * @param multiDetector removes this detector from the list of detectors to detect
     */
    public void remove(MultiDetector multiDetector) {
        multiDetectors.remove(multiDetector);
    }

    public JsonArrayBuilder toJson(HashMap<Detector, ArrayList<Pattern>> detectors) {
        JsonArrayBuilder jsonPatterns = Json.createArrayBuilder();

        for (Map.Entry<Detector, ArrayList<Pattern>> detector : detectors.entrySet()) {
            JsonObjectBuilder jsonPattern = Json.createObjectBuilder();

            jsonPattern.add("name", detector.getKey().getClass().getSimpleName());

            ArrayList<Pattern> patterns = detector.getValue();

            JsonArrayBuilder jsonLinks = Json.createArrayBuilder();

            int i = 0;
            for (Pattern pattern : patterns) {
                jsonPattern.add("name", pattern.getClass().getSimpleName());
                for (JsonObject jsonLink : pattern.toJsonArray(i)) {
                    jsonLinks.add(jsonLink);
                }
                i++;
            }

            jsonPattern.add("links", jsonLinks);
            jsonPatterns.add(jsonPattern);
        }
        return jsonPatterns;
    }

    @Override
    public String toPrettyString() {
        return "\n|-- ItemsetsFinder :" + itemsetsFinder
                + "\n|-- SingleDetectors :" + singleDetectors
                + "\n`-- MultiDetectors :" + multiDetectors;
    }

    @Override
    public String toString() {
        return "\nSolver :" + Debug.indent(toPrettyString());
    }
}
