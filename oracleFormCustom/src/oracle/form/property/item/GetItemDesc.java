package oracle.form.property.item;

/**
 * 
 */
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
public class GetItemDesc {

	/**
	 * @param args
	 */
	// ACP ACR AST BGT CIMCINV CIMCORD CSH CST ENG EXP FND FRS GLD
	// INV MDM MWF ORD OTHER PLN PUR QMS SFC SYS
	File inputFile = new File("C:/Documents and Settings/hand/Desktop/dl/程序/set_property/FMB和FMT/fmt/ACP/ACP551.fmt");
	File outputFile;

	public static final String interfaceTable = "sys_item_desc_interface";
	private String regEx = ".*\"(.*)\".*";
	String FormRegEx = ".+\\\\(.+)$";
	private ArrayList items;
	private String formName;
	private String moduleName;
	private String blockCode;
	private String type = Constant.item_desc_type;
	
	public GetItemDesc() {
		items = new ArrayList();
	}

	public GetItemDesc(String inputFileName, String outputFileName) {
		this.inputFile = new File(inputFileName);
		this.outputFile = new File(outputFileName);
		items = new ArrayList();

	}

	public static void main(String args[]) {
		GetItemDesc getItemDesc = new GetItemDesc();
		getItemDesc.start();
	}

	public void start() {
		beginHandle(inputFile);
	}

