package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.pattern.GroupPattern;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GroupPatternDetector implements SingleDetector {

    int minTime;
    double commonObjectPercentage;

    public GroupPatternDetector(int minTime, double commonObjectPercentage) {
        this.minTime = minTime;
        this.commonObjectPercentage = commonObjectPercentage;
    }

    @Override
    public ArrayList<Pattern> detect(DataBase defaultDataBase, Itemset itemset) {
        Debug.println("Start Group Pattern", Debug.DEBUG);
        ArrayList<Pattern> patterns = new ArrayList<>();

        int lastTime = -1;
        int firstTime = -1;
        int currentTime = -1;
        int currentIndex = -1;
        int lastIndex = -1;
        int firstIndex = -1;

        int numOverlapTimePoint = 0;

        ArrayList<Integer> startConvoy = new ArrayList<>();
        ArrayList<Integer> endConvoy = new ArrayList<>();
        ArrayList<Integer> startConvoyIndex = new ArrayList<>();
        ArrayList<Integer> endConvoyIndex = new ArrayList<>();

        ArrayList<Integer> clusters = new ArrayList<>(itemset.getClusters());
        ArrayList<Integer> times = new ArrayList<>(itemset.getTimes());

        firstTime = times.get(0);
        firstIndex = 0;
        lastTime = firstTime;
        lastIndex = firstIndex;

        //Correct object set
        ArrayList<Integer> goodTransactions = new ArrayList<>(
                defaultDataBase.getClusterTransactions(clusters.get(0)).keySet());

        for (int i = 0; i < times.size(); i++) {
            //Current ObjectSet
            ArrayList<Integer> currentTransactions = new ArrayList<>(
                    defaultDataBase.getClusterTransactions(clusters.get(i)).keySet());

            currentTime = times.get(i);
            currentIndex = i;

            if (currentTime == (lastTime + 1)) {
                ArrayList<Integer> objectTemp = new ArrayList<>();
                for (int j = 0; j < goodTransactions.size(); j++) {
                    if (currentTransactions.contains(goodTransactions.get(j))) {
                        objectTemp.add(goodTransactions.get(j));
                    }
                }
                goodTransactions = objectTemp;
                lastTime = currentTime;
                lastIndex = currentIndex;
            } else {
                if (currentTime > (lastTime + 1)) {
                    //Needs to check if objectSet equals goodTransactions
                    if ((lastTime - firstTime) > minTime && goodTransactions.size() == itemset.getTransactions().size()) {
                        //Needs double check
                        startConvoy.add(firstTime);
                        startConvoyIndex.add(firstIndex);
                        endConvoy.add(lastTime);
                        endConvoyIndex.add(lastIndex);

                        numOverlapTimePoint += lastTime - firstTime;

                        firstTime = currentTime;
                        firstIndex = currentIndex;
                        lastTime = currentTime;
                        lastIndex = currentIndex;
                    } else {
                        firstTime = currentTime;
                        firstIndex = currentIndex;
                        lastTime = currentTime;
                        lastIndex = currentIndex;
                        goodTransactions = currentTransactions;
                    }
                }
            }
        }
        if ((numOverlapTimePoint / defaultDataBase.getTimeIds().size()) >= commonObjectPercentage) {
            Debug.println("New Group Pattern", Debug.DEBUG);
            Set<Time> timesOfPattern = new HashSet<>();
            Set<Transaction> transactionsOfPattern = new HashSet<>();

            for (int transaction : itemset.getTransactions()) {
                transactionsOfPattern.add(defaultDataBase.getTransaction(transaction));
            }

            for (int time : itemset.getTimes()) {
                timesOfPattern.add(defaultDataBase.getTime(time));
            }

            patterns.add(new GroupPattern(transactionsOfPattern, timesOfPattern));
            Debug.println("GPattern", patterns, Debug.INFO);
        }
        return patterns;
    }
}
