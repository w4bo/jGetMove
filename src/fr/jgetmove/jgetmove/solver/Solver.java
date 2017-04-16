package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.motifs.Pattern;

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
    public ArrayList<ArrayList<Integer>> generateClusters() {
        return clusterGenerator.generateClusters(database);
    }

    /**
     * Detect patterns
     *
     * @return a HashMap Detector -> ArrayList< Motif>
     */
    public HashMap<Detector, ArrayList<Pattern>> detectPatterns() {
        HashMap<Detector, ArrayList<Pattern>> motifs = new HashMap<>();

        for (Detector detector : detectors) {
            motifs.put(detector, detector.detect(database, clusterGenerator.getClustersGenerated()));
        }

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
