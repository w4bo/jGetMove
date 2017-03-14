package fr.jgetmove.jgetmove.database;

import java.util.HashMap;
import java.util.Set;

/**
 * Regroupement de transactions à un temps donné
 *
 */
public class Cluster {
	
	private int id;
	/**
	 * HashMap contenant idTransaction -> Transaction
	 */
	private HashMap <Integer , Transaction> transactions;
	
    /**
     * Initialise un nouveau cluster à partir d'un id
     * @param id 
     */
    public Cluster(int id) {
    	this.id = id;
    }
    
    /**
     * Ajoute la transaction au HashMap des transactions en fonction de son id
     * @param Transaction
     */
    private void add(Transaction transaction){
    	transactions.put(transaction.getId(), transaction);
    }
   
    /**
     * Retourne l'id du cluster courant
     * @return id
     */
    public int getIdCluster() {
		return id;
	}

	/**
	 * Set l'idCluster courant à idCluster
	 * @param id
	 */
	public void setIdCluster(int idCluster) {
		this.id= idCluster;
	}
	
	/**
	 * Retourne le HashMap des transactions
	 * @return transaction Ensemble des transactions (HashMap <idTransaction , Transaction>)
	 */
	public HashMap<Integer, Transaction> getTransactions() {
		return transactions;
	}

	/**
	 * Set le transactionSet courant
	 * @param transactions
	 */
	public void setTransactions(HashMap<Integer, Transaction> transactions) {
		this.transactions = transactions;
	}

    
   
    
}
