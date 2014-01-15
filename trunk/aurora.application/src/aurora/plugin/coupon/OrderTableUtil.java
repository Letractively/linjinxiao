package aurora.plugin.coupon;

import uncertain.composite.CompositeMap;

public class OrderTableUtil {
	
	public static final String SEQUENCE_SEPARATOR = ",";

	public static final String ORDER_ID = "order_id";
	public static final String MOBILE_PHONE = "mobile_phone";
	public static final String SEND_MESSAGE = "send_message";
	public static final String EXCEPTION = "exception";
	public static final String STATUS="status";
	public static final String SEQUENCE_NUM="sequence_num";
	public static final String MESSAGE_ID="message_id";
	public static final String RECEIVE_MESSAGE="receive_message";
	
	
	public static final String EVENT="event";
	
	public static final String EVENT_SENDING = "SENDING";
	public static final String EVENT_SEND = "SEND";
	public static final String EVENT_RECEIVE = "RECEIVE";
	
	
	/*public static final String STATUS_NEW = "NEW";
	public static final String STATUS_WAIT = "WAIT";
	public static final String STATUS_RUNNING = "RUNNING";
	public static final String STATUS_EXCEPTION = "EXCEPTION";
	public static final String STATUS_DONE = "DONE";
	
	public static final String DETAIL_STATUS_VMSI = "VALIDATE_MESSAGE_SENDING";
	public static final String DETAIL_STATUS_VMS = "VALIDATE_MESSAGE_SEND";
	public static final String DETAIL_STATUS_VMR = "VALIDATE_MESSAGE_RECEIVE";
	public static final String DETAIL_STATUS_CMSI = "COUPON_MESSAGE_SENDING";
	public static final String DETAIL_STATUS_CMS = "COUPON_MESSAGE_SEND";
	public static final String DETAIL_STATUS_CMR = "COUPON_MESSAGE_RECEIVE";*/

	public static int getOrderId(CompositeMap orderRecord) {
		if (orderRecord == null)
			return -1;
		return orderRecord.getInt(ORDER_ID, -1);
	}
}
