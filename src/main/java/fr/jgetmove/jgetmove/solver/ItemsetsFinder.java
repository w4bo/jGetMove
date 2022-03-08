/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Base;
import fr.jgetmove.jgetmove.database.Itemset;

import java.util.ArrayList;

/**
 * Interface used for all the algorithm in charge of finding itemsets.
 *
 * @author stardisblue
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ItemsetsFinder {


    /**
     * Method invoked, must contain the algorithm in charge of finding the itemsets
     *
     * @param base    database
     * @param minTime minimal number of time
     * @return detected itemsets
     */
    ArrayList<Itemset> generate(Base base, final int minTime);
}
