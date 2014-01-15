package aurora.i18n;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uncertain.cache.ICache;
import uncertain.cache.INamedCacheFactory;
import uncertain.composite.CompositeMap;
import uncertain.composite.transform.GroupConfig;
import uncertain.composite.transform.GroupTransformer;
import uncertain.core.IGlobalInstance;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.GeneralException;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.oraclestreams_bak.IOracleStreamsListener;
import aurora.application.features.oraclestreams_bak.OracleStreamsManager;
import aurora.application.features.oraclestreams_bak.Streams_dml_lcr;
import aurora.application.features.oraclestreams_bak.Streams_dml_lcr_column;
import aurora.database.FetchDescriptor;
import aurora.database.service.BusinessModelService;
import aurora.database.service.DatabaseServiceFactory;
import aurora.database.service.IDatabaseServiceFactory;
import aurora.database.service.SqlServiceContext;
import aurora.service.ServiceThreadLocal;

public class StreamsBasedMessageProvider_bak extends AbstractLocatableObject implements IMessageProvider, IGlobalInstance,
		IOracleStreamsListener {

	private IDatabaseServiceFactory factory;
	private boolean inited = false;
	private HashMap localMPMaps = new HashMap();
	private String descModel;
	private String langPath = "";
	private String defaultLang = "";
	IObjectRegistry registry;
	private String cacheName = "PromptCodeCache";
	private ICache mPromptCache;
	private static String KEY_PROMPT_ID = "prompt_id";
	private static String KEY_PROMPT_CODE = "prompt_code";
	private static String KEY_LANGUAGE = "language";
	private static String KEY_DESCRIPTION = "description";
	private String mlTable = "SYS_PROMPTS";
	private Map<Long, String> prompt_id_map = new HashMap<Long, String>();

	public StreamsBasedMessageProvider_bak(IObjectRegistry registry, IDatabaseServiceFactory fact) {
		this.registry = registry;
		this.factory = fact;
	}

	public void onInitialize() throws Exception {
		if (!inited) {
			OracleStreamsManager osm = (OracleStreamsManager) registry.getInstanceOfType(OracleStreamsManager.class);
			if (osm == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(null, OracleStreamsManager.class, this.getClass()
						.getCanonicalName());
			INamedCacheFactory mCacheFactory = (INamedCacheFactory) registry.getInstanceOfType(INamedCacheFactory.class);
			mPromptCache = mCacheFactory.getNamedCache(cacheName);
			if (mPromptCache == null)
				throw new GeneralException("uncertain.cache.named_cache_not_found", new Object[] { cacheName }, this);
			try {
				loadPromptData();
				osm.addListener(this);
				inited = true;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public ILocalizedMessageProvider getLocalizedMessageProvider(String language_code) {
		return (ILocalizedMessageProvider) localMPMaps.get(language_code);
	}

	public String getMessage(String language_code, String message_code) {
		return String.valueOf(mPromptCache.getValue(getFullCacheKey(message_code, language_code)));
	}

	public String getMessage(String language_code, String message_code, Object[] params) {
		ILocalizedMessageProvider localMessageProvider = (ILocalizedMessageProvider) localMPMaps.get(language_code);
		if (localMessageProvider == null)
			return message_code;
		return localMessageProvider.getMessage(message_code, params);
	}

	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void notice(Streams_dml_lcr lcr) throws Exception {
		if (!mlTable.equalsIgnoreCase(lcr.getObjectName()))
			return;
		Streams_dml_lcr_column[] columns = lcr.getPkColumnList().getArray();
		Long promptId = getPromptId(columns);
		if ("DELETE".equals(lcr.getCommandType())) {
			mPromptCache.remove(prompt_id_map.get(promptId));
		}

		if ("INSERT".equals(lcr.getCommandType()) || "UPDATE".equals(lcr.getCommandType())) {
			if ("UPDATE".equals(lcr.getCommandType())) {
				mPromptCache.remove(prompt_id_map.get(promptId));
			}
			mPromptCache.remove(prompt_id_map.get(promptId));
			Map<String,Long> queryMap = new HashMap<String,Long>();
			queryMap.put("prompt_id", promptId);
			CompositeMap childs = queryData(queryMap);
			if (childs == null || childs.getChilds() == null)
				return;
			CompositeMap record = (CompositeMap) childs.getChilds().get(0);
			mPromptCache.setValue(getFullCacheKey(record), record.get(KEY_DESCRIPTION));
		}

	}

	public void loadPromptData() throws Exception {
		SqlServiceContext context = factory.createContextWithConnection();
		try {
			BusinessModelService service = factory.getModelService(getDescModel(), context.getObjectContext());
			CompositeMap resultMap = service.queryAsMap(new HashMap<String,String>(), FetchDescriptor.fetchAll());
			if (resultMap != null && resultMap.getChilds() != null) {
				for (Iterator<CompositeMap> it = resultMap.getChildIterator(); it.hasNext();) {
					CompositeMap record = it.next();
					String cacheKey = getFullCacheKey(record);
					prompt_id_map.put(record.getLong(KEY_PROMPT_ID), cacheKey);
					mPromptCache.setValue(cacheKey, record.get(KEY_DESCRIPTION));
				}

			}
			cacheMessage(resultMap);
		} finally {
			if (context != null)
				context.freeConnection();
		}
	}
	private void cacheMessage(CompositeMap map) {
		if (map != null) {
			List list = map.getChildsNotNull();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				CompositeMap message = (CompositeMap) it.next();
				String language = message.getString("language");
				ILocalizedMessageProvider localMessageProvider = (ILocalizedMessageProvider) localMPMaps.get(language);
				if (localMessageProvider == null) {
					//TODO should be refactor
//					localMessageProvider = new CacheBasedLocalizedMessageProvider(language, mPromptCache);
					localMPMaps.put(language, localMessageProvider);
				}
				String code = message.getString("prompt_code");
				String description = message.getString("description");
				localMessageProvider.putMessage(code, description);
			}
		}
	}

	private String getFullCacheKey(CompositeMap record) {
		return record.getString(KEY_PROMPT_CODE) + "." + record.getString(KEY_LANGUAGE);
	}

	private String getFullCacheKey(String code, String langugage) {
		return code + "." + langugage;
	}

	private Long getPromptId(Streams_dml_lcr_column[] pkColumns) throws SQLException {
		if (pkColumns == null)
			throw new RuntimeException("pkColumns can not be null!");
		Long cacheKey = null;
		for (int i = 0; i < pkColumns.length; i++) {
			if (pkColumns[i] == null)
				break;
			Streams_dml_lcr_column column = pkColumns[i];
			if (KEY_PROMPT_ID.toUpperCase().equals(column.getColumnName().toUpperCase()))
				cacheKey = Long.valueOf(column.getValue());
		}
		return cacheKey;
	}

	public CompositeMap queryData(Map queryMap) throws Exception {
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		// if (context == null)
		// throw new
		// IllegalStateException("No service context set in ThreadLocal yet");
		if (context == null)
			context = new CompositeMap();
		BusinessModelService service = factory.getModelService(getDescModel(), context);
		CompositeMap resultMap = service.queryAsMap(queryMap, FetchDescriptor.fetchAll());
		return resultMap;
	}
	public String getDescModel() {
		return descModel;
	}

	public void setDescModel(String descModel) {
		this.descModel = descModel;
	}

	public String getLangPath() {
		return langPath;
	}

	public void setLangPath(String langPath) {
		this.langPath = langPath;
	}

	public String getDefaultLang() {
		return defaultLang;
	}

	public void setDefaultLang(String defaultLang) {
		this.defaultLang = defaultLang;
	}
}
