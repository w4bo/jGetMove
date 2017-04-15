package fr.jgetmove.jgetmove.motifs;

import java.util.Set;

import fr.jgetmove.jgetmove.database.*;

/**
 * Class representant le pattern Convoy
 */
public class Convoy implements Pattern {
	
	/**
	 * Liste des clusters présents dans le convoy
	 */
	Set<Cluster> clusters;
	/**
	 * Liste des temps présents dans le convoy
	 */
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
	
	/**
	 * Getter sur la liste des clusters
	 * @return la liste des clusters présents dans le convoy
	 */
	public Set<Cluster> getClusters() {
		return clusters;
	}

	/**
	 * Setter sur la liste des clusters
	 * @param clusters une nouvelle liste de clusters
	 */
	public void setClusters(Set<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	/**
	 * Getter sur la liste des temps
	 * @return La liste des temps des temps présents dans le convoy
	 */
	public Set<Time> getTimes() {
		return times;
	}

	/**
	 * Setter sur la liste des temps
	 * @param times une nouvelle liste de temps
	 */
	public void setTimes(Set<Time> times) {
		this.times = times;
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
