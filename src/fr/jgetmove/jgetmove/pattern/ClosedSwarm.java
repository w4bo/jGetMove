package fr.jgetmove.jgetmove.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;

import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;

/**
 * Class that represent a closed swarm pattern
 *
 */
public class ClosedSwarm extends Swarm {
	
    /**
     * List of transactions in the closed swarm
     */
    Set<Transaction> transactions;

    /**
     * List of times in the closed swarm
     */
    Set<Time> times;

    /**
     * Constructor
     * 
     * @param transactionOfItemset
     *            list of all transactions in the swarm
     * @param timesOfItemset
     *            list of all times in the swarm
     */
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

    public String toString() {
        return "Closed Swarm :\n" + " transactions : [" + transactions + "]" + "times : " + times;
    }

    public String printGetTransactions() {
        StringBuilder s = new StringBuilder();
        for (Transaction transaction : this.getTransactions()) {
            s.append(transaction.getId()).append(',');
        }
        // retire la dernière virgule
        return s.substring(0, s.length() - 1);
    }

    public List<JsonObject> getLinksToJson(int index) {
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

            jsonLinks.add(Json.createObjectBuilder().add("id", index).add("source", source).add("target", target)
                    .add("value", getTransactions().size()).add("label", printGetTransactions()).build());
        }

        return jsonLinks;
    }

}
