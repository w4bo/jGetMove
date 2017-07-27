/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
