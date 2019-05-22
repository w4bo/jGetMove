/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.pattern.ClosedSwarm;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * In charge of detecting closedSwarms
 * <p>
 * A closed swarm is a pattern that has the same transactions across the clusters. The only restriction is that it need to have at least minTime number of times.
 *
 * @author jframos0
 * @author stardisblue
 * @version 1.0.0
 * @since 0.1.0
 */
public class ClosedSwarmDetector implements SingleDetector {

    /**
     * Minimal number of times
     */
    private int minTime;

    /**
     * Default constructor
     *
     * @param minTime minimal number of times
     */
    public ClosedSwarmDetector(int minTime) {
        this.minTime = minTime;
    }

    /**
     * Method in charge of detecting {@link ClosedSwarm} patterns.
     *
     * @param dataBase the database
     * @param itemset  an itemset
     * @return an array of detected ClosedSwarm
     * @implSpec Iterates over the itemsets. Each itemset is already a closed swarm. Checks if minTime is respected and copies the itemset into a {@link ClosedSwarm}
     */
    @Override
    public ArrayList<Pattern> detect(DataBase dataBase, Itemset itemset) {
        ArrayList<Pattern> closedSwarm = new ArrayList<>();

        TreeSet<Integer> times = itemset.getTimes();
        if (times.last() - times.first() >= minTime) {
            Set<Time> timesOfPattern = new HashSet<>();
            Set<Transaction> transactionsOfPattern = new HashSet<>();

            for (int transactionId : itemset.getTransactions()) {
                transactionsOfPattern.add(dataBase.getTransaction(transactionId));
            }

            for (int time : itemset.getTimes()) {
                timesOfPattern.add(dataBase.getTime(time));
            }
            if (transactionsOfPattern.size() < 2) {
                return closedSwarm;
            } else {
                closedSwarm.add(new ClosedSwarm(transactionsOfPattern, timesOfPattern));
            }
        }

        return closedSwarm;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
