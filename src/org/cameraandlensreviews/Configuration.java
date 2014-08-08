package org.cameraandlensreviews;

/**
 * Javabean to move a bunch of config info around easily.
 * 
 * @author Tim
 * Released under GPL 3.0 http://www.gnu.org/licenses/gpl.txt
 */
public class Configuration {
	public String errorMarkerStart = "*** Error:", errorMarkerEnd = "***";
	public String errorValidateFail = "RawImageDigest", errorValidateWarning = "DNGVersion";
	public String inputDir, faultyFileCopyDir;
	public boolean exportFailures;
	
	public String dngValidatorExecutable = "dng_validate.exe";
	public int threads = 2;
	

}
