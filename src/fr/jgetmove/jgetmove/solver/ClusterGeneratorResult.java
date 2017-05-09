package fr.jgetmove.jgetmove.solver;

import java.util.ArrayList;

import fr.jgetmove.jgetmove.config.Config;
import fr.jgetmove.jgetmove.database.Database;

public class ClusterGeneratorResult {
	
	private Database database;
	private ArrayList<ArrayList<Integer>> clusters;
	private ArrayList<ArrayList<Integer>> lvl2TimeIds;
	private ArrayList<ArrayList<Integer>> lvl2ClusterIds;
	private Config config;
	
	ClusterGeneratorResult(Database database,Config config, ArrayList<ArrayList<Integer>> clusters,ArrayList<ArrayList<Integer>> lvl2TimeIds, ArrayList<ArrayList<Integer>> lvl2ClusterIds){
		this.database = database;
		this.clusters = clusters;
		this.lvl2ClusterIds = lvl2ClusterIds;
		this.lvl2TimeIds = lvl2TimeIds;
		this.config = config;
	}

	public Database getDatabase() {
		return database;
	}
	
	public Config getConfig(){
		return config;
	}

	public ArrayList<ArrayList<Integer>> getClusters() {
		return clusters;
	}

	public ArrayList<ArrayList<Integer>> getLvl2TimeIds() {
		return lvl2TimeIds;
	}

	public ArrayList<ArrayList<Integer>> getLvl2ClusterIds() {
		return lvl2ClusterIds;
	}
}
