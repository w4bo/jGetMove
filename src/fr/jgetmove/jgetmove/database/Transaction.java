package fr.jgetmove.jgetmove.database;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Object contenant l'ensemble des clusters dans lequel il est présent.
 */
public class Transaction {
	
	private ArrayList<Integer> itemSets;

    /**
     * id de la transaction
     */
    private int id;

    /**
     * hashmap contenant (idCluster => Cluster)
     */
    private HashMap<Integer, Cluster> clusters;

    /**
     * @param id identifiant de la transaction
     */
    Transaction(int id) {
        this.id = id;
        this.clusters = new HashMap<>();
        itemSets = new ArrayList<Integer>();
    }

    /**
     * @param id       identifiant de la transaction
     * @param clusters ensemble des clusters de la transaction
     */
    Transaction(int id, Cluster[] clusters) {
        this.id = id;
        itemSets = new ArrayList<Integer>();

        for (Cluster cluster : clusters) {
            this.clusters.put(cluster.getId(), cluster);
            if(!itemSets.contains(cluster.getId())){
            	itemSets.add(cluster.getId());
            }
        }
    }

    /**
     * @param id       identifiant de la transaction
     * @param clusters ensemble des clusters de la transaction
     */
    Transaction(int id, HashMap<Integer, Cluster> clusters) {
        this.id = id;
        this.clusters = clusters;
    }

    /**
     * @param cluster ajoute le cluster à la transaction
     */
    void add(Cluster cluster) {
        clusters.put(cluster.getId(), cluster);
        if(!itemSets.contains(cluster.getId())){
        	itemSets.add(cluster.getId());
        }
    }

    /**
     * @return id de la transaction
     */
    int getId() {
        return id;
    }

    /**
     * @return l'ensemble des clusters de la transaction
     */
    HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }
    
    public ArrayList<Integer> getItemsets(){
    	return itemSets;
    }

    @Override
    public String toString() {
        return String.valueOf(clusters.keySet());
    }
}
