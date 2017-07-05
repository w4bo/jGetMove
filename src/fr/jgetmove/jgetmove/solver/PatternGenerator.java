package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.detector.SingleDetector;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class PatternGenerator {

    private int minSupport, maxPattern, minTime;
    private boolean printed;
    private HashMap<SingleDetector, ArrayList<Pattern>> motifs;
    private TreeSet<Itemset> itemsets;

    /**
     * Initialise le solveur.
     *
     * @param minSupport support minimal
     * @param maxPattern nombre maximal de pattern a trouvé
     * @param minTime    temps minimal
     */
    public PatternGenerator(int minSupport, int maxPattern, int minTime) {
        this.minSupport = minSupport;
        this.maxPattern = maxPattern;
        this.minTime = minTime;
        motifs = new HashMap<>();
        itemsets = new TreeSet<>();
        printed = false;

    }


    /**
     * Initialise le solveur.
     */
    public PatternGenerator(DefaultConfig config) {
        this.minSupport = config.getMinSupport();
        this.maxPattern = config.getMaxPattern();
        this.minTime = config.getMinTime();
        motifs = new HashMap<>();
        itemsets = new TreeSet<>();
        printed = false;

    }

}
