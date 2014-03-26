package met.com;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

import com.sun.xml.internal.messaging.saaj.util.Base64;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.composite.XMLOutputter;
import uncertain.exception.ConfigurationFileException;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import aurora.service.ServiceThreadLocal;
import aurora.service.ws.SOAPServiceInterpreter;


public class SSOForJavaServiceStub {
	public static final int DEFAULT_CONNECT_TIMEOUT = 60 * 1000;
	public static final int DEFAULT_READ_TIMEOUT = 600 * 1000;
	
	boolean raiseExceptionOnError = true;
	int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	int readTimeout = DEFAULT_READ_TIMEOUT;
	String user;
	String password;
	boolean noCDATA = false;

	public static final String WS_INVOKER_ERROR_CODE = "aurora.service.ws.invoker_error";
	
	public String getCompanyByUserName(String url,GetCompanyByUserName getCompanyByUserName) throws Exception {
		CompositeMap response = request(url,getCompanyByUserName.toXML());
		String result = getCompanyByUserName.getResult(response);
		return result;
	}
	
	public boolean validateEACToken(String url,ValidateEACToken validateEACToken) throws Exception {
		CompositeMap response = request(url,validateEACToken.toXML());
		String result = validateEACToken.getResult(response);
		return Boolean.valueOf(result);
	}

	public String createToken(String url,CreateToken ssoCreateToken) throws Exception {
		CompositeMap response = request(url,ssoCreateToken.toXML());
		String result = ssoCreateToken.getResult(response);
		return result;
	}

	public String postService(String url,PostService ssoPostService)throws Exception {
		CompositeMap response = request(url,ssoPostService.toXML());
		String result = ssoPostService.getResult(response);
		return result;
	}
	
	public CompositeMap request(String requestUrl,CompositeMap requestBody) throws Exception{
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		URI uri = new URI(requestUrl);
		URL url = uri.toURL();
		PrintWriter out = null;
		BufferedReader br = null;
		CompositeMap soapBody = createSOAPBody();
		soapBody.addChild(requestBody);
		String content = XMLOutputter.defaultInstance().toXML(soapBody.getRoot(), true);
//		content = new String(content.getBytes(),"UTF-8");
		LoggingContext.getLogger(context, this.getClass().getCanonicalName()).config("request:\r\n" + content);
		if(isNoCDATA()){
			content = removeCDATA(content);
		}
		HttpURLConnection httpUrlConnection = null;
		try {
			httpUrlConnection = (HttpURLConnection) url.openConnection();

			httpUrlConnection.setDoInput(true);
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setRequestMethod("POST");

			httpUrlConnection.setConnectTimeout(connectTimeout);
			httpUrlConnection.setReadTimeout(readTimeout);

			// set request header
			addAuthorization(httpUrlConnection, context);
			httpUrlConnection.setRequestProperty("SOAPAction", "urn:anonOutInOp");
			httpUrlConnection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			httpUrlConnection.connect();
			OutputStream os = httpUrlConnection.getOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(os,"UTF-8");
			writer.write("<?xml version='1.0' encoding='UTF-8'?>");
			writer.write(content);
			writer.flush();
			writer.close();
			String soapResponse = null;
			CompositeMap soap = null;
			CompositeLoader cl = new CompositeLoader();
			// http status ok
			if (HttpURLConnection.HTTP_OK == httpUrlConnection.getResponseCode()) {
				soapResponse = inputStream2String(httpUrlConnection.getInputStream());
				LoggingContext.getLogger(context, this.getClass().getCanonicalName()).config("HTTP_OK. response:" + soapResponse);
			} else {
				soapResponse = inputStream2String(httpUrlConnection.getInputStream());
				LoggingContext.getLogger(context, this.getClass().getCanonicalName()).config("HTTP_ERROR. response:" + soapResponse);
				if (raiseExceptionOnError) {
					throw new ConfigurationFileException(WS_INVOKER_ERROR_CODE, new Object[] { url, soapResponse }, null);
				}
			}
			httpUrlConnection.disconnect();
			soap = cl.loadFromString(soapResponse, "UTF-8");
			CompositeMap result = (CompositeMap) soap.getChild(SOAPServiceInterpreter.BODY.getLocalName()).getChilds().get(0);
			return result;
		} catch (Exception e) {
			LoggingContext.getLogger(context, this.getClass().getCanonicalName()).log(Level.SEVERE, "", e);
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				out.close();
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();
			}
		}
	}
	private CompositeMap createSOAPBody() {
		CompositeMap env = new CompositeMap(SOAPServiceInterpreter.ENVELOPE.getPrefix(), "http://www.w3.org/2003/05/soap-envelope",
				SOAPServiceInterpreter.ENVELOPE.getLocalName());
		CompositeMap body = new CompositeMap(SOAPServiceInterpreter.BODY.getPrefix(), "http://www.w3.org/2003/05/soap-envelope",
				SOAPServiceInterpreter.BODY.getLocalName());
		env.addChild(body);
		return body;
	}
	public boolean isNoCDATA() {
		return noCDATA;
	}

	public void setNoCDATA(boolean noCDATA) {
		this.noCDATA = noCDATA;
	}
	private String removeCDATA(String source){
		source = source.replaceAll("<!\\[CDATA\\[","");  
		source =  source.replaceAll("]]>","");   
		return source;
				
	}
	private void addAuthorization(HttpURLConnection httpUrlConnection, CompositeMap context) {
		if (user == null)
			return;
		ILogger logger = LoggingContext.getLogger(context, this.getClass().getCanonicalName());
		String userName = TextParser.parse(user, context);
		String passwd = TextParser.parse(password, context);
		String fullText = userName + ":" + passwd;
		logger.config("plan user/password:" + fullText);
		String decodeText = "Basic " + new String(Base64.encode(fullText.getBytes()));
		logger.config("decode user/password:" + decodeText);
		httpUrlConnection.setRequestProperty("Authorization", decodeText);
	}
	public String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		String result = new String(baos.toByteArray(), "UTF-8");
		return result;
	}

}
