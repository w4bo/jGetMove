/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
 * Class that represent a closed swarm pattern
 *
 * @version 1.0.0
 * @since 0.1.0
 */
public class ClosedSwarm extends Swarm implements PrettyPrint {

    /**
     * List of transactions in the closed swarm
     */
    private Set<Transaction> transactions;

    /**
     * List of times in the closed swarm
     */
    private Set<Time> times;

    /**
     * Constructor
     *
     * @param transactionOfItemset list of all transactions in the swarm
     * @param timesOfItemset       list of all times in the swarm
     */
    // TODO: 03/07/2017 : change time to cluster
    public ClosedSwarm(Set<Transaction> transactionOfItemset, Set<Time> timesOfItemset) {
        transactions = transactionOfItemset;
        times = timesOfItemset;
    }

    /**
     * Getter on transactions
     *
     * @return the list of transactions in the closed Swarm
     */
    public Set<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Getter on times
     *
     * @return the list of times in the closed Swarm
     */
    public Set<Time> getTimes() {
        return times;
    }

    String printGetTransactions() {
        StringBuilder s = new StringBuilder();
        for (Transaction transaction : this.getTransactions()) {
            s.append(transaction.getId()).append(',');
        }
        // retire la dernière virgule
        return s.substring(0, s.length() - 1);
    }

    public List<JsonObject> toJsonArray(int index) {
        ArrayList<Time> timeArrayList = new ArrayList<>(times);
        timeArrayList.sort(null);
        ArrayList<Transaction> transactionArrayList = new ArrayList<>(transactions);

        ArrayList<JsonObject> jsonLinks = new ArrayList<>();

        for (int timeIndex = 0; timeIndex < timeArrayList.size() - 1; timeIndex++) {
            int source = -1;
            int target = -1;

            breakpoint:
            for (Transaction transaction : transactionArrayList) {
                for (int transactionClusterId : transaction.getClusterIds()) {
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

                    if (source != -1 && target != -1) {
                        break breakpoint;
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

    public String toString() {
        return "\nClosed Swarm :" + Debug.indent(toPrettyString());
    }
}
