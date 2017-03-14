package fr.jgetmove.jgetmove.database;

import java.util.ArrayList;

public class Transaction {

    private int id;
    private ArrayList<Cluster> clusters;

    Transaction(int id) {
        this.id = id;
    }

    Transaction(int id, Cluster[] clusters) {
        this.id = id;
        for (int i = 0; i < clusters.length; i++) {
            this.clusters.add(clusters[i]);
        }
    }

    Transaction(int id, int[] clustersId) {
        this.id = id;
        for (int i = 0; i < clustersId.length; i++) {
            clusters.add(new Cluster(clustersId[i]));
        }
    }

    Transaction(int id, ArrayList<Cluster> clusters) {
        this.id = id;
        this.clusters = clusters;
    }

    public void addCluster(Cluster cluster) {
        clusters.add(cluster);
    }

    public void addCluster(int clusterId) {
        clusters.add(new Cluster(clusterId));
    }
}
