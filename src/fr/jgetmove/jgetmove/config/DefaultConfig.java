package fr.jgetmove.jgetmove.config;

/**
 * @version 1.0.0
 * @since 0.1.0
 */
public class DefaultConfig extends Config {

    private final int blockSize;
    private final int minSupport;
    private final int maxPattern;
    private final int minTime;
    private final double commonObjectPercentage; //min_w

    public DefaultConfig(int minSupport, int maxPattern, int minTime, int blockSize, double commonObjectPercentage) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
        this.blockSize = blockSize;
        this.commonObjectPercentage = commonObjectPercentage;
    }

    public int getMinSupport() {
        return minSupport;
    }

    public int getMaxPattern() {
        return maxPattern;
    }

    public int getMinTime() {
        return minTime;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public double getCommonObjectPercentage() {
        return commonObjectPercentage;
    }
}
