package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;

/**
 * Interface related to the detection of patterns
 */
public interface Detector {

    ArrayList<Pattern> detect(Database database, ArrayList<ArrayList<Integer>> clustersGenerated);

}
