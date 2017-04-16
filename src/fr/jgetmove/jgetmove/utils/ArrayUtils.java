package fr.jgetmove.jgetmove.utils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Classe possèdant differents outils destinés à la manipulation des listes et tableaux
 */
public class ArrayUtils {

    /**
     * Verifie si un ensemble est inclus dans un autre
     *
     * @param a le premier ensemble qui doit être contenu dans b
     * @param b le deuxieme ensemble qui doit contenir a
     * @return vrai si a est inclus dans b
     */
    public static boolean isIncluded(Set<Integer> a, Set<Integer> b) {
        if (a.size() > b.size()) {
            return false;
        }

        return b.containsAll(a);

    }

    /**
     * Verifie si deux {@link Set} sont egaux
     *
     * @param a premier set à comparer
     * @param b deuxième set à comparer
     * @return vrai si a et b sont egaux
     */
    public static boolean areEqual(Set<Integer> a, Set<Integer> b) {
        return a.equals(b);
    }

    /**
     * Retourne vrai si les deux tableaux sont identiques
     *
     * @param set  le Set a comparer
     * @param list la liste a comparer
     * @return vrai si les deux sont identiques
     */
    public static boolean isSame(Set<Integer> set, List<Integer> list) {
        // meilleure optimisation possible : http://stackoverflow.com/a/1075817
        TreeSet<Integer> treeSet = new TreeSet<>(set);
        TreeSet<Integer> treeList = new TreeSet<>(list);

        return treeSet.equals(treeList);
    }
}
