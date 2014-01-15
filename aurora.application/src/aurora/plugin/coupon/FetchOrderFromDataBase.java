package aurora.plugin.coupon;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import aurora.application.features.msg.IMessage;

import uncertain.composite.CompositeMap;
import uncertain.core.ILifeCycle;

public class FetchOrderFromDataBase implements Runnable, ILifeCycle {

	private CouponService couponService;
	private ResourceManager resourceManager;
	private int intervalTime;

	private Object fetchNewOrderLock = new Object();
	private Connection connection;

	public FetchOrderFromDataBase(CouponService couponService,int intervalTime) {
		this.couponService = couponService;
		this.resourceManager = couponService.getResourceManager();
		this.intervalTime = intervalTime;
	}

	@Override
	public void run() {
		boolean hasNext = false;
		connection = resourceManager.getConnection();
		while (couponService.isRunning()) {
			synchronized (fetchNewOrderLock) {
				try {
					if (!hasNext) {
						fetchNewOrderLock.wait(intervalTime);
					}
					if (!couponService.isRunning())
						break;
					CompositeMap order = new CompositeMap("record");
					CompositeMap context = new CompositeMap();

					couponService.fetchOrder(connection, context, order);
					if (order == null || OrderTableUtil.getOrderId(order) == -1) {
						continue;
					}
					resourceManager.getFileLogger().log(Level.CONFIG, "add record to queue,order_id=" + OrderTableUtil.getOrderId(order));
					couponService.addToSendMessageOrderQueue(order);
					int record_count = order.getInt("record_count", -1);
					if (record_count > 1) {
						hasNext = true;
					} else {
						hasNext = false;
					}
				} catch (InterruptedException e) {
					// ignore
				} catch (SQLException e) {
					resourceManager.getFileLogger().log(Level.SEVERE, "", e);
					resourceManager.closeConnection(connection);
					connection = resourceManager.getConnection();
				} catch (Exception e) {
					resourceManager.getFileLogger().log(Level.SEVERE, "", e);
				}
			}
		}
		resourceManager.closeConnection(connection);
	}
	public void onMessage(IMessage message) {
		try {
			resourceManager.getFileLogger().log(Level.CONFIG, "receive a messsage:" + message.getText());
			synchronized (fetchNewOrderLock) {
				fetchNewOrderLock.notify();
			}
		} catch (Exception e) {
			resourceManager.getFileLogger().log(Level.WARNING, "Can not add the order:" + message);
		}
	}

	@Override
	public boolean startup() {
		return true;
	}

	@Override
	public void shutdown() {
		resourceManager.closeConnection(connection);
		synchronized (fetchNewOrderLock) {
			fetchNewOrderLock.notify();
		}
	}
}
