package fr.jgetmove.jgetmove.solver;

import java.util.Set;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import java.util.ArrayList;

import fr.jgetmove.jgetmove.database.Database;

public class Solver implements ISolver {
	
	private int minSupport, maxPattern, minTime;
	private Set<Integer> totalItem;
	
	private int sizeGenerated;
	
	/**
	 * @param minSupport support minimal
	 * @param maxPattern nombre maximal de pattern a trouv�
	 * @param minTime temps minimal
	 */
	public Solver(int minSupport, int maxPattern,  int minTime ){
		this.minSupport = minSupport;
		this.maxPattern = maxPattern;
		this.minTime = minTime;
	}
	
	/**
	 * Initialise le solver � partir d'une base de donn�es
	 * @param database la base de donn�es � analyser
	 */
	public void initLcm(Database database){
		
		totalItem = database.getItemset();
		System.out.println("totalItem : " + totalItem);
		
		ArrayList<Integer> itemsets = new ArrayList<Integer>();
		ArrayList<Integer> freqList = new ArrayList<Integer>();
		
		LcmIterNew(database,itemsets, freqList);
	}
	
	/**
	 * @param database La database � analyser
	 * @param itemsets une liste representant les id/itemsets
	 * @param freqList une liste reprensentant less clusters frequents
	 */
	private void LcmIterNew(Database database, ArrayList<Integer> itemsets , ArrayList<Integer> freqList){
		
		ArrayList <ArrayList<Integer>> generatedItemsets = new ArrayList<ArrayList<Integer>>();
		ArrayList <ArrayList<Integer>> generatedTimeId = new ArrayList<ArrayList<Integer>>();
		ArrayList <ArrayList<Integer>> generatedItemId = new ArrayList<ArrayList<Integer>>();
		
		sizeGenerated = 1;
		
		GenerateItemset(database,itemsets,generatedItemsets,generatedTimeId, generatedItemId);	
	    
	    System.out.println("GeneratedItemsets : " + generatedItemsets);
		System.out.println("GeneratedItemId : " + generatedItemId);
		System.out.println("GeneratedTimeId : " + generatedTimeId);
		System.out.println("SizeGenerated : " + sizeGenerated);
		
		for(int nbItemSets=0 ; nbItemSets < generatedItemsets.size() ; nbItemSets++){
			int core_i = CalcurateCoreI(database, generatedItemsets.get(nbItemSets),freqList);
			System.out.println("Core_i : " + core_i);
			
			lower_bound(new ArrayList<Integer>(totalItem), 0 , totalItem.size() ,core_i);
			
		}
		
	}
	
	private int lower_bound(ArrayList<Integer> list, int begin , int end , int bound){
		
		int index = begin;
		while(index < end && list.get(index) <= bound){
			index++;
		}
		return index;
	}

	private int CalcurateCoreI(Database database, ArrayList<Integer> itemsets, ArrayList<Integer> freqList) {
		// TODO Auto-generated method stub
		int nbTransactions = database.getTransactions().size();
		ArrayList<Integer> tempo = new ArrayList<Integer>();
		
		for(int i=0;i<nbTransactions;i++){
			tempo = database.getTransaction(i).getItemsets();
		}
		
		if(itemsets.size() > 0){
			int current = freqList.get(freqList.size()-1);
			
			for(int i=freqList.size()-1 ; i>=0 ; i--){
				if(current!=freqList.get(i)){
					return freqList.get(i);
				}
			}
			return itemsets.get(0);
		}
		return 0;
	}

