package fr.jgetmove.jgetmove.database;

import java.util.HashMap;

/**
 * Regroupement de transactions Ã  un temps donnÃ©
 */
class Cluster {

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
     * @param transaction la transaction Ã  ajouter
     */
    void add(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    /**
     * @return id du cluster courant
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set le temps courant à la valeur time
     * @param time
     */
    public void setTime(Time time){
    	this.time = time;
    }

    /**
     * Retourne le HashMap des transactions
     *
     * @return transaction Ensemble des transactions (HashMap [idTransaction => Transaction])
     */
    HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return String.valueOf(transactions.keySet());
    }
}
