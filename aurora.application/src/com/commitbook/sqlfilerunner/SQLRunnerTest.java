package com.commitbook.sqlfilerunner;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.jdbc.ScriptRunner;


public class SQLRunnerTest {

		public static void main(String[] args) throws Exception{
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			Connection conn = (Connection) DriverManager.getConnection("jdbc:oracle:thin:@192.168.11.65:1521:masdev", "hec2one", "hec2one");
			ScriptRunner runner = new ScriptRunner(conn);
			runner.setSendFullScript(true);
			runner.setStopOnError(true);
			runner.setRemoveCRs(true);
			runner.setErrorLogWriter(null);
			runner.setLogWriter(null);
//			runner.setDelimiter(delimiter)
//			runner.setFullLineDelimiter(fullLineDelimiter)
			File sqlFile = new File("E:\\workspace\\worksheet\\bgt_check_with_cache\\预算检查\\bgt_budget_items.sql");
			FileReader fr = new FileReader(sqlFile);
			runner.runScript(fr);
		}
}
