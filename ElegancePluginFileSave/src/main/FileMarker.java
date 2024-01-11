package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
/**
 * This file takes in files and marks the files with the data provided where no data calculation is done in this file
 * @author repineap
 *
 */
public class FileMarker {
	
	/*
	 * Clears the markers on the given file with my labeled name
	 */
	public static void deleteMarkers(File file) {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(org.eclipse.core.runtime.Path.fromOSString(file.getAbsolutePath()));
        try {
        	resource.deleteMarkers("ElegancePlugin.methodMarker", true, IFile.DEPTH_INFINITE);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}
	
	/*
	 * Creates a marker on a file at the lineNumber with the message and severity connected
	 */
	public static void createMarker(File file, int lineNumber, String message, int severity) {
        // Get the IFile object corresponding to the File object
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(org.eclipse.core.runtime.Path.fromOSString(file.getAbsolutePath()));
        // Check if the IFile object is valid
        if (resource != null && resource.exists()) {
            try {
                // Create a marker on the IFile
            	IMarker myMarker = resource.createMarker("ElegancePlugin.methodMarker");
				myMarker.setAttribute(IMarker.SEVERITY, severity);
				myMarker.setAttribute(IMarker.MESSAGE, message);
				myMarker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
				int[] linePos = lineCharPositions(file, lineNumber);
				myMarker.setAttribute(IMarker.CHAR_START, linePos[0]);
				myMarker.setAttribute(IMarker.CHAR_END, linePos[1]);
				

                // Use the marker
            } catch (CoreException e) {
                e.printStackTrace();
                // Handle CoreException
            }
        } else {
            System.out.println("Invalid or non-existing IFile object.");
        }
    }
	
	/*
	 * Helper method used to calculate where to mark the file
	 */
	public static int[] lineCharPositions(File file, int lineNumber) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentLine = 1;
            int charStart = 0;
            int charEnd = 0;

            while ((line = reader.readLine()) != null) {
                charEnd = charStart + line.length() + 1;
                int firstP = line.indexOf('p');

                if (currentLine == lineNumber) {
                    return new int[] {charStart + firstP, charEnd};
                }

                charStart = charEnd + 1;
                currentLine++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
	}
}
