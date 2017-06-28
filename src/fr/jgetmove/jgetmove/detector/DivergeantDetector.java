package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Path;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.pattern.Convergeant;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class DivergeantDetector {

    public DivergeantDetector() {

    }

    public ArrayList<Pattern> detect(DataBase defaultDataBase, ArrayList<Path> itemsets) {

        ArrayList<Pattern> convergeants = new ArrayList<>();
        TreeSet<Integer> firstClusters = new TreeSet<Integer>();

        for (Path itemset : itemsets) {
            //Pour tt les itemsets
            //Si les transactions de chaque clusters de l'itemsets ne sont pas identiques Alors on peut continuer sinon RIP
            //On récupère le 1er element et on l'inclus dans le tableau firstclusters
            //Tout en vérifiant qu'il ne soit dejà pas présent dans ce tableau.
            if (goodItemsets(defaultDataBase, itemset) && itemset.getClusters().size() > 1) {
                int lastItem = itemset.getClusters().first();
                if(firstClusters.contains(lastItem)){
                    firstClusters.add(lastItem);
                }
            }
        }

        for (int idCluster : firstClusters) {
            //Lorsqu'on a notre tableau, pour tt les firstclusters
            //New Convergeant (defaultDataBase, firstClusters[i]);
            convergeants.add(new Convergeant(defaultDataBase, idCluster));
        }
        return convergeants;
    }

    public boolean goodItemsets(DataBase defaultDataBase, Path itemset) {
        //Vérifie si les transactions de chaque clusters de l'itemset ne sont pas identiques
        HashMap<Integer, Transaction> lastTransactions = defaultDataBase.getClusterTransactions(0);
        for (int idCluster : itemset.getClusters()) {
            HashMap<Integer, Transaction> Transactions = defaultDataBase.getClusterTransactions(idCluster);
            if(!lastTransactions.equals(Transactions)){
                return true;
            }
        }
        return false;
    }
}
