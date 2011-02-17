/**
 * 
 */
package oracle.form.property.window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.form.property.Constant;
import oracle.form.property.DBManager;
import oracle.form.property.FormPropertyHandler;
import oracle.form.property.ui.GUI;


/**
 * @author linjinxiao
 * 
 */
public class GetWindowMessage {

	/**
	 * @param args
	 */
	// ACP ACR AST BGT CIMCINV CIMCORD CSH CST ENG EXP FND FRS GLD
	// INV MDM MWF ORD OTHER PLN PUR QMS SFC SYS
	File inputFile = new File("C:/Documents and Settings/hand/Desktop/dl/����/set_property/FMB��FMT/fmt/ACP/ACP110.fmt");;
	File outputFile;

	private String regEx = ".*\"(.*)\".*";
	String FormRegEx = ".+\\\\(.+)$";
	private ArrayList windowGroups;
	private String formName;
	private String moduleName;
	private String windowCode;

	String type = Constant.form_title_type;
	
	private static final String[][] common_windows = 
			{ 
				{ "ITEM_INFO", "当前域信息","Current Field Information"},
				{ "VERSION", "版本信息","Version Information" }, 
				{ "SYS_PARAMETERS", "系统参数","System Parameter" },
				{ "DATE_LOV_WINDOW", "日历","Calendar" },
				{ "EXPORT_STATUS", "EXPORT_STATUS","Export Status" },
				{ "RECORD_HISTORY", "记录历史","Record History" } 
			};
	public GetWindowMessage(String inputFileName, String outputFileName) {
		this.inputFile = new File(inputFileName);
		this.outputFile = new File(outputFileName);
		windowGroups = new ArrayList();

	}
	public GetWindowMessage() {
		windowGroups = new ArrayList();

	}

	public static void main(String args[]) {
		GetWindowMessage getRadioMessage = new GetWindowMessage();
		getRadioMessage.start();
	} // end main method

	public void start() {
		if (FormPropertyHandler.outputTo == Constant.outputToFile)
			createDestFile();
		beginHandle(inputFile);
	}

