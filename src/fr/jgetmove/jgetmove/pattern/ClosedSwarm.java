package fr.jgetmove.jgetmove.pattern;

import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Set;

/**
 * Class that represent a closed swarm pattern
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
     * @param transactionOfItemset list of all transactions in the swarm
     * @param timesOfItemset       list of all times in the swarm
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

    @Override
    public ArrayList<JsonObject> getLinksToJson(int index) {
        return null;
    }
}
