package com.commitbook.sqlfilerunner;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.types.EnumeratedAttribute;

import uncertain.core.DirectoryConfig;
import uncertain.core.UncertainEngine;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

import aurora.datasource.DataSourceConfig;
import aurora.datasource.DatabaseConnection;

public class AntExecSql {
	
//need UncertainEngine,DirectoryConfig,DataSourceConfig
	private IObjectRegistry mObjectRegistry;
	private DataSourceConfig dsConfig;
	private ILogger logger;
	
	private final static String SQLEXEC_LOGDIRECTORY = "SQLEXEC";  
	private String sqlExecLogDirectory;
	
	public AntExecSql(IObjectRegistry objectRegistry) throws IOException{
		mObjectRegistry = objectRegistry;
		dsConfig = (DataSourceConfig)mObjectRegistry.getInstanceOfType(DataSourceConfig.class);
		if(dsConfig == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, DataSourceConfig.class, this.getClass().getCanonicalName());
		logger = LoggingContext.getLogger(this.getClass().getPackage().getName(), mObjectRegistry);
		initLogDirectory();
	}

	private void initLogDirectory() throws IOException {
		UncertainEngine ue = (UncertainEngine)mObjectRegistry.getInstanceOfType(UncertainEngine.class);
		if(ue == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, UncertainEngine.class, this.getClass().getCanonicalName());
		DirectoryConfig dc = ue.getDirectoryConfig();
		if(dc==null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, DirectoryConfig.class, this.getClass().getCanonicalName());
		String logDirectory = dc.getLogDirectory();
		if(logDirectory == null)
			logDirectory = (new File(System.getProperty("java.io.tmpdir"))).getCanonicalPath();
		sqlExecLogDirectory = logDirectory+File.separator+SQLEXEC_LOGDIRECTORY;
		logger.info("sqlExecLogDirectory is "+sqlExecLogDirectory+". Please check it.");
	}
	
	public void executeSQL(String sqlFileFullPath) throws Exception{
		File sqlFile = new File(sqlFileFullPath);
		executeSQL(sqlFile);
	}
	public void executeSQL(File sqlFile) throws Exception{
		SQLExec antSQLExec = generatorAntSQLExec();
		antSQLExec.setSrc(sqlFile);
		// 输出到文件 sql.out 中；不设置该属性，默认输出到控制台,必须预先存在
		File logFile = new File(sqlExecLogDirectory+File.separator+System.currentTimeMillis()+"_"+sqlFile.getName());
		if(!logFile.exists()){
			File parentFile = logFile.getParentFile();
			if(!parentFile.exists())
				parentFile.mkdirs();
			logFile.createNewFile();
		}
		antSQLExec.setOutput(logFile);
		try{
			antSQLExec.execute();
		}catch(Exception e){
			logger.log(Level.SEVERE, "sqlFile is "+sqlFile+". Please check it.",e);
			throw new Exception(e);
		}
	}
	private SQLExec generatorAntSQLExec(){
		DatabaseConnection dbConn = getDefaultDBConnection(dsConfig);
		if(dbConn == null)
			throw new RuntimeException("Can not get default DatabaseConnection which has no name.");
		
		SQLExec sqlExec = new SQLExec();
		// 设置数据库参数
		sqlExec.setDriver(dbConn.getDriverClass());
		sqlExec.setUrl(dbConn.getUrl());
		sqlExec.setUserid(dbConn.getUserName());
		sqlExec.setPassword(dbConn.getPassword());
		// 有出错的语句该如何处理
		sqlExec.setOnerror((SQLExec.OnError) (EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort")));
		sqlExec.setPrint(true); // 设置是否输出
		
		sqlExec.setProject(new Project()); // 要指定这个属性，不然会出错
		return sqlExec;
	}
	private DatabaseConnection getDefaultDBConnection(DataSourceConfig dsConfig){
		DatabaseConnection[] dbConns = dsConfig.getDatabaseConnections();
		if(dbConns == null)
			return null;
		for(int i=0;i<dbConns.length;i++){
			DatabaseConnection dbConn = dbConns[i];
			String dbConnName = dbConn.getName();
			if(dbConnName == null || "".equals(dbConnName)){
				return dbConn;
			}
		}
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SQLExec sqlExec = new SQLExec();
		// 设置数据库参数
		sqlExec.setDriver("oracle.jdbc.driver.OracleDriver");
		sqlExec.setUrl("jdbc:oracle:thin:@192.168.11.65:1521:masdev");
		sqlExec.setUserid("hec2one");
		sqlExec.setPassword("hec2one");
		// 要执行的脚本
		sqlExec.setSrc(new File("E:\\workspace\\worksheet\\bgt_check_with_cache\\预算检查\\1.sql"));
		// 有出错的语句该如何处理
		sqlExec.setOnerror((SQLExec.OnError) (EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort")));
		sqlExec.setPrint(true); // 设置是否输出
		// 输出到文件 sql.out 中；不设置该属性，默认输出到控制台
		sqlExec.setOutput(new File("E:\\tomcats\\apps\\hec2dev\\WEB-INF\\logs\\SQLEXEC\\1386566802436_bgt_budget_items.sql"));
		sqlExec.setProject(new Project()); // 要指定这个属性，不然会出错
		sqlExec.execute();
	}
	
}
