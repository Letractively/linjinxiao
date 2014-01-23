package aurora.plugin.proc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.JSONAdaptor;
import uncertain.event.RuntimeContext;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILoggerProvider;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import aurora.service.IServiceFactory;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.ServiceInvoker;
import aurora.service.ServiceOutputConfig;

public class ProcRunner {

	public static String executeProcAndGetLog(IObjectRegistry registry, String strContext, String strProcContent) {
		if (registry == null)
			throw new IllegalArgumentException("parameter of IObjectRegistry can not be null!");
		IProcedureManager procedureManager = (IProcedureManager) registry.getInstanceOfType(IProcedureManager.class);
		if (procedureManager == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IProcedureManager.class, ProcRunner.class.getCanonicalName());

		StringBuffer sb = new StringBuffer();
		try {
			CompositeMap cmSvcContent = CompositeLoader.createInstanceForOCM().loadFromString(strProcContent, "UTF-8");
			CompositeMap cmProcContent = parseProcedureConfig(cmSvcContent);
			CompositeMap cmContext = null;
			if (strContext != null && !"".equals(strContext)) {
				CompositeLoader loader = new CompositeLoader();
				cmContext = loader.loadFromString(strContext, "UTF-8");
				clearInstance(cmContext);
			} else {
				cmContext = new CompositeMap("context");
			}
			Procedure proc = procedureManager.createProcedure(cmProcContent);
			ILoggerProvider loggerProvider = null;

			loggerProvider = new StringBufferLoggerProvider();

			StringBufferLoggerProvider.setCurrentStringBuffer(sb);
			executeProc(registry, cmContext, proc, loggerProvider);
			StringBufferLoggerProvider.remove();
			String resp = getSvcOutput(cmSvcContent, cmContext);
			if (resp != null) {
				System.out.println(resp);
				sb.append(resp);
			}

		} catch (Exception e) {
			sb.append(StringBufferLogger.LINE_SEPARATOR).append("exception: ").append(StringBufferLogger.LINE_SEPARATOR);
			String exceptionMessage = getFullStackTrace(e);
			System.out.println(exceptionMessage);
			sb.append(exceptionMessage);
		}
		String message = sb.toString();
		return message;
	}
	
	private static void clearInstance(CompositeMap context) {
		if (context == null)
			return;
		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<Object, Object>> it = context.entrySet().iterator();
		if (it == null)
			return;
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
	}


	private static String getSvcOutput(CompositeMap svc_config, CompositeMap context) throws Exception {
		if ("procedure".equalsIgnoreCase(svc_config.getName()))
			return null;
		ServiceInstance svc = ServiceInstance.getInstance(context);
		svc.setServiceConfigData(svc_config);
		return getSvcResponse(svc);
	}

	private static String getSvcResponse(ServiceInstance svc) throws IOException, JSONException {

		StringBuffer sb = new StringBuffer();

		ServiceContext service_context = svc.getServiceContext();
		String output = null;
		Set<String> arrays_set = null;

		ServiceOutputConfig cfg = svc.getServiceOutputConfig();
		if (cfg != null) {
			output = cfg.getOutput();
			String names_str = cfg.getArrays();
			if (names_str != null) {
				String[] arrays = names_str.split(",");
				arrays_set = new HashSet<String>();
				for (String s : arrays)
					arrays_set.add(s);
			}
		}
		JSONObject json = new JSONObject();
		// Write success flag
		json.put("success", service_context.isSuccess());
		// Write service invoke result
		boolean write_result = service_context.getBoolean("write_result", true);
		if (write_result) {
			// CompositeMap result = context_map.getChild("result");
			CompositeMap result = null;
			if (output != null) {
				Object obj = service_context.getObjectContext().getObject(output);
				if (!(obj instanceof CompositeMap))
					throw new IllegalArgumentException("Target for JSON output is not instance of CompositeMap: " + obj);
				result = (CompositeMap) obj;
			} else
				result = service_context.getModel();
			if (result != null) {
				JSONObject o = JSONAdaptor.toJSONObject(result, arrays_set);
				json.put("result", o);
			}
			String jsonData = getJsonData(json);
			if (result != null) {
				sb.append(StringBufferLogger.LINE_SEPARATOR).append("web service response: ").append(StringBufferLogger.LINE_SEPARATOR);
				String cmContent = result.toXML();
				sb.append(cmContent);
				sb.append(StringBufferLogger.LINE_SEPARATOR).append("json response: ").append(StringBufferLogger.LINE_SEPARATOR);
				sb.append(jsonData);
			}
		}
		return sb.toString();
	}

	private static String getJsonData(JSONObject json) throws JSONException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		json.write(pw);
		pw.close();
		return baos.toString();
	}

	private static CompositeMap parseProcedureConfig(CompositeMap svc) {
		if ("procedure".equalsIgnoreCase(svc.getName()))
			return svc;
		CompositeMap initProcedure = svc.getChild("init-procedure");
		if (initProcedure != null) {
			CompositeMap clone = (CompositeMap) initProcedure.clone();
			clone.setName("procedure");
			clone.setNameSpace("p", "uncertain.proc");
			return clone;
		}
		return svc;
	}

	public static void executeProc(IObjectRegistry registry, CompositeMap context, Procedure proc, ILoggerProvider loggerProvider) throws Exception {
		IServiceFactory serviceFactory = (IServiceFactory) registry.getInstanceOfType(IServiceFactory.class);
		if (serviceFactory == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IServiceFactory.class, ProcRunner.class.getName());
		if (loggerProvider != null)
			RuntimeContext.getInstance(context).setInstanceOfType(ILoggerProvider.class, loggerProvider);
		String service_name = "proc";
		ServiceInvoker.invokeProcedureWithTransaction(service_name, proc, serviceFactory, context);
	}

	private static String getFullStackTrace(Throwable exception) {
		String message = getExceptionStackTrace(exception);
		return message;
	}

	private static String getExceptionStackTrace(Throwable exception) {
		if (exception == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(baos);
		exception.printStackTrace(pw);
		pw.close();
		return baos.toString();
	}

}
