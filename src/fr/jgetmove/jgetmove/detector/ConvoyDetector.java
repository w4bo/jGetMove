package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.pattern.Convoy;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
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
     * @return une nouvelle instance de convoyDetector si elle n'a pas été deja crée
     */
    public static ConvoyDetector getInstance(int minTime) {
        if (convoyDetector == null) {
            convoyDetector = new ConvoyDetector(minTime);
            return convoyDetector;
        }
        return convoyDetector;
    }

    /**
     * Detect les pattern associé au detecteur
     * <p>
     * TODO: fonction d'origine
     *
     * @return une liste de motif/pattern present dans la database pour ce detecteur
     */
    public ArrayList<Pattern> detect(Database database, ArrayList<ArrayList<Integer>> clustersGenerated) {

        ArrayList<Pattern> convoys = new ArrayList<>();

        for (ArrayList<Integer> clusterIds : clustersGenerated) {
            // timeSet
            ArrayList<Integer> times = new ArrayList<>();

            for (int clusterId : clusterIds) {
                times.add(database.getClusterTimeId(clusterId));
            }

            int firstTime = database.getClusterTimeId(clusterIds.get(0)); //Premier temps
            int lastTime = firstTime; //lastTime
            int currentTime;

            // correctTransactionSet
            Set<Integer> goodTransactions = new HashSet<>(database.getClusterTransactions(clusterIds.get(0)).keySet());

            //Pour tout les clusters
            for (Integer clusterId : clusterIds) {
                // currentTransactionSet
                Set<Integer> currentTransactions = new HashSet<>(database.getClusterTransactions(clusterId).keySet());

                currentTime = database.getClusterTimeId(clusterId);

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
                            transactionsOfCluster.add(database.getTransaction(transactionId));
                        }


                        // convoys.add(new Convoy(transactionsOfCluster, timesOfCluster));
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
                Set<Transaction> transactions = new HashSet<>();
                //Get Clusters Object from database
                for (int clusterIndex = 0; clusterIndex < goodTransactions.size(); clusterIndex++) {
                    transactions.add(database.getTransaction(clusterIndex));
                }
                //Get Times Object from database

                for (int time = firstTime; time < lastTime; time++) {
                    timesOfItemset.add(database.getTime(times.get(time)));
                }

                convoys.add(new Convoy(transactions, timesOfItemset));
            }
        }

        Debug.println("Il y a : " + convoys.size() + "Convoy");

        return convoys;
    }
}
