package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.io.Input;

import java.io.IOException;
import java.util.HashMap;

/**
 * Contient toutes les structures de données
 */
public class Database {
    Input inputObj, inputTime;

    private HashMap<Integer, Cluster> clusters;
    private HashMap<Integer, Transaction> transactions;

    /**
     * @param inputObj  fichier (transactionId [clusterId ...])
     * @param inputTime fichier (timeId clusterId)
     */
    public Database(Input inputObj, Input inputTime) {
        try {
            String line;
            while ((line = inputObj.readLine()) != null) {
                String[] splitLine = line.split("( |\\t)+");

                Transaction transaction = new Transaction(Integer.parseInt(splitLine[0]));


                for (int i = 1; i < splitLine.length; i++) {
                    int clusterId = Integer.parseInt(splitLine[i]);
                    Cluster cluster;

                    if (this.clusters.get(clusterId) != null) {
                        cluster = this.clusters.get(clusterId);
                    } else {
                        cluster = new Cluster(clusterId);
                        this.add(cluster);
                    }

                    cluster.add(transaction);
                    transaction.add(cluster);
                }

                this.add(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(Cluster cluster) {
        this.clusters.put(cluster.getId(), cluster);
    }

    public void add(Transaction transaction) {
        this.transactions.put(transaction.getId(), transaction);
    }

}
