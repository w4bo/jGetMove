package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.database.DataBase;
import fr.jgetmove.jgetmove.debug.Debug;

import java.util.ArrayList;

public class ClusterGeneratorResult {

    private DataBase dataBase;
    private ArrayList<ArrayList<Integer>> lvl2TimeIds;
    private ArrayList<ArrayList<Integer>> lvl2ClusterIds;

    ClusterGeneratorResult(DataBase dataBase,
                           ArrayList<ArrayList<Integer>> lvl2TimeIds, ArrayList<ArrayList<Integer>> lvl2ClusterIds) {
        this.dataBase = dataBase;
        this.lvl2ClusterIds = lvl2ClusterIds;
        this.lvl2TimeIds = lvl2TimeIds;
    }

    public DataBase getDataBase() {
        return dataBase;
    }


    public ArrayList<ArrayList<Integer>> getLvl2TimeIds() {
        return lvl2TimeIds;
    }

    public ArrayList<ArrayList<Integer>> getLvl2ClusterIds() {
        return lvl2ClusterIds;
    }

    @Override
    public String toString() {
        return "\n|--_Database " + Debug.indent(dataBase.toString())
                + "\n|-- Lvl2Clusters" + lvl2ClusterIds
                + "\n`-- Lvl2Times" + lvl2TimeIds;
    }
}
