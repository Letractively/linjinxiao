package org.linjinxiao.ws.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sun.xml.internal.messaging.saaj.util.Base64;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import aurora.service.ws.SOAPServiceInterpreter;

public class UrlClient {
	public static void main(String[] args) throws Exception {
		(new UrlClient()).run();
	}
	public void run() throws Exception{
		 
		
		URI uri = new URI("http://localhost:8081/hec2dev/modules/sys/ws_test.svc");
		URL url = uri.toURL();
		PrintWriter out = null;
		BufferedReader br = null;
		HttpURLConnection httpUrlConnection = null;
		try {
//			Authenticator.setDefault(new Authenticator() {
//			      protected PasswordAuthentication getPasswordAuthentication() {
//			        return new
//			           PasswordAuthentication("linjinxiao","linjinxiao".toCharArray());
//			    }});
			httpUrlConnection = (HttpURLConnection) url.openConnection();

			httpUrlConnection.setDoInput(true);
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setRequestMethod("POST");
			
			String encoded = new String
				      (Base64.encode(new String("linjinxiao1:ok").getBytes()));
			httpUrlConnection.setRequestProperty("Authorization", "Basic " + encoded);


			// set request header
			httpUrlConnection.setRequestProperty("SOAPAction", "");
			httpUrlConnection.setRequestProperty("Content-Type",
					"text/xml; charset=UTF-8");
//			httpUrlConnection.setAllowUserInteraction(true);
			// httpUrlConnection.setRequestProperty("Content-Length",
			// soapReuqest.length() + "");
			httpUrlConnection.connect();
			OutputStream os = httpUrlConnection.getOutputStream();
			out = new PrintWriter(os);
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
			CompositeMap soapBody = createSOAPBody();
			CompositeMap parameter = new CompositeMap("parameter");
			soapBody.addChild(parameter);
			String header = "<requestHead seqNo=\"1\"/>";
			String content = "<requestBody>"+
							   " <records>"+
							       "<record ACCOUNT_ID=\"22\" ACCOUNT_OTHER_CODE=\"test\"/>"+
							      " <record ACCOUNT_ID=\"33\" ACCOUNT_OTHER_CODE=\"test2\"/>"+
							   " </records>"+
							  "</requestBody>";
			parameter.addChild((new CompositeLoader().loadFromString(header)));
			CompositeMap test = (new CompositeLoader().loadFromString(content));
			parameter.addChild(test);
//			soapBody.addChild((CompositeMap) inputObject);
			out.println(soapBody.getRoot().toXML());
			out.flush();
			out.close();
			System.out.println(soapBody.getRoot().toXML());
			String soapResponse = null;
			// http status ok
			if (HttpURLConnection.HTTP_OK == httpUrlConnection
					.getResponseCode()) {
				soapResponse = inputStream2String(httpUrlConnection.getInputStream());
			}else{
				soapResponse = inputStream2String(httpUrlConnection.getErrorStream());
			}
			httpUrlConnection.disconnect();
			System.out.println("getResponseCode:"+httpUrlConnection.getResponseCode());
			System.out.println("soapResponse:"+soapResponse);
			if (soapResponse == null || "".equals(soapResponse))
				return;
			CompositeLoader cl = new CompositeLoader();
			CompositeMap soap = cl.loadFromString(soapResponse);
			CompositeMap result = (CompositeMap) soap.getChild(
					SOAPServiceInterpreter.BODY.getLocalName()).getChilds()
					.get(0);
			System.out.println("result:"+result.toXML());
//			if (returnPath != null)
//				runner.getContext().putObject(returnPath, result, true);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (out != null) {
				out.close();
			}
			if (br != null) {
				br.close();
			}
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();
			}
		}

	}
	public String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}
	private CompositeMap createSOAPBody() {
		CompositeMap env = new CompositeMap(SOAPServiceInterpreter.ENVELOPE
				.getPrefix(), SOAPServiceInterpreter.ENVELOPE.getNameSpace(),
				SOAPServiceInterpreter.ENVELOPE.getLocalName());
		CompositeMap body = new CompositeMap(SOAPServiceInterpreter.BODY
				.getPrefix(), SOAPServiceInterpreter.BODY.getNameSpace(),
				SOAPServiceInterpreter.BODY.getLocalName());
		env.addChild(body);
		return body;
	}
}

