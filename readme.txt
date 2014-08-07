DNG Validator UI Tool 0.1 beta
------------------------------
This tool is a simple shell around the dng_validate tool that Adobe provides. It recursively verifies the
integrity of DNG files under a root directory. It has a variety of outputs:
 - A verbose log (generalLog.log)
 - A log of validation errors and warnings (validationErrors.log)
 - A concise list of filenames that failed validation(validationErrors.csv)
 - A longer list of filesnames with paths that failed validation (validationErrorsFilesnames.log)
 - The failed files  can optionally be copied to a directory of your choice

The software is an early beta and will have bugs. It was tested only on windows systems. Bugs can be reported to dng@cameraandlensreviews.org or perhaps via the GitHub project.

Installation
------------
1. Unzip this tool into a directory of your choice. Log files will be created in the log subdirectory of where you run it from.
2. Download Java 7 or higher (http://java.com/en/download/index.jsp)
3. Put the Java executable onto your path 
   Add the directory where your java.exe file is to (Control Panel\All Control Panel Items\System\Advanced System Settings\Environment Variables\System Variables -> PATH)
4. Download the Adobe DNG SDK (see below) and put the executable file into the same directory as this tool.
5. Edit the config.properties file to tell it where 
6. Run with a command like this (this will change as the version number goes up)

java -jar AdobeDNGValidatorUI-0.1beta.jar

Adobe DNG SDK
-------------
You must download the Adobe DNG SDK separately. It's available by searching for "Adobe DNG SDK 1.4",
though 1.5 will no doubt come out at some point.
http://www.adobe.com/support/downloads/thankyou.jsp?ftpID=5475&fileID=5484
http://www.adobe.com/support/downloads/detail.jsp?ftpID=5474


Licenses
--------
Please see the licenses for the various libraries used in the licenses subdirectory.
This tool is released under the GPL 3.0 license.