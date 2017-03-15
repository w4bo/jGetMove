package fr.jgetmove.jgetmove;

import java.io.IOException;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.io.Input;

public class Main {
    public static void main(String[] args) {

        Input inputObj = new Input("assets/test.dat");
        Input inputTime = new Input("assets/testtimeindex.dat");

        Database database;
        
		try {
			
			database = new Database(inputObj, inputTime);
			System.out.println(database);
			
		} catch (IOException | ClusterNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

        
    }
}