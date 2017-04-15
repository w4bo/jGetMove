package fr.jgetmove.jgetmove.motifs;

import java.util.Set;

import fr.jgetmove.jgetmove.database.*;

/**
 * Class representant le pattern Convoy
 */
public class Convoy implements Pattern {
	
	Set<Cluster> clusters;
	Set<Time> times;
	
	/**
	 * Constructeur
	 * @param clusters Liste de clusters present dans le convoy
	 * @param times La liste des temps associées
	 */
	public Convoy(Set<Cluster> clusters,Set<Time> times){
		this.clusters = clusters;
		this.times = times;
		System.out.println(this.toString());
	}
	
	public String toString(){
		String itemsets = "Convoy : \n";
		itemsets += " ClustersId : [";
		for(Cluster cluster : clusters){
			itemsets += cluster.getId() + ",";
		}
		itemsets += " ]";
		return itemsets;
	}

}
