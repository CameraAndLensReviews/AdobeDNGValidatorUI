package org.cameraandlensreviews.workerThreads;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.AbstractQueue;

import org.cameraandlensreviews.Configuration;
import org.cameraandlensreviews.LocalLogManager;

/**
 * Worker thread to run the DNG Validation utility, interpret the output, and log to the logging system.
 * 
 * @author Tim
 * Released under GPL 3.0 http://www.gnu.org/licenses/gpl.txt
 */
public class WorkerThread implements Runnable {
	private String name;
	private LocalLogManager log;
	private AbstractQueue<String> queue;
	private Configuration config;
	
	private int initialQueueSize, queueSizePrintIncrement;
	private static int validationErrors = 0, validationWarnings = 0;
	
	public WorkerThread(String name, LocalLogManager localLogManagerTemp, AbstractQueue<String> fileQueueTemp,
			Configuration config) {
		this.name = name;
		this.log = localLogManagerTemp;
		this.queue = fileQueueTemp;
		this.config = config;
		
		initialQueueSize = queue.size();
		queueSizePrintIncrement = initialQueueSize / 10;
		if (queueSizePrintIncrement < 1) {
			queueSizePrintIncrement = 1;
		}
		if (queueSizePrintIncrement > 100) {
			queueSizePrintIncrement = 100;
		}
	}
	
	@Override
	public void run() {
		log.logGeneral("Thread " + name + " started");
		
		InputStream is = null;
	    InputStreamReader isr = null;
	    BufferedReader br = null;
		
		try {
			while (!queue.isEmpty()) {
				// Get a file off the queue to process
				String file = queue.remove();
				
				// Print progress
				int qs = queue.size();
				if (qs % queueSizePrintIncrement == 0) {
					System.out.println("Remaining queue size is " + qs + ". " + validationErrors + " files failed validation, " + validationWarnings + " files have warnings");
				}
				
				// Form and run the command to execute the Adobe utility
				ProcessBuilder builder = new ProcessBuilder(config.dngValidatorExecutable, file);
				builder.redirectErrorStream(true);
				Process process = builder.start();
				
				// Capture the output to determine if the file is well formed
				is = process.getInputStream();
			    isr = new InputStreamReader(is);
			    br = new BufferedReader(isr);
			    String line;
			    StringBuffer out = new StringBuffer(200);
			    while ((line = br.readLine()) != null) {
			      out.append(line);
			      out.append("\n");
			    }
			    
			    // Log the entire result
			    log.logGeneral(out.toString());
			    
			    // Analyse the output, logging errors
			    if (out.indexOf(config.errorMarkerStart) > 0) {
			    	int startPos = out.indexOf(config.errorMarkerStart) + config.errorMarkerStart.length();
			    	int endPos = out.indexOf(config.errorMarkerEnd, startPos);
			    	if (endPos == 0 || endPos < startPos) {
			    		endPos = out.length();
			    	}
			    	
			    	String errorMessage = out.substring(startPos, endPos);
			    	log.logValidationError(file + " : " + errorMessage.trim());
			    	
			    	// Work out if it was a validation failure or if it couldn't be validated
			    	if (errorMessage.indexOf(config.errorValidateFail) >= 0) {
			    		// Do some logging
			    		int p1 = file.lastIndexOf("\\") + 1;
			    		int p2 = file.length();
			    		log.logCsvFilenameNoPath(file.substring(p1,p2) + ",");
			    		log.logFilenamesWithPaths(file + "\n");
			    		
			    		// Copy the failed file to the output dir, if that option is turned on
			    		if (config.exportFailures) {
			    			copyFileToFailedDirectory(file);
			    		}
			    		
			    		// Count errors
			    		validationErrors++;
			    	}
			    	if (errorMessage.indexOf(config.errorValidateWarning) >= 0) {
			    		validationWarnings++;
			    	}
			    }
			}
			
		} catch (IOException e) {
			log.logGeneral(e.getMessage());
		} finally {
			try {
				br.close();
				isr.close();
				is.close();
			} catch (Exception e ){}
		}
		
		log.logGeneral("Thread " + name + " finished");
	}
	
	/**
	 * Copy the failed file to the output directory, preserving the directory tree structure.
	 * @param file
	 */
	private void copyFileToFailedDirectory(String sourceFileStr) {
		int subdirStart = config.inputDir.length();
		String targetFilename = config.faultyFileCopyDir + sourceFileStr.substring(subdirStart, sourceFileStr.length());
		String targetDirectoryName = targetFilename.substring(0, targetFilename.lastIndexOf("\\"));
		
		try {
			File sourceFile = new File(sourceFileStr);
			
			File targetDirectory = new File(targetDirectoryName);
			targetDirectory.mkdirs();
			File targetFile = new File(targetFilename);
			
			// Don't overwrite existing files
			while (targetFile.exists()) {
				int pos = targetFilename.lastIndexOf(".");
				String newtargetFilename = 
						targetFilename.substring(0, pos) + 					// File path up to the file extention .
						"_" +													// Make a new filename
						targetFilename.substring(pos, targetFilename.length()); // Add . and extension on
				targetFile = new File(newtargetFilename);
				targetFilename = newtargetFilename;
			}
			
			// Copy the file
			Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
		} catch (IOException e ) {
			// Oh well
		}
	}

	/**
	 * Returns validation failures.
	 * @return
	 */
	public static int getValidationErrors() {
		return validationErrors;
	}
	
	/**
	 * Returns how many files couldn't be processed. 
	 * @return
	 */
	public static int getValidationWarnings() {
		return validationWarnings;
	}
}
