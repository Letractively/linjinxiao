package aurora.plugin.oracle.aq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IConsumer;
import aurora.application.features.msg.IMessageDispatcher;
import aurora.application.features.msg.IMessageHandler;
import aurora.application.features.msg.IMessageStub;
import aurora.datasource.DataSourceConfig;
import aurora.datasource.DatabaseConnection;

public class OracleAQClientInstance extends AbstractLocatableObject implements ILifeCycle,OracleAQStub {
	/**
	 * 配置样本
		<aq:Oracle-AQ-client-instance xmlns:msg="aurora.application.features.msg" xmlns:aq="aurora.plugin.oracle.aq">
		
			    <messageHandlers>
			        <msg:DefaultMessageHandler name="refreshPriviledge" procedure="init.load_priviledge_check_data"/>
			        <msg:DefaultMessageHandler name="refreshService" procedure="init.load_system_service"/>
			    </messageHandlers>
				
			    <consumers>
			        <aq:consumer topic="application_foundation" client="agent_a">
			            <events>
			                <msg:event handler="refreshPriviledge" message="priviledge_setting_change"/>
			                <msg:event handler="refreshService" message="service_config_change"/>
			            </events>
			        </aq:consumer>
					<aq:DefaultNoticeConsumer topic="dml_event" client="agent_a"/>
			    </consumers>
				
		</aq:Oracle-AQ-client-instance>
	 * 
	 */
	public static final String PLUGIN = "aurora.plugin.oracleAQ";
	private IMessageHandler[] mMessageHandlers;
	private IConsumer[] consumers;

	private IObjectRegistry registry;
	private Logger logger;
	private Map<String, IMessageHandler> handlersMap = new HashMap<String, IMessageHandler>();
	private IMessageDispatcher messageDispatcher;
	private Map<String, IConsumer> consumerMap;
	private int status = STOP_STATUS;
	private String jdbcDriver = "oracle.jdbc.driver.OracleDriver";
	private String aqDriver = "oracle.AQ.AQOracleDriver";
	private DatabaseConnection connConfig;

	public OracleAQClientInstance(IObjectRegistry registry) {
		this.registry = registry;
		messageDispatcher = new MessageDispatcher(registry);
	}

	public boolean startup() {
		if(status == STARTING_STATUS || status == STARTED_STATUS)
			return true;
		status = STARTING_STATUS;
		logger = Logger.getLogger(PLUGIN);
		DataSourceConfig ds = (DataSourceConfig) registry.getInstanceOfType(DataSourceConfig.class);
		if (ds == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, DataSourceConfig.class, this.getClass().getCanonicalName());
		DatabaseConnection[] connConfigs = ds.getDatabaseConnections();
		if (connConfigs == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, DatabaseConnection.class, this.getClass()
					.getCanonicalName());
		try {
			Class.forName(jdbcDriver);
			Class.forName(aqDriver);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < connConfigs.length; i++) {
			DatabaseConnection config = connConfigs[i];
			if (jdbcDriver.equals(config.getDriverClass())) {
				connConfig = config;
				break;
			}
		}
		if (connConfig == null) {
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, DatabaseConnection.class, this.getClass()
					.getCanonicalName());
		}

		consumerMap = new HashMap<String,IConsumer>();
		// init consumer config
		if (consumers != null) {
			for (int i = 0; i < consumers.length; i++) {
				consumerMap.put(consumers[i].getTopic(), consumers[i]);
			}
		}
		(new Thread() {
			public void run() {
				if (consumers != null) {
					for (int i = 0; i < consumers.length; i++) {
						try {
							consumers[i].init(OracleAQClientInstance.this);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "init jms consumers failed!", e);
							throw new RuntimeException(e);
						}
					}
				}
				status = STARTED_STATUS;
				LoggingContext.getLogger(PLUGIN, registry).log(Level.INFO,"start jms client successful!");
			}
		}).start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					onShutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		registry.registerInstance(IMessageStub.class, this);
		
		return true;
	}

	public void onShutdown() throws Exception {
		if (consumers != null) {
			for (int i = 0; i < consumers.length; i++) {
				consumers[i].onShutdown();
			}
		}
	}

	public IMessageHandler getMessageHandler(String name) {
		return (IMessageHandler) handlersMap.get(name);
	}

	public IMessageHandler[] getMessageHandlers() {
		return mMessageHandlers;
	}

	public void setMessageHandlers(IMessageHandler[] messageHandlers) {
		this.mMessageHandlers = messageHandlers;
		for (int i = 0; i < messageHandlers.length; i++) {
			handlersMap.put(messageHandlers[i].getName(), messageHandlers[i]);
		}
	}

	public IConsumer[] getConsumers() {
		return consumers;
	}

	public void setConsumers(IConsumer[] consumers) {
		this.consumers = consumers;
	}
	public IConsumer getConsumer(String topic) {
		return consumerMap.get(topic);
	}

	public void shutdown() {
		try {
			onShutdown();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "shutdown jms instance failed!", e);
		}
	}

	public IMessageDispatcher getDispatcher() {
		return messageDispatcher;
	}

	public Connection createConnection() throws Exception {
		Connection connection = DriverManager.getConnection(connConfig.getUrl(), connConfig.getUserName(), connConfig.getPassword());
		if (connection == null)
			throw new RuntimeException(" DriverManager getConnection failed!");
		connection.setAutoCommit(false);
		return connection;
	}

	public boolean isStarted() {
		return  status == STARTED_STATUS;
	}
}
