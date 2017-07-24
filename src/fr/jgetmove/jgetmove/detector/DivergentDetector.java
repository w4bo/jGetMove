package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Divergent;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @version 1.0.0
 * @since 0.2.0
 */
public class DivergentDetector implements MultiDetector {

    public ArrayList<Pattern> detect(DataBase defaultDataBase, Collection<Itemset> itemsets) {

        ArrayList<Pattern> divergeants = new ArrayList<>();
        ArrayList<Integer> firstClusters = new ArrayList<>();
        int nbClusters = defaultDataBase.getClusters().size();
        int nbItemsets = itemsets.size();
        boolean[][] itemsetMatrice = new boolean[nbItemsets][nbClusters];
        int indexItemset = 0;
        int nbTrue = 0;
        /*
        Construction de la Matrice [Itemset][Clusters]
         */
        for (Itemset itemset : itemsets) {
            for (int idCluster : itemset.getClusters()) {
                if (itemset.getClusters().size() > 1) {
                    itemsetMatrice[indexItemset][idCluster] = true;
                    nbTrue++;
                }
            }
            if (nbTrue > 0) {
                //Condition qui permet de ne pas incrémenter l'index si on était sur un itemset qui ne comportait qu'un seul cluster
                indexItemset++;
                nbTrue = 0;
            }
        }
        /*
        Ajout des clusters à la liste firstClusters
         */
        for (int clusterId = nbClusters - 1; clusterId >= 0; clusterId--) {
            if (defaultDataBase.getCluster(clusterId).getTimeId() != defaultDataBase.getCluster(defaultDataBase.getClusters().size() - 1).getTimeId()) {
                ArrayList<Integer> itemsetIds = new ArrayList<>();
                for (int j = 0; j < nbItemsets; j++) {
                    if (itemsetMatrice[j][clusterId]) {
                        itemsetIds.add(j);
                    }
                }
                if (itemsetIds.size() > 1) {
                    int idFirstCluster = subDetect(itemsetMatrice, clusterId, itemsetIds, nbClusters);
                    if (!firstClusters.contains(idFirstCluster)) {
                        firstClusters.add(idFirstCluster);
                    }
                }
            }

        }
        /*
        Creation d'un convergeant par cluster dans la liste firstClusters
         */
        for (int idCluster : firstClusters) {
            //Lorsqu'on a notre tableau, pour tt les lastclusters
            //New Convergent (defaultDataBase, lastClusters[i]);
            divergeants.add(new Divergent(defaultDataBase, idCluster));
        }

        return divergeants;
    }

    public int subDetect(boolean[][] itemsetMatrice, int clusterId, ArrayList<Integer> itemsetIds, int nbClusters) {
        boolean finalResult = true; //Variable qui permet de verifier si les itemsets sont identiques (true) ou opposes (false) opposes = tt vrai et dans l'autre cluster tt faux
        for (int i = clusterId - 1; i >= 0; i--) {
            //Pour chaque cluster en partant du clusterId + 1 vu qu'on doit comparer clusterId aux clusters précèdents
            for (int j = 0; j < itemsetIds.size() - 1; j++) {
                //Pour chaque itemset du cluster avec pour id i;
                if (itemsetMatrice[itemsetIds.get(j)][i] == itemsetMatrice[itemsetIds.get(j + 1)][i]) {
                    //Si j et j + 1 sont egaux
                    //On va enregistrer si l'ensemble est identique ou oppose
                    finalResult = itemsetMatrice[itemsetIds.get(j)][i];
                } else {
                    //si j et j+1 sont differents, on retourne le clusterId comme cluster divergeant
                    return clusterId;
                }
            }
            if (finalResult) {
                //si les itemsets etaient identiques, alors clusterId prend la valeur du cluster le plus avance dans le temps.
                clusterId = i;
            }
        }
        return clusterId;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
