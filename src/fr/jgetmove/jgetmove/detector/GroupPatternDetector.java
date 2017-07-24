package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;

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
