package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.Database;

import java.util.ArrayList;

public interface Generator {

    public ArrayList<ArrayList<Integer>> generateClusters(Database database);
}
