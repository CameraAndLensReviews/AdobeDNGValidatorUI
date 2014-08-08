package org.cameraandlensreviews;

import java.io.File;
import java.text.DecimalFormat;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import org.cameraandlensreviews.workerThreads.WorkerThread;

/**
 * Core workflow of the program. Split out from the UI so a GUI can be built at some point.
 * 
 * @author Tim
 */
public class AdobeDNGValidatorWorkflow {
	private LocalLogManager logManager = new LocalLogManager();
	
	public void coreWorkflow(Configuration config) {
		// Build a list of files to validate
		try {
			System.out.print(" -- Finding files to process");
			AbstractQueue<String> queue = new ArrayBlockingQueue<String>(100000);
			findDNGFiles(queue, config.inputDir);
			
			int qs = queue.size();
			System.out.println("\n -- Files loaded. " + qs + " files found");
			
			if (qs == 0) {
				return;
			}
			
			// Keep track of the threads
			ArrayList<Thread> activeThreads = new ArrayList<Thread>(config.threads);
			
			// Start threads to do the work
			System.out.println(" -- Starting processing threads " + config.inputDir);
			logManager.logValidationError("Starting processing " + config.inputDir);
			for (int i = 0; i < config.threads; i++) {
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
			
			System.out.println("\n -- finished processing");
			logManager.logValidationError("Finished processing");
			
			long endTime = System.currentTimeMillis();
			
			long ms = (endTime - startTime);
			long sec = ms / 1000;
			float rate = sec / qs;
			
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(1);
			String rateStr = df.format(rate);
			
			int errors = WorkerThread.getValidationErrors();
			int warnings = WorkerThread.getValidationWarnings();
			
			String message = "Verified " + qs + " file(s) in " + sec + " seconds, " + rateStr + " seconds per file.\n" + errors + " file(s) failed validation, " + warnings + " file(s) couldn't be validated due to Adobe DNG library limitations"; 
			System.out.println(message);
			logManager.logGeneral(message);
			logManager.logValidationError(message);
			
		} catch (Exception e) {
			logManager.logGeneral(e.getMessage());
			logManager.logGeneral("Program exiting");
			
			System.err.println(e.getMessage());
			System.err.println("Program exiting");
		} 
	}
	
	/**
	 * Recursive method to find DNG files
	 * @param queue
	 * @param path
	 */
	private void findDNGFiles(AbstractQueue<String> queue, String path) {
		System.out.print(".");
		File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
            	findDNGFiles(queue, f.getAbsolutePath());
            }
            else {
            	if (f.getAbsolutePath().toString().toLowerCase().endsWith("dng")) {
            		queue.add(f.getAbsolutePath().toString());
            	}
            }
        }
	}
}
