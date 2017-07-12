package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;

public class GroupPatternDetector implements MultiDetector {

    int minTime;
    double commonObjectPercentage;

    public GroupPatternDetector(int minTime, double commonObjectPercentage) {
        this.minTime = minTime;
        this.commonObjectPercentage = commonObjectPercentage;
    }

    @Override
    public ArrayList<Pattern> detect(DataBase defaultDataBase, Collection<Itemset> itemset) {
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
