package fr.jgetmove.jgetmove.config;

public class DefaultConfig extends Config {

    private int minSupport;
    private int maxPattern;
    private int minTime;
    private double commonObjectPercentage; //min_w

    public DefaultConfig(int minSupport, int maxPattern, int minTime, double commonObjectPercentage) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
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

    public double getCommonObjectPercentage() {
        return commonObjectPercentage;
    }
}
