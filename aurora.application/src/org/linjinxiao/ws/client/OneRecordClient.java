package org.linjinxiao.ws.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.httpclient.Header;

import com.sun.xml.internal.messaging.saaj.util.Base64;

public class OneRecordClient {

	public static void main(String[] args) throws AxisFault {
		ServiceClient client = new ServiceClient();
		Options options = new Options();
		options.setTo(new EndpointReference("http://127.0.0.1:8081/hec2dev/modules/sys/test/sys_web_service_one_record_test.svc"));// 修正为实际工程的URL
		addAuthorization("linjinxiao", "ok", options);
		client.setOptions(options);
		OMElement request = makeRequest();
		OMElement response = client.sendReceive(request);
		System.out.println("response:" + response.toString());
	}

	private static void addAuthorization(String userName, String password, Options options) {
		String encoded = new String(Base64.encode(new String(userName + ":" + password).getBytes()));
		List list = new ArrayList();
		// Create an instance of org.apache.commons.httpclient.Header
		Header header = new Header();
		header.setName("Authorization");
		header.setValue("Basic " + encoded);
		list.add(header);
		options.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, list);
	}

	private static OMElement makeRequest() {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement request = factory.createOMElement(new QName("", "parameter"));
		request.addAttribute("record_id", "1", null);
		request.addAttribute("record_code", "axis2", null);
		return request;
	}
}
