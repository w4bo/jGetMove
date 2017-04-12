package fr.jgetmove.jgetmove;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.ConvoyDetector;
import fr.jgetmove.jgetmove.detector.IDetector;
import fr.jgetmove.jgetmove.detector.PatternDetector;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.solver.Solver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Debug.enable();

        try {
            Input inputObj = new Input("assets/test.dat");
            Input inputTime = new Input("assets/testtimeindex.dat");

            Database database = new Database(inputObj, inputTime);
            Debug.println(database);

            Solver solver = new Solver(1, 0, 0);
            solver.init(database);
            
            /**
             * Init Detectors
             */
            Set<IDetector> detectors = new HashSet<IDetector>();
            detectors.add(ConvoyDetector.getInstance());

        	PatternDetector patternDetector = new PatternDetector(database,detectors);
        	patternDetector.run();
        	

        } catch (IOException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }
}