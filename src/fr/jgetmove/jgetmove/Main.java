package fr.jgetmove.jgetmove;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.database.ItemsetsOfBlock;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.*;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.io.Output;
import fr.jgetmove.jgetmove.pattern.Pattern;
import fr.jgetmove.jgetmove.solver.ItemsetFinder;
import fr.jgetmove.jgetmove.solver.PatternGenerator;
import fr.jgetmove.jgetmove.solver.Solver;

import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Main {

    @Parameter(names = {"-d", "--debug"}, description = "Debug mode")
    private boolean debug = false;

    @Parameter(description = "files", arity = 2, required = true)
    private ArrayList<String> files = new ArrayList<>();

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    @Parameter(names = {"-s", "--support-min"}, description = "Min support", required = true)
    private int minSupport = 1;

    @Parameter(names = {"-p", "--pattern-max"}, description = "Max pattern", required = true)
    private int maxPattern = 0;

    @Parameter(names = {"-t", "--time-min"}, description = "Min time", required = true)
    private int minTime = 1;

    @Parameter(names = {"-b", "--bloc-size"}, description = "taille du block")
    private int blockSize = 0;

    @Parameter(names = {"-o", "--output"}, description = "json output file")
    private String outputFile = "assets/results.json";


    public static void main(String... args) {

        Main main = new Main();
        JCommander jcommander = JCommander.newBuilder().addObject(main).build();
        jcommander.parse(args);
        main.run(jcommander);
    }

    private void run(JCommander jcommander) {
        if (help) {
            jcommander.usage();
            return;
        }

        if (debug) {
            Debug.enable();
        }

        try {
            /*
             * Initialise the config parameters
             */
            double commonObjectPercentage = 0;
            DefaultConfig config = new DefaultConfig(minSupport, maxPattern, minTime, commonObjectPercentage);
            /*
             * Create Input from the file test.dat et testtimeindex.dat
             */

            Input inputObj = new Input(files.get(0));
            Input inputTime = new Input(files.get(1));
            /*
             * Create a new dataBase from the Input objects
             */
            DataBase dataBase = new DataBase(inputObj, inputTime);

            Debug.printTitle("DataBase Initialisation", Debug.INFO);
            Debug.println(dataBase, Debug.INFO);

            /*
             * Init ItemsetFinder and singleDetectors
             */
            ItemsetFinder itemsetFinder = new ItemsetFinder(config);
            PatternGenerator patternGenerator = new PatternGenerator(config);

            Set<SingleDetector> singleDetectors = new HashSet<>();
            singleDetectors.add(new ConvoyDetector(minTime));
            singleDetectors.add(new ClosedSwarmDetector(minTime));

            Set<MultiDetector> multiDetectors = new HashSet<>();
            multiDetectors.add(new DivergentDetector());
            multiDetectors.add(new ConvergentDetector());


            //singleDetectors.add(new GroupPatternDetector(config.getMinTime(), config.getCommonObjectPercentage()));

            /*
             * Create solver from itemsetFinder, patternGenerator, singleDetectors and start the generation
             */
            Solver solver = new Solver(itemsetFinder, patternGenerator, singleDetectors, multiDetectors, blockSize);

            Debug.printTitle("Solver Initialisation", Debug.INFO);
            Debug.println(solver, Debug.INFO);

            ArrayList<ItemsetsOfBlock> results = solver.findItemsets(dataBase);
            HashMap<Detector, ArrayList<Pattern>> patterns = solver.detectPatterns(dataBase, results);

            /*
             * Create new Output object from results
             */
            JsonObjectBuilder jsonBuilder = dataBase.toJson();
            jsonBuilder.add("patterns", solver.toJson(patterns));

            Output outputSolver = new Output(outputFile);
            outputSolver.write(jsonBuilder.build().toString());
            outputSolver.close();

        } catch (IOException | ClusterNotExistException | MalformedTimeIndexException e) {
            e.printStackTrace();
        }
    }
}
