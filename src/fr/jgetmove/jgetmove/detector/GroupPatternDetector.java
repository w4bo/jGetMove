/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
