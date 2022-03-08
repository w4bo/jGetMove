/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;

/**
 * Note: not implemented yet
 */
public class GroupPatternDetector implements MultiDetector {

    int minTime;
    double commonObjectPercentage;

    public GroupPatternDetector(int minTime, double commonObjectPercentage) {
        this.minTime = minTime;
        this.commonObjectPercentage = commonObjectPercentage;
    }

    @Override
    public ArrayList<Pattern> detect(DataBase defaultDataBase, Collection<Itemset> itemsets) {
        int nbClusters = defaultDataBase.getClusters().size();
        int nbItemsets = itemsets.size();

        // preprocessing : remove all the unneeded itemsets
        ArrayList<BitSet> itemsetMatrix = new ArrayList<>(nbItemsets);
        for (Itemset itemset : itemsets) {
            BitSet clusters = new BitSet(nbClusters);

            for (int clusterId : itemset.getClusters()) {
                clusters.set(clusterId);
            }
            itemsetMatrix.add(clusters);
        }


        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
