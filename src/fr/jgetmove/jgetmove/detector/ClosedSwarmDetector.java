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
