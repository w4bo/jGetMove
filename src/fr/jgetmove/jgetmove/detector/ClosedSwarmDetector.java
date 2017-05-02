package fr.jgetmove.jgetmove.detector;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.pattern.Pattern;

import java.util.ArrayList;

public class ClosedSwarmDetector implements Detector {

    private int minTime;

    public ClosedSwarmDetector(int minTime) {
        this.minTime = minTime;
    }

    @Override
    public ArrayList<Pattern> detect(Database database, ArrayList<ArrayList<Integer>> clustersGenerated) {
        return null;
    }

	
	/*public ArrayList<Pattern> detect(Database database, ArrayList<ArrayList<Integer>> clustersGenerated) {

		ArrayList<Pattern> closedSwarm = new ArrayList<>();
		
		for (ArrayList<Integer> itemset : clustersGenerated) {
            ArrayList<Integer> timeSet = new ArrayList<>();
            for (int clusterId : itemset) {
                timeSet.add(database.getClusterTimeId(clusterId));
            }
            
            Set<Integer> correctTransactionSet = new HashSet<>(database.getClusterTransactions(itemset.get(0)).keySet());
            if(timeSet.get(timeSet.size()-1) - timeSet.get(0) >= minTime){
            	
            	//Init new ClosedSwarm
            	Set<Time> timesOfItemset = new HashSet<>();
                Set<Transaction> transactionOfItemset = new HashSet<>();
                //Get transactions object
            	for(int transactionId : correctTransactionSet){
            		transactionOfItemset.add(database.getTransaction(transactionId));
            	}
            	//Get Times
            	for(int timeId : timeSet){
            		timesOfItemset.add(database.getTime(timeId));
            	}
            	closedSwarm.add(new ClosedSwarm(transactionOfItemset, timesOfItemset));
            }
		} 
		//Debug.println("Il y a : " + closedSwarm.size() + "closed swarm");
		
		return closedSwarm;
	}*/

}
