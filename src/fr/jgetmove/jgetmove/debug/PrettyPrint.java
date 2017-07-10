package fr.jgetmove.jgetmove.debug;

/**
 * Interface used to display a prettified version of {@link #toString()}
 *<p>
 * If it's implemented, {@link Debug} will use it as default display
 */
public interface PrettyPrint {
    /**
     * Displays a prettified version of {@link #toString()}
     * <p>
     * It's used as display by {@link Debug} if it's implemented.
     *
     * @return String containing a prettified version of {@link #toString()}
     */
    String toPrettyString();
}
