package aurora.plugin.coupon;

import java.util.ArrayList;
import java.util.List;

import com.wondertek.esmp.esms.empp.EMPPConnectResp;
import com.wondertek.esmp.esms.empp.EMPPData;
import com.wondertek.esmp.esms.empp.EMPPObject;
import com.wondertek.esmp.esms.empp.EMPPShortMsg;
import com.wondertek.esmp.esms.empp.EMPPSubmitSM;
import com.wondertek.esmp.esms.empp.EmppApi;

public class ApiTest {
	
	String host = "211.136.163.68";
	int port = 9981;
	String accountId = "10657109081853";
	String password = "BingoBolso+0506";
	String serviceId = "10657109081853";
	EmppApi emppApi ;
	public static void main(String[] args) {
		
		ApiTest test = new ApiTest();
		test.run();
	}
	public void run(){

		emppApi = new EmppApi();
		RecvListenerTest listener = new RecvListenerTest(emppApi);

		try {
			EMPPConnectResp response = emppApi.connect(host, port, accountId,
					password, listener);
			System.out.println(response);
			if (response == null) {
				return;
			}
			if (!emppApi.isConnected()) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		if (emppApi.isSubmitable()) {
			
			try{
				String longMessage = "09939测试abc?1111111111111111111111111111111111111111111111111111111111111111111111";
				String shortMessage = "您在test申请了兑换积分券。将获得1张test电子券。请回复您的姓名以确认此号码有效。本短信免费,回复短信不收另外费1111";
				String message = shortMessage;
				String mobile = "13636399739";
				
				int[] sequenceNumbers;
				if (message.length() >= 70) {
					System.out.println("long message.length():"+message.length());
					sequenceNumbers = sendLongMessage(message,
							new String[] { mobile });
				} else {
					System.out.println("short message.length():"+message.length());
					sequenceNumbers = sendShortMessage(message,
							new String[] { mobile });
				}
				StringBuffer sequenceNum = new StringBuffer("");
				for (int i = 0; i < sequenceNumbers.length; i++) {
					sequenceNum.append(sequenceNumbers[i]).append(
							OrderTableUtil.SEQUENCE_SEPARATOR);
				}
				sequenceNum.substring(0, sequenceNum.length() - 1).toString();
				System.out.println("sequenceNum:"+sequenceNum);

			}catch (Exception e1) {
				e1.printStackTrace();
			} 

		}
	}
		public int[] sendLongMessage(String message, String[] mobiles)
				throws Exception {
			int[] sequenceNumbers = emppApi.submitMsgAsync(message, mobiles,
					serviceId);
			return sequenceNumbers;
		}

		public int[] sendShortMessage(String message, String[] mobiles)
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

	
}
