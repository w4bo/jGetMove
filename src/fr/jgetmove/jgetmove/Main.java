package fr.jgetmove.jgetmove;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.detector.ClosedSwarmDetector;
import fr.jgetmove.jgetmove.detector.ConvoyDetector;
import fr.jgetmove.jgetmove.detector.Detector;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.solver.ClusterGenerator;
import fr.jgetmove.jgetmove.solver.Solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Debug.enable();

        try {
            int minTime = 1;

            Input inputObj = new Input("assets/test.dat");
            Input inputTime = new Input("assets/testtimeindex.dat");

            Database database = new Database(inputObj, inputTime);
            System.out.println(database.toJSON());
            Debug.println(database);

            /*
             * Init ClusterGenerator and detectors
             */
            /*ClusterGenerator clusterGenerator = new ClusterGenerator(1, 0, minTime);
            Set<Detector> detectors = new HashSet<>();
            detectors.add(new ConvoyDetector(minTime));
            detectors.add(new ClosedSwarmDetector(minTime));

            Solver solver = new Solver(database, clusterGenerator, detectors);

            solver.generateClusters();
            //Debug.println(generatedClusters);
            solver.detectPatterns();*/

        } catch (IOException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }
}
