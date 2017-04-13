package fr.jgetmove.jgetmove.detector;

import java.util.ArrayList;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.motifs.Pattern;

/**
 * 
 * Interface related to the detection of patterns
 *
 */
public interface IDetector {
	
	public ArrayList<Pattern> detect(Database database, ArrayList<ArrayList<ArrayList<Integer>>> clustersGenerated);

}
