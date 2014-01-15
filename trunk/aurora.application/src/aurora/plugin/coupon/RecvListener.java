package aurora.plugin.coupon;

import java.math.BigInteger;
import java.util.logging.Level;

import uncertain.logging.ILogger;

import com.wondertek.esmp.esms.empp.EMPPAnswer;
import com.wondertek.esmp.esms.empp.EMPPChangePassResp;
import com.wondertek.esmp.esms.empp.EMPPDeliver;
import com.wondertek.esmp.esms.empp.EMPPDeliverReport;
import com.wondertek.esmp.esms.empp.EMPPObject;
import com.wondertek.esmp.esms.empp.EMPPRecvListener;
import com.wondertek.esmp.esms.empp.EMPPReqNoticeResp;
import com.wondertek.esmp.esms.empp.EMPPSubmitSM;
import com.wondertek.esmp.esms.empp.EMPPSubmitSMResp;
import com.wondertek.esmp.esms.empp.EMPPSyncAddrBookResp;
import com.wondertek.esmp.esms.empp.EMPPTerminate;
import com.wondertek.esmp.esms.empp.EMPPUnAuthorization;
import com.wondertek.esmp.esms.empp.EmppApi;

public class RecvListener implements EMPPRecvListener {

	private CouponService couponService;
	private long reconnectTime = 10 * 1000;
	private EmppApi emppApi = null;

	private int closedCount = 0;

	private ILogger logger;

	public RecvListener(CouponService couponService,EmppApi emppApi, long reconnectTime,ILogger logger) {
		this.couponService = couponService;
		this.emppApi = emppApi;
		this.reconnectTime = reconnectTime;
		this.logger = logger;
	}

	// 处理接收到的消息
	public void onMessage(EMPPObject message) {
		if (message instanceof EMPPUnAuthorization) {
			EMPPUnAuthorization unAuth = (EMPPUnAuthorization) message;
			logger.warning("客户端无权执行此操作 commandId=" + unAuth.getUnAuthCommandId());
			return;
		}
		if (message instanceof EMPPSubmitSMResp) {
			EMPPSubmitSMResp resp = (EMPPSubmitSMResp) message;
			logger.config("收到sumbitResp:");
			byte[] msgId = ResourceManager.fiterBinaryZero(resp.getMsgId());

			logger.config("msgId=" + new BigInteger(msgId));
			logger.config("result=" + resp.getResult());
			couponService.submitStatus(resp);
			return;
		}
		if (message instanceof EMPPDeliver) {
			EMPPDeliver deliver = (EMPPDeliver) message;
			if (deliver.getRegister() == EMPPSubmitSM.EMPP_STATUSREPORT_TRUE) {
				// 收到状态报告
				EMPPDeliverReport report = deliver.getDeliverReport();
				logger.config("收到状态报告:");
				byte[] msgId = ResourceManager.fiterBinaryZero(report.getMsgId());
				BigInteger messsageId = new BigInteger(msgId);
				String status = report.getStat();
				logger.config("msgId=" + messsageId);
				logger.config("status=" + status);
				couponService.receiveStatus(deliver);

			} else {
//				System.out.println("收到" + deliver.getSrcTermId() + "发送的短信。"+"短信内容为：" + deliver.getMsgContent().getMessage());
				// 收到手机回复
				logger.info("收到" + deliver.getSrcTermId() + "发送的短信");
				logger.info("短信内容为：" + deliver.getMsgContent().getMessage());
				couponService.receiveMessage(deliver);
			}
			return;
		}
		if (message instanceof EMPPSyncAddrBookResp) {
			EMPPSyncAddrBookResp resp = (EMPPSyncAddrBookResp) message;
			if (resp.getResult() != EMPPSyncAddrBookResp.RESULT_OK)
				logger.warning("同步通讯录失败");
			else {
				logger.info("收到服务器发送的通讯录信息");
				logger.info("通讯录类型为：" + resp.getAddrBookType());
				logger.info(resp.getAddrBook());
			}
		}
		if (message instanceof EMPPChangePassResp) {
			EMPPChangePassResp resp = (EMPPChangePassResp) message;
			if (resp.getResult() == EMPPChangePassResp.RESULT_VALIDATE_ERROR)
				logger.severe("更改密码：验证失败");
			if (resp.getResult() == EMPPChangePassResp.RESULT_OK) {
				logger.info("更改密码成功,新密码为：" + resp.getPassword());
				emppApi.setPassword(resp.getPassword());
			}
			return;

		}
		if (message instanceof EMPPReqNoticeResp) {
			EMPPReqNoticeResp response = (EMPPReqNoticeResp) message;
			if (response.getResult() != EMPPReqNoticeResp.RESULT_OK)
				logger.severe("查询运营商发布信息失败");
			else {
				logger.info("收到运营商发布的信息");
				logger.info(response.getNotice());
			}
			return;
		}
		if (message instanceof EMPPAnswer) {
			logger.info("收到企业疑问解答");
			EMPPAnswer answer = (EMPPAnswer) message;
			logger.info(answer.getAnswer());

		}
//		System.out.println(message);

	}

	// 处理连接断掉事件
	public void OnClosed(Object object) {
		// 该连接是被服务器主动断掉，不需要重连
		if (object instanceof EMPPTerminate) {
			logger.info("收到服务器发送的Terminate消息，连接终止");
			return;
		}
		// 这里注意要将emppApi做为参数传入构造函数
		RecvListener listener = new RecvListener(couponService,emppApi, reconnectTime,logger);
		logger.info("连接断掉次数：" + (++closedCount));
		for (int i = 1; !emppApi.isConnected(); i++) {
			try {
				logger.info("重连次数:" + i);
				Thread.sleep(reconnectTime);
				emppApi.reConnect(listener);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "", e);
			}
		}
		logger.info("重连成功");
	}

	// 处理错误事件
	public void OnError(Exception e) {
		logger.log(Level.SEVERE, "", e);
	}
}
