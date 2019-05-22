/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.debug;

/**
 * Interface used to display a prettified version of {@link #toString()}
 * <p>
 * If it's implemented, {@link Debug} will use it as default display
 *
 * @author stardisblue
 * @version 1.0.0
 * @since 0.2.0
 */
public interface PrettyPrint {
    /**
     * Displays a prettified version of {@link Object#toString()}
     * <p>
     * It's used as display by {@link Debug} if it's implemented.
     *
     * @return String containing a prettified version of {@link Object#toString()}
     */
    String toPrettyString();
}
