/*
 * Created on 2009-4-27
 */
package aurora.presentation.component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Hashtable;
import java.util.logging.Level;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import oracle.apps.fnd.common.WebAppsContext;
import oracle.apps.fnd.framework.webui.OAPageBean;
import oracle.apps.fnd.security.CSS;
import oracle.apps.fnd.sso.SSOManager;
import oracle.apps.jtf.activity.PageObject;
import oracle.apps.jtf.admin.adminconsole.MenuRenderer;
import oracle.apps.jtf.admin.adminconsole.UIXHelper;
import oracle.apps.jtf.base.Logger;
import oracle.apps.jtf.base.SystemLogger;
import oracle.apps.jtf.base.interfaces.MessageManagerInter;
import oracle.apps.jtf.base.resources.Architecture;
import oracle.apps.jtf.base.session.FWSession;
import oracle.apps.jtf.base.session.NotInServletSessionException;
import oracle.apps.jtf.base.session.ServletSessionManager;
import oracle.apps.jtf.base.session.ServletSessionManagerException;
import oracle.apps.jtf.base.session.UnAuthenticatedUserException;
import oracle.apps.jtf.base.syslog.LogFormat;
import oracle.apps.jtf.cabo.wrapper.JTFCaboUtil;
import oracle.apps.jtf.servlet.event.EventEngine;
import oracle.apps.jtf.servlet.event.WebEventDispatcher;
import oracle.apps.jtf.ui.model.PageDataProvider;
import oracle.apps.jtf.util.GeneralPreference;
import oracle.apps.jtf.util.PortalUtil;
import oracle.cabo.ui.RenderingContext;
import oracle.cabo.ui.beans.StyleSheetBean;
import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.util.template.CompositeMapTagCreator;
import uncertain.util.template.ITagContent;
import uncertain.util.template.ITagCreatorRegistry;
import uncertain.util.template.TagCreatorRegistry;
import uncertain.util.template.TextTemplate;
import aurora.application.ApplicationConfig;
import aurora.application.ApplicationViewConfig;
import aurora.application.IApplicationConfig;
import aurora.presentation.BuildSession;
import aurora.presentation.IViewBuilder;
import aurora.presentation.ViewContext;
import aurora.presentation.ViewCreationException;
import aurora.presentation.component.std.IDGenerator;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class HtmlPage implements IViewBuilder {

	public static final String EVENT_PREPARE_PAGE_CONTENT = "PreparePageContent";
	public static final String KEY_MANIFEST = "manifest";
	public static final String KEY_TITLE = "title";

	private IObjectRegistry mRegistry;
	private ApplicationConfig mApplicationConfig;

	public HtmlPage(IObjectRegistry registry) {
		mRegistry = registry;
		mApplicationConfig = (ApplicationConfig) mRegistry.getInstanceOfType(IApplicationConfig.class);

	}

	/**
	 * Handle ${page:content} in html page template
	 * 
	 * @author Zhou Fan
	 * 
	 */

	public static class PageContentTag implements ITagContent {

		public PageContentTag(BuildSession _session, CompositeMap model, Collection view_list) {
			this._session = _session;
			this.model = model;
			this.view_list = view_list;
		}

		BuildSession _session;
		CompositeMap model;
		Collection view_list;

		public String getContent(CompositeMap context) {
			try {
				_session.buildViews(model, view_list);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return null;
		}
	}

	public static class HtmlPageTagCreator extends ViewContextTagCreator {

		BuildSession _session;

		public HtmlPageTagCreator(BuildSession _session, ViewContext context) {
			super(context);
			this._session = _session;
		}

		public ITagContent createInstance(String namespace, String tag) {
			if ("content".equals(tag)) {
				ViewContext context = getViewContext();
				CompositeMap model = context.getModel();
				Collection view_list = context.getView().getChilds();
				return new PageContentTag(_session, model, view_list);
			} else if ("ebs_top".equals(tag)) {
				ViewContext context = getViewContext();
				CompositeMap model = context.getModel();
				Collection view_list = context.getView().getChilds();
				return new EbsTop(_session, model, view_list);
			} else if ("ebs_btm".equals(tag)) {
				ViewContext context = getViewContext();
				CompositeMap model = context.getModel();
				Collection view_list = context.getView().getChilds();
				return new EbsBtm(_session, model, view_list);
			} else
				return super.createInstance(namespace, tag);
		}

	}

	static class EbsTop implements ITagContent {

		public EbsTop(BuildSession _session, CompositeMap model, Collection view_list) {
			this._session = _session;
			this.model = model;
			this.view_list = view_list;
		}

		BuildSession _session;
		CompositeMap model;
		Collection view_list;

		@Override
		public String getContent(CompositeMap model) {

			CompositeMap context = model.getRoot();
			ILogger logger = LoggingContext.getLogger(context, BuildSession.LOGGING_TOPIC);

			ServiceContext serviceContext = ServiceContext.createServiceContext(context);
			HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
			HttpServletResponse response = svc.getResponse();
			HttpServletRequest request = svc.getRequest();

			boolean ajaxRequest = isAjaxRequest(request, serviceContext);

			PageContext pageContext = (PageContext) request.getAttribute("EBS_PAGECONTEXT");
			if (pageContext != null) {
				if (!ajaxRequest) {
					logger.log("EbsTop begin");
					Writer out = pageContext.getOut();
					try {
						create_ebs_top(pageContext, request, response, out, logger);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "", e);
					}
					logger.log("EbsTop end");
				}
			}
			return "";
		}

		private boolean isAjaxRequest(HttpServletRequest request, ServiceContext context) {
			String httptype = request.getHeader("x-requested-with");
			if ("XMLHttpRequest".equals(httptype)) {
				return true;
			}
			return false;
		}

		private void create_ebs_top(PageContext pageContext, HttpServletRequest request, HttpServletResponse response, Writer out, ILogger logger) throws Exception {

			String appName = "JTF";
			boolean stateless = true;
			String regionName = "JTFHOMEPAGE";

			// <!--%@ include file="jtfincl.jsp"%-->

			PageObject pageObject = null;
			try {
				pageObject = PageObject.startNewIfStarted();
				logger.log("pageObject:" + pageObject);
				if (pageObject != null) {
					pageObject.setContext(request, response);

				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "", e);
			}

			FWSession _fwSession = null;

			MenuRenderer.JtfJspContext _jtfPageContext = new MenuRenderer.JtfJspContext(request, response);
			// deprecated
			int appID;
			int respID;
			String langCode;

			oracle.apps.jtf.util.Utils.setCurrJspPageContext(pageContext);

			// These variables used to be defined in jtfframeworkincl.jsp
			// Now they are initialized in jtfuixincl.jsp
			oracle.apps.jtf.cabo.wrapper.Page _jtt_uix_jtfPage = null;
			oracle.cabo.share.util.NamespaceMap _jtt_uix_pageDataObjectCache = null;
			oracle.apps.jtf.cabo.wrapper.FrameworkContext _jtt_uix_frameworkContext = null;
			oracle.apps.jtf.cabo.wrapper.JTFServletRenderingContext _jtt_uix_renderingContext = null;
			oracle.apps.jtf.admin.adminconsole.UIXHelper _jtt_uix_helper = null;
			oracle.apps.jtf.cabo.wrapper.EventResult _jtt_af_eventResult = null;
			OAPageBean _jtfOAPageBean2 = new OAPageBean();

			// <!--%@ include file="jtfsrnfp.jsp" %-->

			/* under modification by kgopalsa */
			/*
			 * FWSession _fwSession = null; String jtfAppName = appName; boolean
			 * jtfStateless = stateless;
			 */

			// bijal -- logging on the jsp page ends
			{
				try {

					String jtfsSeverity = request.getParameter("jtfdebug");
					logger.log("jtfsSeverity:" + jtfsSeverity);
					if (jtfsSeverity != null) {
						oracle.apps.jtf.base.Logger.startBufferingLog(jtfsSeverity, request.getParameter("jtfdebugfilter"));
					}
				} catch (Throwable t) {
					logger.log(Level.SEVERE, "", t);
				}
				// bijal -- logging on the jsp page ends

				boolean jttIsPortlet = false;
				// int jtfsrnfpx = -1;
				boolean jttThrowUAException = (request.getParameter("jtt_uua") == null) ? false : true;
				logger.log("jttThrowUAException:" + jttThrowUAException);
				if (!jttThrowUAException) {
					if ("n".equals(pageContext.getAttribute("jtt_uua")))
						jttThrowUAException = true;
				}

				try {
					PortalUtil.initLSHashValue();
					jttIsPortlet = PortalUtil.isPortlet(request);
					logger.log("jttIsPortlet:" + jttIsPortlet);

					// Need to also call the SSM's API to set url mode
					oracle.apps.jtf.util.SystemCheck.init(request, response);
					// indicate via flag that the page is a redirect handler.
					oracle.apps.fnd.common.URLTools.setRequestAttribute(request, "JTT_REDIRECT_HANDLER", "Y");
					_fwSession = ServletSessionManager.startRequest(request, response, appName, stateless);
					logger.log("request.getAttribute(\"JTT_REDIRECT\"):" + request.getAttribute("JTT_REDIRECT"));
					if ("Y".equals(request.getAttribute("JTT_REDIRECT")))
						return;
					// if(Logger.isBufferedLogEnabled(Thread.currentThread()) &&
					// !Logger.checkBufferingPrivilege())
					// Logger.stopBufferingLog();
					if (jttIsPortlet)
						ServletSessionManager.doURLRewrite();

					/** begin DPF conext */
					// oracle.apps.jtf.dpf.ui.FlowUtils.beginFlowContext(request);

				} catch (ServletSessionManagerException e) {
					// if(Logger.isBufferedLogEnabled(Thread.currentThread()))
					// Logger.stopBufferingLog();
					String jttekey = e.getKey();
					logger.log("jttekey:" + jttekey);
					if (jttekey != null && jttekey.equals("JTF-0177") && !jttThrowUAException) {

						if (oracle.apps.jtf.interop.OAPageAdapter.isOAMode(pageContext)) {
							String jttnforwardURL = oracle.apps.fnd.framework.webui.OAPageBean.getOALoginURL(request, "ICX", "ICX_SESSION_FAILED");
							logger.log("isOAMode:true");
							// <jsp:forward page='= jttnforwardURL ' />

						} else {
							logger.log("isOAMode:false");
							Hashtable attributes = ServletSessionManager.getSessionInfo(request);
							String loginMode = null;
							if (attributes != null)
								loginMode = (String) attributes.get(ServletSessionManager.LOGIN_MODE);
							logger.log("loginMode:" + loginMode);
							WebAppsContext wctx = Architecture.createAppsContext();
							String jttnextPage = oracle.apps.fnd.sso.Utils.getFwkServerWithoutTS(wctx) + request.getRequestURI();
							if (!oracle.apps.fnd.sso.SessionMgr.isPHPMode(request) || oracle.apps.fnd.sso.Utils.isSSOMode() || loginMode == null
									|| loginMode.equals("JTFS")) {
								// if(SystemLogger.isEnabled(LogFormat.INFORMATION))
								// SystemLogger.out("nextPageBuf: " +
								// jttnextPage, LogFormat.INFORMATION,
								// "jtfsrnfp.jsp");

								// StringBuffer jttnextPageBuf =
								// oracle.apps.jtf.util.Utils.getRequestURL(request);
								// if(SystemLogger.isEnabled(LogFormat.INFORMATION))SystemLogger.out("nextPageBuf: "
								// + jttnextPageBuf.toString(),
								// LogFormat.INFORMATION, "jtfsrnfp.jsp");
								// String jttnextPage =
								// jttnextPageBuf.toString();
								String jttqueryString = request.getQueryString();
								if (jttqueryString != null)
									jttnextPage = jttnextPage + "?" + jttqueryString;
								String jtt_cancel_url = oracle.apps.fnd.sso.SSOManager.getLoginUrl();
								String jttrurl = oracle.apps.fnd.sso.SSOManager.getLoginUrl(jttnextPage, jtt_cancel_url);
								// if(Logger.isEnabled(Logger.INFORMATION))
								// Logger.out("jttrurl is : " + jttrurl,
								// Logger.INFORMATION, "jtfsrnfp.jsp");
								logger.log("jttrurl is : " + jttrurl + "jtfsrnfp.jsp");
								if (jttrurl == null)
									throw new Exception("Login Server Setup Incomplete. Please contact your System Administrator for help.");
								jttrurl += "&errCode=FND_SESSION_ICX_EXPIRED";
								Architecture.clearSession();
								response.sendRedirect(jttrurl);
							} else {
								String jttqueryString = request.getQueryString();
								if (jttqueryString != null)
									jttnextPage = jttnextPage + "?" + jttqueryString;
								String jtt_cancel_url = oracle.apps.fnd.sso.SSOManager.getLoginUrl();
								String jttNewNextPage = oracle.apps.fnd.sso.SSOUtil.getHtmlAlias() + "jtfbookmark.jsp" + "?jttNextPage="
										+ oracle.apps.jtf.util.Utils.encode(jttnextPage);
								String jttrurl = oracle.apps.fnd.sso.SSOManager.getLoginUrl(jttNewNextPage, jtt_cancel_url);
								// if(Logger.isEnabled(Logger.INFORMATION))
								// Logger.out("jttrurl is : " + jttrurl,
								// Logger.INFORMATION, "jtfsrnfp.jsp");
								logger.log("jttrurl is 2 : " + jttrurl + "jtfsrnfp.jsp");
								if (jttrurl == null)
									throw new Exception("Login Server Setup Incomplete. Please contact your System Administrator for help.");
								jttrurl += "&errCode=FND_SESSION_ICX_EXPIRED";
								Architecture.clearSession();
								response.sendRedirect(jttrurl);

							}// end of if for login server check

							String jtt_cancel_url = request.getScheme() + "://" + request.getHeader("Host") + SSOManager.getLoginUrl();
							String jttrurl = SSOManager.getLoginUrl(jttnextPage, jtt_cancel_url);
							// if(Logger.isEnabled(Logger.INFORMATION))Logger.out("jttrurl is : "
							// + jttrurl, Logger.INFORMATION, "jtfsrnfp.jsp");
							if (jttrurl == null)
								throw new Exception("Login Server Setup Incomplete. Please contact your System Administrator for help.");
							jttrurl += "&errCode=FND_SESSION_ICX_EXPIRED";
							response.sendRedirect(jttrurl);

						}// non OA mode.
					} else {
						if (SystemLogger.isLoggingOn())
							SystemLogger.out("ServletSessionManagerException thrown while calling startRequest. **" + e.getAllInfo() + "**" + " Error : " + e,
									LogFormat.INFORMATION, "jtfsrnfp.jsp");
						// if(SystemLogger.isEnabled(LogFormat.INFORMATION))SystemLogger.out("ServletSessionManagerException thrown while calling startRequest. **"
						// + e.getAllInfo() + "**" + " Error : " + e,
						// LogFormat.INFORMATION, "jtfsrnfp.jsp");
						throw e;
					}
				} catch (NotInServletSessionException e) {
					logger.log("NotInServletSessionException:" + e);
					if (SystemLogger.isLoggingOn())
						SystemLogger.out("ServletSessionManagerException thrown while calling startRequest. **" + e.getAllInfo() + "**" + " Error : " + e,
								LogFormat.INFORMATION, "jtfsrnfp.jsp");
					// if(SystemLogger.isEnabled(LogFormat.INFORMATION))SystemLogger.out("ServletSessionManagerException thrown while calling startRequest. **"
					// + e.getAllInfo() + "**" + " Error : " + e,
					// LogFormat.INFORMATION, "jtfsrnfp.jsp");
					throw e;
				} catch (UnAuthenticatedUserException e) {
					if (jttThrowUAException)
						throw e;

					if (SystemLogger.isEnabled(LogFormat.INFORMATION))
						SystemLogger.out("UnAuthenticatedUserException thrown while calling startRequest. **" + e.getAllInfo() + "**" + " Error : " + e,
								LogFormat.INFORMATION, "jtfsrnfp.jsp");
					if (oracle.apps.jtf.interop.OAPageAdapter.isOAMode(pageContext)) {
						String jttnforwardURL = oracle.apps.fnd.framework.webui.OAPageBean.getOALoginURL(request, "ICX", "ICX_SESSION_FAILED");

						// <jsp:forward page='= jttnforwardURL ' />

					} else {
						WebAppsContext wctx = Architecture.createAppsContext();
						wctx.getJDBCConnection();
						String jttnextPage = oracle.apps.fnd.sso.Utils.getFwkServerWithoutTS(wctx) + request.getRequestURI();
						logger.log("jttnextPage:"+jttnextPage);
						if (!oracle.apps.fnd.sso.SessionMgr.isPHPMode(request) || oracle.apps.fnd.sso.Utils.isSSOMode()) {
								logger.log("nextPageBuf: " + jttnextPage+"jtfsrnfp.jsp");

							// StringBuffer jttnextPageBuf =
							// oracle.apps.jtf.util.Utils.getRequestURL(request);
							// if(SystemLogger.isEnabled(LogFormat.INFORMATION))SystemLogger.out("nextPageBuf: "
							// + jttnextPageBuf.toString(),
							// LogFormat.INFORMATION, "jtfsrnfp.jsp");
							// String jttnextPage = jttnextPageBuf.toString();
							String jttqueryString = request.getQueryString();
							if (jttqueryString != null)
								jttnextPage = jttnextPage + "?" + jttqueryString;
							logger.log("jttnextPage 1:"+jttnextPage);
							String jtt_cancel_url = oracle.apps.fnd.sso.SSOManager.getLoginUrl();
							logger.log("jtt_cancel_url 1:"+jtt_cancel_url);
							String jttrurl = oracle.apps.fnd.sso.SSOManager.getLoginUrl(jttnextPage, jtt_cancel_url);
							logger.log("jttrurl 1:"+jttrurl);
							if (Logger.isEnabled(Logger.INFORMATION))
								Logger.out("jttrurl is : " + jttrurl, Logger.INFORMATION, "jtfsrnfp.jsp");
							if (jttrurl == null)
								throw new Exception("Login Server Setup Incomplete. Please contact your System Administrator for help.");
							// jttrurl += "&errCode=JTF-0198";
							Architecture.clearSession();
							response.sendRedirect(jttrurl);

						} else {
							String jttqueryString = request.getQueryString();
							if (jttqueryString != null)
								jttnextPage = jttnextPage + "?" + jttqueryString;
							logger.log("jttnextPage 2:"+jttnextPage);
							String jtt_cancel_url = oracle.apps.fnd.sso.SSOManager.getLoginUrl();
							logger.log("jtt_cancel_url 2:"+jtt_cancel_url);
							String jttNewNextPage = oracle.apps.fnd.sso.SSOUtil.getHtmlAlias() + "jtfbookmark.jsp" + "?jttNextPage="
									+ oracle.apps.jtf.util.Utils.encode(jttnextPage);
							logger.log("jttNewNextPage 2:"+jttNewNextPage);
							String jttrurl = oracle.apps.fnd.sso.SSOManager.getLoginUrl(jttNewNextPage, jtt_cancel_url);
							logger.log("jttrurl 2:"+jttrurl);
							if (Logger.isEnabled(Logger.INFORMATION))
								Logger.out("jttrurl is : " + jttrurl, Logger.INFORMATION, "jtfsrnfp.jsp");
							if (jttrurl == null)
								throw new Exception("Login Server Setup Incomplete. Please contact your System Administrator for help.");
							Architecture.clearSession();
							response.sendRedirect(jttrurl);

						}// end of if for login server check.
						
						String jtt_cancel_url = SSOManager.getLoginUrl();
						logger.log("jtt_cancel_url 3:"+jtt_cancel_url);
						String jttrurl = SSOManager.getLoginUrl(jttnextPage, jtt_cancel_url);
						logger.log("jttrurl 3:"+jttrurl);
						if (Logger.isEnabled(Logger.INFORMATION))
							Logger.out("jttrurl is : " + jttrurl, Logger.INFORMATION, "jtfsrnfp.jsp");
						if (jttrurl == null)
							throw new Exception("Login Server Setup Incomplete. Please contact your System Administrator for help.");
						response.sendRedirect(jttrurl);
					}// non OA mode.
				}

				// if(jtfsrnfpx == 1 || jttThrowUAException)
				// throw new UnAuthenticatedUserException();

				// set cookies for menu rendering
				MenuRenderer.setMenuCookies(request);

				// ServletSessionManager.setCookieValue("JSP",
				// ServletSessionManager.getJSPName());

				// bijal -- logging write params to JTFCookie
				{
					try {
						oracle.apps.jtf.base.session.JTFCookie jtfcookie = oracle.apps.jtf.base.session.ServletSessionManager.getJTFCookie();
						String jtfsSeverity = request.getParameter("jtfdebug");
						boolean jttseverityFromReq = (jtfsSeverity == null) ? false : true;
						String jtffilter = request.getParameter("jtfdebugfilter");
						if (jtfcookie != null) {
							if (jtfsSeverity == null) {
								jtfsSeverity = jtfcookie.getValue("jtfdebug");
							} else {
								jtfcookie.setValue("jtfdebug", jtfsSeverity);
							}
							if (jtffilter == null) {
								jtffilter = jtfcookie.getValue("jtfdebugfilter");
							} else {
								jtfcookie.setValue("jtfdebugfilter", jtffilter);
							}
						}
						if (!jttseverityFromReq && jtfsSeverity != null) {
							if (Logger.checkBufferingPrivilege())
								oracle.apps.jtf.base.Logger.startBufferingLog(jtfsSeverity, jtffilter);
						}
					} catch (Throwable t) {
						if (Logger.isEnabled(Logger.EXCEPTION))
							Logger.out(t.getMessage(), Logger.EXCEPTION, this);
					}
				}
				// bijal -- logging write params to JTFCookie ends

				try {
					if (pageObject != null) {
						pageObject.getPageInfo(request, response);
					}
				} catch (Exception e) {
					Logger.out(e, this);
				}

				// set content language
				String _jtt_htmlLangCode = oracle.apps.jtf.util.HtmlUtil.getHtmlLanguageCode();
				if (_jtt_htmlLangCode != null && !response.containsHeader("Content-Language")) {
					response.setHeader("Content-Language", _jtt_htmlLangCode);
				}

				// Moving the INTEROP Section to another jsp jtfsrnfpinterop.jsp
				// which is a static include

				// Define registration related fields.
				WebEventDispatcher _eventDispatcher = EventEngine.getWebEventDispatcher();
				Exception _EHRegistrationException = null;
				PageDataProvider _pageDataProvider = new PageDataProvider();
				Exception _DPRegistrationException = null;

				_jtt_uix_jtfPage = new oracle.apps.jtf.cabo.wrapper.Page("");
				_jtt_uix_frameworkContext = new oracle.apps.jtf.cabo.wrapper.FrameworkContext(request, response);
				_jtt_uix_frameworkContext.setJspPageContext(pageContext);
				_jtt_uix_pageDataObjectCache = new oracle.cabo.share.util.NamespaceMap();

				_jtt_uix_renderingContext = JTFCaboUtil.createRenderingContext(pageContext);

				pageContext.setAttribute("caboRenderingContext", _jtt_uix_renderingContext);
				pageContext.setAttribute("JTT_UIX_MENU_ENABLED", "Y");
				_jtt_uix_helper = new UIXHelper(pageContext, _jtt_uix_renderingContext);
				// If the rendering context needs to be further configured,
				// please modify
				// oracle.apps.jtf.cabo.wrapper.JTFCaboUtil.createRenderingContext
				// method.
				//
				// The question to be asked is: is this new configuration
				// specific to the
				// rendering context or JTT Application Framework? Products that
				// do not
				// want to make use of JTT Application Framework but want the
				// rendering context
				// will call JTFCaboUtil.createRenderingContext(pageContext)
				// without
				// creating objects like page, frameworkContext, etc.
				//
				// Also, think about if it's really necessary to put code here
				// rather than
				// in a Java class. If this include file is changed, all JSPs
				// that include
				// it have to be recompiled. If the code is placed in a class,
				// the JSP
				// page cache can remain intact.

				JTFCaboUtil.setFrameworkProperties(_jtt_uix_renderingContext, _jtt_uix_jtfPage, _jtt_uix_frameworkContext, _pageDataProvider,
						_jtt_uix_pageDataObjectCache);

				if (_EHRegistrationException == null && (request.getParameter("jtfBinId") == null)) {
					oracle.apps.jtf.cabo.wrapper.PageEvent _pageEvent = new oracle.apps.jtf.cabo.wrapper.PageEvent(_jtt_uix_jtfPage, request);
					try {
						String __evtn = _pageEvent.getName();
						if (__evtn == null)
							__evtn = _eventDispatcher.DEFAULT_EVENT_NAME;
						oracle.apps.jtf.servlet.event.EventHandler __eh = _eventDispatcher.dispatch(__evtn);
						if (__eh != null) {
							_jtt_af_eventResult = __eh.handleEvent(_jtt_uix_frameworkContext, _jtt_uix_jtfPage, _pageEvent);
						}
					} catch (Throwable e) {
						if (e instanceof Exception)
							throw (Exception) e;
					}
				} else if (_EHRegistrationException != null)
					throw _EHRegistrationException;

				if (_DPRegistrationException == null) {
				} else
					throw _DPRegistrationException;

				if (pageContext.getAttribute("caboRenderingContext") != null) {

					StyleSheetBean.sharedInstance().render((RenderingContext) pageContext.getAttribute("caboRenderingContext"));
				}

				MessageManagerInter _jtt_mmi = Architecture.getMessageManagerInstance();
				logger.log("15");

				/** DPF stuff **/
				oracle.apps.jtf.dpf.ui.FlowUtils.endFlowContext(request);

				oracle.apps.jtf.admin.adminconsole.WebNavigationContext _jttm_wnc = new oracle.apps.jtf.admin.adminconsole.WebNavigationContext(
						(oracle.apps.jtf.admin.adminconsole.MenuRenderer.MenuConfig) (_jtfPageContext.get(_jtfPageContext.NAVBARCONFIG)), appName,
						_fwSession.getFWAppsContext());

				_jttm_wnc.setSideNavigationType(oracle.apps.jtf.admin.adminconsole.SideBarConstant.ORIGINAL);

				/*
				 * if
				 * (oracle.apps.jtf.interop.OAPageAdapter.isOAMode(pageContext))
				 * {
				 * 
				 * @ include file = "jtfoamenutopincl.jsp"
				 * 
				 * } else {
				 */
				// check custom renderer
				logger.log("16");
				oracle.apps.jtf.ui.menu.CustomRenderer _jttcr_renderer = null;
				String _jtf_nav_class = GeneralPreference.getCurFWAppsContext().getProfileStore().getProfile("JTF_NAV_CUSTOM_RENDERER");
				if (_jtf_nav_class != null && _jtf_nav_class.trim().length() > 0) {

					// tell renderers to render to an object
					_jttm_wnc.getMenuConfig().dataOnly = true;

					java.lang.reflect.Method _jttcr_rm = Class.forName(_jtf_nav_class).getMethod("getInstance", null);
					try {
						_jttcr_renderer = (oracle.apps.jtf.ui.menu.CustomRenderer) _jttcr_rm.invoke(null, new Object[0]);
						if (_jttcr_renderer == null)
							throw new NullPointerException("getInstance() method must return a CustomRenderer instance.");
						pageContext.setAttribute("JTF_NAV_CUSTOM_RENDERER", _jttcr_renderer);
					} catch (java.lang.reflect.InvocationTargetException ite) {
						if (ite.getTargetException() instanceof Exception)
							throw ((Exception) ite.getTargetException());
						else
							throw ite;
					}
				}
				logger.log("17");
				_jttm_wnc = oracle.apps.jtf.admin.adminconsole.WebNavigationRenderer.renderBegin(_jttm_wnc, pageContext);

				if (_jttcr_renderer != null) {

					oracle.apps.jtf.ui.menu.CustomRenderingContext _jttcr_crc = new oracle.apps.jtf.ui.menu.CustomRenderingContext();
					_jttcr_crc.setOutput(new PrintWriter(out));
					_jttcr_crc.setJspPageContext(pageContext);
					_jttcr_crc.setNavigationData(_jttm_wnc.getRenderedPageNavigation());
					pageContext.setAttribute("JTF_NAV_CUSTOM_RCTXT", _jttcr_crc);

					_jttcr_renderer.renderBegin(_jttcr_crc);
				}

				// }
				logger.log("18");

				/** DPF stuff **/
				oracle.apps.jtf.dpf.ui.FlowUtils.beginFlowContext(request);

				// pageContext.getOut().flush();

				// <!--%@include file="jtfdnbarbtm.jsp" %-->
			}
		}

	}

	static class EbsBtm implements ITagContent {

		public EbsBtm(BuildSession _session, CompositeMap model, Collection view_list) {
			this._session = _session;
			this.model = model;
			this.view_list = view_list;
		}

		BuildSession _session;
		CompositeMap model;
		Collection view_list;

		@Override
		public String getContent(CompositeMap model) {

			CompositeMap context = model.getRoot();
			ILogger logger = LoggingContext.getLogger(context, BuildSession.LOGGING_TOPIC);

			ServiceContext serviceContext = ServiceContext.createServiceContext(context);
			HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
			HttpServletResponse response = svc.getResponse();
			HttpServletRequest request = svc.getRequest();

			boolean ajaxRequest = isAjaxRequest(request, serviceContext);
			PageContext pageContext = (PageContext) request.getAttribute("EBS_PAGECONTEXT");
			if (pageContext != null) {
				if (!ajaxRequest) {
					logger.log("EbsBtm begin");
					Writer out = pageContext.getOut();
					try {
						create_ebs_btm(pageContext, request, response, out);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "", e);
					}

					logger.log("EbsBtm end");
				}
			}
			return "";
		}

		private boolean isAjaxRequest(HttpServletRequest request, ServiceContext context) {
			String httptype = request.getHeader("x-requested-with");
			if ("XMLHttpRequest".equals(httptype)) {
				return true;
			}
			return false;
		}

		private void create_ebs_btm(PageContext pageContext, HttpServletRequest request, HttpServletResponse response, Writer out) throws Exception {
			if (oracle.apps.jtf.interop.OAPageAdapter.isOAMode(pageContext)) {

				oracle.apps.jtf.admin.adminconsole.WebNavigationRenderer.renderEnd(pageContext);

			} else {

				if (pageContext.getAttribute("JTF_NAV_CUSTOM_RENDERER") != null && pageContext.getAttribute("JTF_NAV_CUSTOM_RCTXT") != null) {

					((oracle.apps.jtf.ui.menu.CustomRenderer) pageContext.getAttribute("JTF_NAV_CUSTOM_RENDERER"))
							.renderEnd((oracle.apps.jtf.ui.menu.CustomRenderingContext) pageContext.getAttribute("JTF_NAV_CUSTOM_RCTXT"));
				}

				// render the bottom part of navigation UI
				oracle.apps.jtf.admin.adminconsole.WebNavigationRenderer.renderEnd(
						oracle.apps.jtf.admin.adminconsole.WebNavigationRenderer.getWebNavigationContext(pageContext), pageContext);

			}
		}

	}

	protected ITagCreatorRegistry createTagCreatorRegistry(BuildSession session, ViewContext view_context) {
		HtmlPageTagCreator creator = new HtmlPageTagCreator(session, view_context);
		TagCreatorRegistry reg = new TagCreatorRegistry();
		reg.setDefaultCreator(CompositeMapTagCreator.DEFAULT_INSTANCE);

		reg.registerTagCreator("page", creator);
		reg.setParent(session.getPresentationManager().getTagCreatorRegistry());
		return reg;
	}

	public void buildView(BuildSession session, ViewContext view_context) throws IOException, ViewCreationException {
		try {
			CompositeMap model = view_context.getModel();
			CompositeMap view = view_context.getView();

			String pageid = IDGenerator.getInstance().generate();

			// TODO Change title creation method
			String defaultTitle = "";
			if (mApplicationConfig != null) {
				ApplicationViewConfig view_config = mApplicationConfig.getApplicationViewConfig();
				if (view_config != null) {
					defaultTitle = view_config.getDefaultTitle();
				}
			}
			String title = view.getString(KEY_TITLE, defaultTitle);
			if (title != null) {
				title = session.getLocalizedPrompt(title);
				title = uncertain.composite.TextParser.parse(title, model);
				view_context.getContextMap().put(KEY_TITLE, title);
			}

			String mManifest = view.getString(KEY_MANIFEST);
			if (mManifest != null) {
				view_context.getContextMap().put(KEY_MANIFEST, "manifest=\"" + mManifest + "\"");
			}

			view_context.getContextMap().put("pageid", pageid);
			view_context.getContextMap().put("contextPath", session.getContextPath());
			session.getSessionContext().put("pageid", pageid);
			session.fireBuildEvent(EVENT_PREPARE_PAGE_CONTENT, view_context, true);
		} catch (Exception ex) {
			throw new ViewCreationException("Error when fire 'PreparePageContent' event", ex);
		}
		ITagCreatorRegistry reg = createTagCreatorRegistry(session, view_context);
		TextTemplate template = TemplateRenderer.getViewTemplate(session, view_context, reg);
		try {
			// template.createOutput(session.getWriter(),
			// view_context.getContextMap());
			template.createOutput(session.getWriter(), view_context.getModel());
		} finally {
			template.clear();
		}
	}

	public String[] getBuildSteps(ViewContext context) {
		return null;
	}

	private void create_ebs_top(HttpServletRequest request, HttpServletResponse response, Writer out) throws Exception {

		// MessageManagerInter _jtt_mmi =
		// Architecture.getMessageManagerInstance();

		/** DPF stuff **/
		oracle.apps.jtf.dpf.ui.FlowUtils.endFlowContext(request);
		MenuRenderer.JtfJspContext _jtfPageContext = new MenuRenderer.JtfJspContext(request, response);
		String appName = "JTF";
		boolean stateless = true;
		FWSession _fwSession = ServletSessionManager.startRequest(request, response, appName, stateless);
		oracle.apps.jtf.admin.adminconsole.WebNavigationContext _jttm_wnc = new oracle.apps.jtf.admin.adminconsole.WebNavigationContext(
				(oracle.apps.jtf.admin.adminconsole.MenuRenderer.MenuConfig) (_jtfPageContext.get(_jtfPageContext.NAVBARCONFIG)), appName,
				_fwSession.getFWAppsContext());

		_jttm_wnc.setSideNavigationType(oracle.apps.jtf.admin.adminconsole.SideBarConstant.ORIGINAL);
		// PageContext pageContext = new PageContext();
		Servlet servlet = (Servlet) request.getAttribute("EBS_HTTPSERVLET");
		PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(servlet, request, response, "jtfacerr.jsp", true, JspWriter.DEFAULT_BUFFER,
				true);

		// check custom renderer
		oracle.apps.jtf.ui.menu.CustomRenderer _jttcr_renderer = null;
		String _jtf_nav_class = GeneralPreference.getCurFWAppsContext().getProfileStore().getProfile("JTF_NAV_CUSTOM_RENDERER");
		if (_jtf_nav_class != null && _jtf_nav_class.trim().length() > 0) {

			// tell renderers to render to an object
			_jttm_wnc.getMenuConfig().dataOnly = true;

			java.lang.reflect.Method _jttcr_rm = Class.forName(_jtf_nav_class).getMethod("getInstance", null);
			try {
				_jttcr_renderer = (oracle.apps.jtf.ui.menu.CustomRenderer) _jttcr_rm.invoke(null, new Object[0]);
				if (_jttcr_renderer == null)
					throw new NullPointerException("getInstance() method must return a CustomRenderer instance.");
				pageContext.setAttribute("JTF_NAV_CUSTOM_RENDERER", _jttcr_renderer);
			} catch (java.lang.reflect.InvocationTargetException ite) {
				if (ite.getTargetException() instanceof Exception)
					throw ((Exception) ite.getTargetException());
				else
					throw ite;
			}
		}

		_jttm_wnc = oracle.apps.jtf.admin.adminconsole.WebNavigationRenderer.renderBegin(_jttm_wnc, pageContext);

		if (_jttcr_renderer != null) {

			oracle.apps.jtf.ui.menu.CustomRenderingContext _jttcr_crc = new oracle.apps.jtf.ui.menu.CustomRenderingContext();
			_jttcr_crc.setOutput(new PrintWriter(out));
			_jttcr_crc.setJspPageContext(pageContext);
			_jttcr_crc.setNavigationData(_jttm_wnc.getRenderedPageNavigation());
			pageContext.setAttribute("JTF_NAV_CUSTOM_RCTXT", _jttcr_crc);

			_jttcr_renderer.renderBegin(_jttcr_crc);
		}

		// deprecated, do not use
		// appID = _jttm_wnc.getApplicationID();
		// respID = _jttm_wnc.getResponsibilityID();
		// langCode = _jttm_wnc.getLanguageCode();

		// get warning mesgs both from system and request
		String[] _jttm_w_msgs = new String[2];
		try {
			_jttm_w_msgs[0] = oracle.apps.jtf.base.ha.WarningMsg.renderMsg();
		} catch (Exception eee) {
		}
		_jttm_w_msgs[1] = request.getParameter("jttmwmsg");
		// replace it with the real message
		if (_jttm_w_msgs[1] != null) {
			_jttm_w_msgs[1] = oracle.apps.jtf.util.UIUtil.getMessage(_jttm_w_msgs[1], _jttm_w_msgs[1]);
		}
		if (_jttm_w_msgs[0] != null || _jttm_w_msgs[1] != null) {
			pageContext.setAttribute("_jttm_w_msgs", _jttm_w_msgs);
		}
		// jtfsyswarn should check this attribute and display only non-null
		// messages

		String[] __sys_warn_msg = (String[]) pageContext.getAttribute("_jttm_w_msgs");
		if (__sys_warn_msg != null) {

			String[] __sys_jtfpmts = null;
			try {
				__sys_jtfpmts = oracle.apps.jtf.util.UIUtil.getRegionPrompts("JTFCOMMON",
						Integer.parseInt(oracle.apps.jtf.base.session.ServletSessionManager.getCookieValue(oracle.apps.jtf.base.session.JTFCookie.RESP_ID)),
						"JTF", oracle.apps.jtf.base.session.ServletSessionManager.getCookieValue(oracle.apps.jtf.base.session.JTFCookie.DEFAULT_LANGUAGE));
			} catch (Exception eee) {
			}

			String __sys_warn_title = "Warning";

			if (__sys_jtfpmts != null && __sys_jtfpmts.length >= 2 && __sys_jtfpmts[1] != null)
				__sys_warn_title = __sys_jtfpmts[1];

			if (oracle.apps.jtf.admin.adminconsole.UIXHelper.containsUIXHelper(pageContext)) {
				oracle.apps.jtf.admin.adminconsole.UIXHelper __sys_warn_uixh = oracle.apps.jtf.admin.adminconsole.UIXHelper.getUIXHelper(pageContext);
				String[] __sys_warn_mbox = __sys_warn_uixh.renderMessageBoxBuffered("", oracle.cabo.ui.UIConstants.MESSAGE_TYPE_WARNING, null);
				out.append(__sys_warn_mbox[0]);
				out.append("<div class=\"OraMessageBox\">");
				for (int __jtt_k = 0; __jtt_k < __sys_warn_msg.length; __jtt_k++) {
					if (__sys_warn_msg[__jtt_k] != null) {
						out.append("<LI>");
						// output escaping to solve XSS issue
						out.append(CSS.process(__sys_warn_msg[__jtt_k]));
					}
				}
				out.append("</div>");
				out.append(__sys_warn_mbox[1]);
			} else {
				out.append("		 <p>\r\n" + "		 <table border=0 cellspacing=0 cellpadding=0 summary=\"\">\r\n" + "		 <TR>\r\n"
						+ "		 <TD><IMG height=1 src=\"/OA_MEDIA/jtfutrpx.gif\" width=1></TD>\r\n"
						+ "		 <TD class=OraBGAccentDark><IMG src=\"/OA_MEDIA/jtfumsg.gif\"\r\n" + "		 ></TD>\r\n"
						+ "		 <TD class=OraBGAccentDark colSpan=5><IMG height=1\r\n" + "		 src=\"/OA_MEDIA/jtfutrpx.gif\" width=1></TD>\r\n"
						+ "		 <TD class=OraBGAccentDark><IMG height=1 src=\"/OA_MEDIA/jtfutrpx.gif\"\r\n" + "		 width=13></TD>\r\n"
						+ "		 <TD><IMG height=1 src=\"/OA_MEDIA/jtfutrpx.gif\" width=1></TD>\r\n" + "		 </TR>\r\n" + "		 <TR>\r\n" + "		 <TD>&nbsp;</TD>\r\n"
						+ "		 <TD class=OraBGAccentDark>&nbsp;</TD>\r\n" + "		 <TD class=OraBGAccentDark> <IMG src=\"/OA_MEDIA/jtfuwarn.gif\"></TD>\r\n"
						+ "		 <TD class=OraBGAccentDark>&nbsp;</TD>\r\n" + "		 <TD class=warnHeader>=__sys_warn_title</TD>\r\n"
						+ "		 <TD class=warnMessage><IMG height=1 src=\"/OA_MEDIA/jtfutrpx.gif\"\r\n" + "		 width=20></TD>\r\n"
						+ "		 <TD class=warnMessage width=\"100%\">");

				for (int __jtt_k = 0; __jtt_k < __sys_warn_msg.length; __jtt_k++) {
					if (__sys_warn_msg[__jtt_k] != null) {
						out.append("<LI>");
						// output escaping to solve XSS issue
						out.append(CSS.process(__sys_warn_msg[__jtt_k]));
					}
				}
				out.append(" </TD>\r\n" + "		 <TD class=OraBGAccentDark>&nbsp;</TD>\r\n" + "		 <TD>&nbsp;</TD>\r\n" + "		 </TR>\r\n" + "		 <TR>\r\n"
						+ "		 <TD><IMG height=1 src=\"/OA_MEDIA/jtfutrpx.gif\" width=1></TD>\r\n" + "		 <TD class=OraBGAccentDark colSpan=7><IMG height=10\r\n"
						+ "		 src=\"/OA_MEDIA/jtfutrpx.gif\" width=1></TD>\r\n" + "		 <TD><IMG height=1 src=\"/OA_MEDIA/jtfutrpx.gif\"\r\n"
						+ "		 width=1></TD>\r\n" + "		 </TR>\r\n" + "		 </table>\r\n" + "		 </p>");

			} // end of else of if (...containsUIXHelper)
		} // end of if(..!=null)

		/** DPF stuff **/
		oracle.apps.jtf.dpf.ui.FlowUtils.beginFlowContext(request);

	}

}
