package fr.jgetmove.detector;

import java.util.ArrayList;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.motifs.Motif;

/**
 * Class/Singleton related to the detection of convoys in a database
 *
 */
public class ConvoyDetector implements IDetector{
	
	private static ConvoyDetector convoyDetector;
	
	/**
	 * Empty Constructor
	 */
	public ConvoyDetector(){
		
	}
	
	/**
	 * Cree une instance de ConvoyDetector ou retourne celle deja presente
	 * @return une nouvelle instance de convoyDetector si elle n'a pas été deja crée
	 */
	public ConvoyDetector newInstance(){
		if(convoyDetector == null){
			convoyDetector = new ConvoyDetector();
			return convoyDetector;
		}
		return convoyDetector;
	}

	/**
	 * Detect les pattern associé au detecteur
	 * @return une liste de motif/pattern present dans la database pour ce detecteur
	 */
	public ArrayList<Motif> detect(Database database) {
		// TODO Auto-generated method stub
		return null;
	}

}
