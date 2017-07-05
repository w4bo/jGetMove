package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public interface MultiDetector extends Detector {
    ArrayList<Pattern> detect(final DataBase defaultDataBase, final List<Itemset> itemset);
}
