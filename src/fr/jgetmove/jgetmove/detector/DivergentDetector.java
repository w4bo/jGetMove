/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.pattern.Divergent;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;

/**
 * In charge of detecting Divergent patterns
 * <p>
 * A divergent pattern is when from a defined set of transactions, these transactions diverge over time(forming a tree).
 *
 * @author jframos0
 * @version 1.0.0
 * @see ConvergentDetector
 * @since 0.2.0
 */
public class DivergentDetector implements MultiDetector {

    /**
     * Detects all the {@link Divergent} patterns in a collection of itemsets
     *
     * @param dataBase data binder
     * @param itemsets a collection of all the itemsets detected
     * @return an array of detected patterns
     * @implSpec First, we remove all the itemsets which spread across 1 cluster (to remove noise).
     * The rest is mapped into an matrix itemset x cluster.
     * <p>
     * We iterate over each cluster (in inverse order), excluding the one which are on the last time.
     * At each iterations we look if there are more than one itemset at this point. if there is then it's a divergence.
     * And then we simply need to check since when they are together (using {@link #subDetect(boolean[][], int, ArrayList)}).
     * <p>
     * Once we have the first cluster (at being together) we create a pattern from it.
     * @implNote the current implementation may vary
     */
    public ArrayList<Pattern> detect(DataBase dataBase, Collection<Itemset> itemsets) {

        ArrayList<Pattern> divergeants = new ArrayList<>();
        ArrayList<Integer> firstClusters = new ArrayList<>();
        int nbClusters = dataBase.getClusters().size();
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
            if (dataBase.getCluster(clusterId).getTimeId() != dataBase.getCluster(dataBase.getClusters().size() - 1).getTimeId()) {
                ArrayList<Integer> itemsetIds = new ArrayList<>();
                for (int j = 0; j < nbItemsets; j++) {
                    if (itemsetMatrice[j][clusterId]) {
                        itemsetIds.add(j);
                    }
                }
                if (itemsetIds.size() > 1) {
                    int idFirstCluster = subDetect(itemsetMatrice, clusterId, itemsetIds);
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
            //New Convergent (dataBase, lastClusters[i]);
            divergeants.add(new Divergent(dataBase, idCluster));
        }

        return divergeants;
    }

    /**
     * Detect the first cluster when all the itemsetIds are present
     *
     * @param itemsetMatrice the matrix
     * @param clusterId      the last clusterId when the itemsets are all present
     * @param itemsetIds     a list of the itemsetIds
     * @return the first clusterId where all the itemsetIds are present
     */
    public int subDetect(boolean[][] itemsetMatrice, int clusterId, ArrayList<Integer> itemsetIds) {
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
