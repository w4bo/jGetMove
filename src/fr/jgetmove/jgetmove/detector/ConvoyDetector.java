package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.motifs.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Class/Singleton related to the detection of convoys in a database
 */
public class ConvoyDetector implements Detector {

    private static ConvoyDetector convoyDetector;

    /**
     * Empty Constructor
     */
    private ConvoyDetector() {

    }

    /**
     * Cree une instance de ConvoyDetector ou retourne celle deja presente
     *
     * @return une nouvelle instance de convoyDetector si elle n'a pas été deja crée
     */
    public static ConvoyDetector getInstance() {
        if (convoyDetector == null) {
            convoyDetector = new ConvoyDetector();
            return convoyDetector;
        }
        return convoyDetector;
    }

    /**
     * Detect les pattern associé au detecteur
     *
     * @return une liste de motif/pattern present dans la database pour ce detecteur
     */
    public ArrayList<Pattern> detect(Database database, ArrayList<ArrayList<ArrayList<Integer>>> clustersGenerated) {
        int numConvoys = 0;

        for (ArrayList<ArrayList<Integer>> generatedCluster : clustersGenerated) {

            ArrayList<Integer> timeSet = new ArrayList<>();

            for (ArrayList<Integer> itemset : generatedCluster) {
                for (int clusterId : itemset) {
                    timeSet.add(database.getClusterTime(clusterId).getId());
                }

                int lastTime = -1;
                int firstTime = -1;
                int currentTime = -1;
                int currentIndex = -1;
                int lastIndex = -1;
                int firstIndex = -1;

                firstTime = 0; //Premier temps
                firstIndex = 0; //Premier Index
                lastTime = firstTime; //lastTime
                lastIndex = firstIndex; //lastIndex*/
                // TODO : mais svp les gars ...
                System.err.println(itemset);
                // TODO : mais svp les gars ...
                System.err.println(timeSet);

                Set<Integer> correctTransactionSet = new HashSet<>(database.getClusterTransactions(itemset.get(0)).keySet());
                Set<Integer> currentTransactionSet;
                //Pour tout les temps
                for (int i = 0; i < timeSet.size(); i++) {
                    currentTransactionSet = new HashSet<>(database.getClusterTransactions(itemset.get(i)).keySet());
                    currentTime = timeSet.get(i);
                    currentIndex = i;

                    if (currentTime == lastTime + 1) {
                        ArrayList<Integer> transactionList = new ArrayList<>();

                        for (int transactionId : correctTransactionSet) {
                            if (currentTransactionSet.contains(transactionId)) {
                                transactionList.add(transactionId);
                            }
                        }

                        correctTransactionSet.clear();
                        correctTransactionSet.addAll(transactionList);

                        lastTime = currentTime;
                        lastIndex = currentIndex;
                    } else {
                        if (currentTime > (lastTime + 1)) {
                            //Si l'ecart de temps est superieur a min_t
                            if (lastTime - firstTime >= 0) {

                                firstTime = currentTime;
                                firstIndex = currentIndex;
                                lastTime = currentTime;
                                lastIndex = currentIndex;
                                numConvoys++;
                            } else {
                                firstTime = currentTime;
                                firstIndex = currentIndex;
                                lastTime = currentTime;
                                lastIndex = currentIndex;
                                correctTransactionSet.clear();
                                correctTransactionSet.addAll(currentTransactionSet);
                            }
                        }
                    }
                }
                if ((lastTime - firstTime) >= 0) {
                    numConvoys++;
                }
            }
        }
        // TODO : mais svp les gars ...
        System.err.println("Il y a : " + numConvoys + "Convoy");
        return null;
    }

}
