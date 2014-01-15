package aurora.plugin.coupon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import uncertain.composite.CompositeMap;
import uncertain.core.ILifeCycle;

public class SendMessageManager implements Runnable, ILifeCycle {

	private CouponService couponService;
	private ResourceManager resourceManager;
	private int mThreadCount;

	private ExecutorService sendMessageService;

	public SendMessageManager(CouponService couponService,int threadCount) {
		this.couponService = couponService;
		this.resourceManager = couponService.getResourceManager();
		this.mThreadCount = threadCount;
		startup();
	}

	@Override
	public void run() {
		while (couponService.isRunning()) {
			try {
				CompositeMap orderRecord = couponService.popSendMessageOrderQueue();
				if (orderRecord == null || orderRecord.isEmpty()) {
					Thread.sleep(1000);
					continue;
				}
				resourceManager.getFileLogger().log(
						Level.CONFIG,
						"get a order record from queue,order is" + ResourceManager.LINE_SEPARATOR + ResourceManager.LINE_SEPARATOR
								+ orderRecord.toXML());
				int order_id = OrderTableUtil.getOrderId(orderRecord);
				if (order_id == -1) {
					Thread.sleep(1000);
					continue;
				}
				SendMessageExecutor sme = new SendMessageExecutor(couponService,orderRecord);
				sendMessageService.submit(sme);
			} catch (InterruptedException e) {
				// ignore
			} catch (Throwable e) {
				resourceManager.getFileLogger().log(Level.SEVERE, "", e);
			} finally {
			}
		}
	}

	public boolean startup() {
		sendMessageService = Executors.newFixedThreadPool(mThreadCount);
		return true;
	}

	public void shutdown() {
		if (sendMessageService != null)
			sendMessageService.shutdownNow();
	}
}
