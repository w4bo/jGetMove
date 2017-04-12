package fr.jgetmove.detector;

import java.util.ArrayList;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.motifs.Motif;

/**
 * 
 * Interface related to the detection of patterns in the 
 *
 */
public interface IDetector {
	
	public ArrayList<Motif> detect(Database database);

}
