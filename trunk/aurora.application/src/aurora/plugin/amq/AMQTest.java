package aurora.plugin.amq;

import java.util.Calendar;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;

public class AMQTest implements MessageListener,ExceptionListener{
	String url= "stomp://localhost:61613";
//	String url = "tcp://localhost:61613?wireFormat=stomp";
	private String client;
	private String topic = "/topic/dml_event";
	private Session session;
	private MessageConsumer messageConsumer;
	
	public void init() throws Exception{
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
		Connection connection = factory.createConnection();
		connection.setExceptionListener(this);
		if(client == null){
			client = getAutoClient(topic);
		}
//		connection.setClientID(client);
		System.out.println(1);
//		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session = connection.createSession(false,Session.DUPS_OK_ACKNOWLEDGE);
		System.out.println(2);

		Topic jmsTopic = session.createTopic(topic);
		messageConsumer = session.createDurableSubscriber(jmsTopic, topic);
		messageConsumer.setMessageListener(this);
		connection.start();
		System.out.println("ok");
	}
	
	public void initCustomer() throws Exception{
		StompConnection connection = new StompConnection();
		connection.open("127.0.0.1", 61613);
				
//		connection.connect("system", "manager");
		connection.connect(null, null);
//		StompFrame connect = connection.receive();
//		if (!connect.getAction().equals(Stomp.Responses.CONNECTED)) {
//			throw new Exception ("Not connected");
//		}
				
		connection.begin("tx1");
		connection.send(topic, "message1", "tx1", null);
		connection.send(topic, "message2", "tx1", null);
		connection.commit("tx1");
			
		connection.subscribe(topic, Subscribe.AckModeValues.CLIENT);
//			
//		connection.begin("tx2");
//			
		StompFrame message = connection.receive();
		System.out.println(message.getBody());
		connection.ack(message, "tx2");
//			
//		message = connection.receive();
//		System.out.println(message.getBody());
//		connection.ack(message, "tx2");
//			
//		connection.commit("tx2");
//				
//		connection.disconnect();
	}

	@Override
	public void onException(JMSException arg0) {
		arg0.printStackTrace();
	}

	@Override
	public void onMessage(Message arg0) {
		System.out.println("test");
		
	}
	
	public static void main(String[] args) throws Exception {
		AMQTest test = new AMQTest();
		test.init();
//		test.initCustomer();
		
	}
	public String getAutoClient(String topic){
		return Calendar.getInstance().getTimeInMillis()+(topic!= null?topic:"");
	}
}
