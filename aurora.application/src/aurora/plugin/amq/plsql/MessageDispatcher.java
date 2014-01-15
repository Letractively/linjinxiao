package aurora.plugin.amq.plsql;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;

public class MessageDispatcher {
	
	public static Map<String,ActiveMQConnectionFactory> factoryMap = new HashMap<String,ActiveMQConnectionFactory>();
	
	public static void  sendMessage(String url,String topic,String message) throws Exception{
		if(url == null)
			throw new IllegalArgumentException("url can not be null!");
		if(topic == null)
			throw new IllegalArgumentException("topic can not be null!");
		if(message == null)
			throw new IllegalArgumentException("message can not be null!");
		ActiveMQConnectionFactory factory = factoryMap.get(url);
		if(factory == null){
			factory = new ActiveMQConnectionFactory(url);
			factoryMap.put(url, factory);
		}
		sendMessage(factory,topic,message);
	}
	private static void sendMessage(ActiveMQConnectionFactory connectionFactory,String topic,String message) throws Exception{
		if(topic == null)
			throw new IllegalArgumentException("topic can not be null!");
		if(message == null)
			throw new IllegalArgumentException("message can not be null!");
		Connection connection = null;
		Session session = null;
		MessageProducer messageProducer = null;
		try {
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//			logger.log(Level.CONFIG, "create createTopic {0}", new Object[] { topic });
			Topic jmsTopic = session.createTopic(topic);
			messageProducer = session.createProducer(jmsTopic);
//			logger.log(Level.CONFIG, "start producer connection");
			connection.start();
//			logger.log(Level.CONFIG, "start producer successfull!");
			Message textMessage = session.createTextMessage(message);

			messageProducer.send(textMessage);
//			logger.log(Level.CONFIG, "Message:{0} sent", new Object[] { parsedText });
		} finally {
			freeMessageProducer(messageProducer);
			freeJMSSession(session);
			freeJMSConnection(connection);
		}
	}
	public static void freeMessageProducer(MessageProducer messageProducer) {
		if (messageProducer != null) {
			try {
				messageProducer.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	public static void freeMessageConsumer(MessageConsumer messageConsumer) {
		if (messageConsumer != null) {
			try {
				messageConsumer.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	

	public static void freeJMSSession(Session session) {
		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	public static void freeJMSConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		
	}
}
