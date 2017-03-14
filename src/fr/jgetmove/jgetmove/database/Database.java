package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.io.Input;

import java.io.IOException;
import java.io.SyncFailedException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Contient toutes les structures de données
 */
public class Database {
    private Input inputObj, inputTime;


    private HashMap<Integer, Cluster> clusters;
    private HashMap<Integer, Transaction> transactions;

    /**
     * @param inputObj  fichier (transactionId [clusterId ...])
     * @param inputTime fichier (timeId clusterId)
     */
    public Database(Input inputObj, Input inputTime) {
        this.inputObj = inputObj;
        this.inputTime = inputTime;

        clusters = new HashMap<>();
        transactions = new HashMap<>();

        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param cluster le cluster à ajouter à la base
     */
    private void add(Cluster cluster) {
        this.clusters.put(cluster.getId(), cluster);
    }

    /**
     * @param transaction la transaction à ajouter à la base
     */
    private void add(Transaction transaction) {
        this.transactions.put(transaction.getId(), transaction);
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
        return str;
    }
}
