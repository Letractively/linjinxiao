package aurora.plugin.oracle.aq;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import oracle.AQ.AQDequeueOption;
import oracle.AQ.AQDriverManager;
import oracle.AQ.AQMessage;
import oracle.AQ.AQObjectPayload;
import oracle.AQ.AQQueue;
import oracle.AQ.AQSession;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.GeneralException;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IMessage;
import aurora.application.features.msg.IMessageListener;
import aurora.application.features.msg.IMessageStub;
import aurora.application.features.msg.INoticerConsumer;
import aurora.application.features.msg.MessageCodes;
import aurora.database.DBUtil;

public class DefaultNoticeConsumer extends AbstractLocatableObject implements INoticerConsumer {

	private IObjectRegistry registry;

	private String topic;
	private String client;

	private Connection connection;
	private Map<String, List<IMessageListener>> messageListeners = new HashMap<String, List<IMessageListener>>();
	private ILogger logger;
	private OracleAQStub oracleAQStub;
	private boolean shutdown = false;
	private Thread dequeueThread;
	public DefaultNoticeConsumer(IObjectRegistry registry) {
		this.registry = registry;
	}

	public void init(IMessageStub stub) throws Exception {
		if (!(stub instanceof OracleAQStub)) {
			throw new IllegalArgumentException("The IMessageStub is not OralceAQStub!");
		}
		if (topic == null) {
			throw BuiltinExceptionFactory.createAttributeMissing(this, "topic");
		}
		if (client == null) {
			throw BuiltinExceptionFactory.createAttributeMissing(this, "client");
		}
		logger = LoggingContext.getLogger(this.getClass().getCanonicalName(), registry);
		logger.log(Level.CONFIG, "init oracle aq consumer");
		oracleAQStub = (OracleAQStub) stub;
		connection = oracleAQStub.createConnection();
		dequeueThread = (new Thread() {
			public void run() {
				try {
					AQSession session = AQDriverManager.createAQSession(connection);
					AQQueue queue = session.getQueue(connection.getMetaData().getUserName(), topic);
					logger.log(Level.CONFIG, "start oracle aq consumer successfull!");
					// Dequeue
					AQDequeueOption dequeueOption = new AQDequeueOption();
					dequeueOption.setConsumerName(client);
					dequeueOption.setDequeueMode(AQDequeueOption.DEQUEUE_REMOVE);
					dequeueOption.setVisibility(AQDequeueOption.VISIBILITY_IMMEDIATE);
					while (!shutdown) {
						AQMessage message = queue.dequeue(dequeueOption, AuroraMessage.getORADataFactory());
						try {
							AQObjectPayload payload = message.getObjectPayload();
							AuroraMessage messageData = (AuroraMessage) payload.getPayloadData();
							onMessage(new AQTextMessage(messageData));
							connection.commit();
						} catch (Exception e) {
							connection.rollback();
							logger.log(Level.SEVERE, "", e);
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		dequeueThread.start();
	}

	public void onShutdown() {
		shutdown = true;
		dequeueThread.interrupt();
		DBUtil.closeConnection(connection);
	}

	public void onMessage(IMessage message){
		String messageText = null;
		try {
			messageText = message.getText();
		} catch (Exception e) {
			throw new GeneralException(MessageCodes.JMSEXCEPTION_ERROR, new Object[] { e.getMessage() }, e);
		}
		List<IMessageListener> listeners = messageListeners.get(messageText);
		if (listeners != null) {
			for (IMessageListener l : listeners) {
				try {
					l.onMessage(message);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Listener:" + l.toString() + " occur exception.", e);
					throw new RuntimeException("Listener:" + l.toString() + " occur exception.", e);
				}
			}
		}

	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public void addListener(String message, IMessageListener listener) {
		List<IMessageListener> listeners = messageListeners.get(message);
		if (listeners == null) {
			listeners = new LinkedList<IMessageListener>();
			messageListeners.put(message, listeners);
		}
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeListener(String message, IMessageListener listener) {
		List<IMessageListener> listeners = messageListeners.get(message);
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
	}
}
