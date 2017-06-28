package fr.jgetmove.jgetmove.pattern;

import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Transaction;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Divergeant implements Pattern{

    public Database defaultDatabase;
    public int idCluster;
    public HashMap<Integer, Transaction> TransactionsOfIdCluster;

    public Divergeant(Database defaultDatabase, int idCluster) {
        this.defaultDatabase = defaultDatabase;
        this.idCluster = idCluster;
        this.TransactionsOfIdCluster = defaultDatabase.getClusterTransactions(idCluster);
    }

    public ArrayList<Integer> getTransactionsClusters(int idTransaction) {
        ArrayList<Integer> clusterSet = new ArrayList<>();
        for (Cluster cluster : defaultDatabase.getTransactionClusters(idTransaction).values()) {
            clusterSet.add(cluster.getId());
        }
        return clusterSet;
    }

    public List<JsonObject> getLinksToJson(int index) {
        ArrayList<JsonObject> jsonLinks = new ArrayList<>();
        for (int idTransaction : TransactionsOfIdCluster.keySet()) {
            ArrayList<Integer> clusterSet = getTransactionsClusters(idTransaction);
            for (int i = 1; i < clusterSet.size(); i++){
                jsonLinks.add(Json.createObjectBuilder()
                        .add("id", index)
                        .add("source", clusterSet.get(i-1))
                        .add("target", clusterSet.get(i-1))
                        .add("value", 1)
                        .add("label", idTransaction)
                        .build());
            }
        }
        return jsonLinks;
    }

}
