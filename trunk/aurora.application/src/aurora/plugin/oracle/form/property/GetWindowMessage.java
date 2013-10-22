/**
 * 
 */
package aurora.plugin.oracle.form.property;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.jdbc.OracleConnection;

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
	File inputFile;
	File outputFile;

	private String regEx = ".*\"(.*)\".*";
	String FormRegEx = ".+\\\\(.+)$";
	private ArrayList windowGroups;
	private String formName;
	private String moduleName;
	private String windowCode;

	String type = Common.form_title_type;
	
	String[][] common_windows = { { "ITEM_INFO", "当前域信息" },
			{ "VERSION", "版本信息" }, { "SYS_PARAMETERS", "系统参数" },
			{ "DATE_LOV_WINDOW", "日历" }, { "EXPORT_STATUS", "" },
			{ "RECORD_HISTORY", "记录历史" } };
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
	}

	public void start() {
		if (GetFormProperty.outputTo == Common.outputToFile)
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
			throw new RuntimeException(e);
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
					formName = pickUp(outContent, regEx);
					// System.out.println(formName);
					formName = pickUp(formName, FormRegEx);
					formName = formName.substring(0, formName.indexOf("."));
					// System.out.println("formName:"+formName);
				}
			}
			fis.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		getFormModule();
		if(GetFormProperty.outputTo == Common.outputToGUIPanel)
			outputToGUIPanel();
		else if (GetFormProperty.outputTo == Common.outputToCmdConsole)
			outputToCmdConsole();
		else
			outputToFile();
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
				else if (outContent.trim().equals("ON = 362")) {
					break;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			PrintStream out = new PrintStream(new FileOutputStream(outputFile,true));
			for (int i = 0; i < windowGroups.size(); i++) {
				Window window = (Window) windowGroups.get(i);
				out.println("," + moduleName + "," + formName + ","
						+ window.getCode() + "," + window.getTitle() + ",");
			}
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			throw new RuntimeException(e);
		}
		return outContent;
	}

	public String pickUp(String str, String regEx) {
		String selectedString = null;
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		boolean rs = m.find();
		for (int i = 1; i <= m.groupCount(); i++) {
			selectedString = m.group(i);
		}
		return selectedString;

	}

	private void getFormModule() {
		if(moduleName ==null || moduleName.equals("")){
			moduleName = formName.substring(0, 3);
		}
	}

}
