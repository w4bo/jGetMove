package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.pattern.Convoy;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class/Singleton related to the detection of convoys in a database
 */
public class ConvoyDetector implements Detector {

	private static ConvoyDetector convoyDetector;
	private int minTime;

	/**
	 * Empty Constructor
	 */
	public ConvoyDetector(int minTime) {
		this.minTime = minTime;
	}

	/**
	 * Cree une instance de ConvoyDetector ou retourne celle deja presente
	 *
	 * @return une nouvelle instance de convoyDetector si elle n'a pas été deja
	 *         crée
	 */
	public static ConvoyDetector getInstance(int minTime) {
		if (convoyDetector == null) {
			convoyDetector = new ConvoyDetector(minTime);
			return convoyDetector;
		}
		return convoyDetector;
	}


	public ArrayList<Pattern> detect(Database defaultDatabase, Set<Integer> timeBased, Set<Integer> clusterBased,
			Collection<Transaction> transactions) {
		
		ArrayList<Pattern> convoys = new ArrayList<>();
		
		ArrayList<Integer> times = new ArrayList<>();

        for (int clusterId : clusterBased) {
            times.add(defaultDatabase.getClusterTimeId(clusterId));
        }
        
        ArrayList<Integer> clusters = new ArrayList<>(clusterBased);
        
        int firstTime = defaultDatabase.getClusterTimeId(clusters.get(0)); //Premier temps
        int lastTime = firstTime; //lastTime
        int currentTime;

        // correctTransactionSet
        Set<Integer> goodTransactions = new HashSet<>(defaultDatabase.getClusterTransactions(clusters.get(0)).keySet());

        //Pour tout les clusters
        for (Integer clusterId : clusterBased) {
            // currentTransactionSet
            Set<Integer> currentTransactions = new HashSet<>(defaultDatabase.getClusterTransactions(clusterId).keySet());

            currentTime = defaultDatabase.getClusterTimeId(clusterId);

            //Recopie inutile ?
            if (currentTime == lastTime + 1) {
                goodTransactions.retainAll(currentTransactions);
                lastTime = currentTime;
            } else if (currentTime > (lastTime + 1)) {
                //Si l'ecart de temps est superieur a min_t
                if (lastTime - firstTime >= minTime) {
                    //Init new Convoy
                    // timesOfItemset
                    Set<Time> timesOfCluster = new HashSet<>();
                    Set<Transaction> transactionsOfCluster = new HashSet<>();

                    for (int transactionId : goodTransactions) {
                        transactionsOfCluster.add(defaultDatabase.getTransaction(transactionId));
                    }


                    convoys.add(new Convoy(transactionsOfCluster, timesOfCluster));
                } else {
                    goodTransactions = currentTransactions;
                }

                firstTime = currentTime;
                lastTime = currentTime;
            }
        }

        //Si L'ecart entre lastTime et firstTime est superieur a min_t
        if ((lastTime - firstTime) >= minTime) {
            //Init new Convoy
            Set<Time> timesOfItemset = new HashSet<>();
            Set<Transaction> transactionsConvoy = new HashSet<>();
            //Get Clusters Object from database
            for (int clusterIndex = 0; clusterIndex < goodTransactions.size(); clusterIndex++) {
                transactionsConvoy.add(defaultDatabase.getTransaction(clusterIndex));
            }
            //Get Times Object from database

            for (int time = firstTime; time < lastTime; time++) {
                timesOfItemset.add(defaultDatabase.getTime(times.get(time)));
            }

            convoys.add(new Convoy(transactionsConvoy, timesOfItemset));
        }
        Debug.println("Il y a : " + convoys.size() + "Convoy");

    	return convoys;
	}
}
