package aurora.plugin.coupon;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import uncertain.composite.CompositeMap;
import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import aurora.application.features.msg.IConsumer;
import aurora.application.features.msg.IMessage;
import aurora.application.features.msg.IMessageListener;
import aurora.application.features.msg.IMessageStub;
import aurora.application.features.msg.INoticerConsumer;
import aurora.application.features.msg.Message;
import aurora.database.service.BusinessModelService;
import aurora.database.service.IDatabaseServiceFactory;
import aurora.database.service.SqlServiceContext;
import aurora.service.IServiceFactory;

import com.wondertek.esmp.esms.empp.EMPPConnectResp;
import com.wondertek.esmp.esms.empp.EMPPData;
import com.wondertek.esmp.esms.empp.EMPPDeliver;
import com.wondertek.esmp.esms.empp.EMPPDeliverReport;
import com.wondertek.esmp.esms.empp.EMPPObject;
import com.wondertek.esmp.esms.empp.EMPPShortMsg;
import com.wondertek.esmp.esms.empp.EMPPSubmitSM;
import com.wondertek.esmp.esms.empp.EMPPSubmitSMResp;
import com.wondertek.esmp.esms.empp.EmppApi;

public class CouponService extends AbstractLocatableObject implements
		ILifeCycle, IMessageListener {

	public static final String PLUGIN = CouponService.class.getCanonicalName();
	public static final int SHORT_MESSAGE_MAX_LENGTH = 70;

	private IObjectRegistry mRegistry;

	private String oldOrderBM;
	private String fetchOrderBM;
	private String updateOrderBM;
	private String receiveMessageBM;

	private int threadCount = 2;

	private IDatabaseServiceFactory databaseServiceFactory;
	private IProcedureManager procedureManager;
	private IServiceFactory serviceFactory;

	private String host = "192.168.0.215";
	private int port = 9981;
	private String accountId = "555580001";
	private String password = "cool1226";
	private String serviceId = "555580001";
	private int intervalTime = 10000;// 10秒轮询一次看是否有新订单

	private long reconnectTime = 10 * 1000;

	protected String topic = "coupon";
	protected String message = "coupon_message";

	private EmppApi emppApi;
	private boolean running = true;
	private Queue<CompositeMap> waitSendMessageOrderQueue = new ConcurrentLinkedQueue<CompositeMap>();
	private Queue<CompositeMap> waitUpdateStatusOrderQueue = new ConcurrentLinkedQueue<CompositeMap>();
	private Queue<CompositeMap> receiveMessageQueue = new ConcurrentLinkedQueue<CompositeMap>();
	private FetchOrderFromDataBase fetchOrderTask;
	private ExecutorService mainExecutorService;
	private SendMessageManager sendMessageManager;
	private UpdateStatusManager updateStatusManager;
	private ReceiveMessageManager receiveMessageManager;
	private ResourceManager resourceManager;
	private Logger consoleLogger;

	public CouponService(IObjectRegistry registry) {
		this.mRegistry = registry;
	}

	public boolean startup() {
		consoleLogger = Logger.getLogger(PLUGIN);
		resourceManager = new ResourceManager(mRegistry);
		emppApi = new EmppApi();
		RecvListener listener = new RecvListener(this, emppApi, reconnectTime,
				resourceManager.getFileLogger());

		try {
			// 建立同服务器的连接
			EMPPConnectResp response = emppApi.connect(host, port, accountId,
					password, listener);
			if (response == null) {
				consoleLogger.severe("连接超时失败");
				return false;
			}
			consoleLogger.config(response.debugString());
			if (!emppApi.isConnected()) {
				consoleLogger.severe("连接失败:响应包状态位=" + response.getStatus());
				return false;
			}
			if (!emppApi.isSubmitable()) {
				consoleLogger.severe("不可发送短信=" + response.getStatus());
				return false;
			}
		} catch (Exception e) {
			consoleLogger.log(Level.SEVERE, "发生异常，导致连接失败", e);
			return false;
		}
		return true;
	}

	public void onInitialize() {
		resourceManager.onInitialize();
		if (fetchOrderBM == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this,
					"fetchOrderBM");
		if (updateOrderBM == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this,
					"updateOrderBM");
		if (receiveMessageBM == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this,
					"receiveMessageBM");
		databaseServiceFactory = (IDatabaseServiceFactory) mRegistry
				.getInstanceOfType(IDatabaseServiceFactory.class);
		if (databaseServiceFactory == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this,
					IDatabaseServiceFactory.class, this.getClass().getName());
		procedureManager = (IProcedureManager) mRegistry
				.getInstanceOfType(IProcedureManager.class);
		if (procedureManager == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this,
					IProcedureManager.class, this.getClass().getName());
		serviceFactory = (IServiceFactory) mRegistry
				.getInstanceOfType(IServiceFactory.class);
		if (serviceFactory == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this,
					IServiceFactory.class, this.getClass().getName());

		IMessageStub stub = (IMessageStub) mRegistry
				.getInstanceOfType(IMessageStub.class);
		if (stub == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this,
					IMessageStub.class, this.getClass().getName());
		if (!stub.isStarted())
			consoleLogger
					.warning("JMS MessageStub is not started, please check the configuration.");
		IConsumer consumer = stub.getConsumer(topic);
		if (consumer == null) {
			throw new IllegalStateException(
					"MessageStub does not define the topic '" + topic
							+ "', please check the configuration.");
		}
		if (!(consumer instanceof INoticerConsumer))
			throw BuiltinExceptionFactory.createInstanceTypeWrongException(
					this.getOriginSource(), INoticerConsumer.class,
					IConsumer.class);
		((INoticerConsumer) consumer).addListener(message, this);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		mainExecutorService = Executors.newFixedThreadPool(4);
		fetchOrderTask = new FetchOrderFromDataBase(this, intervalTime);
		sendMessageManager = new SendMessageManager(this, threadCount);
		updateStatusManager = new UpdateStatusManager(this, threadCount * 2);
		receiveMessageManager = new ReceiveMessageManager(this, threadCount);
		mainExecutorService.submit(fetchOrderTask);
		mainExecutorService.submit(sendMessageManager);
		mainExecutorService.submit(updateStatusManager);
		mainExecutorService.submit(receiveMessageManager);
		// 发送消息立即执行
		resetUnfinishedOrderStatus(stub);
	}

	private void resetUnfinishedOrderStatus(IMessageStub messageStub) {
		Connection connection = resourceManager.getConnection();
		if (oldOrderBM != null) {
			try {
				CompositeMap context = new CompositeMap();
				Message msg = new Message(message, null);
				executeBM(connection, oldOrderBM, context, new CompositeMap());
				messageStub.getDispatcher().send(topic, msg, context);
				context.clear();
			} catch (Exception e) {
				resourceManager.getFileLogger().log(Level.SEVERE, "", e);
			} finally {
				resourceManager.closeConnection(connection);
			}
		}
	}

	public String sendMessage(CompositeMap order) throws Exception {
		int order_id = order.getInt(OrderTableUtil.ORDER_ID);
		if (order_id == 0)
			throw BuiltinExceptionFactory.createAttributeMissing(null,
					OrderTableUtil.ORDER_ID);
		String mobile = order.getString(OrderTableUtil.MOBILE_PHONE);
		String message = order.getString(OrderTableUtil.SEND_MESSAGE);
		String logMessage = "发送短信:" + "订单号:" + order_id + " 手机：" + mobile
				+ " 短信内容:" + message;
		resourceManager.getFileLogger().log(Level.CONFIG, logMessage);
		//System.out.println(logMessage);
		int[] sequenceNumbers;
		if (message.length() >= SHORT_MESSAGE_MAX_LENGTH) {
			sequenceNumbers = sendLongMessage(order_id, message,
					new String[] { mobile });
		} else {
			sequenceNumbers = sendShortMessage(order_id, message,
					new String[] { mobile });
		}
		StringBuffer sequenceNum = new StringBuffer("");
		for (int i = 0; i < sequenceNumbers.length; i++) {
			sequenceNum.append(sequenceNumbers[i]).append(
					OrderTableUtil.SEQUENCE_SEPARATOR);
		}
		return sequenceNum.substring(0, sequenceNum.length() - 1).toString();
	}

	public int[] sendLongMessage(int order_id, String message, String[] mobiles)
			throws Exception {
		int[] sequenceNumbers = emppApi.submitMsgAsync(message, mobiles,
				serviceId);
		return sequenceNumbers;
	}

	public int[] sendShortMessage(int order_id, String message, String[] mobiles)
			throws Exception {
		// 详细设置短信的各个属性,不支持长短信
		EMPPSubmitSM msg = (EMPPSubmitSM) EMPPObject
				.createEMPP(EMPPData.EMPP_SUBMIT);
		List<String> dstId = new ArrayList<String>();
		for (String mobile : mobiles) {
			dstId.add(mobile);
		}
		msg.setDstTermId(dstId);
		msg.setSrcTermId(accountId);
		msg.setServiceId(serviceId);

		EMPPShortMsg msgContent = new EMPPShortMsg(
				EMPPShortMsg.EMPP_MSG_CONTENT_MAXLEN);
		msgContent.setMessage(message.getBytes("GBK"));
		msg.setShortMessage(msgContent);
		msg.assignSequenceNumber();
		emppApi.submitMsgAsync(msg);
		int sequenceNumber = msg.getSequenceNumber();
		int[] sequenceNumbers = new int[] { sequenceNumber };
		return sequenceNumbers;
	}

	public void updateOrderStatus(Connection connection, CompositeMap context,
			CompositeMap para) throws Exception {
		executeBM(connection, updateOrderBM, context, para);
	}

	public void receiveMessage(Connection connection, CompositeMap context,
			CompositeMap para) throws Exception {
		executeBM(connection, receiveMessageBM, context, para);
	}

	public void submitStatus(EMPPSubmitSMResp resp) {
		int sequenceNum = resp.getSequenceNumber();
		byte[] msgId = ResourceManager.fiterBinaryZero(resp.getMsgId());
		BigInteger messsageId = new BigInteger(msgId);
		
		CompositeMap orderRecord = new CompositeMap("record");
		orderRecord.put(OrderTableUtil.SEQUENCE_NUM, sequenceNum);
		orderRecord.put(OrderTableUtil.MESSAGE_ID, messsageId);
		orderRecord.put(OrderTableUtil.EVENT, OrderTableUtil.EVENT_SEND);

		int status = resp.getResult();
		//TODO DELETE
		System.out.println("submitStatus :   sequenceNum:" + sequenceNum
				+ " messsageId:" + messsageId+" Result:"+status);
		if (status != ResourceManager.SUMMIT_OK) {
			orderRecord.put(OrderTableUtil.EXCEPTION, "submitStatus:" + status);
		}
		addToUpdateStatusOrderQueue(orderRecord);
	}

	public void receiveStatus(EMPPDeliver deliver) {
		EMPPDeliverReport report = deliver.getDeliverReport();
		int sequenceNum = deliver.getSequenceNumber();
		byte[] msgId = ResourceManager.fiterBinaryZero(deliver.getMsgId());
		BigInteger messsageId = new BigInteger(msgId);

		CompositeMap orderRecord = new CompositeMap("record");
		orderRecord.put(OrderTableUtil.SEQUENCE_NUM, sequenceNum);
		orderRecord.put(OrderTableUtil.MESSAGE_ID, messsageId);
		orderRecord.put(OrderTableUtil.EVENT, OrderTableUtil.EVENT_RECEIVE);

		String status = report.getStat();
		//TODO DELETE
		System.out.println("receiveStatus :   sequenceNum:" + sequenceNum
				+ " messsageId:" + messsageId+" status:"+status);
		if (!ResourceManager.RECEIVE_OK.equals(status)) {
			orderRecord
					.put(OrderTableUtil.EXCEPTION, "receiveStatus:" + status);
		}
		addToUpdateStatusOrderQueue(orderRecord);
	}

	public void receiveMessage(EMPPDeliver deliver) {
		String moiblePhone = deliver.getSrcTermId();
		String receiveMsg = deliver.getMsgContent().getMessage();

		int sequenceNum = deliver.getSequenceNumber();
		byte[] msgId = ResourceManager.fiterBinaryZero(deliver.getMsgId());
		BigInteger messsageId = new BigInteger(msgId);

		// TODO DELETE
		System.out.println("receiveMessage :   sequenceNum:" + sequenceNum
				+ " messsageId" + messsageId);

		System.out.println("收到" + moiblePhone + "发送的短信。" + "短信内容为："
				+ receiveMsg);
		CompositeMap orderRecord = new CompositeMap("record");
		orderRecord.put(OrderTableUtil.MOBILE_PHONE, moiblePhone);
		orderRecord.put(OrderTableUtil.RECEIVE_MESSAGE, receiveMsg);
		addToReceiveMessage(orderRecord);
	}

	public void fetchOrder(Connection connection, CompositeMap context,
			CompositeMap order) throws Exception {
		executeBM(connection, fetchOrderBM, context, order);
	}

	public void executeBM(Connection connection, String bm_name,
			CompositeMap context, CompositeMap parameterMap) throws Exception {
		CompositeMap localContext = context;
		if (localContext == null)
			localContext = new CompositeMap();
		SqlServiceContext sqlContext = SqlServiceContext
				.createSqlServiceContext(localContext);
		if (sqlContext == null)
			throw new RuntimeException(
					"Can not create SqlServiceContext for context:"
							+ localContext.toXML());
		// Connection connection = getConnection();
		connection.setAutoCommit(false);
		sqlContext.setConnection(connection);
		try {
			BusinessModelService service = databaseServiceFactory
					.getModelService(bm_name, localContext);
			service.execute(parameterMap);
			connection.commit();
		} catch (Exception ex) {
			resourceManager.rollbackConnection(connection);
			throw new RuntimeException(ex);
		}
		// finally {
		// if (sqlContext != null)
		// sqlContext.freeConnection();
		// }
	}

	public void addToSendMessageOrderQueue(CompositeMap order) {
		if (order == null)
			return;
		waitSendMessageOrderQueue.add(order);
	}

	public CompositeMap popSendMessageOrderQueue() {
		return waitSendMessageOrderQueue.poll();
	}

	public void addToUpdateStatusOrderQueue(CompositeMap order) {
		if (order == null)
			return;
		waitUpdateStatusOrderQueue.add(order);
	}

	public CompositeMap popUpdateStatusOrderQueue() {
		return waitUpdateStatusOrderQueue.poll();
	}

	public void addToReceiveMessage(CompositeMap order) {
		if (order == null)
			return;
		receiveMessageQueue.add(order);
	}

	public CompositeMap popReceiveMessage() {
		return receiveMessageQueue.poll();
	}

	public void onMessage(IMessage message) {
		try {
			resourceManager.getFileLogger().log(Level.CONFIG,
					"receive a messsage:" + message.getText());
			fetchOrderTask.onMessage(message);
		} catch (Exception e) {
			resourceManager.getFileLogger().log(Level.WARNING,
					"Can not add the order:" + message);
		}
	}

	public void shutdown() {
		running = false;
		fetchOrderTask.shutdown();
		sendMessageManager.shutdown();
		updateStatusManager.shutdown();
		receiveMessageManager.shutdown();
		mainExecutorService.shutdownNow();
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public long getReconnectTime() {
		return reconnectTime;
	}

	public void setReconnectTime(long reconnectTime) {
		this.reconnectTime = reconnectTime;
	}

	public int getIntervalTime() {
		return intervalTime;
	}

	public void setIntervalTime(int intervalTime) {
		this.intervalTime = intervalTime;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOldOrderBM() {
		return oldOrderBM;
	}

	public void setOldOrderBM(String oldOrderBM) {
		this.oldOrderBM = oldOrderBM;
	}

	public String getFetchOrderBM() {
		return fetchOrderBM;
	}

	public void setFetchOrderBM(String fetchOrderBM) {
		this.fetchOrderBM = fetchOrderBM;
	}

	public String getUpdateOrderBM() {
		return updateOrderBM;
	}

	public void setUpdateOrderBM(String updateOrderBM) {
		this.updateOrderBM = updateOrderBM;
	}

	public String getReceiveMessageBM() {
		return receiveMessageBM;
	}

	public void setReceiveMessageBM(String receiveMessageBM) {
		this.receiveMessageBM = receiveMessageBM;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
}
