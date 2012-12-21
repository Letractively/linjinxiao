/**
 * 
 */
package org.linjinxiao.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import oracle.jdbc.OracleConnection;

/**
 * @author linjinxiao
 *
 */
public class TableReader{
	private String dataBaseUrl = "jdbc:oracle:thin:hapdev/hapdev@192.168.11.65:1521:masdev";
	private String sql = "select rownum row_num,t.transaction_type_code,t.description from inv_transaction_lines t";

	public void run() throws Exception{
		ResultSet resultSet = null;
		Statement stmt = null;
		OracleConnection dbConnection = null;
		try {
			String driver = "oracle.jdbc.driver.OracleDriver";
			Class.forName(driver);
			dbConnection = (OracleConnection)DriverManager.getConnection(dataBaseUrl);
			stmt = dbConnection.createStatement();
			resultSet = stmt.executeQuery(sql);
			readData(resultSet);
		}finally{
			DBUtil.closeResultSet(resultSet);
			DBUtil.closeStatement(stmt);
			DBUtil.closeConnection(dbConnection);
		}
	}
	private void readData(ResultSet resultSet) throws Exception{
		while(resultSet.next()) {
			String[] data_row = new String[2];
			int row_num = resultSet.getInt(1);
			data_row[0] = resultSet.getString(2);
			data_row[1] = resultSet.getString(3);
			System.out.println("rownum "+row_num+" : "+data_row[0]+" : "+data_row[1]);
			
		}
	}
	public static void main(String[] args) throws Exception {
		TableReader  readDataBase = new TableReader();
		readDataBase.run();
	}
}
