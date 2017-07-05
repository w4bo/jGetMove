package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.*;
import fr.jgetmove.jgetmove.debug.Debug;
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
 * IIt's used to call {@link PatternGenerator} to merge them in a single array of itemsets used for detecting different patterns with their given detectors.
 */
public class Solver {

    /**
     * Config shared accross the application
     */
    private final DefaultConfig config;

    /**
     * ItemsetsFinder that will handle the creation of itemsets
     */
    private ItemsetsFinder itemsetsFinder;

    /**
     * PatternGenerator handles the block merging and calls the detection
     */
    private PatternGenerator patternGenerator;

    /**
     * List of detectors called foreach itemset
     *
     * @see SingleDetector#detect(DataBase, Itemset)
     */
    private Set<SingleDetector> singleDetectors;

    /**
     * List of detectors called once, passing all the itemsets
     *
     * @see MultiDetector#detect(DataBase, List)
     */
    private Set<MultiDetector> multiDetectors;


    /**
     * Prepares the solver with all the elements used to detect patterns.
     *
     * @param itemsetsFinder   DI, has for function to find all the itemsets of the database
     * @param patternGenerator DI, has for function to join blocks and detect all patterns
     * @param singleDetectors  initializes all the singleDetectors to use
     * @param multiDetectors   initializes all the multiDetectors to use
     * @param config           configuration
     */
    public Solver(ItemsetsFinder itemsetsFinder, PatternGenerator patternGenerator,
                  Set<SingleDetector> singleDetectors, Set<MultiDetector> multiDetectors, DefaultConfig config) {
        this.itemsetsFinder = itemsetsFinder;
        this.patternGenerator = patternGenerator;
        this.singleDetectors = singleDetectors;
        this.multiDetectors = multiDetectors;
        this.config = config;
    }

    /**
     * Prepares the solver with all the elements used to detect patterns.
     *
     * @param itemsetsFinder   DI, has for function to find all the itemsets of the database
     * @param patternGenerator DI, has for function to join blocks and detect all patterns from the itemsets.
     * @param config           configuration
     */
    public Solver(ItemsetsFinder itemsetsFinder, PatternGenerator patternGenerator, DefaultConfig config) {
        this.itemsetsFinder = itemsetsFinder;
        this.patternGenerator = patternGenerator;
        singleDetectors = new HashSet<>();
        multiDetectors = new HashSet<>();
        this.config = config;
    }

    /**
     * Finds all the itemsets from the database.
     * <p>
     * Breaks the task by blocks (a given time interval {@link DefaultConfig#blockSize} ) and returns an array of blocks containing their respective itemsets.
     * <p>
     * If the {@link DefaultConfig#blockSize} is 0, then a single block is returned, containing all the itemset of the database.
     *
     * @return ArrayList of blocks containing it's id and all the itemsets detected in the block
     */
    public HashMap<Integer, ArrayList<Itemset>> findItemsets(DataBase dataBase) {
        Debug.printTitle("Find Itemsets", Debug.INFO);

        HashMap<Integer, ArrayList<Itemset>> results = new HashMap<>();

        int id = 1;

        if (config.getBlockSize() > 0) {

            BlockBase blockBase;
            Iterator<Integer> lastTime = dataBase.getTimeIds().iterator();

            while ((blockBase = createBlock(id, dataBase, lastTime)) != null) {
                // if there is more than one block, min time is ignored for the glory of mankind (or because it could break the block fusion later on).
                ArrayList<Itemset> result = itemsetsFinder.generate(blockBase, 0);
                results.put(id, result);
                ++id;
            }
        } else {
            ArrayList<Itemset> result = itemsetsFinder.generate(dataBase, config.getMinTime());
            results.put(id, result);
        }

        Debug.println("Blocks", results, Debug.DEBUG);
        Debug.println("n° of Blocks", results.size(), Debug.INFO);
        return results;
    }

    private BlockBase createBlock(int id, DataBase dataBase, Iterator<Integer> lastTime) {
        BlockBase blockBase = null;

        if (lastTime.hasNext()) {
            blockBase = new BlockBase(id);

            int counter = 0;

            while (lastTime.hasNext() && counter < config.getBlockSize()) {
                // adds a time in the blockBase
                Integer timeId = lastTime.next();
                Time blockTime = new Time(timeId);
                blockBase.add(blockTime);

                for (Cluster cluster : dataBase.getTime(timeId).getClusters().values()) {
                    // adds a (possibly new) cluster in the blockBase and links it with the correct time
                    Cluster blockCluster = blockBase.getOrCreateCluster(cluster.getId());
                    blockCluster.setTime(blockTime);

                    for (int transactionId : cluster.getTransactions().keySet()) {
                        // get all the transactions of the cluster, adds them in the blockBase and links it with cluster
                        Transaction blockTransaction = blockBase.getOrCreateTransaction(transactionId);

                        blockCluster.add(blockTransaction);
                        blockTransaction.add(blockCluster);
                    }
                }
                ++counter;
            }
        }


        return blockBase;
    }

