package org.cameraandlensreviews;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Wrapped for an asynchronous logging system, log4j v2.
 * 
 * @author Tim
 * Released under GPL 3.0 http://www.gnu.org/licenses/gpl.txt
 */
public class LocalLogManager {
	static final Logger generalLogger = LogManager.getLogger("org.cameraandlensreviews.general");
	static final Logger errorLogger = LogManager.getLogger("org.cameraandlensreviews.error");
	static final Logger csvFilenamesLogger = LogManager.getLogger("org.cameraandlensreviews.csvErrorFilenames");
	static final Logger filenamesWithPathsLogger = LogManager.getLogger("org.cameraandlensreviews.errorFilenamesWithPaths");
	
	public void logValidationError(String message) {
		errorLogger.error(message);
	}
	
	public void logGeneral(String message) {
		generalLogger.error(message);
	}
	
	public void logCsvFilenameNoPath(String message) {
		csvFilenamesLogger.error(message);
	}
	
	public void logFilenamesWithPaths(String message) {
		filenamesWithPathsLogger.error(message);
	}
}
