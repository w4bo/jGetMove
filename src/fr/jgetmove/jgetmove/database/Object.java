package fr.jgetmove.jgetmove.database;

import java.util.ArrayList;

public class Object {

	private int id;
	private ArrayList<Cluster> clusters;

	Object(int id) {
		this.id = id;
	}

	Object(int id, Cluster[] clusters) {
		this.id = id;
		for (int i = 0; i < clusters.length; i++) {
			this.clusters.add(clusters[i]);
		}
	}

	Object(int id, int[] clustersId) {
		this.id = id;
		for (int i = 0; i < clustersId.length; i++) {
			clusters.add(new Cluster(clustersId[i]));
		}
	}

	Object(int id, ArrayList<Cluster> clusters) {
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
