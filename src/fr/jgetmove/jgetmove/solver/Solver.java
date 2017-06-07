package fr.jgetmove.jgetmove.solver;

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
    private PatternGenerator patternGenerator;

    /**
     * Detectors of patterns
     */
    private Set<Detector> detectors;

    /**
     * The result of the ClusterGenerator
     */
    private ClusterGeneratorResult result;

    /**
     * Constructor
     *
     * @param clusterGenerator ClusterGenerator that will handle the creation of itemsets
     * @param detectors        list of detectors
     */
    public Solver(ClusterGenerator clusterGenerator, PatternGenerator patternGenerator,
                  Set<Detector> detectors) {
        this.clusterGenerator = clusterGenerator;
        this.patternGenerator = patternGenerator;
        this.detectors = detectors;
    }

    /**
     * Constructor
     *
     * @param clusterGenerator ClusterGenerator that will handle the creation of itemsets
     */
    public Solver(ClusterGenerator clusterGenerator, PatternGenerator patternGenerator) {
        this.clusterGenerator = clusterGenerator;
        this.patternGenerator = patternGenerator;
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

    public JsonArrayBuilder toJSON(HashMap<Detector, ArrayList<Pattern>> detectors) {
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
        return "Solver"
                + "\n|-- ClusterGenerator :" + clusterGenerator
                + "\n|-- PatternGenerator :" + patternGenerator;
    }
}
