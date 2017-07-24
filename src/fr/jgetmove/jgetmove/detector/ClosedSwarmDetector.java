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
 * @version 1.0.0
 * @since 0.1.0
 */
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
