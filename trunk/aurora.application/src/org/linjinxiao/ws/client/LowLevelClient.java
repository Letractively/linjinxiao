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

public class LowLevelClient {
	public static void main(String[] args) throws AxisFault {
		ServiceClient client = new ServiceClient();
		Options options = new Options();
		options.setTo(new EndpointReference(
				"http://localhost:1234/hec2dev/modules/sys/ws_test.svc"));
		String encoded = new String
			      (Base64.encode(new String("linjinxiao:ok").getBytes()));
        List list = new ArrayList();   
	     // Create an instance of org.apache.commons.httpclient.Header   
	    Header header = new Header();   
	    header.setName("Authorization");   
	    header.setValue("Basic " + encoded);   
	    list.add(header);   
	    options.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, list); 
		client.setOptions(options);
		OMElement request = makeRequest();
		OMElement response = client.sendReceive(request);
		System.out.println("ok:"+response.toString());
	}

	private static OMElement makeRequest() {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement parameter = factory.createOMElement(new QName(
				"http://www.aurora-framework.org/schema", "parameter"));
		OMElement requestHead = factory.createOMElement(new QName("requestHead"));
		OMElement requestBody = factory.createOMElement(new QName("requestBody"));
		OMElement records = factory.createOMElement(new QName("records"));
		OMElement record = factory.createOMElement(new QName("record"));
		records.addChild(record);
		requestBody.addChild(records);
		parameter.addChild(requestHead);
		parameter.addChild(requestBody);
//		OMElement s1 = factory.createOMElement(new QName("record"));
		record.addAttribute("ACCOUNT_ID", "123", null);
//		request.addAttribute("description_id", "1233", null);
		record.addAttribute("ACCOUNT_OTHER_CODE", "2", null);
//		record.addAttribute("creation_date", "2011-11-04", null);
//		record.addAttribute("last_updated_by", "1", null);
//		record.addAttribute("last_update_date", "2011-11-12", null);
//		OMElement s2 = factory.createOMElement(new QName("record"));
//		s2.setText("<def>");
//		request.addChild(s1);
//		request.addChild(s2);
		return parameter;
	}
	private static OMElement makeRequest1() {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement request = factory.createOMElement(new QName(
				"http://www.aurora-framework.org/schema", "cancat"));
		OMElement s1 = factory.createOMElement(new QName("s1"));
		s1.setText("<abc test='a'/>");
		OMElement s2 = factory.createOMElement(new QName("s2"));
		s2.setText("<def>");
		request.addChild(s1);
		request.addChild(s2);
		return request;
	}

}
