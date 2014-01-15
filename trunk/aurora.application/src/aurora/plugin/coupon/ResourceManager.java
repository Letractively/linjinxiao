package aurora.plugin.coupon;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.sql.DataSource;
import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

public class ResourceManager implements ILifeCycle{
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String PLUGIN = ResourceManager.class.getCanonicalName();
	
	public static final int SUMMIT_OK = 0;
	public static final String RECEIVE_OK = "DELIVRD";
	
	
	private IObjectRegistry mRegistry;
	private DataSource dataSource;
	private ILogger fileLogger;
	
	
	public ResourceManager(IObjectRegistry registry){
		this.mRegistry = registry;
		startup();
	}
	public Connection getConnection() {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		if (connection == null)
			throw new IllegalStateException("Can't get database connection from dataSource.");
		return connection;
	}

	public void rollbackConnection(Connection dbConn) {
		if (dbConn == null)
			return;
		try {
			dbConn.rollback();
		} catch (SQLException ex) {
			fileLogger.log(Level.SEVERE, "", ex);
		}
	}

	public void closeConnection(Connection conn) {
		if (conn == null)
			return;
		try {
			conn.close();
		} catch (SQLException ex) {
			fileLogger.log(Level.SEVERE, "", ex);
		}
	}

	@Override
	public boolean startup() {
		fileLogger = LoggingContext.getLogger(PLUGIN, mRegistry);
		return true;
	}
	public void onInitialize() {
		dataSource = (DataSource) mRegistry.getInstanceOfType(DataSource.class);
		if (dataSource == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, DataSource.class, this.getClass().getName());
	}
	public ILogger getFileLogger(){
		return fileLogger;
	}
	@Override
	public void shutdown() {
		
	}

	public static byte[] fiterBinaryZero(byte[] bytes) {
		byte[] returnBytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			returnBytes[i] = bytes[i];
		}
		return returnBytes;
	}
	public static String getFullStackTrace(Throwable exception) {
		String message = getExceptionStackTrace(exception);
		return message;
	}

	public static String getExceptionStackTrace(Throwable exception) {
		if (exception == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(baos);
		exception.printStackTrace(pw);
		pw.close();
		return baos.toString();
	}
}
