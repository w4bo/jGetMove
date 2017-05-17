package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.pattern.Pattern;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * Manager that handles a clusterGenerator and detectors
 */
public class Solver {

    /**
     * ClusterGenerator that will handle the creation of itemsets
     */
    private ClusterGenerator clusterGenerator;

    /**
     * Detectors of patterns
     */
    private Set<Detector> detectors;

    /**
     * The database
     */
    private Database database;

    /**
     * The result of the ClusterGenerator
     */
    private ClusterGeneratorResult result;

    /**
     * Constructor
     *
     * @param database         database from files
     * @param clusterGenerator ClusterGenerator that will handle the creation of itemsets
     * @param detectors        list of detectors
     */
    public Solver(Database database, ClusterGenerator clusterGenerator, Set<Detector> detectors) {
        this.database = database;
        this.clusterGenerator = clusterGenerator;
        this.detectors = detectors;
    }

    /**
     * Constructor
     *
     * @param database         database from files
     * @param clusterGenerator ClusterGenerator that will handle the creation of itemsets
     */
    public Solver(Database database, ClusterGenerator clusterGenerator) {
        this.database = database;
        this.clusterGenerator = clusterGenerator;
        detectors = new HashSet<>();
    }

    /**
     * Generate a list of clusters (Itemsets)
     *
     * @return the list of clusters (Itemsets) generated from clusterGenerator
     */
    public ClusterGeneratorResult generateClusters() {
        result = clusterGenerator.generate();
        return result;
    }

    /**
     * Detect patterns
     *
     * @return a HashMap Detector -> ArrayList< Motif>
     */
    public HashMap<Detector, ArrayList<Pattern>> detectPatterns() {
        PatternGenerator patternGenerator = new PatternGenerator(database, 1, 0, 1);
        Debug.println(result);
        patternGenerator.run(result.getDatabase(), result.getLvl2ClusterIds(), result.getLvl2TimeIds(), detectors);
        /*for (Detector detector : detectors) {
            motifs.put(detector, detector.detect(database, clusterGenerator.getClustersGenerated()));
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

    public String toJSON(HashMap<Detector, ArrayList<Pattern>> detectors, JsonObjectBuilder databaseJson) {
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
        databaseJson.add("pattern", jsonPatterns);
        return databaseJson.build().toString();
    }


}
