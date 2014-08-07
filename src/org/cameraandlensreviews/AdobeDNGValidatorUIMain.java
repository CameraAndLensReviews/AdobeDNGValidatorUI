package org.cameraandlensreviews;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

import org.cameraandlensreviews.workerThreads.WorkerThread;

/**
 * A quick and dirty way to get the Adobe DNG validator to look at multiple files.
 * 
 * @author Tim
 * @version 0.1 beta
 * Released under GPL 3.0 http://www.gnu.org/licenses/gpl.txt
 */
public class AdobeDNGValidatorUIMain {
	private static int threads = -1;
	
	private LocalLogManager logManager = new LocalLogManager(); 
	private Configuration config = new Configuration();

	public static void main(String[] args) {
		AdobeDNGValidatorUIMain m = new AdobeDNGValidatorUIMain();
		m.start(args);
	}

	
	public void start(String[] args) {
		try {
			System.out.println("Starting Adobe DNG Validator UI");
			
			// Load and validate properties
			System.out.println(" -- Loading and validating configuration");
			loadAndValidateProperties(args);
			if (config.inputDir == null || config.inputDir.trim().length() == 0) {
				return;
			}
			System.out.println(" -- Configuration loaded and validated");
			
			// Build a list of files to validate
			System.out.println(" -- Finding files to process");
			AbstractQueue<String> queue = new ArrayBlockingQueue<String>(100000);
			findDNGFiles(queue, config.inputDir);
			
			int qs = queue.size();
			System.out.println("\n -- Files loaded. " + qs + " files found");
			
			if (qs == 0) {
				return;
			}
			
			// Keep track of the threads
			ArrayList<Thread> activeThreads = new ArrayList<Thread>(threads);
			
			// Start threads to do the work
			System.out.println(" -- Starting processing threads " + config.inputDir);
			logManager.logValidationError("\nStarting processing " + config.inputDir);
			for (int i = 0; i < threads; i++) {
				WorkerThread wt = new WorkerThread("Thread" + i, logManager, queue, config);
				
				Thread t = new Thread(wt);
				activeThreads.add(t);
				t.start();
			}
			System.out.println(" -- Processing threads started. This could take some time...");
			
			System.out.println("\nInitial queue size is " + qs);
			
			// Wait until all threads have finished
			long startTime = System.currentTimeMillis();
			for (Thread thread : activeThreads ) {
				thread.join();
			}
			
			System.out.println(" -- finished processing");
			logManager.logValidationError("\nFinished processing");
			
			long endTime = System.currentTimeMillis();
			
			long ms = (endTime - startTime);
			long sec = ms / 1000;
			float rate = qs / sec;
			
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(1);
			String rateStr = df.format(rate);
			
			int errors = WorkerThread.getValidationErrors();
			int warnings = WorkerThread.getValidationWarnings();
			
			String message = "Verified " + qs + " file(s) in " + sec + " seconds, " + rateStr + " files per second.\n" + errors + " file(s) failed validation, " + warnings + " file(s) couldn't be validated due to Adobe DNG library limitations"; 
			System.out.println(message);
			logManager.logGeneral(message);
			logManager.logValidationError(message);
			
		} catch (Exception e) {
			logManager.logGeneral(e.getMessage());
			logManager.logGeneral("Program exiting");
			
			System.err.println(e.getMessage());
			System.err.println("Program exiting");
		} finally {
			System.out.println("\n\nEnding Adobe DNG Validator UI");
		}
	}
	
	public void loadAndValidateProperties(String[] args) throws Exception {
		// Check the config file exists
		File f = new File("config.properties");
		if (!f.exists()) {
			System.out.println("The config file 'config.properties' does not exist. Please retrieve this file from the distribution.");
			return;
		}
		
		// Load and validated properties
		Properties prop = new Properties();
		InputStream input = new FileInputStream("config.properties");
		
		// get the property value and print it out
		try {
			logManager.logGeneral("Opening configuration file");
			// load a properties file
			prop.load(input);
			
			logManager.logGeneral("Reading configuration");
			config.dngValidatorExecutable = prop.getProperty("adobe.dng.validator.executable");
			config.inputDir = prop.getProperty("inputDir");
			config.errorMarkerStart = prop.getProperty("errorMarkerStart");
			config.errorMarkerEnd = prop.getProperty("errorMarkerEnd");
			config.errorValidateFail = prop.getProperty("errorValidateFail");
			config.errorValidateWarning = prop.getProperty("errorValidateWarning");
			
			String dir = prop.getProperty("faultyFileCopyDir");
			if (dir != null && dir.length() > 0) {
				config.faultyFileCopyDir = prop.getProperty("faultyFileCopyDir");
				config.exportFailures = true;
			} else {
				config.faultyFileCopyDir = null;
				config.exportFailures = false;
			}
			
			threads = Integer.parseInt(prop.getProperty("threads"));
			if (threads < 1 || threads > 32) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			throw new IOException("Threads must be an integer number between 1 and 32");
		} catch (IOException ex) {
			throw new IOException("Problem loading properties from file");
		} finally {
			input.close();
		}
		
		// Check the parameters
		File dngValidator = new File(config.dngValidatorExecutable);
		if (!dngValidator.exists()) {
			throw new IOException("Adobe DNG Validator executable not found. See the config file for details of where to download it");
		}
		
		File inputDirF = new File(config.inputDir);
		if (!inputDirF.exists()) {
			throw new IOException("Input directory not found. Did you escape backslashes? eg c:\\temp\\directory");
		}
		if (!inputDirF.isDirectory()) {
			throw new IOException("Input directory is not a directory");
		}
		
		if (config.errorMarkerStart == null || config.errorMarkerStart.length() == 0) {
			throw new IOException("Error marker start not set");
		}
		
		if (config.errorMarkerEnd == null || config.errorMarkerEnd.length() == 0) {
			throw new IOException("Error marker end not set");
		}
		
		if (config.errorValidateFail == null || config.errorValidateFail.length() == 0) {
			throw new IOException("Error marker failure string end not set");
		}
		
		if (config.errorValidateWarning == null || config.errorValidateWarning.length() == 0) {
			throw new IOException("Error marker warning string end not set");
		}
		
		logManager.logGeneral("Configuration file loaded succesfully");
	}
	
	private void findDNGFiles(AbstractQueue<String> queue, String path) {
		System.out.print(".");
		File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
            	findDNGFiles(queue, f.getAbsolutePath());
            }
            else {
            	if (f.getAbsolutePath().toString().endsWith("dng")) {
            		queue.add(f.getAbsolutePath().toString());
            	}
            }
        }
	}
	
	

}
