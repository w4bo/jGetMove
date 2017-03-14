package fr.jgetmove.jgetmove.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Input {
	
	String filePath;

	BufferedReader reader;

	public Input(String filePath){
		
		this.filePath = filePath;
		
		try {
			reader = new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String read() throws IOException{
		if (reader != null){
			return reader.readLine();
		}
		else return null;
	}
}
