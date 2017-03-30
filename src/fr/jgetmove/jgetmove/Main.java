package fr.jgetmove.jgetmove;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.io.Input;
import fr.jgetmove.jgetmove.solver.Solver;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        try {
            Input inputObj = new Input("assets/test.dat");
            Input inputTime = new Input("assets/testtimeindex.dat");

            Database database = new Database(inputObj, inputTime);
            System.out.println(database);

            Solver solver = new Solver(1,1,0);
            solver.initLcm(database);

        } catch (IOException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }
}