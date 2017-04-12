package fr.jgetmove.jgetmove.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.motifs.Pattern;

/**
 * Class related to the detection of patterns in a database with detectors
 *
 */
public class PatternDetector {
	
	private Database database;
	private Set<IDetector> detectors;
	
	/**
	 * Constructor
	 * @param database
	 * @param detectors
	 */
	public PatternDetector(Database database, Set<IDetector> detectors){
		this.database = database;
		this.detectors = detectors;
	}
	
	/**
	 * Constructor
	 * @param database
	 * @param detector
	 */
	public PatternDetector(Database database, IDetector detector){
		this.database = database;
		this.detectors.add(detector);
	}
	
	/**
	 * Detect patterns
	 * @return a HashMap Detector -> ArrayList<Motif>
	 */
	public HashMap<IDetector,ArrayList<Pattern>> run(){
		
		HashMap <IDetector,ArrayList<Pattern>> motifs = new HashMap<>();
		for(IDetector detector : detectors){
			motifs.put(detector, detector.detect(database));
		}
		return motifs;
		
	}
	
	/**
	 * Add a new detector to the list of detectors
	 * @param detector
	 */
	private void addDetector(IDetector detector){
		detectors.add(detector);
	}
	/**
	 * Remove the detector from the list of detectors
	 * @param detector
	 */
	private void removeDetector(IDetector detector){
		detectors.remove(detector);
	}
}
