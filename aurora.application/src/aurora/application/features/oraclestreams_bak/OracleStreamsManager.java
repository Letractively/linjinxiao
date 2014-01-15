package aurora.application.features.oraclestreams_bak;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import oracle.AQ.AQDequeueOption;
import oracle.AQ.AQDriverManager;
import oracle.AQ.AQMessage;
import oracle.AQ.AQQueue;
import oracle.AQ.AQSession;
import aurora.database.DBUtil;
import aurora.datasource.DataSourceConfig;
import aurora.datasource.DatabaseConnection;

import uncertain.core.IGlobalInstance;
import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;

 //应用无法或的context
public class OracleStreamsManager extends AbstractLocatableObject implements IGlobalInstance, ILifeCycle {
	private  List<IOracleStreamsListener> listeners = new LinkedList<IOracleStreamsListener>();
	IObjectRegistry mRegistry;
	ILogger logger;
	private String queueName = "JMS_QUEUE";
	private String jdbcDriver = "oracle.jdbc.driver.OracleDriver";
	private String aqDriver = "oracle.AQ.AQOracleDriver";
	public OracleStreamsManager(IObjectRegistry registry){
		this.mRegistry = registry;
	}
	
	@Override
	public boolean startup() {
		logger = LoggingContext.getLogger(this.getClass().getCanonicalName(), mRegistry);
		(new Thread() {
			public void run() {
				try {
					startStreams();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
		return true;
	}
	private void startStreams() throws Exception{
		Connection connection = null;
		try {
			DataSourceConfig ds = (DataSourceConfig) mRegistry
					.getInstanceOfType(DataSourceConfig.class);
			if (ds == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(
						this, DataSourceConfig.class, this.getClass()
								.getCanonicalName());
			DatabaseConnection[] connConfigs = ds.getDatabaseConnections();
			if(connConfigs == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(
						this, DatabaseConnection.class, this.getClass().getCanonicalName());
			Class.forName(jdbcDriver);
			for(int i=0;i<connConfigs.length;i++){
				DatabaseConnection connConfig = connConfigs[i];
				if(jdbcDriver.equals(connConfig.getDriverClass())){
					connection = DriverManager.getConnection(connConfig.getUrl(), connConfig.getUserName(),connConfig.getPassword());
					break;
				}
			}
			if(connection == null)
				throw new RuntimeException(" No "+jdbcDriver+" type connection is found!");
			connection.setAutoCommit(false);
			// Loads the Oracle AQ driver
			Class.forName(aqDriver);
			AQSession session = AQDriverManager.createAQSession(connection);
			AQQueue queue = session.getQueue(connection.getMetaData()
					.getUserName(), queueName); // Queue name
			// Dequeue
			AQDequeueOption dequeueOption = new AQDequeueOption();
			dequeueOption.setDequeueMode(AQDequeueOption.DEQUEUE_REMOVE);
			while (true) {
				AQMessage message = queue.dequeue(dequeueOption,
						Streams_dml_lcr.getORADataFactory());
				Object payload = message.getObjectPayload().getPayloadData();
				Streams_dml_lcr lcr = (Streams_dml_lcr) payload;
				for(IOracleStreamsListener listener:listeners){
					try{
						listener.notice(lcr);
					}catch(Exception e){
						logger.log(Level.SEVERE,"OracleStreams Listener:"+listener.getName()+" throw excepiton.",e);
					}
				}
				connection.commit();
			}
		} finally {
			DBUtil.closeConnection(connection);
		}

	}
	@Override
	public void shutdown() {
		logger.severe(this.getClass().getCanonicalName()+" shutdown.");
	}
	public void addListener(IOracleStreamsListener listener){
		listeners.add(listener);
	}
	public void removeListener(IOracleStreamsListener listener){
		listeners.remove(listener);
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getAqDriver() {
		return aqDriver;
	}

	public void setAqDriver(String aqDriver) {
		this.aqDriver = aqDriver;
	}
	
}
