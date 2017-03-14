package fr.jgetmove.jgetmove.database;

import java.util.HashMap;

/**
 * Regroupement de transactions à un temps donné
 */
class Cluster {

    private int id;
    /**
     * HashMap contenant idTransaction => Transaction
     */
    private HashMap<Integer, Transaction> transactions;

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
    void add(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    /**
     * @return id du cluster courant
     */
    int getId() {
        return id;
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