    /**
     * Call PatternGenerator and launches the pattern detection system
     *
     * @return a HashMap SingleDetector -> ArrayList< Motif>
     */
    public HashMap<Detector, ArrayList<Pattern>> detectPatterns(DataBase dataBase, HashMap<Integer, ArrayList<Itemset>> results) {
        Debug.printTitle("DetectPatterns", Debug.INFO);
        /*
        // here we will be merging itemsets with each others across blocks so the basic principle is that we will be invoking itemsetsFinder on a different level
        // the equivalence are done like this
        // block -> time
        // itemset -> cluster
        // transaction -> transaction
        HashMap<Integer, Set<Integer>> clustersTransactions = new HashMap<>();
        HashMap<Integer, Pair<Integer, Integer>> equivalenceTable = new HashMap<>();
        HashMap<Integer, Integer> clustersTime = new HashMap<>();

        // custom clusterId(itemsetId) because the times(blocks) are independent with each other and itemsetId begins at 0 for each block
        int clusterId = 0;
        for (Map.Entry<Integer, ArrayList<Itemset>> block : results.entrySet()) {
            int timeId = block.getKey();

            for (Itemset itemset : block.getValue()) {
                clustersTime.put(clusterId, timeId);
                equivalenceTable.put(clusterId, new Pair<>(timeId, itemset.getId()));
                clustersTransactions.put(clusterId, itemset.getTransactions());
                ++clusterId;
            }
        }

        Base itemsetBase = new Base(clustersTransactions, clustersTime);
        ArrayList<Itemset> supersets = itemsetsFinder.generate(itemsetBase, 0);

        // now we know which itemsets are together (each cluster of the superset is an itemset) we need to retrieve them with the equivalence table and flatten the results il a single list of itemsets

        List<Itemset> itemsets = new LinkedList<>();
        for (Itemset superset : supersets) {
            Set<Integer> mergedClusters = new HashSet<>();
            Set<Integer> mergedTimes = new HashSet<>();
            Set<Integer> mergedTransactions = new HashSet<>();
            for (int rawItemsetId : superset.getClusters()) {
                Pair<Integer, Integer> pair = equivalenceTable.get(rawItemsetId);
                int blockId = pair.getKey();
                int itemsetId = pair.getValue();

                Itemset itemset = results.get(blockId).get(itemsetId);


                mergedClusters.addAll(itemset.getClusters());
                mergedTimes.addAll(itemset.getTimes());
                mergedTransactions.addAll(itemset.getTransactions());

            }
            Itemset itemset = new Itemset(itemsets.size(), mergedTransactions, mergedClusters, mergedTimes);
            itemsets.add(itemset);
        }


        Debug.println("Itemsets", itemsets, Debug.DEBUG);
        Debug.println("n° of itemsets", itemsets.size(), Debug.INFO);
        */
        HashMap<Detector, ArrayList<Pattern>> patterns = new HashMap<>(singleDetectors.size() + multiDetectors.size());
        //patternGenerator.generate(dataBase, results);
        for (SingleDetector singleDetector : singleDetectors) {
            patterns.put(singleDetector, new ArrayList<>());
            for (Itemset itemset : results.get(0)) {
                patterns.get(singleDetector).addAll(singleDetector.detect(dataBase, itemset));

            }
        }

        for (MultiDetector multiDetector : multiDetectors) {
            patterns.put(multiDetector, multiDetector.detect(dataBase, results.get(0)));
        }

        Debug.println("Patterns", patterns, Debug.DEBUG);
        Debug.println("n° of patterns", patterns.size(), Debug.INFO);
        return patterns;
    }

    /**
     * {@link SingleDetector#detect(DataBase, Itemset)} is called foreach itemset.
     *
     * @param singleDetector adds this detector to the list of detectors to detect
     */
    private void add(SingleDetector singleDetector) {
        singleDetectors.add(singleDetector);
    }

    /**
     * {@link MultiDetector#detect(DataBase, List)} is called for all Itemsets.
     *
     * @param multiDetector adds this detector to the list of detectors to detect
     */
    private void add(MultiDetector multiDetector) {
        multiDetectors.add(multiDetector);
    }

    /**
     * @param singleDetector removes this detector from the list of detectors to detect
     */
    private void remove(SingleDetector singleDetector) {
        singleDetectors.remove(singleDetector);
    }

    /**
     * @param multiDetector removes this detector from the list of detectors to detect
     */
    private void remove(MultiDetector multiDetector) {
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
                for (JsonObject jsonLink : pattern.getLinksToJson(i)) {
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
    public String toString() {
        return "\n|-- ItemsetsFinder :" + itemsetsFinder
                + "\n|-- PatternGenerator :" + patternGenerator
                + "\n|-- SingleDetectors :" + singleDetectors
                + "\n`-- MultiDetectors :" + multiDetectors;
    }
}
