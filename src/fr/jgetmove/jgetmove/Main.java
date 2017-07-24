package fr.jgetmove.jgetmove;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.Itemset;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.*;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.io.Output;
import fr.jgetmove.jgetmove.pattern.Pattern;
import fr.jgetmove.jgetmove.solver.BlockMerger;
import fr.jgetmove.jgetmove.solver.ItemsetsFinder;
import fr.jgetmove.jgetmove.solver.OptimizedItemsetsFinder;
import fr.jgetmove.jgetmove.solver.Solver;

import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @version 1.0.0
 * @since 0.1.0
 */
public class Main {

    @Parameter(names = {"-d", "--debug"}, description = "allow the debug logs to be displayed")
    private boolean debug = false;

    @Parameter(description = "files", arity = 2, required = true)
    private ArrayList<String> files = new ArrayList<>();

    @Parameter(names = {"-h", "--help"}, description = "displays the help", help = true)
    private boolean help;

    @Parameter(names = {"-s", "--support-min"}, description = "min support : the minimum number of transction in each itemset", required = true)
    private int minSupport = 1;

    // TODO: 06/07/2017 WHAT IS THIS ?
    @Parameter(names = {"-p", "--pattern-max"}, description = "Max pattern", required = true)
    private int maxPattern = 0;

    @Parameter(names = {"-t", "--time-min"}, description = "Min time : the minimum number of times in each itemset", required = true)
    private int minTime = 1;

    @Parameter(names = {"-b", "--bloc-size"}, description = "Block size : the number of times in each block")
    private int blockSize = 0;

    @Parameter(names = {"-o", "--output"}, description = "json output file")
    private String outputFile = "assets/results.json";

    // TODO: 06/07/2017 WHAT IS THIS
    @Parameter(names = {"-c", "--common-object-percentage"}, description = "Common object percentage")
    private double commonObjectPercentage = 0;


    /**
     * Main method
     *
     * @param args program arguments
     */
    public static void main(String... args) {
        Main main = new Main();
        JCommander jcommander = JCommander.newBuilder().addObject(main).build();
        jcommander.parse(args);
        main.run(jcommander);
    }

    /**
     * delegated from the main method
     *
     * @param jcommander jcommander options
     */
    private void run(JCommander jcommander) {
        if (help) { // displays the help
            jcommander.usage();
            return;
        }

        if (debug) { // enables debug
            Debug.enable();
        }

        try {
            // Initializes the config parameters
            DefaultConfig config = new DefaultConfig(minSupport, maxPattern, minTime, blockSize, commonObjectPercentage);

            // Creates Inputs from the file paths
            Input inputObj = new Input(files.get(0));
            Input inputTime = new Input(files.get(1));


            //Create a new dataBase from the Input objects
            Debug.printTitle("DataBase Initialisation", Debug.INFO);
            DataBase dataBase = new DataBase(inputObj, inputTime);
            Debug.println("Database", dataBase, Debug.INFO);

            //Init ItemsetsFinder and BlockMerger
            ItemsetsFinder itemsetsFinder = new OptimizedItemsetsFinder(config);
            BlockMerger blockMerger = new BlockMerger(config);

            // Initializes the detectors
            // Single detectors
            Set<SingleDetector> singleDetectors = new HashSet<>();
            singleDetectors.add(new ConvoyDetector(minTime));
            singleDetectors.add(new ClosedSwarmDetector(minTime));
            //TODO Change GroupPatternDetector singleDetector -> multiDetector

            // multi detectors
            Set<MultiDetector> multiDetectors = new HashSet<>();
            multiDetectors.add(new DivergentDetector());
            multiDetectors.add(new ConvergentDetector());
            // multiDetectors.add(new GroupPatternDetector(minTime, commonObjectPercentage));

            //Create solver, injecting the necessary objects (DI)
            Debug.printTitle("Solver Initialisation", Debug.INFO);
            Solver solver = new Solver(itemsetsFinder, blockMerger, singleDetectors, multiDetectors, config);
            Debug.println(solver, Debug.INFO);


            /*
             * Main algorithm processing
             */
            //generates the itemsets from the database (separated by blocs)
            long then = System.nanoTime();
            ArrayList<ArrayList<Itemset>> results = solver.findItemsets(dataBase);
            Debug.println("It took " + (System.nanoTime() - then) + "ns to find the itemsets", Debug.INFO);
            // merges the blocs (and so itemsets) and detect patterns from the result
            then = System.nanoTime();
            HashMap<Detector, ArrayList<Pattern>> patterns = solver.blockMerge(dataBase, results);
            Debug.println("It took " + (System.nanoTime() - then) + "ns to find generate the patterns", Debug.INFO);
            /*
             * JSON Output process
             */
            // Extracts the database structure in JSON

            JsonObjectBuilder jsonBuilder = dataBase.toJson();

            // adds patterns
            jsonBuilder.add("patterns", solver.toJson(patterns));

            // Opens the output file
            Output outputSolver = new Output(outputFile);

            // writes the JSON in it
            outputSolver.write(jsonBuilder.build().toString());
            outputSolver.close();

        } catch (IOException | ClusterNotExistException | MalformedTimeIndexException e) {
            e.printStackTrace();
        }
    }
}
