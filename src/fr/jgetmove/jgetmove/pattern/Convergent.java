package fr.jgetmove.jgetmove.pattern;

import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Convergent implements Pattern, PrettyPrint {

    public DataBase defaultDataBase;
    public int idCluster;
    public HashMap<Integer, Transaction> TransactionsOfIdCluster;

    public Convergent(DataBase defaultDataBase, int idCluster) {
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

    public List<JsonObject> getJsonLinks(int index) {
        ArrayList<JsonObject> jsonLinks = new ArrayList<>();
        //TODO ClusterSet n'est pas trié dans l'ordre croissant des ID, du coup ça fausse le résulat graphique ...
        for (int idTransaction : TransactionsOfIdCluster.keySet()) {
            ArrayList<Integer> clusterSet = getTransactionsClusters(idTransaction);
            List<Integer> clusters;
            if (clusterSet.get(clusterSet.size() - 1) != idCluster) {
                clusters = clusterSet.subList(0, clusterSet.indexOf(idCluster) + 1);
            } else {
                clusters = clusterSet;
            }
            for (int i = 1; i < clusters.size(); i++) {
                jsonLinks.add(Json.createObjectBuilder()
                        .add("id", index)
                        .add("source", clusters.get(i - 1))
                        .add("target", clusters.get(i))
                        .add("value", 1)
                        .add("label", idTransaction)
                        .build());
            }
        }
        return jsonLinks;
    }

    @Override
    public String toPrettyString() {
        return "\n|-- Cluster : " + idCluster +
                "\n`-- Transactions : " + TransactionsOfIdCluster.values();
    }

    @Override
    public String toString() {
        return "\nConvergent :" + Debug.indent(toPrettyString());
    }
}
