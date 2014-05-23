package aurora.plugin.resource;

import java.io.File;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import aurora.database.rsconsumer.CompositeMapCreator;

public class FileDeleter extends CompositeMapCreator {
	String baseDir;
	String filePath;
	boolean continueAfterError = true;
	private IObjectRegistry register;
	private ILogger logger;

	public FileDeleter(IObjectRegistry register) {
		this.register = register;
	}

	public void endRow() {
		String parsedFilePath = TextParser.parse(filePath, currentRecord);
		File targetFile;
		if (baseDir != null)
			targetFile = new File(baseDir, parsedFilePath);
		else
			targetFile = new File(parsedFilePath);
		try {
			String fileFullPath = targetFile.getCanonicalPath();
			if (!targetFile.exists()) {
				if (continueAfterError) {
					logger.severe("targetFile:" + fileFullPath + " is not exits!");
					return;
				} else
					throw new IllegalArgumentException("targetFile:" + fileFullPath + " is not exits!");

			}
			boolean success = targetFile.delete();
			if (!success) {

				if (continueAfterError) {
					logger.severe("delete targetFile:" + fileFullPath + " failed!");
					return;
				} else {
					throw new IllegalArgumentException("delete targetFile:" + fileFullPath + " failed!");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void beginConfigure(CompositeMap config) {
		super.beginConfigure(config);

	}

	public void endConfigure() {
		if (filePath == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this, "filePath");
		logger = LoggingContext.getLogger(this.getClass().getCanonicalName(), register);
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isContinueAfterError() {
		return continueAfterError;
	}

	public boolean getContinueAfterError() {
		return continueAfterError;
	}

	public void setContinueAfterError(boolean continueAfterError) {
		this.continueAfterError = continueAfterError;
	}
}
