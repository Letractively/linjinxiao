package aurora.plugin.coupon;

import java.sql.Connection;
import java.util.logging.Level;

import uncertain.composite.CompositeMap;

public class ReceiveMessageExecutor implements Runnable {
	
	private CouponService couponService;
	private ResourceManager resourceManager;
	private CompositeMap orderRecord;
	private Connection connection;
	
	public ReceiveMessageExecutor(CouponService couponService,CompositeMap orderRecord,Connection connection) {
		this.couponService = couponService;
		this.resourceManager = couponService.getResourceManager();
		this.orderRecord = orderRecord;
		this.connection = connection;
	}
	@Override
	public void run()  {
		CompositeMap orderRecordClone = (CompositeMap)orderRecord.clone();
		CompositeMap context = new CompositeMap();
		try {
			resourceManager.getFileLogger().log(Level.CONFIG,
					"ReceiveMessageExecutor:" + ResourceManager.LINE_SEPARATOR + orderRecord.toXML());
			//TODO delete
//			System.out.println("ReceiveMessageExecutor:" + ResourceManager.LINE_SEPARATOR + orderRecord.toXML());
			couponService.receiveMessage(connection, context, orderRecordClone);
			couponService.addToSendMessageOrderQueue(orderRecordClone);
		} catch (Exception e) {
			resourceManager.getFileLogger().log(Level.SEVERE, "", e);
		} finally {
			
		}
	}
}