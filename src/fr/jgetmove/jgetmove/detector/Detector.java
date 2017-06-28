package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Interface related to the detection of patterns
 */
public interface Detector {

    ArrayList<Pattern> detect(DataBase defaultDataBase, Set<Integer> timeBased, Set<Integer> clusterBased,
                              Collection<Transaction> transactions);

}
