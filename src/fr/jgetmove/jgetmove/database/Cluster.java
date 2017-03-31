package fr.jgetmove.jgetmove.database;

import java.util.HashMap;

/**
 * Regroupement de transactions à un temps donné
 */
public class Cluster {

    private int id;
    /**
     * HashMap contenant idTransaction => Transaction
     */
    private HashMap<Integer, Transaction> transactions;
    /**
     * Temps associé à un cluster
     */
    private Time time;

    /**
     * @param id identifiant du cluster
     */
    Cluster(int id) {
        this.id = id;
        transactions = new HashMap<>();
    }

    /**
     * Ajoute la transaction au HashMap des transactions en fonction de son id
     *
     * @param transaction la transaction à ajouter
     */
    public void add(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    /**
     * @return id du cluster courant
     */
    public int getId() {
        return id;
    }

    /**
     * @return le Time associé au cluster
     */
    public Time getTime() {
        return time;
    }

    /**
     * @param time le temps à associer au cluster
     */
    public void setTime(Time time) {
        this.time = time;
    }

    public int getTimeId() {
        return time.getId();
    }

    /**
     * Retourne le HashMap des transactions
     *
     * @return transaction Ensemble des transactions (HashMap [idTransaction => Transaction])
     */
    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return String.valueOf(transactions.keySet());
    }
}
