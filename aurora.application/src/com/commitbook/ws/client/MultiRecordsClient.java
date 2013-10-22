package com.commitbook.ws.client;

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

public class MultiRecordsClient {
	public static void main(String[] args) throws AxisFault {
		ServiceClient client = new ServiceClient();
		Options options = new Options();
		options.setTo(new EndpointReference("http://localhost:8081/hec2dev/modules/sys/test/sys_web_service_multi_records_test.svc"));
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
		OMElement parameter = factory.createOMElement(new QName("http://www.aurora-framework.org/schema", "parameter"));
		OMElement requestHead = factory.createOMElement(new QName("requestHead"));
		OMElement requestBody = factory.createOMElement(new QName("requestBody"));
		OMElement records = factory.createOMElement(new QName("records"));
		OMElement record1 = factory.createOMElement(new QName("record"));
		record1.addAttribute("record_id", "11", null);
		record1.addAttribute("record_code", "multi1", null);
		records.addChild(record1);
		OMElement record2 = factory.createOMElement(new QName("record"));
		record2.addAttribute("record_id", "22", null);
		record2.addAttribute("record_code", "multi2", null);
		records.addChild(record2);
		requestBody.addChild(records);
		parameter.addChild(requestHead);
		parameter.addChild(requestBody);
		return parameter;
	}
}
