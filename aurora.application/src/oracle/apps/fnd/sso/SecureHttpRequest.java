package oracle.apps.fnd.sso;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import oracle.apps.fnd.common.AppsLog;
import oracle.apps.fnd.common.ErrorStack;
import oracle.apps.fnd.common.ProfileStore;
import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.fnd.common.WebAppsContext;
import oracle.apps.fnd.security.AolSecurity;
import oracle.apps.fnd.sso.SimpleURLToken;
import oracle.apps.fnd.util.URLEncoder;

public class SecureHttpRequest
  extends HttpServletRequestWrapper
{
  public static final String RCS_ID = "$Header: SecureHttpRequest.java 120.10.12010000.11 2010/05/26 20:24:22 rsantis ship $";
  public static final boolean RCS_ID_RECORDED = VersionInfo.recordClassVersion("$Header: SecureHttpRequest.java 120.10.12010000.11 2010/05/26 20:24:22 rsantis ship $", "oracle.apps.fnd.sso");
  public static final String UNSECURED_AGENT_PROPNAME = "oracle.apps.fnd.sso.security.disable.client";
  public static final String WEBENTRY_LIST_PROPNAME = "oracle.apps.fnd.sso.WebEntries";
  private static final String className = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $]";
  static HashMap localUrl = null;
  boolean fromExpansion = false;
  public static final String ICX_QS_NAME = "iqn";
  public static final String ICX_SIGNATURE = "s1";
  public static final String HASH_SIGNATURE = "s2";
  public static final String UNSECURE_SIGNATURE = "s3";
  
  public static String getWebEntry(String paramString, WebAppsContext paramWebAppsContext)
    throws MalformedURLException
  {
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].getWebEntry";
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    boolean bool1 = localAppsLog.isEnabled(str1, 1);
    boolean bool2 = localAppsLog.isEnabled(str1, 2);
    boolean bool3 = localAppsLog.isEnabled(str1, 6);
    if (bool2) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (bool1) {
      localAppsLog.write(str1, "url=" + paramString, 1);
    }
    URL localURL;
    try
    {
      localURL = new URL(paramString);
      if (bool1) {
        localAppsLog.write(str1, "Parsed", 1);
      }
    }
    catch (MalformedURLException localMalformedURLException)
    {
      if (bool1) {
        localAppsLog.write(str1, localMalformedURLException, 1);
      }
      if (bool2) {
        localAppsLog.write(str1, "End with Exception", 2);
      }
      throw localMalformedURLException;
    }
    StringBuffer localStringBuffer = new StringBuffer();
    String str2 = localURL.getProtocol().toLowerCase();
    int i = localURL.getPort();
    if (i == -1) {
      if ("http".equals(str2)) {
        i = 80;
      } else if ("https".equals(str2)) {
        i = 443;
      } else {
        throw new MalformedURLException("Protocol '" + str2 + "' is not supported [" + paramString + "]");
      }
    }
    localStringBuffer.append(str2).append("://").append(localURL.getHost().toLowerCase()).append(":").append(i).append('/');
    if (bool1) {
      localAppsLog.write(str1, "WEP=" + localStringBuffer.toString(), 1);
    }
    if (bool2) {
      localAppsLog.write(str1, "End", 2);
    }
    return localStringBuffer.toString();
  }
  
  static String[] getAllValidWebEntries(WebAppsContext paramWebAppsContext)
  {
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].getAllValidWebEntries";
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    boolean bool1 = localAppsLog.isEnabled(str1, 1);
    boolean bool2 = localAppsLog.isEnabled(str1, 2);
    boolean bool3 = localAppsLog.isEnabled(str1, 6);
    if (bool2) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    String[]  localObject1 = null;
    try
    {
      Vector localVector = new Vector();
      String str2 = System.getProperty("oracle.apps.fnd.sso.WebEntries", "");
      if (bool1) {
        localAppsLog.write(str1, "Using System Property oracle.apps.fnd.sso.WebEntries=" + str2, 1);
      }
      String[] arrayOfString1 = str2.split(",");
      if ((arrayOfString1 == null) || (arrayOfString1.length == 0))
      {
        if (bool1) {
          localAppsLog.write(str1, "no enties found", 1);
        }
        return null;
      }
      for (int i = 0; i < arrayOfString1.length; i++) {
        if (arrayOfString1[i].length() > 0) {
          localVector.add(arrayOfString1[i]);
        }
      }
      if (localVector.isEmpty())
      {
        if (bool1) {
          localAppsLog.write(str1, "not null property, but no entries found", 1);
        }
        return null;
      }
      localObject1 = localVector.size() > 0 ? (String[])localVector.toArray(new String[localVector.size()]) : null;
      if (bool1) {
        if ((localObject1 != null) && (localObject1.length > 0)) {
          for (int j = 0; j < localObject1.length; j++) {
            localAppsLog.write(str1, j + ":" + localObject1[j], 1);
          }
        } else if (bool1) {
          localAppsLog.write(str1, "no entries", 1);
        }
      }
      return localObject1;
    }
    finally
    {
      if (bool2) {
        localAppsLog.write(str1, "END " + (localObject1 == null ? "0" : new StringBuilder().append(" total=").append(localObject1.length).toString()), 2);
      }
    }
  }
  
  public static synchronized HashMap getLocalUrl(WebAppsContext paramWebAppsContext)
  {
    if (localUrl == null)
    {
      String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].getLocalUrl";
      AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
      boolean bool1 = localAppsLog.isEnabled(str1, 1);
      boolean bool2 = localAppsLog.isEnabled(str1, 2);
      boolean bool3 = localAppsLog.isEnabled(str1, 6);
      if (bool2) {
        localAppsLog.write(str1, "BEGIN", 2);
      }
      localUrl = new HashMap();
      ProfileStore localProfileStore = paramWebAppsContext.getProfileStore();
      String str2 = paramWebAppsContext.getProfileStore().getProfile("APPS_SERVLET_AGENT");
      if (bool1) {
        localAppsLog.write(str1, "APPS_SERVLET_AGENT=" + str2, 1);
      }
      try
      {
        String str3 = getWebEntry(str2, paramWebAppsContext);
        localUrl.put(str3, "APPS_SERVLET_AGENT");
        if (bool1) {
          localAppsLog.write(str1, "added (APPS_SERVLET_AGENT)" + str3, 1);
        }
        localUrl.put(URLEncoder.encode(str3, SessionMgr.getCharSet(paramWebAppsContext)), "(enc)APPS_SERVLET_AGENT");
        if (bool1) {
          localAppsLog.write(str1, "added encoded(APPS_SERVLET_AGENT)" + URLEncoder.encode(str3, SessionMgr.getCharSet(paramWebAppsContext)), 1);
        }
      }
      catch (MalformedURLException localMalformedURLException1)
      {
        if (bool3) {
          localAppsLog.write(str1, localMalformedURLException1, 6);
        }
        throw new RuntimeException("Invalid APPS_SERVLET_ANGENT '" + str2 + "' " + localMalformedURLException1);
      }
      String[] arrayOfString = getAllValidWebEntries(paramWebAppsContext);
      for (int i = 0; (arrayOfString != null) && (i < arrayOfString.length); i++) {
        try
        {
          String str4 = getWebEntry(arrayOfString[i], paramWebAppsContext);
          localUrl.put(str4, "Additiona Web entry " + i);
          if (bool1) {
            localAppsLog.write(str1, "added [additional] " + str4, 1);
          }
        }
        catch (MalformedURLException localMalformedURLException2)
        {
          if (bool3) {
            localAppsLog.write(str1, localMalformedURLException2, 6);
          }
          if (bool2) {
            localAppsLog.write(str1, "Ignored  " + arrayOfString[i], 2);
          }
        }
      }
      if (bool2) {
        localAppsLog.write(str1, "END=", 1);
      }
    }
    return localUrl;
  }
  
  public static boolean isLocal(String paramString, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].isLocal";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (bool2) {
      localAppsLog.write(str1, "url=" + paramString, 1);
    }
    if (paramString == null)
    {
      if (bool1) {
        localAppsLog.write(str1, "END->True", 2);
      }
      return true;
    }
    if (paramString.startsWith("/"))
    {
      if (bool1) {
        localAppsLog.write(str1, "END->True for ^/.*", 2);
      }
      return true;
    }
    if (localUrl == null)
    {
      if (bool2) {
        localAppsLog.write(str1, "Creating  table of locaUrl", 1);
      }
      localUrl = getLocalUrl(paramWebAppsContext);
      if (bool2) {
        localAppsLog.write(str1, "DONE creating table of locaUrl", 1);
      }
    }
    String str2 = null;
    try
    {
      str2 = getWebEntry(paramString, paramWebAppsContext);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      if (bool2) {
        localAppsLog.write(str1, localMalformedURLException, 1);
      }
      if (bool1) {
        localAppsLog.write(str1, "END->No(exception)", 2);
      }
      return false;
    }
    if (bool2) {
      localAppsLog.write(str1, "wenEntry='" + str2 + "'", 1);
    }
    if (localUrl.containsKey(str2))
    {
      if (bool2) {
        localAppsLog.write(str1, "Yes, is local  becasue '" + localUrl.get(str2) + "'", 1);
      }
      if (bool1) {
        localAppsLog.write(str1, "END->Yes", 2);
      }
      return true;
    }
    if (bool1) {
      localAppsLog.write(str1, "END->No", 2);
    }
    return false;
  }
  
  private String getSiteWebEntry(WebAppsContext paramWebAppsContext)
  {
    return paramWebAppsContext.getProfileStore().getProfile("APPS_FRAMEWORK_AGENT");
  }
  
  public class IllegalStateException
    extends Exception
  {
    public IllegalStateException(String paramString)
    {
      super();
    }
  }
  
  public static class EmptyRequest
    extends SecureHttpRequest
  {
    Hashtable<String, String[]> parameters;
    
    public EmptyRequest(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext)
    {
    	super(paramHttpServletRequest,paramWebAppsContext, false);
    	//super(paramWebAppsContext, false, null);
      this.parameters = new Hashtable();
      this.parameters.putAll(paramHttpServletRequest.getParameterMap());
      for (String str : SecureHttpRequest.protectedParams) {
        this.parameters.remove(str);
      }
      this.parameters.put("errCode", new String[] { "FND_SSO_PHISH_ERROR" });
      this.table = this.parameters;
      this.QS = (this.originalQS = paramHttpServletRequest.getQueryString());
      this.fromExpansion = true;
    }
    
    public String getPararameter(String paramString)
    {
      try
      {
        return getParameterValues(paramString)[0];
      }
      catch (NullPointerException localNullPointerException) {}
      return null;
    }
    
    public String getQueryString()
    {
      return null;
    }
    
    public Map getParameterMap()
    {
      return this.parameters;
    }
    
    public Enumeration getParameterNames()
    {
      return this.parameters.keys();
    }
    
    public String[] getParameterValues(String paramString)
    {
      return (String[])this.parameters.get(paramString);
    }
  }
  
  public static String noSecured_agent_pattern = System.getProperty("oracle.apps.fnd.sso.security.disable.client");
  
  private static boolean noSignatureRequired(HttpServletRequest paramHttpServletRequest)
  {
    return noSignatureRequired(paramHttpServletRequest, Utils.getLog());
  }
  
  private static boolean noSignatureRequired(HttpServletRequest paramHttpServletRequest, AppsLog paramAppsLog)
  {
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.noSignatureRequied";
    boolean bool1 = paramAppsLog.isEnabled(str1, 2);
    boolean bool2 = paramAppsLog.isEnabled(str1, 1);
    if (bool1) {
      paramAppsLog.write(str1, "BEGIN", 2);
    }
    if (bool2) {
      paramAppsLog.write(str1, "request=" + paramHttpServletRequest.getPathInfo() + "?" + paramHttpServletRequest.getQueryString(), 1);
    }
    if (noSecured_agent_pattern == null)
    {
      if (bool1) {
        paramAppsLog.write(str1, "END->false, no exceptions", 2);
      }
      return false;
    }
    String str2 = paramHttpServletRequest.getHeader("User-Agent");
    if (str2 == null)
    {
      if (bool1) {
        paramAppsLog.write(str1, "END->false, no User-Agent", 2);
      }
      return false;
    }
    if (bool2) {
      paramAppsLog.write(str1, "agent=" + str2, 1);
    }
    boolean bool3 = str2.indexOf(noSecured_agent_pattern) >= 0;
    if (bool2) {
      paramAppsLog.write(str1, "idx=" + str2.indexOf(noSecured_agent_pattern) + " pattern='" + noSecured_agent_pattern + "'", 1);
    }
    if (bool2) {
      paramAppsLog.write(str1, "ret=" + bool3, 1);
    }
    if (bool1) {
      paramAppsLog.write(str1, "END->" + bool3, 2);
    }
    return bool3;
  }
  
  private static boolean validateSignature(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext)
    throws SecurityException
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].validateSginature";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (noSignatureRequired(paramHttpServletRequest, localAppsLog))
    {
      if (bool1) {
        localAppsLog.write(str1, "END->Valid (insecure client)", 2);
      }
      return true;
    }
    String[] arrayOfString = removeSignature(paramHttpServletRequest.getQueryString(), "s1");
    

    String str2 = arrayOfString[1];
    String str3;
    String str4;
    String str5;
    if ((str2 != null) && (arrayOfString[0] != null))
    {
      if (bool2) {
        localAppsLog.write(str1, "ICX_SIGNATURE=" + str2, 1);
      }
      if (loadICXSession(paramHttpServletRequest, paramWebAppsContext))
      {
        str3 = paramHttpServletRequest.getQueryString();
        str4 = arrayOfString[0];
        str5 = ICXSignature(str4, paramWebAppsContext);
        if (bool2)
        {
          localAppsLog.write(str1, "qs=" + str3, 1);
          localAppsLog.write(str1, "qsNosig=" + str4, 1);
          localAppsLog.write(str1, "Expected signature=" + str5, 1);
          localAppsLog.write(str1, "signature=" + str2, 1);
        }
        if (str2.equals(str5))
        {
          if (bool1) {
            localAppsLog.write(str1, "END-> valid icx sign", 2);
          }
          return true;
        }
        if (bool1) {
          localAppsLog.write(str1, "END-> EXCEPTION invalid icx_sign", 2);
        }
        throw new SecurityException("Invalid ICX signature");
      }
      if (bool2) {
        localAppsLog.write(str1, "has no icx session", 1);
      }
      if (bool1) {
        localAppsLog.write(str1, "END-> false(icx failed)", 2);
      }
      return false;
    }
    if (bool2) {
      localAppsLog.write(str1, "NO ICX segnature", 1);
    }
    str2 = paramHttpServletRequest.getParameter("s2");
    if (str2 != null)
    {
      if (bool2) {
        localAppsLog.write(str1, "HASH_SIGNATURE=" + str2, 1);
      }
      str3 = paramHttpServletRequest.getQueryString();
      str4 = removeParameter(str3, "s2");
      str5 = HASHSignature(str4, paramWebAppsContext);
      if (bool2)
      {
        localAppsLog.write(str1, "qs=" + str3, 1);
        localAppsLog.write(str1, "qsNosig=" + str4, 1);
        localAppsLog.write(str1, "Expected signature=" + str5, 1);
        localAppsLog.write(str1, "signature=" + str2, 1);
      }
      if (str2.equals(str5))
      {
        if (bool1) {
          localAppsLog.write(str1, "END-> valid hash sign", 2);
        }
        return true;
      }
      if (bool1) {
        localAppsLog.write(str1, "END-> EXCEPTION invalid has_sign", 2);
      }
      throw new SecurityException("Invalid HASH signature");
    }
    if (bool2) {
      localAppsLog.write(str1, "NO HASH segnature", 1);
    }
    return false;
  }
  
  public static String ICXSignature(String paramString, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].ICXSignature";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    String str2 = null;
    try
    {
      if (bool2) {
        localAppsLog.write(str1, "s=" + paramString, 1);
      }
      int i = paramString.indexOf('?');
      if (bool2) {
        localAppsLog.write(str1, "p=" + i, 1);
      }
      if (i > 0)
      {
        if (bool2) {
          localAppsLog.write(str1, "using=" + paramString.substring(i + 1), 1);
        }
        str2 = paramWebAppsContext.computeMAC(paramString.substring(i + 1));
        if (bool2) {
          localAppsLog.write(str1, "ret[1]=" + str2, 1);
        }
      }
      else
      {
        if (bool2) {
          localAppsLog.write(str1, "using=" + paramString, 1);
        }
        str2 = paramWebAppsContext.computeMAC(paramString);
        if (bool2) {
          localAppsLog.write(str1, "ret[2]=" + str2, 1);
        }
      }
      if (bool2) {
        localAppsLog.write(str1, "signature=" + str2, 1);
      }
      if (bool1) {
        localAppsLog.write(str1, "END", 2);
      }
      return str2;
    }
    catch (Exception localException)
    {
      if (localAppsLog.isEnabled(str1, 1)) {
        localAppsLog.write(str1, "Exception " + Utils.getExceptionStackTrace(localException), 1);
      }
      if (bool1) {
        localAppsLog.write(str1, "END-> no changes", 2);
      }
    }
    return paramString;
  }
  
  public static String HASHSignature(String paramString, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].HASHSignature";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (bool2) {
      localAppsLog.write(str1, "string=" + paramString, 1);
    }
    int i = paramString.indexOf('?');
    String str2;
    if (i > 0)
    {
      str2 = aolSec.hash(paramString.substring(i + 1));
      if (bool2) {
        localAppsLog.write(str1, "str=" + paramString.substring(i + 1), 1);
      }
    }
    else
    {
      if (bool2) {
        localAppsLog.write(str1, "str=" + paramString, 1);
      }
      str2 = aolSec.hash(paramString);
    }
    if (bool2) {
      localAppsLog.write(str1, "signature=" + str2, 1);
    }
    if (bool1) {
      localAppsLog.write(str1, "END", 2);
    }
    return str2;
  }
  
  private static String appendParameter(String paramString1, String paramString2)
  {
    int i = paramString1.indexOf('?');
    int j = i > 0 ? paramString1.indexOf('&', i) : -1;
    int k = 0;
    if (i > 0)
    {
      if ((j > 0) || (paramString1.charAt(paramString1.length() - 1) != '?')) {
        k = 1;
      }
    }
    else {
      k = 2;
    }
    paramString1 = paramString1 + (k == 0 ? paramString2 : new StringBuilder().append(k == 1 ? '&' : '?').append(paramString2).toString());
    return paramString1;
  }
  
  public static String ICX_Sign(String paramString1, String paramString2, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].ICX_Sign";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (paramString2 == null)
    {
      if (bool1) {
        localAppsLog.write(str1, "END-> No session try other method", 2);
      }
      return NOICX_Sign(paramString1, paramWebAppsContext);
    }
    if (bool2) {
      localAppsLog.write(str1, " cookie=" + paramString2, 1);
    }
    String str2 = ICXSignature(paramString1, paramWebAppsContext);
    if (bool2) {
      localAppsLog.write(str1, "url=" + paramString1 + "\ns=" + str2, 1);
    }
    String str3 = appendParameter(paramString1, "s1=" + str2);
    if (bool2) {
      localAppsLog.write(str1, "->" + str3 + str2, 1);
    }
    if (bool1) {
      localAppsLog.write(str1, "END", 2);
    }
    return str3;
  }
  
  public static String NOICX_Sign(String paramString, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].NOICX_Sign";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    String str2 = HASHSignature(paramString, paramWebAppsContext);
    if (bool2) {
      localAppsLog.write(str1, "url=" + paramString + "\ns=" + str2, 1);
    }
    String str3 = appendParameter(paramString, "s2=" + str2);
    if (bool2) {
      localAppsLog.write(str1, "->" + str3, 1);
    }
    if (bool1) {
      localAppsLog.write(str1, "END", 2);
    }
    return str3;
  }
  
  public static String recalc_Sign(String paramString, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].recalc_Sign";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if ((paramString == null) || ("".equals(paramString)) || (paramWebAppsContext == null))
    {
      if (localAppsLog.isEnabled(str1, 6)) {
        localAppsLog.write(str1, "NULL url or context received", 6);
      }
      if (bool1) {
        localAppsLog.write(str1, "END-> null!!", 2);
      }
      return null;
    }
    if (bool2) {
      localAppsLog.write(str1, "url:" + paramString, 1);
    }
    String[] arrayOfString = removeSignature(paramString, "s1");
    String str2 = paramWebAppsContext.getSessionCookieValue();
    if ((arrayOfString[1] != null) && (str2 != null) && (!"-1".equals(str2)))
    {
      if (bool2) {
        localAppsLog.write(str1, "recalc ICX_SIGNATURE", 1);
      }
      paramString = ICX_Sign(arrayOfString[0], str2, paramWebAppsContext);
    }
    else
    {
      arrayOfString = removeSignature(paramString, "s2");
      if (arrayOfString[1] != null)
      {
        if (bool2) {
          localAppsLog.write(str1, "recalc HASH_SIGNATURE", 1);
        }
        paramString = NOICX_Sign(arrayOfString[0], paramWebAppsContext);
      }
      else if (bool2)
      {
        localAppsLog.write(str1, "recalc no signature ", 1);
      }
    }
    if (bool1) {
      localAppsLog.write(str1, "END->" + paramString, 1);
    }
    return paramString;
  }
  
  public static String[] removeSignature(String paramString1, String paramString2)
  {
    String str = "oracle.app.fnd.sso.SecureHttpRequest.removeSignature";
    AppsLog localAppsLog = Utils.getLog();
    if (localAppsLog.isEnabled(str, 2)) {
      localAppsLog.write(str, "BEGIN:url=" + paramString1 + " name=" + paramString2, 2);
    }
    String[] arrayOfString = { null, null };
    if (paramString1 == null) {
      return arrayOfString;
    }
    char[] arrayOfChar1 = paramString1.toCharArray();
    int i = 1;int j = 1;int k = 0;int m = 0;int n = 0;
    char[] arrayOfChar2 = paramString2.toCharArray();
    for (;;)
    {
      j++;
      if (j >= arrayOfChar1.length) {
        break;
      }
      switch (i)
      {
      case 0: 
        if (arrayOfChar1[j] == '?') {
          i = 1;
        }
        break;
      case 1: 
        if (arrayOfChar1[j] == arrayOfChar2[0]) {
          i = 2;
        }
        break;
      case 2: 
        m = arrayOfChar1[(j - 1)] == '?' ? j : j - 1;
        k = 0;
        while ((j < arrayOfChar1.length) && (k < arrayOfChar2.length) && (arrayOfChar1[j] == arrayOfChar2[k]))
        {
          j++;
          k++;
        }
        if ((k == arrayOfChar2.length) && ((j == arrayOfChar1.length) || (arrayOfChar1[j] == '=') || (arrayOfChar1[j] == '&')))
        {
          n = j + 1;
          while ((j < arrayOfChar1.length) && (arrayOfChar1[j] != '&')) {
            j++;
          }
          j = arrayOfChar1[m] == '&' ? j : j + 1;
          arrayOfString[0] = (paramString1.substring(0, m) + paramString1.substring(j, paramString1.length()));
          

          arrayOfString[1] = ((n >= arrayOfChar1.length) || (n >= j) || (arrayOfChar1[n] == '&') ? null : paramString1.substring(n, arrayOfChar1[(m - 1)] == '?' ? j - 1 : j));
          j = arrayOfChar1.length;
        }
        i = 1;
      }
    }
    if (localAppsLog.isEnabled(str, 2)) {
      localAppsLog.write(str, "END [0]=" + arrayOfString[0] + " [1]=" + arrayOfString[0], 2);
    }
    return arrayOfString;
  }
  
  public static boolean isSecure(String paramString, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].NOICX_Sign";
    boolean bool1 = localAppsLog.isEnabled(str, 2);
    boolean bool2 = localAppsLog.isEnabled(str, 1);
    if (bool1) {
      localAppsLog.write(str, "BEGIN", 2);
    }
    if (bool2) {
      localAppsLog.write(str, "url=" + paramString, 1);
    }
    if (paramString == null)
    {
      if (bool1) {
        localAppsLog.write(str, "END->T (null)", 2);
      }
      return true;
    }
    if (hasInvalidChars(paramString, paramWebAppsContext))
    {
      if (bool2) {
        localAppsLog.write(str, "REJECTED containes invalid chars", 1);
      }
      if (bool1) {
        localAppsLog.write(str, "END ->no ", 2);
      }
      throw new SecurityException("Invalid chars on request ");
    }
    if (URLToken.isRegistered(paramString))
    {
      if (bool1) {
        localAppsLog.write(str, "END-> Yes(registered)", 2);
      }
      return true;
    }
    if (isLocal(paramString, paramWebAppsContext))
    {
      if (isBanned(paramString, paramWebAppsContext))
      {
        if (bool1) {
          localAppsLog.write(str, "END-> NO (banned)", 2);
        }
        return false;
      }
      if (bool1) {
        localAppsLog.write(str, "END-> Yes(local)", 2);
      }
      return true;
    }
    if (bool1) {
      localAppsLog.write(str, "END-> No", 2);
    }
    return false;
  }
  
  static Vector banned = new Vector();
  
  static Vector bannedVector(WebAppsContext paramWebAppsContext)
  {
    Vector localVector = new Vector();
    localVector.add("/weboam/redirectURL");
    






    localVector.add("/FNDSSOLoginRedirect");
    localVector.add("/oracle.apps.fnd.sso.FNDSSOLoginRedirect");
    localVector.add("/FNDSSOLogoutRedirect");
    localVector.add("/oracle.apps.fnd.sso.FNDSSOLogoutRedirect");
    return localVector;
  }
  
  static Enumeration getBannedEnumeration(WebAppsContext paramWebAppsContext)
  {
    synchronized (banned)
    {
      if (banned.size() == 0) {
        banned = bannedVector(paramWebAppsContext);
      }
      return banned.elements();
    }
  }
  
  public static boolean isBanned(String paramString, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].isBanned";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (bool2) {
      localAppsLog.write(str1, "url=" + paramString, 1);
    }
    Object localObject1 = paramString;
    boolean bool3 = false;
    while (!bool3)
    {
      String localObject2 = URLDecode((String)localObject1, SessionMgr.getCharSet(paramWebAppsContext));
      
      bool3 = ((String)localObject2).equals(localObject1);
      if (!bool3) {
        localObject1 = localObject2;
      }
    }
    if (bool2) {
      localAppsLog.write(str1, "Totally decoded url=" + (String)localObject1, 1);
    }
    for (Object localObject2 = getBannedEnumeration(paramWebAppsContext); ((Enumeration)localObject2).hasMoreElements();)
    {
      String str2 = (String)((Enumeration)localObject2).nextElement();
      if (((String)localObject1).indexOf(str2) >= 0)
      {
        if (bool2) {
          localAppsLog.write(str1, "Matches url=" + str2, 1);
        }
        if (bool1) {
          localAppsLog.write(str1, "END->Yes", 2);
        }
        return true;
      }
    }
    if (bool1) {
      localAppsLog.write(str1, "END->No", 2);
    }
    return false;
  }
  
  static String[] invalidChar = { "CR", "\n", "LF", "\r" };
  
  static boolean hasInvalidChars(String paramString, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].hasInvalidChars";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (bool2) {
      localAppsLog.write(str1, "url=" + paramString, 1);
    }
    if ((paramString == null) || (paramString.length() == 0))
    {
      if (bool1) {
        localAppsLog.write(str1, "END-> yes (empty)", 2);
      }
      return true;
    }
    boolean bool3 = false;
    String str2 = paramString;
    String str3 = paramString;
    String str4 = SessionMgr.getCharSet(paramWebAppsContext);
    while (!bool3)
    {
      str2 = URLDecode(str3, str4);
      bool3 = str2.equals(str3);
      str3 = str2;
    }
    if (bool2) {
      localAppsLog.write(str1, "url[decoded]=" + str2, 1);
    }
    for (int i = 1; i < invalidChar.length; i++) {
      if (str2.indexOf(invalidChar[i]) >= 0)
      {
        if (bool2) {
          localAppsLog.write(str1, "invalid char " + invalidChar[(i - 1)], 1);
        }
        if (bool1) {
          localAppsLog.write(str1, "END->true", 2);
        }
        return true;
      }
    }
    if (bool1) {
      localAppsLog.write(str1, "END->false", 2);
    }
    return false;
  }
  
  static AolSecurity aolSec = new AolSecurity();
  static final String ClassName = "oracle.apps.fnd.sso.SecureHttpRequest";
  static final String ERROR_NO_REASON = "No reason at all";
  Hashtable<String, String[]> table = null;
  HttpServletRequest request = null;
  String last_known_reason;
  String failure_reason;
  String originalQS = null;
  String QS;
  String icx_cookie;
  
  private static AppsLog getLog()
  {
    return Utils.isAppsContextAvailable() ? getLog(Utils.getAppsContext()) : AppsLog.getAnonymousLog();
  }
  
  private static AppsLog getLog(WebAppsContext paramWebAppsContext)
  {
    return (AppsLog)paramWebAppsContext.getLog();
  }
  
  public static HttpServletRequest getExtended(HttpServletRequest paramHttpServletRequest)
  {
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.getExtended";
    AppsLog localAppsLog = getLog();
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    String[] arrayOfString = paramHttpServletRequest.getParameterValues("iqn");
    if (bool2)
    {
      localAppsLog.write(str1, "URI=" + paramHttpServletRequest.getQueryString(), 1);
      for (Enumeration<String > localObject = paramHttpServletRequest.getParameterNames(); ((Enumeration)localObject).hasMoreElements();)
      {
        String str2 = (String)((Enumeration)localObject).nextElement();
        localAppsLog.write(str1, "Parameter " + str2 + "=" + paramHttpServletRequest.getParameter(str2), 1);
      }
    }
    if ((arrayOfString == null) || (arrayOfString.length != 1))
    {
      if (bool1)
      {
        if (bool2) {
          if (arrayOfString == null) {
            localAppsLog.write(str1, " parameter iqn not present", 1);
          } else {
            localAppsLog.write(str1, " parameter iqn  too many times (" + arrayOfString.length + ")", 1);
          }
        }
        localAppsLog.write(str1, "END -> returned the original", 2);
      }
      return paramHttpServletRequest;
    }
    HttpServletRequest localObject = null;
    try
    {
      localObject = new SecureHttpRequest(paramHttpServletRequest, arrayOfString[0]);
      if (bool1) {
        localAppsLog.write(str1, "END -> retreived from ICX session", 2);
      }
      return localObject;
    }
    catch (IllegalStateException localIllegalStateException)
    {
      if (localAppsLog.isEnabled(4)) {
        localAppsLog.write(str1, " Cannot retreive ICX request from '" + arrayOfString[0] + "'", 4);
      }
    }
    return new EmptyRequest(paramHttpServletRequest, null);
  }
  
  private static String[] protectedParams = { "returnUrl", "cancelUrl", "requestUrl", "homeUrl" };
  private static Hashtable pParams = null;
  static final int MSIE_THRESHOLD = 500;
  static final int OTHERS_THRESHOLD = 4095;
  static int MAX_URL_LENGTH;
  static final String propertyName = "oracle.apps.fnd.sso.AgentLimits";
  static Hashtable<String, Integer> agentLimits;
  static String redirUrl;
  static Class urlDecoderClass;
  static Method decodeMethod;
  static boolean prejava14;
  
  private static boolean isProtectedParam(String paramString)
  {
    return pParams.get(paramString) != null;
  }
  
  private SecureHttpRequest(HttpServletRequest paramHttpServletRequest, String paramString)
    throws SecureHttpRequest.IllegalStateException
  {
    super(paramHttpServletRequest);
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.<init>";
    AppsLog localAppsLog = getLog();
    

    this.icx_cookie = null;
    this.originalQS = super.getQueryString();
    this.QS = null;
    boolean bool = Utils.isAppsContextAvailable();
    try
    {
      WebAppsContext localWebAppsContext = Utils.getAppsContext();
      localAppsLog = getLog(localWebAppsContext);
      if (localAppsLog.isEnabled(2)) {
        localAppsLog.write(str1, "BEGIN " + str1, 2);
      }
      if (!hasICXSession(localWebAppsContext))
      {
        if (localAppsLog.isEnabled(2)) {
          localAppsLog.write(str1, "END With error  , no icx Session  ", 2);
        }
        throw new IllegalStateException("No ICX session (bookmarked URL maybe)");
      }
      String str2 = getICXQS(paramString);
      
      String str3 = getCharacterEncoding();
      if (localAppsLog.isEnabled(1)) {
        localAppsLog.write(str1, "getCharacterEncoding()->" + (str3 == null ? "NULL" : str3), 1);
      }
      if (str3 == null) {
        str3 = "UTF-8";
      }
      if (localAppsLog.isEnabled(1)) {
        localAppsLog.write(str1, "cs:" + str3, 1);
      }
      scan(str2, str3, str3);
      if (localAppsLog.isEnabled(2)) {
        localAppsLog.write(str1, "END ", 2);
      }
    }
    catch (IllegalStateException localIllegalStateException)
    {
      if (localAppsLog.isEnabled(4)) {
        localAppsLog.write(str1, "END With error  ", 4);
      }
      throw localIllegalStateException;
    }
    finally
    {
      if (!bool) {
        Utils.releaseAppsContext();
      }
    }
  }
  
  public static HttpServletRequest check(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext)
  {
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].check";
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    boolean bool3 = localAppsLog.isEnabled(str1, 6);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    try
    {
      HttpServletRequest localHttpServletRequest = getExtended(paramHttpServletRequest);
      if (localHttpServletRequest == paramHttpServletRequest)
      {
        if (bool2) {
          localAppsLog.write(str1, "No compressed", 1);
        }
        return verifySecurity(paramHttpServletRequest, null, paramWebAppsContext);
      }
      if (bool2) {
        localAppsLog.write(str1, "Compressed are safe", 1);
      }
      ((SecureHttpRequest)localHttpServletRequest).fromExpansion = true;
      return localHttpServletRequest;
    }
    catch (Throwable localThrowable)
    {
      Object localObject1;
      if (bool3)
      {
        localObject1 = "";
        for (int i = 0; i < protectedParams.length; i++)
        {
          String str2 = paramHttpServletRequest.getParameter(protectedParams[i]);
          if ((str2 != null) && (str2.length() > 0)) {
            localObject1 = (String)localObject1 + "\n" + protectedParams[i] + "=" + str2;
          }
        }
        localAppsLog.write(str1, "Possible PHISHING attack" + (String)localObject1, 6);
        localAppsLog.write(str1, localThrowable, 6);
      }
      if (bool2) {
        localAppsLog.write(str1, "Returning an empty request", 1);
      }
      return new EmptyRequest(paramHttpServletRequest, paramWebAppsContext);
    }
    finally
    {
      if (bool1) {
        localAppsLog.write(str1, "END", 2);
      }
    }
  }
  
  private SecureHttpRequest(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext)
    throws SecurityException
  {
    super(paramHttpServletRequest);
    int i = (paramWebAppsContext != null) || (Utils.isAppsContextAvailable()) ? 1 : 0;
    try
    {
      WebAppsContext localWebAppsContext = paramWebAppsContext != null ? paramWebAppsContext : Utils.getAppsContext();
      
      String str = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].SecureHttpRequest(req,wctx)";
      AppsLog localAppsLog = Utils.getLog(localWebAppsContext);
      boolean bool1 = localAppsLog.isEnabled(str, 2);
      boolean bool2 = localAppsLog.isEnabled(str, 1);
      if (bool1) {
        localAppsLog.write(str, "BEGIN", 2);
      }
      this.table = new Hashtable();
      this.originalQS = paramHttpServletRequest.getQueryString();
      secureParse(this.table, this.originalQS, localWebAppsContext);
      this.QS = buildQS(localWebAppsContext);
      if (bool2)
      {
        localAppsLog.write(str, "originalQS=" + this.originalQS, 1);
        localAppsLog.write(str, "QS=" + this.QS, 1);
      }
      if (bool1) {
        localAppsLog.write(str, "END", 2);
      }
    }
    finally
    {
      if ((i == 0) && (paramWebAppsContext == null)) {
        Utils.releaseAppsContext();
      }
    }
  }
  
  private SecureHttpRequest(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext, boolean paramBoolean)
  {
    super(paramHttpServletRequest);
  }
  
  private void secureParse(Hashtable paramHashtable, String paramString, WebAppsContext paramWebAppsContext)
    throws SecurityException
  {
    if ((paramString == null) || (paramString.length() == 0)) {
      return;
    }
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].secureParse";
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    boolean bool3 = bool2;
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (bool2) {
      localAppsLog.write(str1, "qs=" + paramString, 1);
    }
    String str2 = SessionMgr.getCharSet(paramWebAppsContext);
    String str3;
    for (Object localObject1 = new StringTokenizer(paramString, "&"); ((StringTokenizer)localObject1).hasMoreElements();)
    {
      str3 = (String)((StringTokenizer)localObject1).nextElement();
      if (bool3) {
        localAppsLog.write(str1, "pd=" + str3, 1);
      }
      if ((str3 != null) && (str3.length() != 0))
      {
        int i = str3.indexOf('=');
        if (bool3) {
          localAppsLog.write(str1, "eq=" + i, 1);
        }
        String localObject3 = null;
        String localObject4 = null;
        if (i < 0)
        {
          localObject3 = str3;localObject4 = null;
        }
        else if (i == 0)
        {
          localObject3 = null;localObject4 = str3.substring(1);
        }
        else
        {
          localObject3 = str3.substring(0, i);localObject4 = str3.substring(i + 1);
        }
        String str4 = localObject4 = localObject4 == null ? null : URLDecode((String)localObject4, str2);
        if (bool3) {
          localAppsLog.write(str1, "Original value '" + str4 + "'", 1);
        }
        if (bool3) {
          localAppsLog.write(str1, "n=" + (String)localObject3, 1);
        }
        if (bool3) {
          localAppsLog.write(str1, "v[" + str2 + "]=" + (String)localObject4, 1);
        }
        if (pParams.get(localObject3) != null)
        {
          if (bool3) {
            localAppsLog.write(str1, (String)localObject3 + " is  protected, checking...", 1);
          }
          if (hasInvalidChars((String)localObject4, paramWebAppsContext))
          {
            if (bool2) {
              localAppsLog.write(str1, "REJECTED " + (String)localObject3 + "=" + (String)localObject4, 1);
            }
            if (bool1) {
              localAppsLog.write(str1, "END with errors", 2);
            }
            throw new SecurityException("Invalid parameter " + (String)localObject3);
          }
          String localObject5 = URLDecode((String)localObject4, str2);
          if (bool3) {
            localAppsLog.write(str1, "v(decoded)=" + (String)localObject5, 1);
          }
          if (!isSecure((String)localObject5, paramWebAppsContext))
          {
            if (bool2) {
              localAppsLog.write(str1, "REJECTED " + (String)localObject3 + "=" + (String)localObject4, 1);
            }
            if (bool1) {
              localAppsLog.write(str1, "END with errors", 2);
            }
            throw new SecurityException("Invalid parameter " + (String)localObject3);
          }
          if (bool3) {
            localAppsLog.write(str1, "ACCEPTED v=" + (String)localObject4, 1);
          }
          if (URLToken.isRegistered((String)localObject4)) {
            str4 = localObject4 = URLToken.expand((String)localObject4);
          }
          if (((String)localObject4).charAt(0) == '/') {
            str4 = localObject4 = getSiteWebEntry(paramWebAppsContext) + (String)localObject4;
          }
        }
        else if (bool3)
        {
          localAppsLog.write(str1, (String)localObject3 + " is not protected", 1);
        }
        Object localObject5 = paramHashtable.get(localObject3);
        if (bool3) {
          localAppsLog.write(str1, "Adding '" + str4 + "'", 1);
        }
        Vector localVector;
        if (localObject5 == null)
        {
          localVector = new Vector();
          localVector.add(str4);
          paramHashtable.put(localObject3, localVector);
        }
        else
        {
          localVector = (Vector)localObject5;
          localVector.add(str4);
        }
      }
    }
