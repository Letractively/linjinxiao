package aurora.plugin.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.logging.Level;

import uncertain.composite.CompositeMap;
import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import uncertain.ocm.ObjectRegistryImpl;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import aurora.service.IServiceFactory;
import aurora.service.ServiceInvoker;

public class AutoGPS extends AbstractLocatableObject implements ILifeCycle {
	public static final String PLUGIN = AutoGPS.class.getCanonicalName();

	public final static String LOGIN_COMMAND = "01";
	public final static String KEEP_CONNECTION_ALIVE_COMMAND = "02";
	public final static String LOGIN_RESP_COMMAND = "51";
	public final static String KEEP_CONNECTION_ALIVE_RESP_COMMAND = "52";
	public final static String DEFAULT_CHARSETNAME = "US-ASCII";
	public final static String SEPARATOR = "|";
	public final static int DEFAULT_READINTERVAL = 0;
	public final static int DEFAULT_WRITEINTERVAL = 45000;

	private IObjectRegistry registry;
	private IProcedureManager procedureManager;
	private IServiceFactory serviceFactory;
	private ILogger logger;

	Socket gpsClientSocket;

	InputStream socketReader;
	OutputStream socketWriter;

	String serverIP;
	int serverPort;
	String userName;
	String password;
	String procName;
	String charsetName = DEFAULT_CHARSETNAME;
	int readInterval = DEFAULT_READINTERVAL;
	int writeInterval = DEFAULT_WRITEINTERVAL;
	boolean startNow = true;
	boolean isRunning = true;
	boolean restart = false;

	Thread initThread;
	SocketReaderThread readerThread;
	SocketWriterThread writerThread;
	private int reconnectTime = 60000;// 1 minute 60000
	private int maxReconnectTime = 3600000;// 1 hour 3600000
	private ConnectionMonitor connMonitor;

	public AutoGPS(IObjectRegistry registry) {
		this.registry = registry;
		procedureManager = (IProcedureManager) registry.getInstanceOfType(IProcedureManager.class);
		if (procedureManager == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, IProcedureManager.class, this.getClass().getName());
		serviceFactory = (IServiceFactory) registry.getInstanceOfType(IServiceFactory.class);
		if (serviceFactory == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, IServiceFactory.class, this.getClass().getName());
		init();
	}

	public AutoGPS(IObjectRegistry registry, IProcedureManager procedureManager, IServiceFactory serviceFactory) {
		this.registry = registry;
		this.procedureManager = procedureManager;
		this.serviceFactory = serviceFactory;
		init();

	}

	private void init() {
		logger = LoggingContext.getLogger(PLUGIN, registry);
//		logger = new ConsoleLogger(PLUGIN);
		connMonitor = new ConnectionMonitor(this);
//		monitor.setDaemon(true);
		connMonitor.start();
	}

	@Override
	public boolean startup() {
		if (startNow) {
			start();
		}
		return true;
	}

	public synchronized void start() {
		initThread = new Thread() {
			public void run() {
				try {
					InetAddress addr = InetAddress.getByName(serverIP);
					gpsClientSocket = new Socket();
					InetSocketAddress socketAddress = new InetSocketAddress(addr, serverPort);
					gpsClientSocket.connect(socketAddress, 30000);
					logger.log(Level.INFO, "connect successful.");
					socketReader = gpsClientSocket.getInputStream();
					socketWriter = gpsClientSocket.getOutputStream();
					if (login()) {
						registry.registerInstance(AutoGPS.class, AutoGPS.this);
						readerThread = new SocketReaderThread();
						writerThread = new SocketWriterThread();
						readerThread.start();
						writerThread.start();
					}
					restart = false;
				} catch (Exception e) {
					restart = true;
					logger.log(Level.SEVERE, "init socket failed!", e);
				}
			}
		};
		initThread.start();
	}

	public static void start(IObjectRegistry registry) throws Exception {
		AutoGPS autoGPS = (AutoGPS) registry.getInstanceOfType(AutoGPS.class);
		if (autoGPS == null) {
			autoGPS = new AutoGPS(registry);
			autoGPS.start();
		} else if (!autoGPS.isRunning) {
			autoGPS.start();
		}
	}

