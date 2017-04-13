package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.motifs.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Class related to the detection of patterns in a database with detectors
 */
public class DetectionManager {

    private Database database;
    private Set<Detector> detectors;
    private ArrayList<ArrayList<ArrayList<Integer>>> clustersGenerated;

    /**
     * Constructor
     *
     * @param database
     * @param detectors
     */
    public DetectionManager(Database database, Set<Detector> detectors, ArrayList<ArrayList<ArrayList<Integer>>> clustersGenerated) {
        this.database = database;
        this.detectors = detectors;
        this.clustersGenerated = clustersGenerated;
    }

    /**
     * Constructor
     *
     * @param database
     * @param detector
     */
    public DetectionManager(Database database, Detector detector, ArrayList<ArrayList<ArrayList<Integer>>> clustersGenerated) {
        this.database = database;
        this.detectors.add(detector);
        this.clustersGenerated = clustersGenerated;
    }

    /**
     * Detect patterns
     *
     * @return a HashMap Detector -> ArrayList<Motif>
     */
    public HashMap<Detector, ArrayList<Pattern>> run() {

        HashMap<Detector, ArrayList<Pattern>> motifs = new HashMap<>();
        for (Detector detector : detectors) {
            motifs.put(detector, detector.detect(database, clustersGenerated));
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
