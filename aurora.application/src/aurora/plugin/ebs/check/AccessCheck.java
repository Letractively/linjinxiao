package aurora.plugin.ebs.check;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.apps.fnd.common.WebAppsContext;
import oracle.apps.fnd.common.WebRequestUtil;
import oracle.apps.fnd.functionSecurity.Function;
import oracle.apps.fnd.functionSecurity.FunctionSecurity;
import oracle.apps.fnd.functionSecurity.SecurityGroup;
import oracle.apps.fnd.sso.Utils;

import uncertain.composite.CompositeMap;
import uncertain.datatype.StringType;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.proc.ProcedureRunner;
import aurora.database.DBUtil;
import aurora.database.FetchDescriptor;
import aurora.database.ResultSetLoader;
import aurora.database.rsconsumer.CompositeMapCreator;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.ServiceThreadLocal;
import aurora.service.http.HttpServiceInstance;

public class AccessCheck extends AbstractLocatableObject {

	public static Hashtable<String, Integer> cacheFunction = new Hashtable<String, Integer>();
	ILogger logger;

	public static void run() throws Exception {
		AccessCheck checker = new AccessCheck();
		checker.execute();
	}

	public void execute() throws Exception {
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		if (context == null) {
			context = new CompositeMap("context");
		}
		logger = LoggingContext.getLogger(context, AccessCheck.class.getCanonicalName());
		ServiceContext serviceContext = ServiceContext.createServiceContext(context);
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = svc.getRequest();
		HttpServletResponse response = svc.getResponse();
		WebAppsContext webAppcontext = WebRequestUtil.validateContext(request, response);
		logger.log("AccessCheck..");
		logger.log("context:" + context.toXML());

		boolean ajaxRequest = isAjaxRequest(request, serviceContext);
		if (!ajaxRequest) {
			String reqTraxId = (String) request.getAttribute("ICX_TRANSACTION_ID");
			logger.log("Attribute ICX_TRANSACTION_ID:" + reqTraxId);
			if (reqTraxId != null) {
				String paraTraxId = request.getParameter("transactionid");
				logger.log("para transactionid:" + paraTraxId);
				if (!reqTraxId.equals(paraTraxId)) {
					context.putObject("/access-check/@status_code", "unauthorized", true);
				}
				return;
			}
			String service_name = svc.getName();
			logger.log("service_name:" + service_name);
			Function function = queryFunction(service_name, webAppcontext);
			if (function != null) {
				String redirectUrl = getLocalRFUrl(function);
				logger.log("redirectUrl:" + redirectUrl);
				if (redirectUrl != null && !"".equals(redirectUrl)) {
					context.putObject("/access-check/@status_code", "redirect", true);
					context.putObject("/access-check/@redirectUrl", redirectUrl, true);
				}
			}
			return;
		} else {
			String referer = request.getHeader("referer");
			logger.log("referer:" + referer);
			if (referer == null) {
				context.putObject("/access-check/@status_code", "unauthorized", true);
				return;
			}
			URL refUrl = new URL(referer);
			Object functionid = getParameterValue(refUrl.getQuery(), "function_id");
			if (functionid == null) {
				context.putObject("/access-check/@status_code", "unauthorized", true);
				return;
			}

			Function function = queryFunction(Integer.valueOf(functionid.toString()), webAppcontext);
			if (function != null) {
				String webHtmlCall = function.getWebHTMLCall();
				logger.log("webHtmlCall:" + webHtmlCall);
				if (webHtmlCall != null) {
					int screenIndex = webHtmlCall.indexOf(".screen");
					if (screenIndex != -1)
						return;
				}
			}
			context.putObject("/access-check/@status_code", "unauthorized", true);
			return;
		}

		// String functionId = request.getParameter("function_id");
		// if(functionId)
		//
		// String requestBase =
		// "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		// logger.log("requestBase:"+requestBase);
		// String referer = request.getHeader("referer");
		// logger.log("referer:"+referer);
		// if(referer != null && referer.startsWith(requestBase)){
		// return;
		// }
	}

	public static String getParameterValue(String queryString, String key) {
		String[] params = queryString.split("&");
		for (String param : params) {
			String[] kv = param.split("=");
			if (key.equalsIgnoreCase(kv[0])) {
				return kv[1];
			}
		}
		return null;
	}