	public static void shutdown(IObjectRegistry registry) throws Exception {
		AutoGPS autoGPS = (AutoGPS) registry.getInstanceOfType(AutoGPS.class);
		if (autoGPS != null) {
			autoGPS.shutdown();
		}
	}

	private boolean login() throws Exception {
		if (socketWriter == null)
			throw new IllegalStateException("socket reader is not avaliable!");
		String sequence = String.valueOf(System.currentTimeMillis());
		// @024|1234|01||test,test.pwd|
		String contentFormat = "|{0}|{1}||{2},{3}|";
		String loginContent = MessageFormat.format(contentFormat, sequence, LOGIN_COMMAND, userName, password);
		int length = loginContent.length();
		if (length > 10000)
			throw new IllegalArgumentException(" loginContent=" + loginContent + "'s length big than 10000");
		String fullFormat = "@%03d%s";
		String full = String.format(fullFormat, length, loginContent);
		logger.log(Level.CONFIG, "login string:" + full);
		byte[] loginBytes = full.getBytes(charsetName);
		socketWriter.write(loginBytes);
		socketWriter.flush();
		String receiveContent = null;
		try {
			receiveContent = inputStream2String(socketReader);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "", e);
		}
		if (receiveContent != null && !"".equals(receiveContent)) {
			String[] args = receiveContent.split("@");
			for (String str : args) {
				if (str != null && !"".equals(str)) {
					String[] infoSegments = str.split("\\|");
					if (LOGIN_RESP_COMMAND.equals(infoSegments[2])) {
						String sucess = "0";
						if (!sucess.equals(infoSegments[infoSegments.length - 1])) {
							logger.severe("login failed!");
						} else {
							logger.log(Level.INFO, "login successful.");
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	class SocketReaderThread extends Thread {
		public void run() {
			while (isRunning&&!restart) {
				String receiveContent = null;
				try {
					receiveContent = inputStream2String(socketReader);
				} catch (IOException e) {
					logger.log(Level.SEVERE, "", e);
					restart = true;
					break;
				}
				if (receiveContent != null && !"".equals(receiveContent)) {
					logger.log(Level.CONFIG, "receive:" + receiveContent);
					String[] args = receiveContent.split("@");
					for (String str : args) {
						if (str != null && !"".equals(str)) {
							String[] infoSegments = str.split("\\|");
							if (KEEP_CONNECTION_ALIVE_RESP_COMMAND.equals(infoSegments[2])) {
								String sucess = "0";
								if (!sucess.equals(infoSegments[infoSegments.length - 1])) {
									logger.severe("Connection is break. Try to reconnetion..");
									try {
										login();
									} catch (Exception e) {
										logger.log(Level.SEVERE, "", e);
									}
								}
							} else {
								executeProc(procName, receiveContent);
							}
						}
					}
				}
				try {
					Thread.sleep(readInterval);
				} catch (InterruptedException e) {

				}
			}
		}
	}

	protected void executeProc(String procedure_name, String gps_info) {
		logger.log(Level.CONFIG, "load procedure:{0}", new Object[] { procedure_name });
		Procedure proc = procedureManager.loadProcedure(procedure_name);
		executeProc(gps_info, proc);
	}

	protected void executeProc(String gps_info, Procedure proc) {
		if (proc == null)
			throw new IllegalArgumentException("Procedure can not be null!");
		try {
			CompositeMap fakeContext = new CompositeMap("context");
			String name = "gps." + gps_info;
			fakeContext.putObject("/parameter/@gps_info", gps_info, true);
			fakeContext.putObject("/parameter/@info", gps_info, true);
			fakeContext.putObject("/session/@user_id", -1, true);
			ServiceInvoker.invokeProcedureWithTransaction(name, proc, serviceFactory, fakeContext);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	class SocketWriterThread extends Thread {
		public void run() {
			// @015|1235|02||test|
			String sequence = String.valueOf(System.currentTimeMillis());
			String contentFormat = "|{0}|{1}||{2}|";
			String keepContent = MessageFormat.format(contentFormat, sequence, KEEP_CONNECTION_ALIVE_COMMAND, userName);
			int length = keepContent.length();
			String fullFormat = "@%03d%s";
			String full = String.format(fullFormat, length, keepContent);
			logger.log(Level.CONFIG, "keep string:" + full);
			while (isRunning&&!restart) {
				try {
					byte[] keepBytes = full.getBytes(charsetName);
					socketWriter.write(keepBytes);
					socketWriter.flush();
				} catch (Exception e) {
					logger.log(Level.SEVERE, "", e);
				}
				try {
					Thread.sleep(writeInterval);
				} catch (InterruptedException e) {

				}
			}
		}
	}

	public String inputStream2String(InputStream is) throws IOException {
		byte[] bytes = new byte[1024];
		int len = is.read(bytes);
		if (len < 0)
			return null;
		String result = new String(bytes, 0, len, Charset.forName(charsetName));
		return result;
	}

	@Override
	public void shutdown() {
		isRunning = false;
		if (connMonitor != null) {
			connMonitor.interrupt();
		}
		shutdownIntern();
	}

	private void shutdownIntern() {
		if (initThread != null)
			initThread.interrupt();
		if (readerThread != null) {
			readerThread.interrupt();
		}
		if (writerThread != null) {
			writerThread.interrupt();
		}
		if (socketWriter != null) {
			try {
				socketWriter.close();
			} catch (IOException e) {
			}
		}
		if (socketReader != null) {
			try {
				socketReader.close();
			} catch (IOException e) {
			}
		}
		try {
			gpsClientSocket.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean getStartNow() {
		return startNow;
	}

	public boolean isStartNow() {
		return startNow;
	}

	public void setStartNow(boolean startNow) {
		this.startNow = startNow;
	}

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public int getReadInterval() {
		return readInterval;
	}

	public void setReadInterval(int readInterval) {
		this.readInterval = readInterval;
	}

	public int getWriteInterval() {
		return writeInterval;
	}

	public void setWriteInterval(int writeInterval) {
		this.writeInterval = writeInterval;
	}

	public String getProcName() {
		return procName;
	}

	public void setProcName(String procName) {
		this.procName = procName;
	}

	public int getReconnectTime() {
		return reconnectTime;
	}

	public void setReconnectTime(int reconnectTime) {
		this.reconnectTime = reconnectTime;
	}

	public int getMaxReconnectTime() {
		return maxReconnectTime;
	}

	public void setMaxReconnectTime(int maxReconnectTime) {
		this.maxReconnectTime = maxReconnectTime;
	}

	class ConnectionMonitor extends Thread {

		private AutoGPS gps;
		private int minReconnectTime;
		private int maxReconnectTime;

		private int nextReconnectTime = 0;

		public ConnectionMonitor(AutoGPS gps) {
			this.gps = gps;
			this.minReconnectTime = gps.getReconnectTime();
			this.maxReconnectTime = gps.getMaxReconnectTime();
		}

		public void run() {
			while (gps.isRunning) {
				if (!gps.restart) {
					nextReconnectTime = minReconnectTime;
					sleepOneSecond();
				} else {
					shutdownIntern();
					startServer();
				}
			}
		}

		private void sleepOneSecond() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		private void startServer() {
			int thisReconnectTime = computeConnectTime();
			try {
				Thread.sleep(thisReconnectTime);
			} catch (InterruptedException e) {
			}
			gps.start();
		}

		private int computeConnectTime() {
			int thisReconnectTime = nextReconnectTime;
			if (thisReconnectTime == 0)
				nextReconnectTime = minReconnectTime;
			else {
				if (nextReconnectTime < maxReconnectTime) {
					if (nextReconnectTime * 2 <= maxReconnectTime) {
						nextReconnectTime = nextReconnectTime * 2;
					} else {
						nextReconnectTime = maxReconnectTime;
					}
				}
			}
			return thisReconnectTime;
		}
	}

	public static void main(String[] args) throws Exception {
		IObjectRegistry registry = new ObjectRegistryImpl();
		AutoGPS gps = new AutoGPS(registry, null, null);
		gps.setServerIP("222.66.200.66");
		gps.setServerPort(5556);
		gps.setUserName("7492");
		gps.setPassword("7492.pwd");
		gps.start();
		// Thread.sleep(10000);
		//
		// System.out.println("shutdown");
		// AutoGPS.shutdown(registry);
		// Thread.sleep(50000);
		// System.out.println("start");
		// AutoGPS.start(registry);
		// Thread.sleep(10000);
		// System.out.println("shutdown");
		// AutoGPS.shutdown(registry);
		// Thread.sleep(50000);
		// System.out.println("start");
		// AutoGPS.start(registry);
	}

}
