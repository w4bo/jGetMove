package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.pattern.ClosedSwarm;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ClosedSwarmDetector implements Detector {

    private int minTime;

    public ClosedSwarmDetector(int minTime) {
        this.minTime = minTime;
    }

    @Override
    public ArrayList<Pattern> detect(Database defaultDatabase, Set<Integer> timeBased, Set<Integer> clusterBased,
                                     Collection<Transaction> transactions) {
        ArrayList<Pattern> closedSwarm = new ArrayList<>();

        ArrayList<Integer> times = new ArrayList<>(timeBased);
        if (times.get(times.size() - 1) - times.get(0) >= minTime) {
            Set<Time> timesOfPattern = new HashSet<>();
            Set<Transaction> transactionsOfPattern = new HashSet<>();
            
            for(Transaction transaction : transactions){
                transactionsOfPattern.add(defaultDatabase.getTransaction(transaction.getId()));
            }

            for (Integer time : timeBased) {
                timesOfPattern.add(defaultDatabase.getTime(time));
            }

            closedSwarm.add(new ClosedSwarm(transactionsOfPattern, timesOfPattern));
            System.out.println(closedSwarm);
        }
        return closedSwarm;
    }
}
