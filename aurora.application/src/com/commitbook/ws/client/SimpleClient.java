package com.commitbook.ws.client;

import java.rmi.RemoteException;

import com.commitbook.simple.CancatRequest;
import com.commitbook.simple.CancatResponse;
import com.commitbook.simple.SimpleStub;


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
