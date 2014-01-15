package aurora.plugin.coupon;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import uncertain.composite.CompositeMap;

public class UpdateStatusExecutor implements Runnable {
	
	private CouponService couponService;
	private ResourceManager resourceManager;
	private CompositeMap orderRecord;
	private Connection connection;
	
	public UpdateStatusExecutor(CouponService couponService,CompositeMap orderRecord,Connection connection) {
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
					"UpdateStatusExecutor:" + ResourceManager.LINE_SEPARATOR + orderRecord.toXML());
			//TODO delete
//			System.out.println("UpdateStatusExecutor:" + ResourceManager.LINE_SEPARATOR + orderRecord.toXML());
			couponService.updateOrderStatus(connection, context, orderRecordClone);
		}catch(SQLException e){
			System.out.println("hascode:"+e.hashCode()+" getErrorCode:"+e.getErrorCode());
			resourceManager.getFileLogger().log(Level.SEVERE, "", e);
		} catch (Exception e) {
			System.out.println("hascode:"+e.hashCode());
			resourceManager.getFileLogger().log(Level.SEVERE, "", e);
		} finally {
			
		}
	}
}
