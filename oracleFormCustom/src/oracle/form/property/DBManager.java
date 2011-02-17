package oracle.form.property;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	
	private String dataBaseUrl;
	private String dataBaseDriver = "oracle.jdbc.driver.OracleDriver";
	private static Connection dbConnection;
	public DBManager(String dataBaseUrl){
		this.dataBaseUrl = dataBaseUrl;
	}
	public DBManager(String dataBaseUrl,String dataBaseDriver){
		this.dataBaseUrl = dataBaseUrl;
		this.dataBaseDriver = dataBaseDriver;
	}
	public void init() throws ClassNotFoundException, SQLException{
		Class.forName(dataBaseDriver);
		dbConnection = DriverManager.getConnection(dataBaseUrl);
	}
	public  static Connection getDBConnection(){
		return dbConnection;
	}
}
