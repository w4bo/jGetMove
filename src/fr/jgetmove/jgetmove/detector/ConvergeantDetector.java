package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Path;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.pattern.Convergeant;
import fr.jgetmove.jgetmove.pattern.Pattern;
import fr.jgetmove.jgetmove.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Set;


public class ConvergeantDetector {

    public ConvergeantDetector() {

    }

    public ArrayList<Pattern> detect(DataBase defaultDataBase, ArrayList<Path> itemsets) {

        ArrayList<Pattern> convergeants = new ArrayList<>();
        ArrayList<Integer> lastClusters = new ArrayList<>();
        int nbClusters = defaultDataBase.getClusters().size();
        int nbItemsets = itemsets.size();
        boolean[][] itemsetMatrice = new boolean[nbItemsets][nbClusters];
        int indexItemset = 0;
        /*
        Construction de la Matrice [Itemset][Clusters]
         */
        for (Path itemset : itemsets){
            for (int idCluster: itemset.getClusters()) {
                if(itemset.getClusters().size() < 2){
                    itemsetMatrice[indexItemset][idCluster] = true;
                }
            }
            indexItemset++;
        }
        /*
        Ajout des clusters à la liste lastClusters
         */
        for(int clusterId = 0; clusterId < nbClusters; clusterId++){
            if(defaultDataBase.getCluster(clusterId).getTimeId() != 1) {
                ArrayList<Integer> itemsetIds = new ArrayList<>();
                for (int j = 0; j < nbItemsets; j++){
                    if(itemsetMatrice[j][clusterId]){
                        itemsetIds.add(j);
                    }
                }
                if(itemsetIds.size() > 1){
                    int idlastCluster = subDetect(itemsetMatrice,clusterId,itemsetIds,nbClusters);
                    lastClusters.add(idlastCluster);
                }
            }

        }
        /*
        Creation d'un convergeant par cluster dans la liste lastClusters
         */
        for (int idCluster : lastClusters) {
            //Lorsqu'on a notre tableau, pour tt les lastclusters
            //New Convergeant (defaultDataBase, lastClusters[i]);
            convergeants.add(new Convergeant(defaultDataBase, idCluster));
        }
        return convergeants;
    }

    public int subDetect(boolean[][] itemsetMatrice, int clusterId, ArrayList<Integer> itemsetIds, int nbClusters){
        boolean finalResult = true; //Variable qui permet de verifier si les itemsets sont identiques (true) ou opposes (false) opposes = tt vrai et dans l'autre cluster tt faux
        for(int i = clusterId + 1; i < nbClusters; i++){
            //Pour chaque cluster en partant du clusterId + 1 vu qu'on doit comparer clusterId aux clusters suivants
            for(int j = 0; j < itemsetIds.size() - 1 ; j++) {
                //Pour chaque itemset du cluster avec pour id i;
                if(itemsetMatrice[itemsetIds.get(j)][i] == itemsetMatrice[itemsetIds.get(j + 1)][i]){
                    //Si j et j + 1 sont egaux
                    if (itemsetMatrice[itemsetIds.get(j)][i]){
                        //On va enregistrer si l'ensemble est identique ou oppose
                       finalResult = true;
                    } else {
                        finalResult = false;
                    }
                } else {
                    //si j et j+1 sont differents, on retourne le clusterId comme cluster convergeant
                    return clusterId;
                }
            }
            if(finalResult){
                //si les itemsets etaient identiques, alors clusterId prend la valeur du cluster le plus avance dans le temps.
                clusterId = i;
            }
        }
        return clusterId;
    }
}