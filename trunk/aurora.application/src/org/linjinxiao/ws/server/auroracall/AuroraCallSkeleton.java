/**
 * AuroraCallSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
package org.linjinxiao.ws.server.auroracall;

import org.linjinxiao.ws.server.auroracall.type.Record_type0;
import org.linjinxiao.ws.server.auroracall.type.Records_type0;
import org.linjinxiao.ws.server.auroracall.type.SoapResponse;

/**
 * AuroraCallSkeleton java skeleton for the axisService
 */
public class AuroraCallSkeleton {

	/**
	 * Auto generated method signature
	 * 
	 * @param soapRequest
	 * @return soapResponse
	 */

	public org.linjinxiao.ws.server.auroracall.type.SoapResponse auroraCall(org.linjinxiao.ws.server.auroracall.type.SoapRequest soapRequest) {
		Record_type0 record1 = new Record_type0();
		record1.setRecord_id("1111");
		record1.setRecord_code("axis2");
		Record_type0 record2 = new Record_type0();
		record2.setRecord_id("2222");
		record2.setRecord_code("soapUI");
		Records_type0 records = new Records_type0();
		records.addRecord(record1);
		records.addRecord(record2);
		SoapResponse response = new SoapResponse();
		response.setRecords(records);
		return response;
	}

}
