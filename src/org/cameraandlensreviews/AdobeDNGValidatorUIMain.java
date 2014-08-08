package org.cameraandlensreviews;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A quick and dirty way to get the Adobe DNG validator to look at multiple files.
 * 
 * @author Tim
 * @version 0.1 beta
 * Released under GPL 3.0 http://www.gnu.org/licenses/gpl.txt
 */
public class AdobeDNGValidatorUIMain {
	private LocalLogManager logManager = new LocalLogManager(); 
	//private Configuration config = new Configuration();

	public static void main(String[] args) {
		System.out.println("Starting Adobe DNG Validator UI");
		
		AdobeDNGValidatorUIMain m = new AdobeDNGValidatorUIMain();
		Configuration config = m.start(args);
		
		if (config != null) {
			AdobeDNGValidatorWorkflow w = new AdobeDNGValidatorWorkflow();
			w.coreWorkflow(config);
		} else {
			System.out.println("\n\nUsage: AdobeDNGValidatorUIMain [inputDir] [dir for faulty files]");
			System.out.println("On windows double escape backslashes: c:\\\\temp\\\\directoryName");
		}
		
		System.out.println("\n\nEnding Adobe DNG Validator UI");
	}

	
	public Configuration start(String[] args) {
		try {
			// Load and validate properties
			System.out.println(" -- Loading and validating configuration");
			Configuration config = loadAndValidateProperties();
			
			if (config.inputDir == null || config.inputDir.trim().length() == 0) {
				System.out.println(" -- No configuration found, using defaults and command line parameters");
			} else {
				System.out.println(" -- Configuration loaded and validated");
			}
			
			// Load command line arguments
			loadCommandLineArgs(args, config);
			
			// Make sure we have at least an input dir
			if (config.inputDir == null) {
				return null;
			}
			if (config.faultyFileCopyDir == null || config.faultyFileCopyDir.trim().length() == 0) {
				System.out.println("No output directory has been configured or specified, faulty files will not be copied");
			}
		
			return config;
		} catch (Exception e) {
			logManager.logGeneral(e.getMessage());
			logManager.logGeneral("Program exiting");
			
			System.err.println(e.getMessage());
			System.err.println("Program exiting");
		} 
		
		return null;
	}
	
	/**
	 * Load the command line arguments
	 * 
	 * @param args
	 * @param config
	 */
	private void loadCommandLineArgs(String[] args, Configuration config) {
		if (args == null || args.length == 0) {
			return;
		}
		
		// Load the input directory
		if (args.length >= 1 && args[0].trim().length() > 0) {
			File testFile = new File(args[0]);
			if (testFile.exists() && testFile.isDirectory()) {
				config.inputDir = args[0];
			}
		}
		
		// Load the output directory
		if (args.length >= 2 && args[1].trim().length() > 0) {
			File testFile = new File(args[1]);
			if (testFile.exists() && testFile.isDirectory()) {
				config.faultyFileCopyDir = args[1];
			}
		}
	}


	public Configuration loadAndValidateProperties() throws Exception {
		Configuration config = new Configuration();
		
		// Check the config file exists
		File f = new File("config.properties");
		if (!f.exists()) {
			System.out.println("The config file 'config.properties' does not exist. Please retrieve this file from the distribution if possible.");
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
			
			config.threads = Integer.parseInt(prop.getProperty("threads"));
			if (config.threads < 1 || config.threads > 32) {
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
			//throw new IOException("Input directory not found. Did you escape backslashes? eg c:\\\\temp\\\\directory");
			System.out.println("No input directory found in configuration file. Command line still to be checked.");
		}
		if (inputDirF.exists() && !inputDirF.isDirectory()) {
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
		
		return config;
	}
}
