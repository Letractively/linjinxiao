/**
 * 
 */
package org.linjinxiao.database;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.jdbc.OracleConnection;

/**
 * @author linjinxiao
 * 
 */
public class BolbReader {
	private static String dataBaseUrl = "jdbc:oracle:thin:fsms/fsms@127.0.0.1:1521:hand1332";
	String attachment_id = "1391";
	private String sql = "select m.content from fnd_atm_attachment m where m.attachment_id=" + attachment_id;

	public void run() throws Exception {

		OutputStream os = null;
		InputStream is = null;

		Connection dbConnection = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			String driver = "oracle.jdbc.driver.OracleDriver";
			Class.forName(driver);
			dbConnection = (OracleConnection) DriverManager.getConnection(dataBaseUrl);

			stmt = dbConnection.prepareStatement(sql);
			resultSet = stmt.executeQuery();
			if (!resultSet.next())
				System.out.println("attachment_id '" + attachment_id + "' not found ");
			Blob content = resultSet.getBlob(1);

			if (content != null) {
				is = content.getBinaryStream();
				os = new FileOutputStream("c:/1.doc");
				int c;
				int chunk = is.available();
				System.out.println("chunk" + chunk);
				byte[] buffer = new byte[1024];
				while ((c = is.read(buffer)) != -1) {
					os.write(buffer, 0, c);
				}
			}
		} finally {
			DBUtil.closeResultSet(resultSet);
			DBUtil.closeStatement(stmt);
			DBUtil.closeConnection(dbConnection);
			if (os != null)
				os.close();
			if (is != null)
				is.close();
		}
	}

	public static void main(String[] args) throws Exception {
		BolbReader blob = new BolbReader();
		blob.run();

	}
}
