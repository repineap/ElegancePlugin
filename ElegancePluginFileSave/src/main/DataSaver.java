package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * In charge of the saving of data which gets passed to it from the FileAnalyzer to the src/TestResults/{functionName}-compdata.txt
 * @author repineap
 *
 */
public class DataSaver {
	String folderPath;
	String filePath;
	
	public DataSaver(String funcName, String srcPath) {
		folderPath = srcPath + "TestResults" + File.separator;
		filePath = folderPath + funcName + "-compdata.txt";

        // Create a File object representing the folder
        File folder = new File(folderPath);

        // Create the folder if it doesn't exist
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
            	System.err.println("Failed to create folder");
                return;
            }
        }
	}
	
	public boolean writeData(String data) {
		PrintWriter writer;
		
		try {
			writer = new PrintWriter(new FileWriter(filePath, true));
			writer.println(data);
			writer.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