	public static String getLocalRFUrl(Function localFunction) {
		boolean bool = Utils.isAppsContextAvailable();
		WebAppsContext localWebAppsContext = Utils.getAppsContext();
		String localRFUrl = null;
		try {
			FunctionSecurity localFunctionSecurity = new FunctionSecurity(localWebAppsContext);
			SecurityGroup localSecurityGroup = localFunctionSecurity.getSecurityGroup(0L);
			localRFUrl = localFunctionSecurity.getRunFunctionURL(localFunction, null, localSecurityGroup, "");
		} finally {
			if (!bool) {
				Utils.releaseAppsContext();
			}
		}
		return localRFUrl;
	}

	public Function queryFunction(int functionId, WebAppsContext webAppcontext) throws Exception {
		FunctionSecurity functionSecurity = new FunctionSecurity(webAppcontext);
		Function function = functionSecurity.getFunction(functionId);
		return function;
	}

	public Function queryFunction(String service_full_name, WebAppsContext webAppcontext) throws Exception {
		logger.log("cacheFunction:" + cacheFunction + " service_full_name:" + service_full_name);
		Integer functionId = cacheFunction.get(service_full_name);
		FunctionSecurity functionSecurity = new FunctionSecurity(webAppcontext);
		if (functionId != null) {
			Function function = functionSecurity.getFunction(functionId);
			if (service_full_name.equals(function.getWebHTMLCall()))
				return function;
		}
		functionId = queryFunctionId(service_full_name, webAppcontext);
		if (functionId > 0) {
			return functionSecurity.getFunction(functionId);
		}
		return null;
	}

	public int queryFunctionId(String service_full_name, WebAppsContext webAppcontext) throws Exception {
		StringBuffer query_sql = new StringBuffer();
		query_sql.append(" select t.function_id");
		query_sql.append("   from fnd_form_functions t ");
		query_sql.append("  where t.web_html_call= ? ");

		PrepareParameter[] parameters = new PrepareParameter[1];
		parameters[0] = new PrepareParameter(new StringType(), service_full_name);
		CompositeMap result = sqlQueryWithParas(webAppcontext, query_sql.toString(), parameters);
		if (result != null) {
			logger.log("result:" + result.toXML());
			List<CompositeMap> childList = result.getChilds();
			if (childList != null) {
				if (childList.size() > 1)
					throw new IllegalArgumentException(" find more than one record with parameter:'web_html_call'=" + service_full_name);
				CompositeMap record = childList.get(0);
				int function_id = record.getInt("function_id");
				if (function_id > 0)
					cacheFunction.put(service_full_name, function_id);
				return function_id;
			}
		}
		return -1;
	}

	public CompositeMap sqlQueryWithParas(WebAppsContext webAppcontext, String prepareSQL, PrepareParameter[] prepareParameters) throws Exception {
		ResultSet resultSet = null;
		CompositeMap result = new CompositeMap("result");
		PreparedStatement st = null;
		try {
			Connection conn = webAppcontext.getJDBCConnection();
			st = conn.prepareStatement(prepareSQL);
			if (prepareParameters != null) {
				for (int i = 0; i < prepareParameters.length; i++) {
					PrepareParameter parameter = prepareParameters[i];
					parameter.getDataType().setParameter(st, i + 1, parameter.getValue());
				}
			}
			resultSet = st.executeQuery();
			ResultSetLoader mRsLoader = new ResultSetLoader();
			mRsLoader.setFieldNameCase(Character.LOWERCASE_LETTER);
			FetchDescriptor desc = FetchDescriptor.fetchAll();
			CompositeMapCreator compositeCreator = new CompositeMapCreator(result);
			mRsLoader.loadByResultSet(resultSet, desc, compositeCreator);
		} finally {
			DBUtil.closeStatement(st);
			webAppcontext.releaseJDBCConnection();
		}
		return result;
	}

	private boolean isAjaxRequest(HttpServletRequest request, ServiceContext context) {
		String httptype = request.getHeader("x-requested-with");
		if ("XMLHttpRequest".equals(httptype)) {
			return true;
		}
		return false;
	}

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = svc.getRequest();
		HttpServletResponse response = svc.getResponse();
		WebAppsContext webAppcontext = WebRequestUtil.validateContext(request, response);
		int validId = WebRequestUtil.verifyTransaction(request, response, webAppcontext);
		if (validId == -1) {
			context.putObject("/access-check/@status_code", "unauthorized", true);
			// String errorUrl =
			// "http://syfdemo.ml.com:8000/OA_HTML/jsp/fnd/fnderror.jsp?text=此功能不是当前用户的有效责任。请联系您的系统管理员。";
		}
	}

}
