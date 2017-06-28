package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
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

    //TODO Retester quand les itemsets en double seront corrigés + opti si besoin
    public ArrayList<Pattern> detect(DataBase defaultDataBase, Set<Integer> timeBased, Set<Integer> clusterBased,
                                     Collection<Transaction> transactions) {

        ArrayList<Pattern> convoys = new ArrayList<>();
        ArrayList<Integer> itemset = new ArrayList<>(clusterBased);
        ArrayList<Integer> times = new ArrayList<>(timeBased);
        ArrayList<Integer> transactionsOfItemset = new ArrayList<>(); //Minimal transactions of an itemset
        ArrayList<Integer> transactionsOfAClusterOfItemset;

        int lastTime = times.get(0);
        int currentTime;
        int sizeMin = 77777777; //Ivre, il resta appuyer sur la touche "7" pendant 2 secondes
        int size;
        ArrayList<ArrayList <Integer> > tabTimesSet = new ArrayList< ArrayList<Integer>>(); //Ensemble des temps consécutifs des temps de l'itemset
        ArrayList<Integer> timesSet = new ArrayList<Integer>(); //un ensemble consécutif de temps de l'itemset

        for (int i = 0; i < times.size(); i++){
            //Cette boucle determine les transactions de l'itemset + l'ensemble des temps consécutifs de l'itemsets
            currentTime = times.get(i);
            transactionsOfAClusterOfItemset = new ArrayList<>(defaultDataBase.getClusterTransactions(itemset.get(i)).keySet());
            size = transactionsOfAClusterOfItemset.size();

            if(size == sizeMin && !(transactionsOfItemset.equals(transactionsOfAClusterOfItemset))){
                //Si on a ce cas par exemple [2,3] et [3,5] TofItemset = [3] mais on doit pas rentrer dedans si [2,3] et [2,3]
                transactionsOfItemset.retainAll(transactionsOfAClusterOfItemset);
                sizeMin = transactionsOfItemset.size();
            }

            if(size < sizeMin){
                sizeMin = size;
                transactionsOfItemset = new ArrayList<>(defaultDataBase.getClusterTransactions(itemset.get(i)).keySet());
            }

            if(currentTime > (lastTime + 1) && timesSet.size() > 1){
                //Si le temps actuel n'est plus consécutif aux temps précédent
                ArrayList<Integer> timesSetClone = (ArrayList<Integer>) timesSet.clone();
                tabTimesSet.add(timesSetClone);
                timesSet.clear();
            }

            if (currentTime == lastTime + 1) {
                if(!timesSet.contains(lastTime)){timesSet.add(lastTime);}
                timesSet.add(currentTime);
                if(currentTime == times.get(times.size() - 1)){
                    tabTimesSet.add(timesSet);
                }
            }
            lastTime = currentTime;
        }
        //TODO : opti surtout ici je pense
        for (ArrayList<Integer> actualTimesSet : tabTimesSet) {
            //Plus qu'à remplir
            Set<Time> timesOfClusters = new HashSet<>();
            Set<Transaction> transactionsOfClusters = new HashSet<>();
            for (int transactionId : transactionsOfItemset) {
                transactionsOfClusters.add(defaultDataBase.getTransaction(transactionId));
            }
            for (int time : actualTimesSet) {
                timesOfClusters.add(defaultDataBase.getTime(time));
            }
            convoys.add(new Convoy(transactionsOfClusters,timesOfClusters));
        }
        Debug.println("Convoys", convoys, Debug.INFO);
        return convoys;
    }
}
