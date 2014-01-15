package aurora.plugin.coupon;

import java.util.logging.Level;


import uncertain.composite.CompositeMap;

public class SendMessageExecutor implements Runnable {
	
	private CouponService couponService;
	private ResourceManager resourceManager;
	private CompositeMap orderRecord;
	
	public SendMessageExecutor(CouponService couponService,CompositeMap orderRecord) {
		this.couponService = couponService;
		this.resourceManager = couponService.getResourceManager();
		this.orderRecord = orderRecord;
	}
	@Override
	public void run()  {
		CompositeMap orderRecordClone = (CompositeMap)orderRecord.clone();
		orderRecordClone.put(OrderTableUtil.EVENT, OrderTableUtil.EVENT_SENDING);
		String sequenceNum = null;
		try {
			sequenceNum = couponService.sendMessage(orderRecord);
			//TODO DELETE
			System.out.println("SendMessageExecutor sequenceNum:"+sequenceNum);
			orderRecordClone.put(OrderTableUtil.SEQUENCE_NUM,sequenceNum);
		} catch (Exception e) {
			resourceManager.getFileLogger().log(Level.SEVERE, "", e);
			resourceManager.getFileLogger().log(Level.SEVERE, "pass parameter =" + orderRecordClone.toXML());
			orderRecordClone.put(OrderTableUtil.EXCEPTION, ResourceManager.getFullStackTrace(e));
		} finally {
			resourceManager.getFileLogger().log(Level.CONFIG,
					"SendMessageExecutor:" + ResourceManager.LINE_SEPARATOR + orderRecord.toXML());
			//TODO delete
//			System.out.println("SendMessageExecutor:" + ResourceManager.LINE_SEPARATOR + orderRecord.toXML());
			couponService.addToUpdateStatusOrderQueue(orderRecordClone);
		}
	}
}