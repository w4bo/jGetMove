package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.io.Input;

import java.io.IOException;
import java.io.SyncFailedException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Contient toutes les structures de donnÃ©es
 */
public class Database {
    private Input inputObj, inputTime;


    private HashMap<Integer, Cluster> clusters;
    private HashMap<Integer, Transaction> transactions;
    private HashMap<Integer , Time> times;

    /**
     * @param inputObj  fichier (transactionId [clusterId ...])
     * @param inputTime fichier (timeId clusterId)
     * @throws IOException 
     * @throws ClusterNotExistException 
     */
    public Database(Input inputObj, Input inputTime) throws IOException, ClusterNotExistException {
        this.inputObj = inputObj;
        this.inputTime = inputTime;

        clusters = new HashMap<>();
        transactions = new HashMap<>();
        times = new HashMap<>();
        //Initialisation des clusters et transactions
        initClusterAndTransaction();
        //Initialisation des temps
        initTimeAndCluster();
        
        
    }
    
    /**
     * Initialise les HashMap de clusters et transactions 
     * @throws IOException
     */
    private void initClusterAndTransaction() throws IOException{
    	String line;
        int transactionId = 0;
        while ((line = inputObj.readLine()) != null) {
        	String[] splitLine = line.split("( |\\t)+");
            Transaction transaction = new Transaction(transactionId);

            for (String strClusterId : splitLine) {
            	int clusterId = Integer.parseInt(strClusterId);

                Cluster cluster;

                if (clusters.get(clusterId) == null) {
                	cluster = new Cluster(clusterId);
                    this.add(cluster);
                } else {
                	cluster = clusters.get(clusterId);
                }
                cluster.add(transaction);
                transaction.add(cluster);
            }

            this.add(transaction);

            transactionId++;
         }
      }
    
    /**
     * Initialise la liste des temps ainsi que les clusters associés
     * @throws IOException
     * @throws ClusterNotExistException
     */
    private void initTimeAndCluster() throws IOException, ClusterNotExistException{
    	String line;
    	int timeId;
    	int clusterId;
    	
    	while ((line = inputTime.readLine()) != null){
    		String[] splitLine = line.split(" ");
    		if(splitLine.length == 2){ //Check si non malformé
    			
    			timeId = Integer.parseInt(splitLine[0]);
    			clusterId = Integer.parseInt(splitLine[1]);
    			Time time;
    			Cluster cluster;
    			 
    			//Check si le temps existe
    			if(times.get(timeId) == null){
    				time = new Time(timeId);
    				this.add(time);
    			}
    			else time = times.get(timeId);
    			 
    			//Check si le cluster existe	 
    			if(clusters.get(clusterId) == null){
    				throw new ClusterNotExistException();
    			}
    			else {
    				cluster = clusters.get(clusterId);
    				cluster.setTime(time);
    				time.add(cluster);
    			}
    		}
    	}
    }

    /**
     * @param cluster le cluster Ã  ajouter Ã  la base
     */
    private void add(Cluster cluster) {
        this.clusters.put(cluster.getId(), cluster);
    }

    /**
     * @param transaction la transaction Ã  ajouter Ã  la base
     */
    private void add(Transaction transaction) {
        this.transactions.put(transaction.getId(), transaction);
    }
    
    /**
     * @param time le temps à ajouter à la base
     */
    private void add(Time time){
    	this.times.put(time.getId(), time);
    }

    /**
     * @return l'ensemble des Clusters de la base
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * @return l'ensemble des Transactions de la base
     */
    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        String str = "Fichiers :" + inputObj + "; " + inputTime + "\n";
        str += "Clusters :" + clusters + "\n";
        str += "Transactions :" + transactions + "\n";
        str += "Temps :" + times + "\n";
        return str;
    }
}
