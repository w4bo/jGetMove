/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Convergent;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;


/**
 * In charge of detecting Convergent patterns
 * <p>
 * A convergeant pattern is when more and more transactions are joining across a period of time
 *
 * @author jframos0
 * @version 1.0.0
 * @implSpec We just detect if across time, more and more itemsets are together. The moment they begin to part is when the pattern ends. This way, it detects all the englobing conveargent patterns.
 * @see DivergentDetector
 * @since 0.2.0
 */
public class ConvergentDetector implements MultiDetector {

    /**
     * Detects all the {@link Convergent} in a collection of itemsets.
     *
     * @param dataBase data binder
     * @param itemsets a collection of all the itemsets detected
     * @return an array of detected patterns
     * @implSpec First, we remove all the itemsets which spread across 1 cluster (to remove noise).
     * The rest is mapped into an matrix itemset x cluster.
     * <p>
     * We iterate over each cluster (excluding the one which are on the first time).
     * At each iterations we look if there are more than one itemset at this point. if there is then it's sa convergeance.
     * And then we simply need to check how long they are staying together (using {@link #subDetect(boolean[][], int, ArrayList, int)}).
     * <p>
     * Once we have the latest cluster (at being together) we create a pattern from it.
     * @implNote the current implementation may vary
     */
    public ArrayList<Pattern> detect(DataBase dataBase, Collection<Itemset> itemsets) {

        ArrayList<Pattern> convergeants = new ArrayList<>();
        ArrayList<Integer> lastClusterIndexes = new ArrayList<>();
        ArrayList<Integer> equivalence = new ArrayList<>(dataBase.getClusterIds());

        int nbClusters = dataBase.getClusters().size();
        int nbItemsets = itemsets.size();
        boolean[][] itemsetMatrice = new boolean[nbItemsets][nbClusters];
        int indexItemset = 0;
        int nbTrue = 0;
        /*
        Construction de la Matrice [Itemset][Clusters]
         */
        for (Itemset itemset : itemsets) {
            for (int clusterIndex = 0; clusterIndex < nbClusters; ++clusterIndex) {
                if (itemset.getClusters().size() > 1) {
                    itemsetMatrice[indexItemset][clusterIndex] = true;
                    nbTrue++;
                }
                clusterIndex++;
            }
            if (nbTrue > 0) {
                //removing itemsets that are alone
                indexItemset++;
                nbTrue = 0;
            }
        }

        /*
        Ajout des clusters à la liste lastClusterIndexes
         */
        for (int clusterIndex = 0; clusterIndex < nbClusters; clusterIndex++) {
            if (dataBase.getCluster(equivalence.get(clusterIndex)).getTimeId() != 1) {
                ArrayList<Integer> itemsetIds = new ArrayList<>();
                for (int itemsetId = 0; itemsetId < nbItemsets; itemsetId++) {
                    if (itemsetMatrice[itemsetId][clusterIndex]) {
                        itemsetIds.add(itemsetId);
                    }
                }
                if (itemsetIds.size() > 1) {
                    int lastClusterIndex = subDetect(itemsetMatrice, clusterIndex, itemsetIds, nbClusters);
                    if (!lastClusterIndexes.contains(lastClusterIndex)) {
                        lastClusterIndexes.add(lastClusterIndex);
                    }
                }
            }

        }
        /*
        Creation d'un convergeant par cluster dans la liste lastClusterIndexes
         */


        for (int clusterIndex : lastClusterIndexes) {
            //Lorsqu'on a notre tableau, pour tt les lastclusters
            //New Convergent (dataBase, lastClusterIndexes[i]);
            convergeants.add(new Convergent(dataBase, equivalence.get(clusterIndex)));
        }

        return convergeants;
    }

    /**
     * Detect the last cluster when all the itemsetIds are present
     *
     * @param itemsetMatrice the matrix
     * @param clusterIndex   the first clusterIndex when the itemsets are all present
     * @param itemsetIds     a list of the itemsetIds
     * @param nbClusters     the maximum number of clusters
     * @return the last clusterIndex where all the itemsetIds are present
     */
    public int subDetect(boolean[][] itemsetMatrice, int clusterIndex, ArrayList<Integer> itemsetIds, int nbClusters) {
        boolean finalResult = true; //Variable qui permet de verifier si les itemsets sont identiques (true) ou opposes (false) opposes = tt vrai et dans l'autre cluster tt faux
        int lastClusterIndex = clusterIndex;
        for (int index = clusterIndex + 1; index < nbClusters; index++) {
            //Pour chaque cluster en partant du clusterIndex + 1 vu qu'on doit comparer clusterIndex aux clusters suivants
            for (int j = 0; j < itemsetIds.size() - 1; j++) {
                //Pour chaque itemset du cluster avec pour id index;
                if (itemsetMatrice[itemsetIds.get(j)][index] == itemsetMatrice[itemsetIds.get(j + 1)][index]) {
                    //Si j et j + 1 sont egaux
                    //On va enregistrer si l'ensemble est identique ou oppose
                    finalResult = itemsetMatrice[itemsetIds.get(j)][index];
                } else {
                    //si j et j+1 sont differents, on retourne le clusterIndex comme cluster convergeant
                    return lastClusterIndex;
                }
            }
            if (finalResult) {
                //si les itemsets etaient identiques, alors clusterIndex prend la valeur du cluster le plus avance dans le temps.
                lastClusterIndex = index;
            }
        }
        return lastClusterIndex;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
