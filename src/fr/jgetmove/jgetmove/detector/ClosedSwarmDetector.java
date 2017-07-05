package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.pattern.ClosedSwarm;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class ClosedSwarmDetector implements SingleDetector {

    private int minTime;

    public ClosedSwarmDetector(int minTime) {
        this.minTime = minTime;
    }

    @Override
    public ArrayList<Pattern> detect(final DataBase defaultDataBase, final Itemset itemset) {
        ArrayList<Pattern> closedSwarm = new ArrayList<>();

        TreeSet<Integer> times = itemset.getTimes();
        if (times.last() - times.first() >= minTime) {
            Set<Time> timesOfPattern = new HashSet<>();
            Set<Transaction> transactionsOfPattern = new HashSet<>();

            for (int transactionId : itemset.getTransactions()) {
                transactionsOfPattern.add(defaultDataBase.getTransaction(transactionId));
            }

            for (int time : itemset.getTimes()) {
                timesOfPattern.add(defaultDataBase.getTime(time));
            }
            if(transactionsOfPattern.size() < 2){
                return closedSwarm;
            } else {
                closedSwarm.add(new ClosedSwarm(transactionsOfPattern, timesOfPattern));
            }
            Debug.println("ClosedSwarm", closedSwarm, Debug.INFO);
        }
        return closedSwarm;
    }
}
