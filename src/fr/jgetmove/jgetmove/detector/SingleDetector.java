package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;

/**
 * Interface related to the detection of patterns
 */
public interface SingleDetector extends Detector {

    ArrayList<Pattern> detect(DataBase defaultDataBase, Itemset itemset);

}
