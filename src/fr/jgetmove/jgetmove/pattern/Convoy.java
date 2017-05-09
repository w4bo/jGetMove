package fr.jgetmove.jgetmove.pattern;

import fr.jgetmove.jgetmove.database.Time;
import fr.jgetmove.jgetmove.database.Transaction;
import fr.jgetmove.jgetmove.debug.Debug;

import java.util.ArrayList;
import java.util.Set;
import javax.json.*;

/**
 * Class representant le pattern Convoy
 */
public class Convoy implements Pattern {

    /**
     * Liste des transactions présents dans le convoy
     */
    private Set<Transaction> transactions;
    /**
     * Liste des temps présents dans le convoy
     */
    private Set<Time> times;

    /**
     * Constructeur
     *
     * @param clusters Liste de transactions present dans le convoy
     * @param times    La liste des temps associées
     */
    public Convoy(Set<Transaction> transactions, Set<Time> times) {
        this.transactions = transactions;
        this.times = times;
        Debug.println(this);
    }

    /**
     * Getter sur la liste des transactions
     *
     * @return la liste des clusters présents dans le convoy
     */
    public Set<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Setter sur la liste des transactions
     *
     * @param clusters une nouvelle liste de transactions
     */
    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Getter sur la liste des temps
     *
     * @return La liste des temps des temps présents dans le convoy
     */
    public Set<Time> getTimes() {
        return times;
    }

    /**
     * Setter sur la liste des temps
     *
     * @param times une nouvelle liste de temps
     */
    public void setTimes(Set<Time> times) {
        this.times = times;
    }


    public String toString() {
        return "Convoy:\n" + " transactions : [" + transactions;
    }
    
    public String printGetTransactions(){
    	String s = "";
    	for(Transaction transaction : this.getTransactions()) {
    		s += transaction.getId() + ",";
    	}
    	s = s.substring(0, s.length()-1); //retire la dernière virgule
    	return s;
    }
    
    public JsonArrayBuilder getLinksToJson(int index, JsonArrayBuilder patternEntryArray){
    	ArrayList<Time> timeArray = new ArrayList<>(times);
    	ArrayList<Transaction> transactionArray = new ArrayList<>(transactions);
    	int source = -1,
			target = -1;
		for(int timeIndex = 0; timeIndex < timeArray.size() - 1; timeIndex++ ){			
			for (int transactionClusterId: transactionArray.get(0).getClusterIds() ) {
				if(source == -1){
					for (int clusterId: timeArray.get(timeIndex).getClusterIds() ) {
						if(clusterId == transactionClusterId){
							source = clusterId;
						}
					}
				}

				if(target ==-1){
					for (int clusterId: timeArray.get(timeIndex+1).getClusterIds() ) {
						if(clusterId == transactionClusterId){
							target = clusterId;
						}
					}
				}
			}
		}
		patternEntryArray.add(Json.createObjectBuilder()
        		.add("id",index)
        		.add("source", source)
        		.add("target", target)
        		.add("value", this.getTransactions().size())
        		.add("label", this.printGetTransactions()));
        return patternEntryArray;	
    }
}
