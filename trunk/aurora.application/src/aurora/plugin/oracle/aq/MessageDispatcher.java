package aurora.plugin.oracle.aq;

import java.sql.Connection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import oracle.AQ.AQDriverManager;
import oracle.AQ.AQEnqueueOption;
import oracle.AQ.AQMessage;
import oracle.AQ.AQObjectPayload;
import oracle.AQ.AQQueue;
import oracle.AQ.AQSession;
import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IMessage;
import aurora.application.features.msg.IMessageDispatcher;
import aurora.application.features.msg.IMessageStub;
import aurora.database.DBUtil;

public class MessageDispatcher extends AbstractLocatableObject implements IMessageDispatcher {

	private IObjectRegistry mRegistry;

	public MessageDispatcher(IObjectRegistry registry) {
		this.mRegistry = registry;
	}

	public void send(String topic, IMessage message, CompositeMap context) throws Exception {
		if (topic == null)
			BuiltinExceptionFactory.createAttributeMissing(this, "topic");
		ILogger logger = LoggingContext.getLogger(context, this.getClass().getCanonicalName());
		IMessageStub messageStub = (IMessageStub) mRegistry.getInstanceOfType(IMessageStub.class);
		if (messageStub == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IMessageStub.class, this.getClass().getCanonicalName());
		if (!messageStub.isStarted()) {
			throw new IllegalStateException("Message Provider is not started, please check the configuration.");
		}
		if (!(messageStub instanceof OracleAQStub)) {
			throw new IllegalArgumentException("The IMessageStub is not OralceAQStub!");
		}
		Connection connection = ((OracleAQStub) messageStub).createConnection();
		try {
			AQSession session = AQDriverManager.createAQSession(connection);
			AQQueue queue = session.getQueue(connection.getMetaData().getUserName(), topic);
			AQEnqueueOption enqueueOption = new AQEnqueueOption();
			enqueueOption.setVisibility(AQEnqueueOption.VISIBILITY_IMMEDIATE);
			AQMessage msg = queue.createMessage();
			// AQMessageProperty property = new AQMessageProperty();
			// property.setDelay(AQMessageProperty.DELAY_NONE);
			// msg.setMessageProperty(property);
			AQObjectPayload payload = msg.getObjectPayload();
			AuroraMessage aurora_msg = convertMsg(message);
			if (aurora_msg == null)
				throw new RuntimeException("Convert message failed :" + message.getText());
			payload.setPayloadData(aurora_msg);
			queue.enqueue(enqueueOption, msg);
			connection.commit();
			logger.log(Level.CONFIG, "Message:{0} sent", new Object[] { message.getText() });
		} finally {
			DBUtil.closeConnection(connection);
		}
	}

	private AuroraMessage convertMsg(IMessage message) throws Exception {
		if (message == null)
			return null;
		CompositeMap paras = message.getProperties();
		AuroraMessageProperties properties = null;
		if (paras != null && !paras.isEmpty()) {
			@SuppressWarnings("unchecked")
			Set<Entry<Object, Object>> set = paras.entrySet();
			AuroraMessageProperty[] propertyArray = new AuroraMessageProperty[set.size()];
			int i = 0;
			for (Entry<Object, Object> element : set) {
				propertyArray[i++] = new AuroraMessageProperty(element.getKey().toString(), element.getValue().toString());
			}
			properties = new AuroraMessageProperties(propertyArray);
		}
		return new AuroraMessage(message.getText(), properties);
	}

}
