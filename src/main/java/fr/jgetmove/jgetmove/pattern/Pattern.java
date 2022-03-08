/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.pattern;

import javax.json.JsonObject;
import java.util.List;

/**
 * Interface representing a pattern.
 *
 * @author stardisblue
 * @author jframos0
 * @author Carmona-Anthony
 * @version 1.0.0
 * @since 0.1.0
 */
public interface Pattern {
    /**
     * @param index identifier
     * @return a list of JsonObject to represent the pattern in the output file
     */
    List<JsonObject> toJsonArray(int index);
}
