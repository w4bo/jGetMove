/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.config;

/**
 * Contains all the variables used in the application
 *
 * @author Carmona-Anthony
 * @author stardisblue
 * @version 1.1.0
 * @since 0.1.0
 */
public class Config {

    private final int blockSize;
    private final int minSupport;
    private final int maxPattern;
    private final int minTime;
    private final double commonObjectPercentage;

    public Config(int minSupport, int maxPattern, int minTime, int blockSize, double commonObjectPercentage) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
        this.blockSize = blockSize;
        this.commonObjectPercentage = commonObjectPercentage;
    }

    /**
     * @return minimal number of transactions foreach itemset
     */
    public int getMinSupport() {
        return minSupport;
    }

    /**
     * @return maximum number of patterns
     */
    public int getMaxPattern() {
        return maxPattern;
    }

    /**
     * @return minimal number of times foreach itemset
     */
    public int getMinTime() {
        return minTime;
    }

    /**
     * @return number of times foreach block
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return minimal percentage of common object for the grouppattern
     */
    public double getCommonObjectPercentage() {
        return commonObjectPercentage;
    }
}