	/**
	 * @param database la database � analyser
	 * @param itemsets une liste representant les id/itemsets
	 * @param generatedItemsets une liste reprensentant les itemsetsGenerees
	 * @param generatedTimeId une liste reprensentant les tempsGenerees
	 * @param generatedItemId une liste reprensentant les itemGenerees
	 * @param sizeGenerated la taille des itemsets gener�es
	 */
	private void GenerateItemset(Database database, ArrayList<Integer> itemsets,
			ArrayList<ArrayList<Integer>> generatedItemsets, ArrayList<ArrayList<Integer>> generatedTimeId,
			ArrayList<ArrayList<Integer>> generatedItemId) {
		
		if(itemsets.size() == 0){
			sizeGenerated = 0;
			return;
		}
		else {
			Integer [] times = database.getTimes().keySet().toArray(new Integer[database.getTimes().keySet().size()]);
			ArrayList <Integer> clusterId = new ArrayList <Integer> (database.getClusters().keySet());
			ArrayList <Integer> listOfDates = new ArrayList<Integer>();
			int numberSameTime = 0;
			int lastTime = 0;
			
			for(int i=0;i<itemsets.size();i++){
				listOfDates.add(times[itemsets.get(i)]);
				if(i!= itemsets.size() -1){
					if(times[itemsets.get(i)] == times[itemsets.get(i+1)]){
						numberSameTime++;
					}	
				}
				lastTime = times[itemsets.get(i)];
			}
			generatedItemsets.clear();
			
			if(numberSameTime == 0){
				generatedTimeId.add(listOfDates);
				generatedItemId.add(clusterId);
			}
			
			//Manage MultiClustering
			ArrayList<ArrayList<Integer>> posDates = new ArrayList<ArrayList<Integer>>();
			for(int i=1; i<lastTime ; i++){
				ArrayList<Integer> row = new ArrayList<Integer>();
				for(int j=0; j<itemsets.size(); j++){
					if(times[itemsets.get(i)]==i){
						row.add(itemsets.get(i));
					}
				}
				posDates.add(row);
			}
			//sizeGenerated stands for the number of potential itemsets to generate
			System.out.println("sizeGenerated stands for the number of potential itemsets to generate");
			sizeGenerated = 1;
			for(int i=0; i<posDates.size();i++){
				sizeGenerated *= posDates.get(i).size();
			}
			
			//initialise the set of generated itemsets
			System.out.println("initialise the set of generated itemsets");
			for (int itemsetIndex = 0; itemsetIndex < posDates.size(); ++itemsetIndex)
			{
				ArrayList<Integer> itemset = posDates.get(itemsetIndex);
				ArrayList<Integer> singleton = new ArrayList<Integer>();
				
				if(itemsetIndex == 0){
					for(Integer iterator : itemset){
						singleton.add(iterator);
						System.out.println("Add Singleton : " + singleton);
						generatedItemsets.add(singleton);	
					}
				} else {
					//OUI
					ArrayList<ArrayList<Integer> > new_results = new ArrayList<ArrayList<Integer> >();
					ArrayList<Integer> new_result = new ArrayList<Integer>();
					for(ArrayList<Integer> iterator : generatedItemsets){
						ArrayList<Integer> result = iterator;
						for(Integer itItem : itemset){
							new_result = result;
							new_result.add(itItem);
							new_results.add(new_result);
						}	
					}
					System.out.println("Test : " + new_result );
					generatedItemsets = new_results;
				}
			}
			// Remove the itemsets already existing in the set of transactions
			
			System.out.println("Remove Itemsets already existing");
			int nb = database.getNumberOfTransaction();
			ArrayList<Integer> tempo;
			ArrayList<ArrayList<Integer> > CheckedItemsets = new ArrayList<ArrayList<Integer> >();
			ArrayList<Integer> currentItemsets = new ArrayList<Integer>();
			int nbitemsets = 0;
			boolean insertok;
			for (int u = 0; u < generatedItemsets.size(); u++){
				insertok = true;
				currentItemsets.clear();
				currentItemsets = generatedItemsets.get(u);
				for (int i = 0; i < nb ; i++){
					tempo = database.getTransaction(i).getItemsets();
					if(tempo==generatedItemsets.get(u)) {
						insertok = false; 
						break;
					}
				}
				if(insertok) {
					CheckedItemsets.add(currentItemsets);
					nbitemsets++;
				}
			}
			sizeGenerated=nbitemsets;
			generatedItemsets.clear();
		    generatedItemsets=CheckedItemsets;
		    System.out.println("GeneratedItemsets : " + CheckedItemsets);
		    
		    // updating list of dates
		    System.out.println("updating list of dates");
		    listOfDates.clear();
		    for (int l=0;l< sizeGenerated;l++){
		    	for(int u=0; u<generatedItemsets.get(l).size(); u++){
		    		for(int i=0; i<itemsets.size();i++){
		    			if(generatedItemsets.get(l).get(u) == itemsets.get(i)){
		    				listOfDates.add(times[itemsets.get(i)]);
		    			}
		    		}
		    	}
		    }
		    generatedTimeId.add(listOfDates);
		    generatedItemId.add(clusterId);
		    
		}	
	}
}
