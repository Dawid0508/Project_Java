package project;

import org.apache.logging.log4j.*;
import java.net.URL;

public class Logging {
	
	private static Logger logger;
	
	public Logging(Class<?> clazz) {
		Logging.logger = LogManager.getLogger(clazz);
	}
	
	public void logFatal(String message, String url) {
		if(url != null) {
			logger.fatal(message + " " + url);
		}else {
			logger.fatal(message);
		}
	}
	
	public void logWarn(String message) {
		logger.warn(message);
	}
	
	public void logError(String message, Throwable throwable) {
		logger.error(message, throwable);
	}
	
	public void logError(String message) {
		logger.error(message);
	}
	
	public void logInfo(String message) {
		logger.info(message);
	}
	
	public void logDebug(String message) {
		logger.debug(message);
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	
	public static void main(String[] args) {
		//Przyklad uzycia
		Logging logging = new Logging(Logging.class);
		
		//Exception e = new Exception("");
		logging.logFatal("Fatal message", null);
		//logging.logError("Error message", e);
		logging.logError("Error message");
		logging.logWarn("Warning message");
		logging.logInfo("Informational message");
		logging.logDebug("Debug message");

	}
}
