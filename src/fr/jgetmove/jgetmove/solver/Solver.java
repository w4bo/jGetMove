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
 * Manager that handles a itemsetFinder and singleDetectors
 */
public class Solver {

    private final int blockSize;
    /**
     * ItemsetFinder that will handle the creation of itemsets
     */
    private ItemsetFinder itemsetFinder;
    private PatternGenerator patternGenerator;

    /**
     * Detectors of patterns
     */
    private Set<SingleDetector> singleDetectors;
    private Set<MultiDetector> multiDetectors;


    /**
     * Constructor
     *
     * @param itemsetFinder   ItemsetFinder that will handle the creation of itemsets
     * @param singleDetectors list of singleDetectors
     * @param blockSize       size of block
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
     * Constructor
     *
     * @param itemsetFinder ItemsetFinder that will handle the creation of itemsets
     */
    public Solver(ItemsetFinder itemsetFinder, PatternGenerator patternGenerator, int blockSize) {
        this.itemsetFinder = itemsetFinder;
        this.patternGenerator = patternGenerator;
        singleDetectors = new HashSet<>();
        this.blockSize = blockSize;
    }

    /**
     * Generate a list of clusters (Itemsets)
     *
     * @return the list of clusters (Itemsets) generated from itemsetFinder
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
     * Detect patterns
     *
     * @return a HashMap SingleDetector -> ArrayList< Motif>
     */
    public HashMap<Detector, ArrayList<Pattern>> detectPatterns(DataBase dataBase, ArrayList<ItemsetsOfBlock> results) {
        Debug.printTitle("DetectPatterns", Debug.INFO);

        HashMap<Detector, ArrayList<Pattern>> motifs = new HashMap<>(singleDetectors.size());
        //patternGenerator.generate(dataBase, results);
        // TODO : é ouais
        for (ItemsetsOfBlock itemsets : results) {
            for (Itemset itemset : itemsets.getItemsets()) {
                for (SingleDetector singleDetector : singleDetectors) {
                    motifs.put(singleDetector, singleDetector.detect(dataBase, itemset));
                }
            }

            for (MultiDetector multiDetector : multiDetectors) {
                motifs.put(multiDetector, multiDetector.detect(dataBase, itemsets.getItemsetArrayList()));
            }
        }

        return motifs;
    }

    /**
     * Add a new singleDetector to the list of singleDetectors
     *
     * @param singleDetector
     */
    private void add(SingleDetector singleDetector) {
        singleDetectors.add(singleDetector);
    }

    private void add(MultiDetector multiDetector) {
        multiDetectors.add(multiDetector);
    }

    /**
     * Remove the singleDetector from the list of singleDetectors
     *
     * @param singleDetector
     */
    private void remove(SingleDetector singleDetector) {
        singleDetectors.remove(singleDetector);
    }

    private void removeDetector(MultiDetector multiDetector) {
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
                + "\n`-- PatternGenerator :" + patternGenerator;
    }
}
