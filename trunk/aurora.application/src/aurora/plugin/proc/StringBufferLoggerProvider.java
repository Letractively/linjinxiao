package aurora.plugin.proc;

import uncertain.logging.ILogger;
import uncertain.logging.ILoggerProvider;

public class StringBufferLoggerProvider implements ILoggerProvider{

	private static ThreadLocal<StringBuffer> stringBufferThreadLocal = new ThreadLocal<StringBuffer>();
	
	public static StringBuffer getCurrentStringBuffer() {
		return (StringBuffer) stringBufferThreadLocal.get();
	}

	public static void setCurrentStringBuffer(StringBuffer sb) {
		stringBufferThreadLocal.set(sb);
	}

	public static void remove() {
		stringBufferThreadLocal.remove();
	}
	
	
	@Override
	public ILogger getLogger(String topic) {
		StringBuffer sb = StringBufferLoggerProvider.getCurrentStringBuffer();
		return new StringBufferLogger(sb, topic);
	}

}
