package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @version 1.1.0
 * @since 0.2.0
 */
public interface MultiDetector extends Detector {
    ArrayList<Pattern> detect(DataBase defaultDataBase, Collection<Itemset> itemset);
}
