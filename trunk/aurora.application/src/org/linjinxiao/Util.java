package org.linjinxiao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class Util {

	public String getExceptionMessage(Exception e){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(baos);
		e.printStackTrace(pw);
		pw.close();
		return baos.toString();
	}
	public void logToFile(String message) {
		File logFile = new File("youFileName.log");
		PrintStream out = null;
		try {
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			out = new PrintStream(new FileOutputStream(logFile, true));
			out.println(new Date() + message);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
	}
}