	private void beginHandle(File level) {
		if (FormPropertyHandler.outputTo == Constant.outputToFile)
			createDestFile();
		if (level.isDirectory()) {
			File[] nextLevel = level.listFiles();
			for (int i = 0; i < nextLevel.length; i++) {
				if ((!nextLevel[i].isDirectory())
						&& nextLevel[i].getName().endsWith(".fmt")) {
					// System.out.println(nextLevel[i]);
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
		String outputFileName = fileName + "_" + type + "_" + dateString + ".csv";

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
			items.clear();
			String outContent;

			FileInputStream fis = new FileInputStream(level);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(fis));

			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("IDFOS_TYP = 7")) {
					blockCode = getName(bufferedReader);
				} else if (outContent.trim().equals("IDFOS_TYP = 30")) {
					createObject(bufferedReader);
				} else if (outContent.trim().startsWith("MODNAME")) {
					outContent = bufferedReader.readLine();
					String[] strs = outContent.split(FormPropertyHandler.formNameSpilt);
					formName = strs[strs.length-1];
//					formName = pickUp(outContent, regEx);
//					formName = pickUp(formName, FormRegEx);
//					formName = formName.substring(0, formName.indexOf("."));
				}
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		getFormModule();
		if(FormPropertyHandler.outputTo == Constant.outputToGUIPanel)
			outputToGUIPanel();
		else if (FormPropertyHandler.outputTo == Constant.outputToCmdConsole)
			outputToCmdConsole();
		else if (FormPropertyHandler.outputTo == Constant.outputToFile)
			outputToFile();
		else
			try {
				outputToDataBase();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	private void createObject(BufferedReader bufferedReader) {
		Item item = new Item();
		item.setBlockCode(blockCode);
		String outContent;
		String itemCode = getName(bufferedReader);
		item.setItemCode(itemCode);
		item.setItemTitle("");
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("TN = 33")) {
					String canvas = getName(bufferedReader);
					if (!canvas.equals(""))
						items.add(item);
				} else if (outContent.trim().equals("TN = 394")) {
					String itemTitle = getName(bufferedReader);
					if (!itemTitle.equals("")) {
						item.setItemTitle(itemTitle);
						break;
					}
				} else if (outContent.trim().equals("TN = 151")) {
					String itemTitle = getName(bufferedReader);
					item.setItemTitle(itemTitle);

				} else if (outContent.trim().equals("ON = 136")) {
					break;
				}
			}
		} catch (Exception e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	}

	private void outputToCmdConsole() {
		for (int i = 0; i < common_item.length; i++) {
			String[] item = common_item[i];
			System.out.println("," + moduleName + "," + formName + ","
					+ item[0] + "," + item[1] + "," + item[2] + ",");
		}
		for (int i = 0; i < items.size(); i++) {
			Item item = (Item) items.get(i);
			System.out.println("," + moduleName + "," + formName + ","
					+ item.getBlockCode() + "," + item.getItemCode() + ","
					+ item.getItemTitle() + ",");
		}
	}

	private void outputToGUIPanel() {
		for (int i = 0; i < common_item.length; i++) {
			String[] item = common_item[i];
			GUI.outputContent.append("," + moduleName + "," + formName + ","
					+ item[0] + "," + item[1] + "," + item[2] + ",");
			GUI.outputContent.append("\r\n");
		}
		for (int i = 0; i < items.size(); i++) {
			Item item = (Item) items.get(i);
			GUI.outputContent.append("," + moduleName + "," + formName + ","
					+ item.getBlockCode() + "," + item.getItemCode() + ","
					+ item.getItemTitle() + ",");
			GUI.outputContent.append("\r\n");
		}
	}

	private void outputToFile() {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(outputFile,
					true));

			for (int i = 0; i < common_item.length; i++) {
				String[] item = common_item[i];
				out.println("," + moduleName + "," + formName + "," + item[0]
						+ "," + item[1] + "," + item[2] + ",");
			}
			for (int i = 0; i < items.size(); i++) {
				Item item = (Item) items.get(i);
				out.println("," + moduleName + "," + formName + ","
						+ item.getBlockCode() + "," + item.getItemCode() + ","
						+ item.getItemTitle() + ",");
			}
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void outputToDataBase()  throws SQLException{
		String insertSQL = "insert into sys_item_desc_interface (BATCH_ID,MODULE_CODE,FUNCTION_CODE,BLOCK_CODE,ITEM_CODE,ITEM_DESC_ZH) values (?,?,?,?,?,?)";
		Connection dbConnection = DBManager.getDBConnection();
		PreparedStatement statement = dbConnection.prepareStatement(insertSQL);
		dbConnection.setAutoCommit(false);
		for (int i = 0; i < common_item.length; i++) {
			String[] item = common_item[i];
			statement.setInt(1, FormPropertyHandler.batch_id);
			statement.setString(2, moduleName);
			statement.setString(3, formName);
			statement.setString(4, item[0]);
			statement.setString(5, item[1]);
			statement.setString(6, item[2]);
			statement.executeUpdate();
		}
		for (int i = 0; i < items.size(); i++) {
			Item item = (Item) items.get(i);
			statement.setInt(1, FormPropertyHandler.batch_id);
			statement.setString(2, moduleName);
			statement.setString(3, formName);
			statement.setString(4, item.getBlockCode());
			statement.setString(5, item.getItemCode());
			statement.setString(6, item.getItemTitle());
			statement.execute();
		}
		statement.close();
		dbConnection.commit();
		dbConnection.setAutoCommit(true);
		
	}

	private String getName(BufferedReader bufferedReader) {
		String outContent = "";
		String preRegEx = ".*\"(.*)";
		String postRegEx = "(.*)\"";
		String returnStr = "";
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				outContent = outContent.trim();
				if (outContent.trim().startsWith("TV")) {
					// System.out.println("*****"+outContent);
					if (outContent.trim().equals("TV = NULLP")) {
						break;
					} else if (outContent.startsWith("TV = <<")
							&& !outContent.endsWith(">>")) {
						returnStr = returnStr
								+ pickUp(outContent, preRegEx).trim();
						// 处理带分行的描述
						while ((outContent = bufferedReader.readLine()) != null) {
							if ((!outContent.startsWith("TV = <<"))
									&& outContent.endsWith(">>")) {
								returnStr = returnStr
										+ pickUp(outContent, postRegEx).trim();
								return returnStr;
							} else if (!outContent.startsWith("TV = <<")
									&& !outContent.endsWith(">>")) {
								returnStr = returnStr + outContent.trim();
							}
						}

					} else
						return pickUp(outContent, regEx);
				}

			}
		} catch (Exception e) {
			// TODO 自动生成 catch 块
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
		// String dataBaseUrl =
		// "jdbc:oracle:thin:mas9i/mas9i@192.168.11.65:1521:masdev";
		// String driver = "oracle.jdbc.driver.OracleDriver";
		// try {
		// Class.forName(driver);
		// OracleConnection dbConnection = (OracleConnection) DriverManager
		// .getConnection(dataBaseUrl);
		// Statement stmt = dbConnection.createStatement();
		// String sql = "select m.module_code "
		// + " from sys_function_vl t, sys_module_vl m "
		// + " where t.function_code = " + "'" + formName + "'"
		// + " and m.module_id = t.module_id";
		// ResultSet gdb_batch_jobs_temp_rows = stmt.executeQuery(sql);
		// while (gdb_batch_jobs_temp_rows.next()) {
		// moduleName = gdb_batch_jobs_temp_rows.getString(1);
		// // System.out.println(moduleName);
		// }
		// gdb_batch_jobs_temp_rows.close();
		// stmt.close();
		// dbConnection.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		if (moduleName == null || moduleName.equals("")) {
			moduleName = formName.substring(0, 3);
		}
	}

	String[][] common_item = { { "ITEM_INFO", "TRIGGER_BLOCK", "数据块", "" },
			{ "ITEM_INFO", "TRIGGER_ITEM", "数据域", "" },
			{ "ITEM_INFO", "LOV_NAME", "值列表名称", "" },
			{ "ITEM_INFO", "GROUP_NAME", "记录组名称", "" },
			{ "ITEM_INFO", "BLOCK_SOURCE_NAME", "数据来源", "" },
			{ "ITEM_INFO", "BLOCK_WHERE_CLAUSE", "查询条件", "" },
			{ "ITEM_INFO", "BLOCK_ORDER_BY_CLAUSE", "排序条件", "" },
			{ "ITEM_INFO", "OK", "返回", "" }, { "VERSION", "CONTENT", "", "" },
			{ "VERSION", "BACK", "确定", "" },
			{ "EXPORT_STATUS", "BEGIN_EXPORT", "开始导出", "" },
			{ "EXPORT_STATUS", "CANCEL", "取消", "" },
			{ "SYS_PARAM_INFO", "SYS_PARAMETERS", "", "" },
			{ "SYS_PARAM_INFO", "NEW_PARAMETER", "输入要查询的系统参数", "" },
			{ "SYS_PARAM_INFO", "CONFIRM", "确认", "" },
			{ "SYS_PARAM_INFO", "QUERY", "查询", "" },
			{ "RECORD_HISTORY", "CREATED_BY", "创建者", "" },
			{ "RECORD_HISTORY", "CREATION_DATE", "创建日期", "" },
			{ "RECORD_HISTORY", "LAST_UPDATED_BY", "更新者", "" },
			{ "RECORD_HISTORY", "LAST_UPDATE_DATE", "更新日期", "" },
			{ "RECORD_HISTORY", "TABLE_NAME", "表名", "" },
			{ "RECORD_HISTORY", "OK", "确定", "" },
			{ "DATE_CONTROL_BLOCK", "OK_BUTTON", "确定", "" },
			{ "DATE_CONTROL_BLOCK", "CANCEL_BUTTON", "取消", "" },
			{ "DATE_CONTROL_BLOCK", "MONTH_PLUS1", ">", "" },
			{ "DATE_CONTROL_BLOCK", "YEAR_PLUS1", ">>", "" },
			{ "DATE_CONTROL_BLOCK", "YEAR_MINUS1", "<<", "" },
			{ "DATE_CONTROL_BLOCK", "MONTH_MINUS1", "<", "" },
			{ "DATE_CONTROL_BLOCK", "DISPLAY_MON_YEAR", "", "" },
			{ "DATE_CONTROL_BLOCK", "DAY_LABEL1", "", "" },
			{ "DATE_CONTROL_BLOCK", "DAY_LABEL2", "", "" },
			{ "DATE_CONTROL_BLOCK", "DAY_LABEL3", "", "" },
			{ "DATE_CONTROL_BLOCK", "DAY_LABEL4", "", "" },
			{ "DATE_CONTROL_BLOCK", "DAY_LABEL5", "", "" },
			{ "DATE_CONTROL_BLOCK", "DAY_LABEL6", "", "" },
			{ "DATE_CONTROL_BLOCK", "DAY_LABEL7", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON1", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON2", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON3", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON4", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON5", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON6", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON7", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON8", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON9", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON10", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON11", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON12", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON13", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON14", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON15", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON16", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON17", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON18", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON19", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON20", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON21", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON22", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON23", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON24", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON25", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON26", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON27", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON28", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON29", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON30", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON31", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON32", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON33", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON34", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON35", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON36", "", "" },
			{ "DATE_BUTTON_BLOCK", "DATE_BUTTON37", "", "" }, };

}
