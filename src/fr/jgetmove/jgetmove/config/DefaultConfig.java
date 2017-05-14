package fr.jgetmove.jgetmove.config;

public class DefaultConfig extends Config {

    private int minSupport;
    private int maxPattern;
    private int minTime;

    public DefaultConfig(int minSupport, int maxPattern, int minTime) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
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
}
