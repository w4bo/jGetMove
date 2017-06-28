package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.database.Path;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.pattern.Convergeant;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.*;

public class DivergeantDetector {

    public DivergeantDetector() {

    }

    public ArrayList<Pattern> detect(Database defaultDatabase, ArrayList<Path> itemsets) {

        ArrayList<Pattern> convergeants = new ArrayList<>();
        TreeSet<Integer> firstClusters = new TreeSet<Integer>();

        for (Path itemset : itemsets) {
            //Pour tt les itemsets
            //Si les transactions de chaque clusters de l'itemsets ne sont pas identiques Alors on peut continuer sinon RIP
            //On récupère le 1er element et on l'inclus dans le tableau firstclusters
            //Tout en vérifiant qu'il ne soit dejà pas présent dans ce tableau.
            if(goodItemsets(defaultDatabase,itemset) && itemset.getClusters().size() > 1){
                int lastItem = itemset.getClusters().first();
                if(firstClusters.contains(lastItem)){
                    firstClusters.add(lastItem);
                }
            }
        }

        for (int idCluster : firstClusters) {
            //Lorsqu'on a notre tableau, pour tt les firstclusters
            //New Convergeant (defaultDatabase, firstClusters[i]);
            convergeants.add(new Convergeant(defaultDatabase,idCluster));
        }
        return convergeants;
    }

    public boolean goodItemsets(Database defaultDatabase, Path itemset){
        //Vérifie si les transactions de chaque clusters de l'itemset ne sont pas identiques
        HashMap<Integer, Transaction> lastTransactions = defaultDatabase.getClusterTransactions(0);
        for (int idCluster : itemset.getClusters()) {
            HashMap<Integer, Transaction> Transactions = defaultDatabase.getClusterTransactions(idCluster);
            if(!lastTransactions.equals(Transactions)){
                return true;
            }
        }
        return false;
    }
}
