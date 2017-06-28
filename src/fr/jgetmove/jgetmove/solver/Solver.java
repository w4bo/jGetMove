package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.*;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.pattern.Pattern;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * Manager that handles a pathFinder and detectors
 */
public class Solver {

    private final int blockSize;
    /**
     * PathFinder that will handle the creation of itemsets
     */
    private PathFinder pathFinder;
    private PatternGenerator patternGenerator;

    /**
     * Detectors of patterns
     */
    private Set<Detector> detectors;

    /**
     * The results of the PathFinder
     */
    private ArrayList<PathsOfBlock> results;

    /**
     * Constructor
     *
     * @param pathFinder PathFinder that will handle the creation of itemsets
     * @param detectors  list of detectors
     * @param blockSize
     */
    public Solver(PathFinder pathFinder, PatternGenerator patternGenerator,
                  Set<Detector> detectors, int blockSize) {
        this.pathFinder = pathFinder;
        this.patternGenerator = patternGenerator;
        this.detectors = detectors;
        this.blockSize = blockSize;
        this.results = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param pathFinder PathFinder that will handle the creation of itemsets
     */
    public Solver(PathFinder pathFinder, PatternGenerator patternGenerator, int blockSize) {
        this.pathFinder = pathFinder;
        this.patternGenerator = patternGenerator;
        detectors = new HashSet<>();
        this.results = new ArrayList<>();
        this.blockSize = blockSize;
    }

    /**
     * Generate a list of clusters (Itemsets)
     *
     * @return the list of clusters (Itemsets) generated from pathFinder
     */
    public ArrayList<PathsOfBlock> generatePath(DataBase dataBase) {
        if (blockSize > 0) {

            BlockBase blockBase;
            int id = 0;
            Iterator<Integer> lastTime = dataBase.getTimeIds().iterator();

            while ((blockBase = createBlock(id, dataBase, lastTime)) != null) {
                TreeSet<Path> result = pathFinder.generate(blockBase);
                results.add(new PathsOfBlock(id, result));
                ++id;
            }
        } else {
            results.add(new PathsOfBlock(0, pathFinder.generate(dataBase)));
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
     * @return a HashMap Detector -> ArrayList< Motif>
     */
    public HashMap<Detector, ArrayList<Pattern>> detectPatterns() {
        Debug.println("results", results, Debug.WARNING);
        //patternGenerator.run(results, detectors);
        /*for (Detector detector : detectors) {
            motifs.put(detector, detector.detect(database, pathFinder.getClustersGenerated()));
        }*/

        return patternGenerator.getResults();
    }

    /**
     * Add a new detector to the list of detectors
     *
     * @param detector
     */
    private void addDetector(Detector detector) {
        detectors.add(detector);
    }

    /**
     * Remove the detector from the list of detectors
     *
     * @param detector
     */
    private void removeDetector(Detector detector) {
        detectors.remove(detector);
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
        return "\n|-- PathFinder :" + pathFinder
                + "\n`-- PatternGenerator :" + patternGenerator;
    }
}
