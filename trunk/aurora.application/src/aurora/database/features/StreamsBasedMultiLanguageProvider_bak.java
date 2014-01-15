package aurora.database.features;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import uncertain.cache.ICache;
import uncertain.cache.INamedCacheFactory;
import uncertain.composite.CompositeMap;
import uncertain.core.ConfigurationError;
import uncertain.core.IGlobalInstance;
import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.GeneralException;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.oraclestreams_bak.IOracleStreamsListener;
import aurora.application.features.oraclestreams_bak.OracleStreamsManager;
import aurora.application.features.oraclestreams_bak.Streams_dml_lcr;
import aurora.application.features.oraclestreams_bak.Streams_dml_lcr_column;
import aurora.bm.BusinessModel;
import aurora.bm.IModelFactory;
import aurora.database.DBUtil;
import aurora.database.FetchDescriptor;
import aurora.database.ParsedSql;
import aurora.database.ResultSetLoader;
import aurora.database.SqlRunner;
import aurora.database.profile.IDatabaseFactory;
import aurora.database.rsconsumer.CompositeMapCreator;
import aurora.database.service.SqlServiceContext;

//TODO 抽取依赖关系，只剩接口。另外考虑如何启用新的方式比较方便。
//用最新的ojdbc14,否则lcr出来的数据有乱码。
public class StreamsBasedMultiLanguageProvider_bak extends AbstractLocatableObject
		implements IGlobalInstance, ILifeCycle,IOracleStreamsListener {

	public static final String DEFAULT_SCREEN_CACHE_NAME = "MultiLanguageCache";
	public static final String DEFAULT_KEY_PREFIX = "${/session/@lang}";
	final static String KEY_ML_MODEL = "model";
	final static String KEY_ML_DESCRIPTION = "description";
	final static String KEY_ML_PK_ID = "pk_id";
	final static String KEY_BUILD_IN_PK = "language";

	private String mMultiLanguageCacheKeyPrefix = DEFAULT_KEY_PREFIX;
	private String mMultiLanguageCacheName = DEFAULT_SCREEN_CACHE_NAME;
	private ICache mMultiLanguageCache;
	private INamedCacheFactory mCacheFactory;
	private IObjectRegistry mRegistry;
	private IDatabaseFactory databaseFactory;
	private IModelFactory modelFactory;
	private CompositeMap dbProperties = null;
	private String pkId;
	private String mlDescription;
	private String mlTable;
	public StreamsBasedMultiLanguageProvider_bak(INamedCacheFactory mCacheFactory,
			IObjectRegistry mRegistry, IModelFactory modelFactory,
			IDatabaseFactory databaseFactory) {
		super();
		this.mCacheFactory = mCacheFactory;
		this.mRegistry = mRegistry;
		this.modelFactory = modelFactory;
		this.databaseFactory = databaseFactory;
	}

	public boolean startup() {
		OracleStreamsManager osm = (OracleStreamsManager)mRegistry.getInstanceOfType(OracleStreamsManager.class);
		if (osm == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this,
					OracleStreamsManager.class, this.getClass().getCanonicalName());
		mMultiLanguageCache = mCacheFactory
				.getNamedCache(mMultiLanguageCacheName);
		if (mMultiLanguageCache == null)
			throw new GeneralException("uncertain.cache.named_cache_not_found",
					new Object[] { mMultiLanguageCacheName }, this);
		try {
			loadMultiLanguageData();
			osm.addListener(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	private void loadMultiLanguageData() throws SQLException, IOException {
		CompositeMap result = new CompositeMap("result");
		queryData(buildQuerySql(), result);
		initCacheData(result, pkId, mlDescription);
	}

	private void queryData(String querySQL, CompositeMap result)
			throws SQLException, IOException {
		DataSource ds = (DataSource) mRegistry
				.getInstanceOfType(DataSource.class);
		if (ds == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this,
					DataSource.class, this.getClass().getCanonicalName());
		ResultSet rs_header = null;
		SqlServiceContext sql_context = null;
		Connection conn = null;
		try {
			conn = ds.getConnection();
			sql_context = SqlServiceContext.createSqlServiceContext(conn);
			ParsedSql stmt = createStatement(querySQL);
			SqlRunner runner = new SqlRunner(sql_context, stmt);
			rs_header = runner.query(null);
			ResultSetLoader mRsLoader = new ResultSetLoader();
			mRsLoader.setFieldNameCase(Character.LOWERCASE_LETTER);
			FetchDescriptor desc = FetchDescriptor.fetchAll();
			CompositeMapCreator compositeCreator = new CompositeMapCreator(
					result);
			mRsLoader.loadByResultSet(rs_header, desc, compositeCreator);
		} finally {
			DBUtil.closeConnection(conn);
		}
	}

	private String buildQuerySql() throws IOException {
		dbProperties = databaseFactory.getProperties();
		if (dbProperties == null)
			throw new ConfigurationError("Database Properties undifined");
		CompositeMap mlProperties = dbProperties
				.getChild("multi-language-storage");
		if (mlProperties == null)
			throw new ConfigurationError(
					"multi-language-storage Properties undifined");
		mlDescription = mlProperties.getString(KEY_ML_DESCRIPTION);
		if (mlDescription == null)
			throw new ConfigurationError(
					"multi-language-storage description undifined");
		mlDescription = mlDescription.toLowerCase();
		pkId = mlProperties.getString(KEY_ML_PK_ID);
		if (pkId == null)
			throw new ConfigurationError(
					"multi-language-storage pk_id undifined");
		pkId = pkId.toLowerCase();
		String mlModelString = mlProperties.getString(KEY_ML_MODEL);
		if (mlModelString == null)
			throw new ConfigurationError(
					"multi-language-storage model undifined");
		BusinessModel mlModel = modelFactory.getModelForRead(mlModelString);
		mlTable = mlModel.getBaseTable();
		String querySQL = "select " + mlDescription + "," + pkId
				+ ","+KEY_BUILD_IN_PK+" from " + mlTable;
		return querySQL;
	}

	private void initCacheData(CompositeMap data, String pkId,
			String mlDescription) {
		if (data == null)
			return;
		List childList = data.getChilds();
		if (childList == null)
			return;
		for (Iterator<CompositeMap> it = childList.iterator(); it.hasNext();) {
			CompositeMap record = it.next();
			mMultiLanguageCache.setValue(getFullCacheKey(record, pkId),
					record.get(mlDescription));
		}
	}

	public void shutdown() {

	}

	private String getFullCacheKey(CompositeMap record, String pkId) {
		if (record == null)
			return null;
		return record.get(pkId.toLowerCase()).toString() + "."
				+ record.getString(KEY_BUILD_IN_PK);
	}

	public String getFullCacheKey(String pkidValue, String language) {
		return pkidValue + "." + language;
	}

	private String getFullCacheKey(Streams_dml_lcr_column[] pkColumns)
			throws SQLException {
		if (pkColumns == null)
			throw new RuntimeException("pkColumns can not be null!");
		String cacheKey = "";
		for (int i = 0; i < pkColumns.length; i++) {
			if (pkColumns[i] == null)
				break;
			Streams_dml_lcr_column column = pkColumns[i];
			if (KEY_BUILD_IN_PK.equals(column.getColumnName().toLowerCase()))
				cacheKey = cacheKey + "." + column.getValue();
			else if (pkId.equals(column.getColumnName().toLowerCase())) {
				cacheKey = column.getValue() + cacheKey;
			}
		}
		return cacheKey;
	}

	public String getDescription(String pkidValue, String language) {
		return (String) mMultiLanguageCache.getValue(getFullCacheKey(pkidValue,
				language));
	}

	private String queryValue(Streams_dml_lcr_column[] pkColumns)
			throws IOException, SQLException {
		if (pkColumns == null )
			throw new RuntimeException("pkColumns can not be null!");
		String querySQL = buildQuerySql() + "  where ";
		for (int i = 0; i < pkColumns.length; i++) {
			if (pkColumns[i] == null)
				break;
			if (i > 0) {
				querySQL += " and ";
			}
			querySQL += pkColumns[i].getColumnName()
					+ " = "
					+ ("VARCHAR2".equals(pkColumns[i].getColumnType()) ? "'"
							: "")
					+ pkColumns[i].getValue()
					+ ("VARCHAR2".equals(pkColumns[i].getColumnType()) ? "'"
							: "");
		}
		CompositeMap result = new CompositeMap("result");
		queryData(querySQL, result);
		if (result == null || result.getChilds() == null
				|| result.getChilds().size() != 1)
			throw new RuntimeException("queryValue result size is wrong!");
		CompositeMap record = (CompositeMap) result.getChilds().get(0);
		return record.getString(mlDescription);
	}

	public ICache getCache() {
		return mMultiLanguageCache;
	}

	public String getCacheKeyPrefix() {
		return mMultiLanguageCacheKeyPrefix;
	}

	public void setResponseCacheKeyPrefix(String cacheKeyPrefix) {
		mMultiLanguageCacheKeyPrefix = cacheKeyPrefix;
	}

	public String getCacheName() {
		return mMultiLanguageCacheName;
	}

	public void setCacheName(String cacheName) {
		mMultiLanguageCacheName = cacheName;
	}

	ParsedSql createStatement(String sql) {
		ParsedSql stmt = new ParsedSql();
		stmt.parse(sql);
		return stmt;
	}

	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void notice(Streams_dml_lcr lcr) throws Exception {
		if(!mlTable.equalsIgnoreCase(lcr.getObjectName()))
			return;
		Streams_dml_lcr_column[] columns = lcr.getPkColumnList()
				.getArray();
		String cacheKey = getFullCacheKey(columns);
		if ("DELETE".equals(lcr.getCommandType())) {
			mMultiLanguageCache.remove(cacheKey);
		}
		if ("INSERT".equals(lcr.getCommandType())
				|| "UPDATE".equals(lcr.getCommandType())) {
			String value = queryValue(columns);
			mMultiLanguageCache.setValue(cacheKey, value);
		}
		
	}

}
