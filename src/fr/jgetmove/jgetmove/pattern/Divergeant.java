package fr.jgetmove.jgetmove.pattern;

import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Transaction;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Divergeant implements Pattern{

    public DataBase defaultDataBase;
    public int idCluster;
    public HashMap<Integer, Transaction> TransactionsOfIdCluster;

    public Divergeant(DataBase defaultDataBase, int idCluster) {
        this.defaultDataBase = defaultDataBase;
        this.idCluster = idCluster;
        this.TransactionsOfIdCluster = defaultDataBase.getClusterTransactions(idCluster);
    }

    public ArrayList<Integer> getTransactionsClusters(int idTransaction) {
        ArrayList<Integer> clusterSet = new ArrayList<>();
        for (Cluster cluster : defaultDataBase.getTransactionClusters(idTransaction).values()) {
            clusterSet.add(cluster.getId());
        }
        return clusterSet;
    }

    public String toString(){
        return "Divergeants:\n" + " Cluster : [" + idCluster + "]";
    }

    public List<JsonObject> getLinksToJson(int index) {
        ArrayList<JsonObject> jsonLinks = new ArrayList<>();
        for (int idTransaction : TransactionsOfIdCluster.keySet()) {
            ArrayList<Integer> clusterSet = getTransactionsClusters(idTransaction);
            System.out.println("CLUSTERSET : " + clusterSet);
            List<Integer> clusters;
            if(clusterSet.get(0) != idCluster){
                clusters = clusterSet.subList(clusterSet.indexOf(idCluster),clusterSet.size());
                System.out.println("CLUSTERS : " + clusters);
            } else {
                clusters = clusterSet;
            }
            for (int i = 1; i < clusters.size(); i++){
                jsonLinks.add(Json.createObjectBuilder()
                        .add("id", index)
                        .add("source", clusters.get(i-1))
                        .add("target", clusters.get(i))
                        .add("value", 1)
                        .add("label", idTransaction)
                        .build());
            }
        }
        return jsonLinks;
    }

}
