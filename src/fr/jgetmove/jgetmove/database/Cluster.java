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
     * @param id identifiant du cluster
     */
    public Cluster(int id) {
        this.id = id;
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
     * Retourne le HashMap des transactions
     *
     * @return transaction Ensemble des transactions (HashMap [idTransaction => Transaction])
     */
    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    /**
     * @param transactions set le transactionset du cluster
     */
    public void setTransactions(HashMap<Integer, Transaction> transactions) {
        this.transactions = transactions;
    }


}