	private void beginHandle(File level) {
		if (level.isDirectory()) {
			File[] nextLevel = level.listFiles();
			for (int i = 0; i < nextLevel.length; i++) {
				if ((!nextLevel[i].isDirectory())
						&& nextLevel[i].getName().endsWith(".fmt")) {
					System.out.println(nextLevel[i]);
					getContent(nextLevel[i]);
				}
				else if(nextLevel[i].isDirectory()){
					beginHandle(nextLevel[i]);
				}
			}
		} else if (level.getName().endsWith(".fmt"))
			getContent(level);

	}
	private void createDestFile() {
		String fileName = inputFile.getName();
		if (outputFile == null)
			outputFile = inputFile;
		if (!inputFile.isDirectory())
			fileName = fileName.substring(0, fileName.indexOf("."));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String dateString = dateFormat.format(date);
		String outputFileName = fileName +"_" +type + "_" +dateString + ".csv";
		try {
			if (outputFile.isDirectory())
				outputFile = new File(outputFile.getAbsolutePath() + "/"
						+ outputFileName);
			else
				outputFile = new File(outputFile.getParent() + "/"
						+ outputFileName);
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			} else {
				outputFile.delete();
				outputFile.createNewFile();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getContent(File level) {
		try {
			windowGroups.clear();
			initWindowGroups();
			String outContent;

			FileInputStream fis = new FileInputStream(level);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(fis));

			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("IDFOS_TYP = 78")) {
					windowCode = getName(bufferedReader);
					createObject(bufferedReader);
				} else if (outContent.trim().startsWith("MODNAME")) {
					outContent = bufferedReader.readLine();
					String[] strs = outContent.split(FormPropertyHandler.formNameSpilt);
					formName = strs[strs.length-1];
				}
			}
			// System
			fis.close();
		} catch (Exception e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
		}
		getFormModule();
		if(FormPropertyHandler.outputTo == Constant.outputToGUIPanel)
			outputToGUIPanel();
		else if (FormPropertyHandler.outputTo == Constant.outputToCmdConsole)
			outputToCmdConsole();
		else if (FormPropertyHandler.outputTo == Constant.outputToFile){
			outputToFile();
		}
		else{
			try {
				outputToDataBase();
			} catch (SQLException e) {
				e.printStackTrace();
				FormPropertyHandler.log(e.getMessage());
			}
		}
	}

	private void outputToDataBase() throws SQLException {
		String insertSQL = "insert into sys_window_title_interface (batch_id,module_code,function_code,window_code,title_zh) values (?,?,?,?,?)";
		Connection dbConnection = DBManager.getDBConnection();
		PreparedStatement statement = dbConnection.prepareStatement(insertSQL);
		dbConnection.setAutoCommit(false);
		for (int i = 0; i < windowGroups.size(); i++) {
			Window window = (Window) windowGroups.get(i);
			statement.setInt(1, FormPropertyHandler.batch_id);
			statement.setString(2, moduleName);
			statement.setString(3, formName);
			statement.setString(4, window.getCode());
			statement.setString(5, window.getTitle());
			statement.executeUpdate();
		}
		statement.close();
		dbConnection.commit();
		dbConnection.setAutoCommit(true);
		
	}
	
	private void initWindowGroups() {
		for(int i=0;i<common_windows.length;i++){
			String[] windowProperty =  common_windows[i];
			Window window = new Window();
			window.setCode(windowProperty[0]);
			window.setTitle(windowProperty[1]);
			windowGroups.add(window);
		}

	}

	private void createObject(BufferedReader bufferedReader) {
		Window window = new Window();
		// windowGroups.add(window);
		window.setCode(windowCode);
		String outContent;
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("TN = 307")) {
					String windowTitle = getName(bufferedReader);
					window.setTitle(windowTitle);
					if (!windowTitle.equals(""))
						windowGroups.add(window);
					break;
				}
				// else if (outContent.trim().startsWith("IDFOS_TYP")) {
				// String IDFOS_TYP = outContent.trim();
				// if (IDFOS_TYP.equals("IDFOS_TYP = 90")) {
				// } else if (IDFOS_TYP.equals("IDFOS_TYP = 93")) {
				// } else if (IDFOS_TYP.equals("IDFOS_TYP = 94")) {
				// } else
				// break;
				//
				// }
				else if (outContent.trim().equals("ON = 362")) {
					break;
				}
			}
		} catch (Exception e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
		}
	}

	private void outputToCmdConsole() {
		for (int i = 0; i < windowGroups.size(); i++) {
			Window window = (Window) windowGroups.get(i);
			System.out.println("," + moduleName + "," + formName + ","
					+ window.getCode() + "," + window.getTitle() + ",");
		}
	}
	private void outputToGUIPanel() {
		for (int i = 0; i < windowGroups.size(); i++) {
			Window window = (Window) windowGroups.get(i);
			GUI.outputContent.append("," + moduleName + "," + formName + ","
					+ window.getCode() + "," + window.getTitle() + ",");
			GUI.outputContent.append("\r\n");
		}
	}

	private void outputToFile() {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(outputFile,
					true));
			for (int i = 0; i < windowGroups.size(); i++) {
				Window window = (Window) windowGroups.get(i);
				out.println("," + moduleName + "," + formName + ","
						+ window.getCode() + "," + window.getTitle() + ",");
			}
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getName(BufferedReader bufferedReader) {
		String outContent = null;
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().startsWith("TV")) {
					return pickUp(outContent, regEx);
				}

			}
		} catch (Exception e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
		}
		return outContent;
	}

	public String pickUp(String str, String regEx) {
		String selectedString = null;
		// String regEx = ".*\"(.*)\".*";

		// str=" TV = <<\"INVOICE_NUMBER_FROM\">> ";

		Pattern p = Pattern.compile(regEx);

		Matcher m = p.matcher(str);

//		boolean rs = m.find();
		m.find();
		for (int i = 1; i <= m.groupCount(); i++) {
			selectedString = m.group(i);
			// System.out.println(m.group(i));

		}
		return selectedString;

	}

	private void getFormModule() {
//		String dataBaseUrl = "jdbc:oracle:thin:mas9i/mas9i@192.168.11.238:1521:masdev";
//		String driver = "oracle.jdbc.driver.OracleDriver";
//		try {
//			Class.forName(driver);
//			OracleConnection dbConnection = (OracleConnection) DriverManager
//					.getConnection(dataBaseUrl);
//			Statement stmt = dbConnection.createStatement();
//			String sql = "select m.module_code "
//					+ " from sys_function_vl t, sys_module_vl m "
//					+ " where t.function_code = " + "'" + formName + "'"
//					+ " and m.module_id = t.module_id";
//			ResultSet gdb_batch_jobs_temp_rows = stmt.executeQuery(sql);
//			while (gdb_batch_jobs_temp_rows.next()) {
//				moduleName = gdb_batch_jobs_temp_rows.getString(1);
//				// System.out.println(moduleName);
//			}
//			gdb_batch_jobs_temp_rows.close();
//			stmt.close();
//			dbConnection.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(moduleName ==null || moduleName.equals("")){
			moduleName = formName.substring(0, 3);
		}
	}

}
