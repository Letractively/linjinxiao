package org.linjinxiao.webservice;

import java.rmi.RemoteException;

import org.aurora.www.simple.CancatRequest;
import org.aurora.www.simple.CancatResponse;
import org.aurora.www.simple.SimpleStub;


public class SimpleClient {
	public static void main(String[] args) throws RemoteException {
		SimpleStub ss = new SimpleStub();
		CancatRequest cc = new CancatRequest();
		cc.setS1("<abc test='a'/>");
		cc.setS2("ok");
		CancatResponse cr = ss.cancat(cc);
		System.out.println(cr.getCancatResponse());
	}
}
