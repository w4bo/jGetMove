package fr.jgetmove.jgetmove;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.ConvoyDetector;
import fr.jgetmove.jgetmove.detector.DetectionManager;
import fr.jgetmove.jgetmove.detector.Detector;
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
        	int minTime = 0;
        	
            Input inputObj = new Input("assets/test.dat");
            Input inputTime = new Input("assets/testtimeindex.dat");

            Database database = new Database(inputObj, inputTime);
            Debug.println(database);

            Solver solver = new Solver(1, 0, 0);
            solver.init(database);

            /*
             * Init Detectors
             */
            Set<Detector> detectors = new HashSet<>();
            detectors.add(ConvoyDetector.getInstance(minTime));

            DetectionManager detectionManager = new DetectionManager(database, detectors, solver.getClustersGenerated());
            detectionManager.run();
        } catch (IOException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }
}
