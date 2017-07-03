package fr.jgetmove.jgetmove.solver;

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
 * It's used to call {@link ItemsetFinder} to detect all the itemsets per block
 * <p>
 * IIt's used to call {@link PatternGenerator} to merge them in a single array of itemsets used for detecting different patterns with their given detectors.
 */
public class Solver {

    /**
     * Size of each block, used for ItemsetFinder
     */
    private final int blockSize;

    /**
     * ItemsetFinder that will handle the creation of itemsets
     */
    private ItemsetFinder itemsetFinder;

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
     * @see MultiDetector#detect(DataBase, ArrayList)
     */
    private Set<MultiDetector> multiDetectors;


    /**
     * Prepares the solver with all the elements used to detect patterns.
     *
     * @param itemsetFinder    DI, has for function to find all the itemsets of the database
     * @param patternGenerator DI, has for function to join blocks and detect all patterns
     * @param singleDetectors  initializes all the singleDetectors to use
     * @param multiDetectors   initializes all the multiDetectors to use
     * @param blockSize        fixes the size of each block.
     */
    public Solver(ItemsetFinder itemsetFinder, PatternGenerator patternGenerator,
                  Set<SingleDetector> singleDetectors, Set<MultiDetector> multiDetectors, int blockSize) {
        this.itemsetFinder = itemsetFinder;
        this.patternGenerator = patternGenerator;
        this.singleDetectors = singleDetectors;
        this.multiDetectors = multiDetectors;
        this.blockSize = blockSize;
    }

    /**
     * Prepares the solver with all the elements used to detect patterns.
     *
     * @param itemsetFinder    DI, has for function to find all the itemsets of the database
     * @param patternGenerator DI, has for function to join blocks and detect all patterns from the itemsets.
     * @param blockSize        fixes the size of each block.
     */
    public Solver(ItemsetFinder itemsetFinder, PatternGenerator patternGenerator, int blockSize) {
        this.itemsetFinder = itemsetFinder;
        this.patternGenerator = patternGenerator;
        singleDetectors = new HashSet<>();
        multiDetectors = new HashSet<>();
        this.blockSize = blockSize;
    }

    /**
     * Finds all the itemsets from the database.
     * <p>
     * Breaks the task by blocks (a given time interval {@link Solver#blockSize} ) and returns an array of blocks containing their respective itemsets.
     * <p>
     * If the {@link Solver#blockSize} is 0, then a single block is returned, containing all the itemset of the database.
     *
     * @return ArrayList of blocks containing it's id and all the itemsets detected in the block
     */
    public ArrayList<ItemsetsOfBlock> findItemsets(DataBase dataBase) {
        Debug.printTitle("Find Itemsets", Debug.INFO);

        ArrayList<ItemsetsOfBlock> results = new ArrayList<>();

        if (blockSize > 0) {

            BlockBase blockBase;
            int id = 0;
            Iterator<Integer> lastTime = dataBase.getTimeIds().iterator();

            while ((blockBase = createBlock(id, dataBase, lastTime)) != null) {
                TreeSet<Itemset> result = itemsetFinder.generate(blockBase);
                results.add(new ItemsetsOfBlock(id, result));
                ++id;
            }
        } else {
            results.add(new ItemsetsOfBlock(0, itemsetFinder.generate(dataBase)));
        }

        System.out.println("results = " + results);
        return results;
    }

    private BlockBase createBlock(int id, DataBase dataBase, Iterator<Integer> lastTime) {
        BlockBase blockBase = null;

        if (lastTime.hasNext()) {
            blockBase = new BlockBase(id);

            int counter = 0;

            while (lastTime.hasNext() && counter < blockSize) {
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
    public HashMap<Detector, ArrayList<Pattern>> detectPatterns(DataBase dataBase, ArrayList<ItemsetsOfBlock> results) {
        Debug.printTitle("DetectPatterns", Debug.INFO);

        HashMap<Detector, ArrayList<Pattern>> pattern = new HashMap<>(singleDetectors.size() + multiDetectors.size());
        //patternGenerator.generate(dataBase, results);
        // TODO : é ouais
        for (ItemsetsOfBlock itemsets : results) {
            for (Itemset itemset : itemsets.getItemsets()) {
                for (SingleDetector singleDetector : singleDetectors) {
                    pattern.put(singleDetector, singleDetector.detect(dataBase, itemset));
                }
            }

            for (MultiDetector multiDetector : multiDetectors) {
                pattern.put(multiDetector, multiDetector.detect(dataBase, itemsets.getItemsetArrayList()));
            }
        }

        return pattern;
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
     * {@link MultiDetector#detect(DataBase, ArrayList)} is called for all Itemsets.
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
        return "\n|-- ItemsetFinder :" + itemsetFinder
                + "\n|-- PatternGenerator :" + patternGenerator
                + "\n|-- SingleDetectors :" + singleDetectors
                + "\n`-- MultiDetectors :" + multiDetectors;
    }
}
