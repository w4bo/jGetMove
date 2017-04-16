package fr.jgetmove.jgetmove.solver;

import java.util.ArrayList;
import fr.jgetmove.jgetmove.database.Database;

public interface Generator {
	
	public ArrayList<ArrayList<Integer>> generateClusters(Database database);
}
