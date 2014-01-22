package aurora.plugin.proc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import uncertain.logging.ILogger;

public class StringBufferLogger implements ILogger {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	StringBuffer sb;
	String topic;
	Level filterLevel = Level.CONFIG;

	public StringBufferLogger(StringBuffer sb,String topic) {
		this.sb = sb;
		this.topic = topic;
	}
	
	public StringBufferLogger(StringBuffer sb,String topic,Level level) {
		this.sb = sb;
		this.topic = topic;
		this.filterLevel = level;
	}

	public void log(String message) {
		log(Level.INFO,message,null,null);
	}

	public void log(String message, Object[] parameters) {
		log(Level.INFO,message,null,parameters);
	}

	public void log(Level level, String message) {
		log(level,message,null,null);
	}

	public void log(Level level, String message, Throwable thrown) {
		log(level,message,thrown,null);
	}

	public void log(Level level, String message, Object[] parameters) {
		log(level,message,null,parameters);
	}

	public void config(String message) {
		log(Level.CONFIG,message,null,null);
	}

	public void info(String message) {
		log(Level.INFO,message,null,null);
	}

	public void warning(String message) {
		log(Level.WARNING,message,null,null);
	}

	public void severe(String message) {
		log(Level.SEVERE,message,null,null);
	}

	public void setLevel(Level level) {
		this.filterLevel = level;
	}
	
	public void log(Level level, String message,Throwable thrown,Object[] parameters) {
		if(level.intValue()<filterLevel.intValue())
			return;
		String fullMessage = getPrefix(level)+message;
		if(parameters != null)
			fullMessage = MessageFormat.format(fullMessage,parameters);
		if(thrown != null)
			fullMessage = fullMessage+" "+getFullStackTrace(thrown);
		sb.append(fullMessage).append(LINE_SEPARATOR);
		System.out.println(fullMessage);
	}
	
	private String getFullStackTrace(Throwable exception) {
		String message = getExceptionStackTrace(exception);
		return message;
	}

	private String getExceptionStackTrace(Throwable exception) {
		if (exception == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(baos);
		exception.printStackTrace(pw);
		pw.close();
		return baos.toString();
	}

	private String getPrefix(Level level) {
		// 2014-01-09 16:34:53.465 [uncertain.core] [INFO]
		String format = "%s [%s] [%s] ";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
		String sysdate = dateFormat.format(new Date());
//		String prefix = sysdate+separator+"["+topic+"]"+separator+""
		String prefix = String.format(format,sysdate,topic,level.toString());
		return prefix;
	}

}
