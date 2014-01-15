package aurora.application.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uncertain.cache.ICache;
import uncertain.cache.INamedCacheFactory;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
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
import aurora.database.service.SqlServiceContext;
import aurora.service.ServiceThreadLocal;
public class StreamsBasedLookupCodeProvider_bak extends AbstractLocatableObject implements ILookupCodeProvider, IGlobalInstance,
		IOracleStreamsListener {

	private String lookupType = "cache";
	private String lookupSql;
	private String lookupModel;
	private String sortField;
	private String needCache;
	private DatabaseServiceFactory factory;
	private IObjectRegistry registry;
	private boolean inited = false;
	private static final String DEFAULT_SORT_FIELD = "code_value_id";
	private String cacheName = "lookupCodeCache";
	private ICache mlookupCodeCache;
	private String cacheKeyPrefix = "${@code}.${@language}";
	private Map<Long, List<CompositeMap>> code_id_maps = new HashMap<Long, List<CompositeMap>>();
	private Map<Long, Long> code_value_id_maps = new HashMap<Long, Long>();

	public StreamsBasedLookupCodeProvider_bak(IObjectRegistry registry) {
		super();
		this.registry = registry;
	}

	public List getLookupList(String language, String lookup_code) throws Exception {
		List result = (List) mlookupCodeCache.getValue(getFullCacheKey(lookup_code, language));
		if (result != null)
			sorList(result);
		return result;
	}

	private void sorList(List result) {
		Collections.sort(result, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				CompositeMap r1 = (CompositeMap) arg0;
				CompositeMap r2 = (CompositeMap) arg1;
				Integer id1 = r1.getInt(getSortField());
				Integer id2 = r2.getInt(getSortField());
				return id1.compareTo(id2);
			}
		});
	}

	public String getLookupPrompt(String language, String lookup_code, Object lookup_value) {
		return null;
	}

	public void onInitialize() throws Exception {
		factory = (DatabaseServiceFactory) registry.getInstanceOfType(DatabaseServiceFactory.class);
		init();
	}
	private void init() throws Exception {
		if ("cache".equalsIgnoreCase(lookupType) && !inited) {
			OracleStreamsManager osm = (OracleStreamsManager) registry.getInstanceOfType(OracleStreamsManager.class);
			if (osm == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(null, OracleStreamsManager.class, this.getClass()
						.getCanonicalName());
			INamedCacheFactory mCacheFactory = (INamedCacheFactory) registry.getInstanceOfType(INamedCacheFactory.class);
			if (mCacheFactory == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(null, INamedCacheFactory.class, this.getClass()
						.getCanonicalName());
			mlookupCodeCache = mCacheFactory.getNamedCache(cacheName);
			if (mlookupCodeCache == null)
				throw new GeneralException("uncertain.cache.named_cache_not_found", new Object[] { cacheName }, this);
			try {
				loadLookupCodeData();
				osm.addListener(this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String getLookupType() {
		return lookupType;
	}

	public void setLookupType(String lookupType) {
		this.lookupType = lookupType;
	}

	public String getLookupSql() {
		return lookupSql;
	}

	public void setLookupSql(String lookupSql) {
		this.lookupSql = lookupSql;
	}

	public String getLookupModel() {
		return lookupModel;
	}

	public void setLookupModel(String lookupModel) {
		this.lookupModel = lookupModel;
	}

	public String getSortField() {
		return sortField == null ? DEFAULT_SORT_FIELD : sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public String getNeedCache() {
		return needCache;
	}

	public void setNeedCache(String needCache) {
		this.needCache = needCache;
	}

	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void notice(Streams_dml_lcr lcr) throws Exception {
		if ("sys_codes".equalsIgnoreCase(lcr.getObjectName())) {
			sys_codes_notice(lcr);
			return;
		}
		if ("sys_code_values".equalsIgnoreCase(lcr.getObjectName())) {
			sys_code_values_notice(lcr);
			return;
		}
		if ("fnd_descriptions".equalsIgnoreCase(lcr.getObjectName())) {
			fnd_descriptions_notice(lcr);
			return;
		}
	}

	private void fnd_descriptions_notice(Streams_dml_lcr lcr) throws Exception {
		Streams_dml_lcr_column[] pkColumns = lcr.getPkColumnList().getArray();
		if (pkColumns == null)
			return;
		Long description_id = null;
		String language = "";
		for (int i = 0; i < pkColumns.length; i++) {
			if (pkColumns[i] == null)
				break;
			Streams_dml_lcr_column column = pkColumns[i];
			if ("language".equals(column.getColumnName().toLowerCase()))
				language = column.getValue();
			else if ("description_id".equals(column.getColumnName().toLowerCase())) {
				description_id = Long.valueOf(column.getValue());
			}
		}
		Map map = new HashMap();
		map.put("language", language);
		map.put("code_value_name_id", description_id);
		CompositeMap result = queryData(map);
		CompositeMap transResult = GroupTransformer.transform(result, new GroupConfig(new String[] { "code_id" }));
		for (Object child : transResult.getChilds()) {
			CompositeMap record = (CompositeMap) child;
			Long code_id = record.getLong("code_id");
			Map queryMap = new HashMap();
			queryMap.put("code_id", code_id);
			CompositeMap code_id_result = queryData(queryMap);
			updateCache(code_id, code_id_result);
		}
	}

	private void updateCache(Long code_id, CompositeMap dbData) {
		List<CompositeMap> childs = code_id_maps.get(code_id);
		if(childs != null)
			for (CompositeMap record : childs) {
				mlookupCodeCache.remove(getFullCacheKey(record));
			}
		code_id_maps.remove(code_id);
		if (dbData == null) {
			return;
		}
		CompositeMap transResult = GroupTransformer.transform(dbData, new GroupConfig(new String[] { "code", "language" }));
		if (transResult == null || transResult.getChilds() == null)
			return;
		code_id_maps.put(code_id, transResult.getChilds());
		for (Object child : transResult.getChilds()) {
			CompositeMap record = (CompositeMap) child;
			mlookupCodeCache.setValue(getFullCacheKey(record), record.getChilds());
		}
	}

	private void sys_code_values_notice(Streams_dml_lcr lcr) throws Exception {
		Streams_dml_lcr_column[] pkColumns = lcr.getPkColumnList().getArray();
		if (pkColumns == null)
			return;
		Long code_value_id = null;
		for (int i = 0; i < pkColumns.length; i++) {
			if (pkColumns[i] == null)
				break;
			Streams_dml_lcr_column column = pkColumns[i];
			if ("code_value_id".equals(column.getColumnName().toLowerCase()))
				code_value_id = Long.valueOf(column.getValue());
		}
		if ("DELETE".equals(lcr.getCommandType()) || "UPDATE".equals(lcr.getCommandType())) {
			if ("DELETE".equals(lcr.getCommandType()))
				code_value_id_maps.remove(code_value_id);
			long code_id = code_value_id_maps.get(code_value_id);
			Map map = new HashMap();
			map.put("code_id", code_id);
			CompositeMap result = queryData(map);
			updateCache(code_id, result);
		}
		if ("INSERT".equals(lcr.getCommandType())) {
			Map map = new HashMap();
			map.put("code_value_id", code_value_id);
			CompositeMap result = queryData(map);
			if (result == null||result.getChilds() == null)
				return;
			long code_id = ((CompositeMap) (result.getChilds().get(0))).getLong("code_id");
			code_value_id_maps.put(code_value_id, code_id);
			updateCache(code_id, result);
		}
	}

	private void sys_codes_notice(Streams_dml_lcr lcr) throws Exception {
		Streams_dml_lcr_column[] pkColumns = lcr.getPkColumnList().getArray();
		if (pkColumns == null)
			return;
		Long code_id = null;
		for (int i = 0; i < pkColumns.length; i++) {
			if (pkColumns[i] == null)
				break;
			Streams_dml_lcr_column column = pkColumns[i];
			if ("code_id".equals(column.getColumnName().toLowerCase()))
				code_id = Long.valueOf(column.getValue());
		}
		if ("DELETE".equals(lcr.getCommandType())) {
			updateCache(code_id, null);
		}
		if ("INSERT".equals(lcr.getCommandType()) || "UPDATE".equals(lcr.getCommandType())) {
			Map map = new HashMap();
			map.put("code_id", code_id);
			CompositeMap result = queryData(map);
			updateCache(code_id, result);

		}
	}

	public void loadLookupCodeData() throws Exception {
		SqlServiceContext context = factory.createContextWithConnection();
		try {// 是否有更优的算法
			BusinessModelService service = factory.getModelService(getLookupModel(), context.getObjectContext());
			CompositeMap resultMap = service.queryAsMap(new HashMap(), FetchDescriptor.fetchAll());
			if (resultMap != null && resultMap.getChilds() != null) {
				CompositeMap config = new CompositeMap();
				CompositeMap level1 = new CompositeMap();
				level1.put(GroupConfig.KEY_GROUP_KEY_FIELDS, "code_id");
				level1.put(GroupConfig.KEY_RECORD_NAME, "level1");
				config.addChild(level1);
				CompositeMap level2 = new CompositeMap();
				level2.put(GroupConfig.KEY_GROUP_KEY_FIELDS, "code,language");
				level2.put(GroupConfig.KEY_RECORD_NAME, "level2");
				config.addChild(level2);
				CompositeMap transResult = GroupTransformer.transformByConfig(resultMap, config);
				for (Iterator<CompositeMap> code_it = transResult.getChildIterator(); code_it.hasNext();) {
					CompositeMap code_id_child = code_it.next();
					code_id_maps.put(code_id_child.getLong("code_id"), code_id_child.getChilds());
					for (Iterator<CompositeMap> lang_it = code_id_child.getChildIterator(); lang_it.hasNext();) {
						CompositeMap lang_child = lang_it.next();
						mlookupCodeCache.setValue(getFullCacheKey(lang_child), lang_child.getChilds());
					}
				}
				for (Iterator<CompositeMap> it = resultMap.getChildIterator(); it.hasNext();) {
					CompositeMap record = it.next();
					code_value_id_maps.put(record.getLong("code_value_id"), record.getLong("code_id"));
				}

			}
			inited = true;
		} finally {
			if (context != null)
				context.freeConnection();
		}
	}

	public String getFullCacheKey(CompositeMap map) {
		if (map == null)
			return null;
		return TextParser.parse(cacheKeyPrefix, map);
	}

	public String getFullCacheKey(String code, String language) {
		return code + "." + language;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getCacheKeyPrefix() {
		return cacheKeyPrefix;
	}

	public void setCacheKeyPrefix(String cacheKeyPrefix) {
		this.cacheKeyPrefix = cacheKeyPrefix;
	}
	// TODO linjinxiao 这里ServiceThreadLocal.getCurrentThreadContext()为null，是否有问题
	public CompositeMap queryData(Map queryMap) throws Exception {
//		List result = new ArrayList();
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		// if (context == null)
		// throw new
		// IllegalStateException("No service context set in ThreadLocal yet");
		if (context == null)
			context = new CompositeMap();
		BusinessModelService service = factory.getModelService(getLookupModel(), context);
		CompositeMap resultMap = service.queryAsMap(queryMap, FetchDescriptor.fetchAll());
		return resultMap;
	}
}