//    String str3;
    Object localObject3;
    String[] localObject4;
    for (Enumeration<String> localObject1 = paramHashtable.keys(); ((Enumeration)localObject1).hasMoreElements();)
    {
      str3 = (String)((Enumeration)localObject1).nextElement();
      Object localObject2 = paramHashtable.get(str3);
      localObject3 = null;
      if (localObject2 != null) {
        localObject3 = (Vector)localObject2;
      }
      localObject4 = new String[((Vector)localObject3).size()];
      ((Vector)localObject3).toArray((Object[])localObject4);
      paramHashtable.put(str3, localObject4);
      if (bool3)
      {
        localAppsLog.write(str1, "parameter " + str3, 1);
        for (int j = 0; j < localObject4.length; j++) {
          localAppsLog.write(str1, "\t" + localObject4[j], 1);
        }
      }
    }
    if (bool1) {
      localAppsLog.write(str1, "END", 2);
    }
  }
  
  private String buildQS(WebAppsContext paramWebAppsContext)
  {
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].buildQS";
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    boolean bool3 = bool2;
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 1;
    for (Enumeration localEnumeration = this.table.keys(); localEnumeration.hasMoreElements();)
    {
      String str2 = (String)localEnumeration.nextElement();
      String[] arrayOfString = (String[])this.table.get(str2);
      for (int j = 0; j < arrayOfString.length; j++)
      {
        if (i == 0) {
          localStringBuffer.append('&');
        } else {
          i = 0;
        }
        localStringBuffer.append(str2).append('=').append(arrayOfString[j]);
      }
    }
    if (bool2) {
      localAppsLog.write(str1, "ret->" + localStringBuffer.toString(), 2);
    }
    if (bool1) {
      localAppsLog.write(str1, "END", 2);
    }
    return localStringBuffer.toString();
  }
  
  public static boolean hasICXSession(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext)
  {
    return hasICXSessionCookie(paramHttpServletRequest, paramWebAppsContext) ? loadICXSession(paramHttpServletRequest, paramWebAppsContext) : false;
  }
  
  private static String ICX_Cookie(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext)
  {
    String str = paramWebAppsContext.getSessionCookieName();
    Cookie[] arrayOfCookie = paramHttpServletRequest.getCookies();
    for (int i = 0; (arrayOfCookie != null) && (i < arrayOfCookie.length); i++) {
      if (arrayOfCookie[i].getName().equals(str)) {
        return arrayOfCookie[i].getValue();
      }
    }
    return null;
  }
  
  private boolean hasICXSession(WebAppsContext paramWebAppsContext)
  {
    return hasICXSessionCookie(paramWebAppsContext) ? loadICXSession(paramWebAppsContext) : false;
  }
  
  private static boolean hasICXSessionCookie(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog();
    String str = "oracle.apps.fnd.sso.SecureHttpRequest.hasICXSession";
    if (localAppsLog.isEnabled(str, 2)) {
      localAppsLog.write(str, "BEGIN ", 2);
    }
    boolean bool = ICX_Cookie(paramHttpServletRequest, paramWebAppsContext) != null;
    if (localAppsLog.isEnabled(str, 2)) {
      localAppsLog.write(str, "END  result->" + bool, 2);
    }
    return bool;
  }
  
  private boolean hasICXSessionCookie(WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = getLog();
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.hasICXSession";
    if (localAppsLog.isEnabled(str1, 2)) {
      localAppsLog.write(str1, "BEGIN currect icx_cookie=" + this.icx_cookie, 2);
    }
    if (this.icx_cookie != null)
    {
      if (localAppsLog.isEnabled(str1, 2)) {
        localAppsLog.write(str1, "END  (cached) icx_cookie=" + this.icx_cookie, 2);
      }
      return true;
    }
    String str2 = paramWebAppsContext.getSessionCookieName();
    

    Cookie[] arrayOfCookie = getCookies();
    for (int i = 0; (arrayOfCookie != null) && (i < arrayOfCookie.length); i++) {
      if (arrayOfCookie[i].getName().equals(str2)) {
        this.icx_cookie = arrayOfCookie[i].getValue();
      }
    }
    boolean i = this.icx_cookie != null ? true : false;
    if (localAppsLog.isEnabled(str1, 2)) {
      localAppsLog.write(str1, "END  result->" + i + " icx_cookie=" + this.icx_cookie, 2);
    }
    return i;
  }
  
  private boolean loadICXSession(WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = getLog();
    String str = "oracle.apps.fnd.sso.SecureHttpRequest.loadICXSession";
    boolean bool1 = localAppsLog.isEnabled(str, 2);
    if (bool1) {
      localAppsLog.write(str, "BEGIN", 2);
    }
    if (paramWebAppsContext.getSessionIdAsInt() != -1)
    {
      if (bool1) {
        localAppsLog.write(str, "END -> already loaded id=" + paramWebAppsContext.getSessionIdAsInt(), 2);
      }
      return true;
    }
    paramWebAppsContext.validateSession(this.icx_cookie, true);
    boolean bool2 = paramWebAppsContext.getSessionIdAsInt() != -1;
    if (bool1) {
      localAppsLog.write(str, "END -> valid=" + bool2 + " id=" + paramWebAppsContext.getSessionIdAsInt(), 2);
    }
    return bool2;
  }
  
  private static boolean loadICXSession(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog();
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.loadICXSession";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (paramWebAppsContext.getSessionIdAsInt() != -1)
    {
      if (bool1) {
        localAppsLog.write(str1, "END -> already loaded id=" + paramWebAppsContext.getSessionIdAsInt(), 2);
      }
      return true;
    }
    String str2 = ICX_Cookie(paramHttpServletRequest, paramWebAppsContext);
    if (str2 == null)
    {
      if (bool1) {
        localAppsLog.write(str1, "END->false(no cookie)", 2);
      }
      return false;
    }
    paramWebAppsContext.validateSession(str2, true);
    boolean bool2 = paramWebAppsContext.getSessionIdAsInt() != -1;
    if (localAppsLog.isEnabled(str1, 1)) {
      localAppsLog.write(str1, "cookie=" + str2 + " sessionid=" + paramWebAppsContext.getSessionIdAsInt(), 1);
    }
    if (bool1) {
      localAppsLog.write(str1, "END -> valid=" + bool2, 2);
    }
    return bool2;
  }
  
  private String getICXQS(String paramString)
    throws SecureHttpRequest.IllegalStateException
  {
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.getICXQS";
    boolean bool = Utils.isAppsContextAvailable();
    try
    {
      WebAppsContext localWebAppsContext = Utils.getAppsContext();
      AppsLog localAppsLog = getLog(localWebAppsContext);
      if (localAppsLog.isEnabled(2))
      {
        localAppsLog.write(str1, "BEGIN", 2);
        if (localAppsLog.isEnabled(1)) {
          localAppsLog.write(str1, "sessionID=" + localWebAppsContext.getSessionIdAsInt() + " attName=" + "iqn" + paramString, 1);
        }
      }
      String str2 = "iqn" + paramString;
      if (str2.equals("iqn")) {
        return "";
      }
      String str3 = localWebAppsContext.getSessionAttribute(str2);
      if ((str3 == null) || (str2.equals(str3)))
      {
        if (localAppsLog.isEnabled(4)) {
          localAppsLog.write("module", " Incorrect reference to session attribute \n  maybe caused  a bookmared URL SessionId=" + localWebAppsContext.getSessionIdAsInt() + " attName=" + str2, 4);
        }
        str3 = null;
        throw new IllegalStateException("Invalid session reference to query string");
      }
      if (localAppsLog.isEnabled(2)) {
        if (localAppsLog.isEnabled(1)) {
          localAppsLog.write(str1, "END value=" + str3, 1);
        } else {
          localAppsLog.write(str1, "END", 2);
        }
      }
      return str3;
    }
    finally
    {
      if (!bool) {
        Utils.releaseAppsContext();
      }
    }
  }
  
  void scan(String paramString1, String paramString2, String paramString3)
  {
    AppsLog localAppsLog = getLog();
    if (localAppsLog.isEnabled(2)) {
      localAppsLog.write("oracle.apps.fnd.sso.SecureHttpRequest.scan", " called", 2);
    }
    scanInto(this.table = new Hashtable(), paramString1, paramString2, paramString3);
  }
  
  public static void scanInto(Hashtable paramHashtable, String paramString1, String paramString2, String paramString3)
  {
    AppsLog localAppsLog = getLog();
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.scanInto";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (bool2) {
      localAppsLog.write(str1, " q=" + paramString1 + "\n originCharset=" + paramString2 + "\n targetCS=" + paramString3, 1);
    }
    if (paramString2 == null) {
      paramString2 = "UTF8";
    }
    if (paramString3 == null) {
      paramString3 = paramString2;
    }
    if (bool2) {
      localAppsLog.write(str1, "(2)originCharset=" + paramString2 + "\n targetCS=" + paramString3, 1);
    }
    if ((paramString1 == null) || (paramString1.length() == 0))
    {
      if (bool1) {
        localAppsLog.write(str1, "END empty query string", 2);
      }
      return;
    }
    boolean bool3 = !paramString2.equalsIgnoreCase(paramString3);
    if (bool2) {
      localAppsLog.write(str1, "transconding=" + bool3, 1);
    }
    char[] arrayOfChar = paramString1.toCharArray();
    
    int i = 0;
    int k;
    int j = k = -1;
    Object localObject2 = null;
    Object localObject3;
    Object localObject4;
    for (int m = 0; m <= arrayOfChar.length; m++) {
      if ((m == arrayOfChar.length) || (arrayOfChar[m] == '&'))
      {
        if ((m > i) && (arrayOfChar[i] != '='))
        {
          String str2 = String.valueOf(arrayOfChar, i, j > 0 ? j : m - i);
          String localObject1 = j > 0 ? null : m > k ? String.valueOf(arrayOfChar, k, m - k) : null;
          localObject3 = paramHashtable.get(str2);
          if (bool3) {
            try
            {
              localObject2 = new String(localObject1.getBytes(paramString2), paramString3);
            }
            catch (UnsupportedEncodingException localUnsupportedEncodingException)
            {
              localObject2 = localObject1;
            }
          } else {
            localObject2 = localObject1;
          }
          if (bool2) {
            localAppsLog.write(str1, "\t " + str2 + '=' + (String)localObject2, 1);
          }
          if (localObject3 == null)
          {
            localObject4 = new Vector();
            ((Vector)localObject4).add(localObject2 != null ? localObject2 : "");
            paramHashtable.put(str2, localObject4);
          }
          else
          {
            ((Vector)localObject3).add(localObject2);
          }
        }
        i = m + 1;
        j = k = -1;
      }
      else if ((j == -1) && (arrayOfChar[m] == '=') && (m > i))
      {
        k = m + 1;
        j = m - i;
      }
    }
    for (Enumeration localEnumeration = paramHashtable.keys(); localEnumeration.hasMoreElements();)
    {
      localObject3 = localEnumeration.nextElement();
      localObject4 = paramHashtable.get(localObject3);
      if ((localObject4 != null) && ((localObject4 instanceof Vector)))
      {
        Vector localVector = (Vector)localObject4;
        if (localVector.size() > 0)
        {
          String[] arrayOfString = new String[localVector.size()];
          localVector.toArray(arrayOfString);
          



          paramHashtable.put(localObject3, arrayOfString);
        }
      }
    }
    if (bool1) {
      localAppsLog.write(str1, "END", 2);
    }
  }
  
  public String getQueryString()
  {
    return this.QS;
  }
  
  public String getOriginalParameter(String paramString)
  {
    String str = super.getParameter(paramString);
    AppsLog localAppsLog = getLog();
    if (localAppsLog.isEnabled(1)) {
      localAppsLog.write("oracle.apps.fnd.sso.SecureHttpRequest.getOriginalParameter", paramString + "=>" + str, 1);
    }
    return str;
  }
  
  public Enumeration getParameterNames()
  {
    return this.table.keys();
  }
  
  public Map getParameterMap()
  {
    return this.table;
  }
  
  public String[] getParameterValues(String paramString)
  {
    return (String[])this.table.get(paramString);
  }
  
  public String getParameter(String paramString)
  {
    AppsLog localAppsLog = getLog();
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.getParameter";
    try
    {
      String str2 = ((String[])this.table.get(paramString))[0];
      if (localAppsLog.isEnabled(str1, 2)) {
        localAppsLog.write(str1, paramString + "=" + str2, 2);
      }
      return str2;
    }
    catch (NullPointerException localNullPointerException)
    {
      if (localAppsLog.isEnabled(str1, 2)) {
        localAppsLog.write(str1, paramString + "=<null>", 2);
      }
    }
    return null;
  }
  
  public static void forceShortRedirect(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse)
  {
    boolean bool1 = Utils.isAppsContextAvailable();
    AppsLog localAppsLog = null;
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.forceShortRedirect";
    try
    {
      WebAppsContext localWebAppsContext = Utils.getAppsContext();
      
      localAppsLog = getLog();
      boolean bool2 = localAppsLog.isEnabled(str1, 2);
      boolean bool3 = localAppsLog.isEnabled(str1, 1);
      if (bool2)
      {
        localAppsLog.write(str1, "BEGIN ", 2);
        if (bool3) {
          localAppsLog.write(str1, " request =" + paramHttpServletRequest.getPathTranslated(), 1);
        }
      }
      if (localWebAppsContext.getUserId() == -1)
      {
        if (bool3) {
          localAppsLog.write(str1, "No sesion in context, trying to load one", 1);
        }
        String localObject1 = SessionMgr.getAppsCookie(paramHttpServletRequest);
        if (localObject1 != null) {
          localWebAppsContext.validateSession((String)localObject1, true);
        }
        if (bool3) {
          localAppsLog.write(str1, " first attempt cookie=" + (String)localObject1 + " id=" + localWebAppsContext.getSessionIdAsInt(), 1);
        }
      }
      if (localWebAppsContext.getUserId() == -1)
      {
        if (bool3) {
          localAppsLog.write(str1, " No session available, trying to create GUEST one", 1);
        }
        if (!localWebAppsContext.createAnonymousSession())
        {
          if (localAppsLog.isEnabled(4))
          {
        	ErrorStack  localObject1 = localWebAppsContext.getErrorStack();
            if ((localObject1 != null) && (((ErrorStack)localObject1).getMessageCount() > 0))
            {
              localAppsLog.write(str1, "Exception at WebAppsContext.createAnonymousSession", 4);
              localAppsLog.write(str1, ((ErrorStack)localObject1).getAllMessages(), 4);
              ((ErrorStack)localObject1).clear();
            }
            else
            {
              localAppsLog.write(str1, "Exception at WebAppsContext.createAnonymousSession WITHOUT error description", 4);
            }
            localAppsLog.write(str1, "Can't create a GUEST session, rising RunTimeException ", 4);
          }
          throw new RuntimeException("Can't create guest session");
        }
        Cookie localObject1 = new Cookie(localWebAppsContext.getSessionCookieName(), localWebAppsContext.getSessionCookieValue());
        String str2 = localWebAppsContext.getSessionCookieDomain();
        try
        {
          if (str2 == null) {
            str2 = SessionMgr.getServerDomain(paramHttpServletRequest, paramHttpServletResponse);
          }
        }
        catch (Exception localException2) {}
        if (str2 != null) {
          ((Cookie)localObject1).setDomain(str2);
        }
        ((Cookie)localObject1).setPath("/");
        ((Cookie)localObject1).setMaxAge(-1);
        
        paramHttpServletResponse.addCookie((Cookie)localObject1);
        if (bool3) {
          localAppsLog.write(str1, "Created guest session for '" + localWebAppsContext.getSessionCookieDomain() + "' " + localWebAppsContext.getSessionCookieName() + "=" + localWebAppsContext.getSessionCookieValue() + " sessionId=" + localWebAppsContext.getSessionIdAsInt(), 1);
        }
      }
      Object localObject1 = paramHttpServletRequest.getQueryString();
      String str2 = localWebAppsContext.computeMAC((String)localObject1);
      localWebAppsContext.setSessionAttribute("iqn" + str2, (String)localObject1);
      String str3 = "iqn=" + str2;
      String str4 = paramHttpServletRequest.getRequestURI();
      if (bool3)
      {
        localAppsLog.write(str1, " STORING iqn" + str2 + " = " + (String)localObject1, 1);
        localAppsLog.write(str1, " REDIRECT TO " + str4 + "?" + str3, 1);
      }
      paramHttpServletResponse.sendRedirect(str4 + "?" + str3);
      if (bool2) {
        localAppsLog.write(str1, "END (derirecting)", 2);
      }
    }
    catch (Exception localException1)
    {
      if (localAppsLog == null) {
        localAppsLog = getLog();
      }
      if (localAppsLog.isEnabled(4))
      {
        localAppsLog.write(str1, "Exception " + localException1.toString(), 4);
        localAppsLog.write(str1, localException1, 4);
      }
      throw new RuntimeException(localException1);
    }
    finally
    {
      if (!bool1) {
        Utils.releaseAppsContext();
      }
    }
  }
  
  public static HttpServletRequest verifySecurity(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, WebAppsContext paramWebAppsContext)
  {
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    String str = "oracle.apps.fnd.sso.SecureHttpRequest[$Revision: 120.10.12010000.11 $].verifySecurity";
    boolean bool1 = localAppsLog.isEnabled(str, 2);
    boolean bool2 = localAppsLog.isEnabled(str, 1);
    boolean bool3 = localAppsLog.isEnabled(str, 4);
    if (bool1) {
      localAppsLog.write(str, "BEGIN", 2);
    }
    try
    {
      if (((paramHttpServletRequest instanceof SecureHttpRequest)) || (noSignatureRequired(paramHttpServletRequest, localAppsLog)))
      {
        if (bool2)
        {
          if ((paramHttpServletRequest instanceof SecureHttpRequest)) {
            localAppsLog.write(str, "Already a secure object", 1);
          }
          if (noSignatureRequired(paramHttpServletRequest, localAppsLog)) {
            localAppsLog.write(str, "Is an UNSECURE client ", 1);
          }
        }
        if (bool1) {
          localAppsLog.write(str, "END-> (same)", 2);
        }
        return paramHttpServletRequest;
      }
      if (bool2) {
        localAppsLog.write(str, "Not a secure object", 1);
      }
      if (validateSignature(paramHttpServletRequest, paramWebAppsContext))
      {
        if (bool2) {
          localAppsLog.write(str, "Valid signature found", 1);
        }
        return paramHttpServletRequest;
      }
      if (bool2) {
        localAppsLog.write(str, "Not signed", 1);
      }
      HttpServletRequest localObject1 = new SecureHttpRequest(paramHttpServletRequest, paramWebAppsContext);
      if (bool2) {
        localAppsLog.write(str, "Secure Object created", 1);
      }
      return localObject1;
    }
    catch (SecurityException localSecurityException1)
    {
      Object localObject2;
      if (bool3) {
        localAppsLog.write(str, localSecurityException1, 4);
      }
      if (bool2) {
        localAppsLog.write(str, "Removing parameters ", 1);
      }
      try
      {
        return new EmptyRequest(paramHttpServletRequest, paramWebAppsContext);
      }
      catch (SecurityException localSecurityException2)
      {
        if (bool3) {
          localAppsLog.write(str, localSecurityException2, 4);
        }
        if (bool2) {
          localAppsLog.write(str, "Returnin null... will crash ", 1);
        }
        return null;
      }
    }
    catch (Throwable localThrowable)
    {
      if (bool3) {
        localAppsLog.write(str, localThrowable, 4);
      }
      throw new SecurityException("failed", localThrowable);
    }
    finally
    {
      if (bool1) {
        localAppsLog.write(str, "END", 2);
      }
    }
  }
  
  private static synchronized void loadAgentLimits()
  {
    AppsLog localAppsLog = getLog();
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.loadAgentLimits";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    if (agentLimits != null)
    {
      if (bool1) {
        localAppsLog.write(str1, "END already loaded", 2);
      }
      return;
    }
    MAX_URL_LENGTH = 4095;
    if (bool2) {
      localAppsLog.write(str1, "MAX_URL_LENGTH=" + MAX_URL_LENGTH, 1);
    }
    agentLimits = new Hashtable();
    String str2 = System.getProperty("oracle.apps.fnd.sso.AgentLimits");
    int i = 0;
    if (str2 != null)
    {
      if (bool2) {
        localAppsLog.write(str1, "Loading oracle.apps.fnd.sso.AgentLimits=" + str2, 1);
      }
      try
      {
        String[] arrayOfString1 = str2.split(":");
        for (int j = 0; j < arrayOfString1.length; j++)
        {
          String[] arrayOfString2 = arrayOfString1[j].split("=");
          if (arrayOfString2.length == 2)
          {
            agentLimits.put(arrayOfString2[0], new Integer(arrayOfString2[1]));
            if (bool2) {
              localAppsLog.write(str1, "added " + arrayOfString2[0] + "=" + ((Integer)agentLimits.get(arrayOfString2[0])).intValue(), 2);
            }
          }
          else
          {
            if (localAppsLog.isEnabled(str1, 4)) {
              localAppsLog.write(str1, "Exception during property parsing \n System property oracle.apps.fnd.sso.AgentLimits=" + str2 + "\n entry '" + arrayOfString1[j] + "'", 4);
            }
            throw new RuntimeException("Invalid agent limit spec");
          }
        }
      }
      catch (RuntimeException localRuntimeException)
      {
        if (localAppsLog.isEnabled(str1, 4))
        {
          localAppsLog.write(str1, "Exception during property parsing \n System property oracle.apps.fnd.sso.AgentLimits=" + str2, 4);
          
          localAppsLog.write(str1, localRuntimeException, 4);
        }
        if (bool2) {
          localAppsLog.write(str1, "Error parsing string, cotinue with defaults instead", 1);
        }
        i = 1;
      }
    }
    else
    {
      if (bool2) {
        localAppsLog.write(str1, "Property oracle.apps.fnd.sso.AgentLimits nod defined", 1);
      }
      i = 1;
    }
    if (i != 0)
    {
      agentLimits.put("MSIE", new Integer(500));
      if (bool2) {
        localAppsLog.write(str1, "adde MSIE=500", 1);
      }
    }
    else if (bool2)
    {
      localAppsLog.write(str1, "no defaults", 1);
    }
    if (bool1) {
      localAppsLog.write(str1, "END", 2);
    }
  }
  
  public static boolean TooLong(String paramString1, String paramString2)
  {
    AppsLog localAppsLog = getLog();
    String str1 = "oracle.apps.fnd.sso.SecureHttpRequest.TooLong";
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1)
    {
      localAppsLog.write(str1, "BEGIN length=" + (paramString1 != null ? paramString1.length() : 0) + " agent=" + paramString2, 2);
      if (bool2) {
        localAppsLog.write(str1, "MAX_UR_LENGTH=" + MAX_URL_LENGTH + "url=" + paramString1, 1);
      }
    }
    if ((paramString2 == null) || (paramString2.length() == 0))
    {
      if (bool1) {
        localAppsLog.write(str1, "END -> false  Zero length AGENT ", 2);
      }
      return false;
    }
    if ((paramString1 == null) || (paramString1.length() == 0))
    {
      if (bool1) {
        localAppsLog.write(str1, "END -> false  Zero length URL ", 2);
      }
      return false;
    }
    if (agentLimits == null)
    {
      if (bool2) {
        localAppsLog.write(str1, "load agent limits", 1);
      }
      loadAgentLimits();
    }
    int i = paramString1.length();
    
    boolean bool3 = false;
    for (Enumeration localEnumeration = agentLimits.keys(); (!bool3) && (localEnumeration.hasMoreElements());)
    {
      String str2 = (String)localEnumeration.nextElement();
      if (paramString2.indexOf(str2) > 0)
      {
        if (bool2) {
          localAppsLog.write(str1, "'" + paramString2 + "' matches '" + str2 + "'", 1);
        }
        bool3 = i >= ((Integer)agentLimits.get(str2)).intValue();
        if (bool2) {
          localAppsLog.write(str1, " too log=" + bool3 + " url length=" + i + " > agemt_limit=" + ((Integer)agentLimits.get(str2)).intValue(), 1);
        }
      }
      else if (bool2)
      {
        localAppsLog.write(str1, " didn't match to agent='" + str2 + "'", 1);
      }
    }
    if (bool1) {
      localAppsLog.write(str1, "END ->" + ((bool3) || (i >= MAX_URL_LENGTH)), 2);
    }
    return (bool3) || (i >= MAX_URL_LENGTH);
  }
  
  public static boolean TooLong(HttpServletRequest paramHttpServletRequest)
  {
    if ((paramHttpServletRequest instanceof SecureHttpRequest))
    {
      SecureHttpRequest localSecureHttpRequest = (SecureHttpRequest)paramHttpServletRequest;
      if (localSecureHttpRequest.fromExpansion) {
        return false;
      }
    }
    return TooLong(paramHttpServletRequest.getPathInfo() + paramHttpServletRequest.getQueryString(), paramHttpServletRequest.getHeader("User-Agent"));
  }
  
  public static String removeParameter(String paramString1, String paramString2)
  {
    StringBuffer localStringBuffer = new StringBuffer(paramString1.length());
    int i = paramString1.indexOf('?');
    String[] arrayOfString = null;
    if (i > 0)
    {
      localStringBuffer.append(paramString1.substring(0, i + 1));
      arrayOfString = paramString1.substring(i + 1).split("&");
    }
    else
    {
      arrayOfString = paramString1.split("&");
    }
    int j = 1;
    if (arrayOfString != null) {
      for (int k = 0; k < arrayOfString.length; k++) {
        if (!arrayOfString[k].startsWith(paramString2 + "="))
        {
          if (j == 0) {
            localStringBuffer.append("&");
          }
          j = 0;
          localStringBuffer.append(arrayOfString[k]);
        }
      }
    }
    return localStringBuffer.toString();
  }
  
  public static String shortUrl(String paramString, WebAppsContext paramWebAppsContext)
  {
    String str1 = "oracle.apps.fnd.ssoSecureHttpRequest.shortUrl";
    AppsLog localAppsLog = Utils.getLog(paramWebAppsContext);
    boolean bool1 = localAppsLog.isEnabled(str1, 2);
    boolean bool2 = localAppsLog.isEnabled(str1, 1);
    if (bool1) {
      localAppsLog.write(str1, "BEGIN", 2);
    }
    String str2 = "TCK_" + paramWebAppsContext.computeMAC(paramString);
    paramWebAppsContext.setSessionAttribute(str2, paramString);
    if (bool2) {
      localAppsLog.write(str1, "ticket=" + str2, 1);
    }
    String str3 = getRedirUrl(paramWebAppsContext) + str2;
    if (bool2) {
      localAppsLog.write(str1, "redir=" + str3, 1);
    }
    if (bool1) {
      localAppsLog.write(str1, "END", 2);
    }
    return str3;
  }
  
  static String getRedirUrl(WebAppsContext paramWebAppsContext)
  {
    synchronized (redirUrl)
    {
      if ("".equals(redirUrl))
      {
        StringBuffer localStringBuffer = new StringBuffer(Utils.getAppsServletAgent(paramWebAppsContext));
        if (localStringBuffer.charAt(localStringBuffer.length() - 1) != '/') {
          localStringBuffer.append('/');
        }
        if (Utils.release11i) {
          localStringBuffer.append("AppsSSOServlet?redir=");
        } else {
          localStringBuffer.append("AppsPwdChange?redir&");
        }
        redirUrl = localStringBuffer.toString();
      }
      return redirUrl;
    }
  }
  
  private static String URLDecode(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      return null;
    }
    Object localObject1;
    synchronized (urlDecoderClass)
    {
      if (urlDecoderClass.equals(Object.class))
      {
        try
        {
          urlDecoderClass = Class.forName("java.net.URLDecoder");
        }
        catch (ClassNotFoundException localClassNotFoundException)
        {
          throw new RuntimeException(localClassNotFoundException.toString());
        }
        localObject1 = new Class[] { String.class, String.class };
        try
        {
          decodeMethod = urlDecoderClass.getMethod("decode", (Class[])localObject1);
          prejava14 = false;
        }
        catch (NoSuchMethodException localNoSuchMethodException1)
        {
          localObject1 = new Class[] { String.class };
          try
          {
            decodeMethod = urlDecoderClass.getMethod("decode", (Class[])localObject1);
            prejava14 = true;
          }
          catch (NoSuchMethodException localNoSuchMethodException2)
          {
            urlDecoderClass = Object.class;
            throw new RuntimeException(localNoSuchMethodException1.toString());
          }
        }
      }
    }
    if (prejava14)
    {
      localObject1 = new Object[] { paramString1 };
      try
      {
    	  localObject1 = decodeMethod.invoke(urlDecoderClass, (Object[])localObject1);
      }
      catch (IllegalAccessException localIllegalAccessException1)
      {
        throw new RuntimeException(localIllegalAccessException1.toString());
      }
      catch (InvocationTargetException localInvocationTargetException1)
      {
        throw new RuntimeException(localInvocationTargetException1.toString());
      }
    }
    else
    {
      localObject1 = new Object[] { paramString1, paramString2 };
      try
      {
    	  localObject1 = decodeMethod.invoke(urlDecoderClass, (Object[])localObject1);
      }
      catch (IllegalAccessException localIllegalAccessException2)
      {
        throw new RuntimeException(localIllegalAccessException2.toString());
      }
      catch (InvocationTargetException localInvocationTargetException2)
      {
        throw new RuntimeException(localInvocationTargetException2.toString());
      }
    }
    return localObject1 != null ? (String)localObject1 : null;
  }
  
  static
  {
    pParams = new Hashtable();
    for (int i = 0; i < protectedParams.length; i++) {
      pParams.put(protectedParams[i], protectedParams[i]);
    }
    agentLimits = null;
    


























































































































































































































































    redirUrl = "";
    



















    urlDecoderClass = Object.class;
    decodeMethod = null;
    prejava14 = false;
    













































































    new SimpleURLToken("APPSHOMEPAGE", "APPSHOMEPAGE");
  }
}
