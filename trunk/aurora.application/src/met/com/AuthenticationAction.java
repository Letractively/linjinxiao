package met.com;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import aurora.service.ServiceInstance;
import aurora.service.ServiceThreadLocal;
import aurora.service.http.HttpServiceInstance;

// ---------------------------------------------------------------------------------
// 配置文件 或 常量
//
// 本应用Context地址
// Constants.CLOUD_DOMAIN = "http://localhost:8080"
//
// 固定值
// Constants.CLOUD_SSO_IASID = "01"
//
// 单点登录跳转地址
// Constants.CLOUD_SSO_EACURL = "http://192.168.2.120:907/Account/Login"
//
// webservice接口地址
// cloud.sso.webservice.url=http://192.168.2.120:888/SSOForJavaService.asmx 
// ---------------------------------------------------------------------------------

/**
 * 
 * @author duser
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class AuthenticationAction {

//	public static String DEFAULT_CLOUD_DOMAIN = "http://192.168.2.38:9001";
//	public static String DEFAULT_CLOUD_SSO_IASID = "01";
//	public static String DEFAULT_CLOUD_SSO_EACURL = "http://192.168.2.120:907/Account/Login";

//	public static String DEFAULT_LOGIN_WS_URL = "http://192.168.2.120:888/SSOForJavaService.asmx";
//	public static String DEFAULT_GET_COMPANY_WS_URL = "http://192.168.2.120:908/Service.asmx";

	SSOForJavaServiceStub ssoServiceStub;
	String targetAppUrl;
	String IASID;
	String EACURL;
	String login_ws_url;
	String get_company_ws_url;
	
//	String ssoServiceStub
	ILogger logger;
	
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

//	public static void loginUser() throws Exception {
//		loginUser(DEFAULT_CLOUD_DOMAIN, DEFAULT_CLOUD_SSO_IASID, DEFAULT_CLOUD_SSO_EACURL, DEFAULT_LOGIN_WS_URL,DEFAULT_GET_COMPANY_WS_URL);
//	}

	public static void loginUser(String appUrl, String IASID, String EACURL, String login_ws_url,String get_company_ws_url) throws Exception {
		AuthenticationAction auth = new AuthenticationAction();
		auth.targetAppUrl = appUrl;
		auth.IASID = IASID;
		auth.EACURL = EACURL;
		auth.login_ws_url = login_ws_url;
		auth.get_company_ws_url = get_company_ws_url;
		auth.execute();
		
	}
	public void execute() throws Exception{
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		if(context == null){
			context = new CompositeMap("context");
		}
		logger = LoggingContext.getLogger(context, this.getClass().getCanonicalName());
		ssoServiceStub = new SSOForJavaServiceStub();
		logger.config("NEW LOGIN ACTION is HERE !");
		
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
		boolean anthResult = loginBtsUser(svc);
		context.putObject("/parameter/@sso_eacurl", EACURL, true);
		context.putObject("/parameter/@anthResult", anthResult, true);
		context.putObject("/parameter/@user_name", userId, true);
		// 验证成功，转跳
		if (anthResult) {
			GetCompanyByUserName getCompany = new GetCompanyByUserName();
			getCompany.setUserName(userId);
			String companyCode = ssoServiceStub.getCompanyByUserName(get_company_ws_url,getCompany);
			context.putObject("/parameter/@company_code", companyCode, true);
		}
		
	}

	
	private boolean loginBtsUser(HttpServiceInstance svc) {
		// 验证结果
		boolean authResult = false;
		// 取得登录参数
		// HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletRequest request = svc.getRequest();
		String IASID = request.getParameter("IASID");
		String timeStamp = request.getParameter("TimeStamp");
		// 取用户访问地址
		String appUrl = request.getParameter("AppUrl");
		if (appUrl == null || ("").equals(appUrl)) {
			String queryStr = request.getQueryString();
			appUrl = targetAppUrl + request.getRequestURI();
			if (queryStr == null) {
				// appUrl = request.getRequestURL().toString();
			} else {
				// appUrl = request.getRequestURL() + "?" + appUrl;
				appUrl = appUrl + "?" + queryStr;
			}
		}
		String userAccount = request.getParameter("UserAccount");
		String authenticator = request.getParameter("Authenticator");

		if (IASID == null || ("").equals(IASID)) {
			// 首次登录
			loginNewUser(appUrl, svc);
		} else if ((IASID != null && !("").equals(IASID)) && (timeStamp != null && !("").equals(timeStamp)) && (appUrl != null && !("").equals(appUrl))
				&& (userAccount != null && !("").equals(userAccount)) && (authenticator != null && !("").equals(authenticator))) {
			// 已经带有用户信息，进行进一步验证
			try {
//				SSOForJavaServiceStub ssoServiceStub = new SSOForJavaServiceStub();
				// 验证用户帐户token值
				ValidateEACToken validateEACToken = new ValidateEACToken();
				validateEACToken.setIASID(IASID);
				validateEACToken.setTimeStamp(timeStamp);
				validateEACToken.setAppUrl(appUrl);
				validateEACToken.setUserAccount(userAccount);
				validateEACToken.setAuthenticator(authenticator);
				boolean tokenValid = ssoServiceStub.validateEACToken(login_ws_url, validateEACToken);
				logger.config("++++++AuthenticationAction.loginBtsUser++++++ DATA : Validate Token result : " + tokenValid);
				if (tokenValid == false) {
					// 验证失败，重新登录
					loginNewUser(appUrl, svc);
				} else {
					// 登录成功，参数传递，进入j_spring_security_check(UserDetailServiceImpl)进行权限处理
					userId = userAccount;
					authResult = true;
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "", e);
			}
		}
		return authResult;
	}

	/**
	 * 新用户登录，转向云平台登录页面
	 */
	private void loginNewUser(String appUrl, HttpServiceInstance svc) {
		logger.config("++++++AuthenticationAction.loginNewUser++++++ DATA : loginNewUser START......");
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String now = sdf.format(date);
		String timeStamp = now;
		// String appUrl ="http://www.baidu.com";
		try {
//			SSOForJavaServiceStub ssoServiceStub = new SSOForJavaServiceStub();
			CreateToken ssoCreateToken = new CreateToken();
			ssoCreateToken.setAppUrl(appUrl);
			ssoCreateToken.setIASID(IASID);
			ssoCreateToken.setTimeStamp(timeStamp);
			String strToken = ssoServiceStub.createToken(login_ws_url,ssoCreateToken);
			logger.config("++++++AuthenticationAction.loginNewUser++++++ DATA : create token : " + strToken);
			PostService ssoPostService = new PostService();
			ssoPostService.setStrToken(strToken);
			ssoPostService.setEACUrl(EACURL);
			String _responseStr = ssoServiceStub.postService(login_ws_url,ssoPostService);
			logger.config("++++++AuthenticationAction.loginNewUser++++++ DATA : response write to brower html string = " + _responseStr);
			// Struts2Utils.renderHtml(_responseStr, "encoding:GBK",
			// "no-cache:false");
			HttpServletResponse response = svc.getResponse();
			response.setContentType("text/html;charset=UTF-8");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			PrintWriter out = response.getWriter();
			out.write(_responseStr);
			out.flush();
			out.close();
			logger.config("++++++AuthenticationAction.loginNewUser++++++ DATA : loginNewUser END......");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}