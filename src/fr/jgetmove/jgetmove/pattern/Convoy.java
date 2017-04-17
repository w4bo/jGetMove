package fr.jgetmove.jgetmove.pattern;

import fr.jgetmove.jgetmove.database.Cluster;
import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.debug.Debug;

import java.util.Set;

/**
 * Class representant le pattern Convoy
 */
public class Convoy implements Pattern {

    /**
     * Liste des clusters présents dans le convoy
     */
    private Set<Cluster> clusters;
    /**
     * Liste des temps présents dans le convoy
     */
    private Set<Time> times;

    /**
     * Constructeur
     *
     * @param clusters Liste de clusters present dans le convoy
     * @param times    La liste des temps associées
     */
    public Convoy(Set<Cluster> clusters, Set<Time> times) {
        this.clusters = clusters;
        this.times = times;
        Debug.println(this);
    }

    /**
     * Getter sur la liste des clusters
     *
     * @return la liste des clusters présents dans le convoy
     */
    public Set<Cluster> getClusters() {
        return clusters;
    }

    /**
     * Setter sur la liste des clusters
     *
     * @param clusters une nouvelle liste de clusters
     */
    public void setClusters(Set<Cluster> clusters) {
        this.clusters = clusters;
    }

    /**
     * Getter sur la liste des temps
     *
     * @return La liste des temps des temps présents dans le convoy
     */
    public Set<Time> getTimes() {
        return times;
    }

    /**
     * Setter sur la liste des temps
     *
     * @param times une nouvelle liste de temps
     */
    public void setTimes(Set<Time> times) {
        this.times = times;
    }


    public String toString() {
        return "Convoy:\n" + " ClustersId : [" + clusters;
    }

}
