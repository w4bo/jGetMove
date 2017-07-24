/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.jgetmove.jgetmove.pattern;

import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class representant le pattern Convoy
 *
 * @version 1.0.0
 * @since 0.1.0
 */
public class Convoy implements Pattern, PrettyPrint {

    /**
     * Liste des transactions présents dans le convoy
     */
    private Set<Transaction> transactions;
    /**
     * Liste des temps présents dans le convoy
     */
    private Set<Time> times;

    /**
     * Constructeur
     *
     * @param transactions Liste de transactions present dans le convoy
     * @param times        La liste des temps associées
     */
    public Convoy(Set<Transaction> transactions, Set<Time> times) {
        this.transactions = transactions;
        this.times = times;
    }

    /**
     * Getter sur la liste des transactions
     *
     * @return la liste des clusters présents dans le convoy
     */
    public Set<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Setter sur la liste des transactions
     *
     * @param transactions une nouvelle liste de transactions
     */
    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Getter sur la liste des temps
     *
     * @return La liste des temps des temps présents dans le convoy
     */
    public Set<Time> getTimes() {
        return times;
    }

    /**
     * Setter sur la liste des temps
     *
     * @param times une nouvelle liste de temps
     */
    public void setTimes(Set<Time> times) {
        this.times = times;
    }


    public String printGetTransactions() {
        StringBuilder s = new StringBuilder();
        for (Transaction transaction : this.getTransactions()) {
            s.append(transaction.getId()).append(',');
        }
        //retire la dernière virgule
        return s.substring(0, s.length() - 1);
    }

    public List<JsonObject> getJsonLinks(int index) {
        ArrayList<Time> timeArrayList = new ArrayList<>(times);
        timeArrayList.sort(null);
        ArrayList<Transaction> transactionArrayList = new ArrayList<>(transactions);

        ArrayList<JsonObject> jsonLinks = new ArrayList<>();

        for (int timeIndex = 0; timeIndex < timeArrayList.size() - 1; timeIndex++) {
            int source = -1;
            int target = -1;

            for (int transactionClusterId : transactionArrayList.get(0).getClusterIds()) {
                if (source == -1) {
                    for (int clusterId : timeArrayList.get(timeIndex).getClusterIds()) {
                        if (clusterId == transactionClusterId) {
                            source = clusterId;
                        }
                    }
                }

                if (target == -1) {
                    for (int clusterId : timeArrayList.get(timeIndex + 1).getClusterIds()) {
                        if (clusterId == transactionClusterId) {
                            target = clusterId;
                        }
                    }
                }
            }

            jsonLinks.add(Json.createObjectBuilder()
                    .add("id", index)
                    .add("source", source)
                    .add("target", target)
                    .add("value", getTransactions().size())
                    .add("label", printGetTransactions())
                    .build());
        }

        return jsonLinks;
    }

    @Override
    public String toPrettyString() {
        return "\n|-- transactions : " + transactions +
                "\n`-- times : " + times;
    }

    @Override
    public String toString() {
        return "\nConvoy :" + Debug.indent(toPrettyString());
    }
}
