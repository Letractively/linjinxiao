package aurora.service.http;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IConsumer;
import aurora.application.features.msg.IMessage;
import aurora.application.features.msg.IMessageListener;
import aurora.application.features.msg.IMessageStub;
import aurora.application.features.msg.INoticerConsumer;

public class NotifyMessageAsyncServlet extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ConcurrentHashMap<String,SessionMessageHandler> cookie_lock_map = new ConcurrentHashMap<String,SessionMessageHandler>();
	private ILogger logger;
	private ExecutorService exec;

	public void init(ServletConfig config) throws ServletException {
		UncertainEngine uncertainEngine = WebContextInit.getUncertainEngine(config.getServletContext());
		if (uncertainEngine == null)
			throw new ServletException("Uncertain engine not initialized");

	    IObjectRegistry mRegistry = (IObjectRegistry) uncertainEngine.getObjectRegistry();
	    if (mRegistry == null)
			throw new ServletException("IObjectRegistry not initialized");
		
	    exec = Executors.newCachedThreadPool();
		logger = LoggingContext.getLogger(this.getClass().getCanonicalName(), mRegistry);
		MessageListener messageListener = new MessageListener(mRegistry);
		messageListener.onInitliaze();
	}


	/** destroy the executor */
	public void destroy() {
	  exec.shutdown();
	}
	
	public void service(final ServletRequest req, final ServletResponse res)
		    throws ServletException, IOException {

		HttpServletRequest httpServletRequest = (HttpServletRequest)req;
		Cookie[] cookies = httpServletRequest.getCookies();
		String jsessionid = null;
		if(cookies != null){
			for(Cookie cookie:cookies){
				if("JSESSIONID".equalsIgnoreCase(cookie.getName()))
					jsessionid = cookie.getValue();
			}
		}
		if(jsessionid == null)
			return;
//		System.out.println(jsessionid);
		final AsyncContext asyncContext = req.startAsync();
		asyncContext.setTimeout(100000);
		  // attach listener to respond to lifecycle events of this AsyncContext
		asyncContext.addListener(new AsyncListener() {
		    /** complete() has already been called on the async context, nothing to do */
		    public void onComplete(AsyncEvent event) throws IOException {
		    	System.out.println("onComplete ");
		    }
		    /** timeout has occured in async task... handle it */
		    public void onTimeout(AsyncEvent event) throws IOException {
//		      log("onTimeout called");
//		      log(event.toString());
		      asyncContext.getResponse().getWriter().write("{\"result\":\"TimeOUt\"}");
		      asyncContext.complete();
		    }
		    /** THIS NEVER GETS CALLED - error has occured in async task... handle it */
		    public void onError(AsyncEvent event) throws IOException {
		      log("onError called");
		      log(event.toString());
		      asyncContext.getResponse().getWriter().write("ERROR");
		      asyncContext.complete();
		    }
		    /** async context has started, nothing to do */
		    public void onStartAsync(AsyncEvent event) throws IOException {
		    	System.out.println("onStart ");
		    }
		  });
		SessionMessageHandler messageHandler = new SessionMessageHandler(asyncContext);
		cookie_lock_map.put(jsessionid, messageHandler);
		exec.execute(messageHandler);
		
	}
	
	class SessionMessageHandler implements Runnable{
		AsyncContext asyncContext;
		boolean finished = false;
		public SessionMessageHandler(AsyncContext asyncContext){
			this.asyncContext = asyncContext;
		}
		@Override
		public void run() {
			while(finished){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		public void onMessage(IMessage message) {
			try {
				asyncContext.getResponse().getWriter().write("({'result':'Task finished!'})");
				asyncContext.complete();
				finished = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	
	
	class MessageListener implements IMessageListener{

		IObjectRegistry mRegistry;
		protected String topic = "task";
		protected String message = "task_message";
		public MessageListener(IObjectRegistry objectRegistry) {
			this.mRegistry = objectRegistry;
		}
		public void onInitliaze(){
			IMessageStub stub = (IMessageStub) mRegistry.getInstanceOfType(IMessageStub.class);
			if (stub == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IMessageStub.class, this.getClass().getName());
			if (!stub.isStarted())
				logger.warning("JMS MessageStub is not started, please check the configuration.");
			IConsumer consumer = stub.getConsumer(topic);
			if (consumer == null) {
				throw new IllegalStateException("MessageStub does not define the topic '" + topic + "', please check the configuration.");
			}
			if (!(consumer instanceof INoticerConsumer))
				throw BuiltinExceptionFactory.createInstanceTypeWrongException(null, INoticerConsumer.class, IConsumer.class);
			((INoticerConsumer) consumer).addListener(message, this);

		}
		@Override
		public void onMessage(IMessage message) {
			try {
				logger.log(Level.CONFIG, "receive a messsage:" + message.getText());
				CompositeMap properties = message.getProperties();
				String jsessionid = properties.getString("jsessionid");
				if(jsessionid == null){
					String cookie = properties.getString("cookie");
					if(cookie == null)
						return;
					jsessionid = getJsessionidFromCookie(cookie);
				}
//				System.out.println(jsessionid);	
				SessionMessageHandler object = cookie_lock_map.get(jsessionid);
				cookie_lock_map.remove(jsessionid);
				if(object != null)
					object.onMessage(message);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Can not add the task:" + message);
			}
			
		}
		private String getJsessionidFromCookie(String cookie){
			if(cookie == null)
				return null;
			int jessionid_begin = cookie.indexOf("JSESSIONID=");
			if(jessionid_begin == -1)
				return null;
			int valueBegin = jessionid_begin+"JSESSIONID=".length();
			int endIndex = cookie.indexOf(";",valueBegin);
			return cookie.substring(valueBegin, endIndex);
		}
		
	}

}
