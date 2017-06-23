package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Path;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.pattern.Pattern;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * Manager that handles a pathDetector and detectors
 */
public class Solver {

    /**
     * PathDetector that will handle the creation of itemsets
     */
    private PathDetector pathDetector;
    private PatternGenerator patternGenerator;

    /**
     * Detectors of patterns
     */
    private Set<Detector> detectors;

    /**
     * The result of the PathDetector
     */
    private ArrayList<Path> result;

    /**
     * Constructor
     *
     * @param pathDetector PathDetector that will handle the creation of itemsets
     * @param detectors        list of detectors
     */
    public Solver(PathDetector pathDetector, PatternGenerator patternGenerator,
                  Set<Detector> detectors) {
        this.pathDetector = pathDetector;
        this.patternGenerator = patternGenerator;
        this.detectors = detectors;
    }

    /**
     * Constructor
     *
     * @param pathDetector PathDetector that will handle the creation of itemsets
     */
    public Solver(PathDetector pathDetector, PatternGenerator patternGenerator) {
        this.pathDetector = pathDetector;
        this.patternGenerator = patternGenerator;
        detectors = new HashSet<>();
    }

    /**
     * Generate a list of clusters (Itemsets)
     *
     * @return the list of clusters (Itemsets) generated from pathDetector
     */
    public ArrayList<Path> generateClusters() {
        result = pathDetector.generate();
        return result;
    }

    /**
     * Detect patterns
     *
     * @return a HashMap Detector -> ArrayList< Motif>
     */
    public HashMap<Detector, ArrayList<Pattern>> detectPatterns() {
        Debug.println("resultats", result, Debug.WARNING);
        //patternGenerator.run(result.getDatabase(), result.getLvl2ClusterIds(), result.getLvl2TimeIds(), detectors);
        /*for (Detector detector : detectors) {
            motifs.put(detector, detector.detect(database, pathDetector.getClustersGenerated()));
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
                + "\n|-- PathDetector :" + pathDetector
                + "\n|-- PatternGenerator :" + patternGenerator;
    }
}
