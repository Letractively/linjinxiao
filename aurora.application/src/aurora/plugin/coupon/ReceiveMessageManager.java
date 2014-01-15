package aurora.plugin.coupon;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import uncertain.composite.CompositeMap;
import uncertain.core.ILifeCycle;

public class ReceiveMessageManager implements Runnable, ILifeCycle {

	private CouponService couponService;
	private ResourceManager resourceManager;
	private int mThreadCount;

	private Queue<Connection> connectionQueue = new ConcurrentLinkedQueue<Connection>();
	private ExecutorService receiveMessageService;

	public ReceiveMessageManager(CouponService couponService,int threadCount) {
		this.couponService = couponService;
		this.resourceManager = couponService.getResourceManager();
		this.mThreadCount = threadCount;
		startup();
	}

	@Override
	public void run() {
		while (couponService.isRunning()) {
			Connection connection = connectionQueue.poll();
			try {
				if (connection == null || connection.isClosed()) {
					connection = resourceManager.getConnection();
				}
				CompositeMap orderRecord = couponService.popReceiveMessage();
				if (orderRecord == null || orderRecord.isEmpty()) {
					Thread.sleep(1000);
					continue;
				}
				ReceiveMessageExecutor use = new ReceiveMessageExecutor(couponService,orderRecord, connection);
				receiveMessageService.submit(use);
			} catch (InterruptedException e) {
				// ignore
			} catch (SQLException e) {
				resourceManager.getFileLogger().log(Level.SEVERE, "", e);
				resourceManager.closeConnection(connection);
				connection = resourceManager.getConnection();
			} catch (Throwable e) {
				resourceManager.getFileLogger().log(Level.SEVERE, "", e);
			} finally {
				connectionQueue.add(connection);
			}
		}
	}

	public boolean startup() {
		receiveMessageService = Executors.newFixedThreadPool(mThreadCount);
		for (int i = 0; i < mThreadCount; i++) {
			connectionQueue.add(resourceManager.getConnection());
		}
		return true;
	}

	public void shutdown() {
		if (receiveMessageService != null)
			receiveMessageService.shutdownNow();
		Connection connection = null;
		while ((connection = connectionQueue.poll()) != null) {
			resourceManager.closeConnection(connection);
		}
	}
}