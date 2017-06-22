package fr.jgetmove.jgetmove.utils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Classe possèdant differents outils destinés à la manipulation des listes et tableaux
 */
public class ArrayUtils {

    /**
     * Checks if <tt>A</tt> is contained in <tt>B</tt>
     *
     * @param a set to be checked for containment
     * @param b set which is supposed to contain <tt>a</tt>
     * @return <tt>true</tt> if <tt>a</tt> is contained in <tt>b</tt>
     */
    public static boolean isIncluded(Set<Integer> a, Set<Integer> b) {
        if (a.size() > b.size()) {
            return false;
        }

        return b.containsAll(a);

    }

    /**
     * Checks if the set and the list are equal
     * <p>
     * The two are converted to treeset and then are checked with {@link TreeSet#equals(Object)}
     *
     * @param set  Set to check
     * @param list List to check
     * @return <tt>true</tt> if they are the same
     */
    public static boolean isSame(Set<Integer> set, List<Integer> list) {
        // meilleure optimisation possible : http://stackoverflow.com/a/1075817
        TreeSet<Integer> treeSet = new TreeSet<>(set);
        TreeSet<Integer> treeList = new TreeSet<>(list);

        return treeSet.equals(treeList);
    }
}
