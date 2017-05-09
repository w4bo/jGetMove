package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
        HashMap<Detector, ArrayList<Pattern>> motifs = new HashMap<>();
        
        PatternGenerator patternGenerator = new PatternGenerator(database, 1, 0, 1);
        Debug.println("Database " + result.getDatabase());
        Debug.println("Results clusters " + result.getClusters());
        Debug.println("Results lvl2ClusterIds " + result.getLvl2ClusterIds());
        Debug.println("Results lvl2TimeIds " + result.getLvl2TimeIds());
        patternGenerator.run(result.getDatabase(),result.getLvl2ClusterIds() , result.getLvl2TimeIds(), detectors);
        /*for (Detector detector : detectors) {
            motifs.put(detector, detector.detect(database, clusterGenerator.getClustersGenerated()));
        }*/

        return motifs;
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
}
