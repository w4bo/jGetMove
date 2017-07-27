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
import fr.jgetmove.jgetmove.pattern.Convoy;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * In charge of detecting convoys
 * <p>
 * A convoy is a pattern when a set of transactions are together across consecutive, incremental times.
 *
 * @author jframos0
 * @author Carmona-Anthony
 * @version 1.0.0
 * @since 0.1.0
 */
public class ConvoyDetector implements SingleDetector {

    private int minTime;

    /**
     * Default constructor
     */
    public ConvoyDetector(int minTime) {
        this.minTime = minTime;
    }

    /**
     * Detects all the {@link Convoy}
     *
     * @param dataBase data binder
     * @param itemset  the itemset to iterate
     * @return all the convoys detected in the itemset
     */
    //TODO Retester quand les itemsets en double seront corrigés + opti si besoin
    public ArrayList<Pattern> detect(DataBase dataBase, Itemset itemset) {

        ArrayList<Pattern> convoys = new ArrayList<>();
        ArrayList<Integer> clusters = new ArrayList<>(itemset.getClusters());
        ArrayList<Integer> times = new ArrayList<>(itemset.getTimes());
        ArrayList<Integer> transactionsOfItemset = new ArrayList<>(itemset.getTransactions()); //Minimal transactions of an itemset
        ArrayList<Integer> transactionsOfAClusterOfItemset;
        int lastTime = times.get(0);
        int currentTime;
        int sizeMin = transactionsOfItemset.size(); //Ivre, il resta appuyer sur la touche "7" pendant 2 secondes
        int size;
        ArrayList<ArrayList<Integer>> tabTimesSet = new ArrayList<>(); //Ensemble des temps consécutifs des temps de l'itemset
        ArrayList<Integer> timesSet = new ArrayList<>(); //un ensemble consécutif de temps de l'itemset

        if (transactionsOfItemset.size() < 2) {
            return convoys;
        }
        for (int i = 0; i < times.size(); i++) {
            //Cette boucle determine les transactions de l'itemset + l'ensemble des temps consécutifs de l'itemsets
            currentTime = times.get(i);
            transactionsOfAClusterOfItemset = new ArrayList<>(dataBase.getClusterTransactions(clusters.get(i)).keySet());
            size = transactionsOfAClusterOfItemset.size();
            if (size == sizeMin && !(transactionsOfItemset.equals(transactionsOfAClusterOfItemset))) {
                //Si on a ce cas par exemple [2,3] et [3,5] TofItemset = [3] mais on doit pas rentrer dedans si [2,3] et [2,3]
                transactionsOfItemset.retainAll(transactionsOfAClusterOfItemset);
                sizeMin = transactionsOfItemset.size();
            }

            if (size < sizeMin) {
                sizeMin = size;
                transactionsOfItemset = new ArrayList<>(dataBase.getClusterTransactions(clusters.get(i)).keySet());
            }

            if (currentTime > (lastTime + 1) && timesSet.size() > 1) {
                //Si le temps actuel n'est plus consécutif aux temps précédent
                ArrayList<Integer> timesSetClone = new ArrayList<>(timesSet);
                tabTimesSet.add(timesSetClone);
                timesSet.clear();
            }

            if (currentTime == lastTime + 1) {
                if (!timesSet.contains(lastTime)) {
                    timesSet.add(lastTime);
                }
                timesSet.add(currentTime);
                if (currentTime == times.get(times.size() - 1)) {
                    tabTimesSet.add(timesSet);
                }
            }
            lastTime = currentTime;
        }
        //TODO : opti surtout ici je pense
        for (ArrayList<Integer> actualTimesSet : tabTimesSet) {
            //Plus qu'à remplir
            Set<Time> timesOfClusters = new HashSet<>();
            Set<Transaction> transactionsOfClusters = new HashSet<>();
            for (int transactionId : transactionsOfItemset) {
                transactionsOfClusters.add(dataBase.getTransaction(transactionId));
            }
            for (int time : actualTimesSet) {
                timesOfClusters.add(dataBase.getTime(time));
            }
            convoys.add(new Convoy(transactionsOfClusters, timesOfClusters));
        }
        return convoys;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
