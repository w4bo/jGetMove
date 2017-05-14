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
     * crée
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


        ArrayList<Integer> clusters = new ArrayList<>(clusterBased);
        ArrayList<Integer> times = new ArrayList<>(timeBased);

        int firstTime = times.get(0);
        int lastTime = firstTime;
        int currentTime;
        int currentIndex;
        int firstIndex = 0;
        int lastIndex = firstIndex;

        //CurrentTransactionSet
        ArrayList<Integer> currentTransactions;
        // correctTransactionSet
        ArrayList<Integer> goodTransactions = new ArrayList<>(
                defaultDatabase.getClusterTransactions(clusters.get(0)).keySet());

        //Pour tout les clusters
        for (int i = 0; i < times.size(); i++) {
            // currentTransactionSet
            currentTransactions = new ArrayList<>(defaultDatabase.getClusterTransactions(clusters.get(i)).keySet());

            currentTime = times.get(i);
            currentIndex = i;

            if (currentTime == lastTime + 1) {
                ArrayList<Integer> objectTemp = new ArrayList<>();
                for (int goodTransaction : goodTransactions) {
                    if (currentTransactions.contains(goodTransaction)) objectTemp.add(goodTransaction);
                }

                goodTransactions = objectTemp;
                lastTime = currentTime;
                lastIndex = currentIndex;

            } else {

                if (currentTime > (lastTime + 1)) {
                    int temp1 = goodTransactions.size();
                    int temp2 = transactions.size();


                    if ((lastTime - firstTime) >= minTime && (temp1 == temp2)) {
                        //Init new Convoy
                        // timesOfItemset
                        Set<Time> timesOfCluster = new HashSet<>();
                        Set<Transaction> transactionsOfCluster = new HashSet<>();

                        for (int transactionId : goodTransactions) {
                            transactionsOfCluster.add(defaultDatabase.getTransaction(transactionId));
                        }
                        for (int j = firstIndex; j <= lastIndex; j++) {
                            timesOfCluster.add(defaultDatabase.getTime(times.get(j)));
                        }

                        convoys.add(new Convoy(transactionsOfCluster, timesOfCluster));
                    } else {

                        goodTransactions = currentTransactions;
                    }
                    firstTime = currentTime;
                    firstIndex = currentIndex;
                    lastTime = currentTime;
                    lastIndex = currentIndex;
                }
            }
        }

        if ((lastTime - firstTime) >= minTime) {
            //Init new Convoy
            // timesOfItemset
            Set<Time> timesOfCluster = new HashSet<>();
            Set<Transaction> transactionsOfCluster = new HashSet<>();

            for (int transactionId : goodTransactions) {
                transactionsOfCluster.add(defaultDatabase.getTransaction(transactionId));
            }
            for (int j = firstIndex; j <= lastIndex; j++) {
                timesOfCluster.add(defaultDatabase.getTime(times.get(j)));
            }
            convoys.add(new Convoy(transactionsOfCluster, timesOfCluster));
        }

        Debug.println("Il y a : " + convoys.size() + "Convoy");
        Debug.println("Convoys", convoys);

        return convoys;
    }
}
