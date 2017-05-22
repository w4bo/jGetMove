package fr.jgetmove.jgetmove;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.ClosedSwarmDetector;
import fr.jgetmove.jgetmove.detector.ConvoyDetector;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.detector.GroupPatternDetector;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.io.Output;
import fr.jgetmove.jgetmove.pattern.Pattern;
import fr.jgetmove.jgetmove.solver.ClusterGenerator;
import fr.jgetmove.jgetmove.solver.PatternGenerator;
import fr.jgetmove.jgetmove.solver.Solver;

import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        //Debug.enable();

        try {
            /*
             * Initialise the config parameters
             */
            int minSupport = 1;
            int maxPattern = 0;
            int minTime = 0;
            double commonObjectPercentage = 0; 
            DefaultConfig config = new DefaultConfig(minSupport, maxPattern, minTime,commonObjectPercentage);
            /*
             * Create Input from the file test.dat et testtimeindex.dat
             */
            Input inputObj = new Input("assets/test.dat");
            Input inputTime = new Input("assets/testtimeindex.dat");
            /*
             * Create a new database from the Input objects
             */
            Database database = new Database(inputObj, inputTime);
            /*
             * Init ClusterGenerator and detectors
             */
            ClusterGenerator clusterGenerator = new ClusterGenerator(database, config);
            PatternGenerator patternGenerator = new PatternGenerator(database, config);
            Set<Detector> detectors = new HashSet<>();
            detectors.add(new ConvoyDetector(minTime));
            detectors.add(new ClosedSwarmDetector(minTime));
            detectors.add(new GroupPatternDetector(config.getMinTime(),config.getCommonObjectPercentage()));
            /*
             * Create solver from clusterGenerator, patternGenerator, detectors and start the generation
             */
            Solver solver = new Solver(clusterGenerator, patternGenerator, detectors);
            solver.generateClusters();
            HashMap<Detector,ArrayList<Pattern>> patterns = solver.detectPatterns();
            /*
             * Create new Output object from results  
             */
            JsonObjectBuilder databaseJson = database.toJSON();
            Output outputSolver = new Output("assets/jsonResults.json");
            outputSolver.write(solver.toJSON(patterns,databaseJson));

        } catch (IOException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }
}
