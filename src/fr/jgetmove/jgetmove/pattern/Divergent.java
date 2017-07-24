/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

/**
 * @version 1.0.0
 * @since 0.2.0
 */
public class Divergent implements Pattern, PrettyPrint {

    public DataBase defaultDataBase;
    public int idCluster;
    public HashMap<Integer, Transaction> TransactionsOfIdCluster;

    public Divergent(DataBase defaultDataBase, int idCluster) {
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
            if (clusterSet.get(0) != idCluster) {
                clusters = clusterSet.subList(clusterSet.indexOf(idCluster), clusterSet.size());
            } else {
                clusters = clusterSet;
            }
            System.out.println("OOKT " + clusterSet);
            System.out.println("OOKT " + clusters);
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
        return "Divergent :" + Debug.indent(toPrettyString());
    }
}
