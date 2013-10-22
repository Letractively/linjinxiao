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
public class GetLovMessage {

	/**
	 * @param args
	 */

	// ACP ACR AST BGT CIMCINV CIMCORD CSH CST ENG EXP FND FRS GLD
	// INV MDM MWF ORD OTHER PLN PUR QMS SFC SYS
	File inputFile;
	File lovTitleFile;
	File lovColumnFile;

	private String regEx = ".*\"(.*)\".*";
	String FormRegEx = ".+\\\\(.+)$";
	private ArrayList lovObjects;
	private String formName;
	private String moduleName;
	File outputDir;

	String lov_title_type = Common.lov_title_type;
	String lov_column_type = Common.lov_col_type;

	private boolean lov_title_flag = true;
	private boolean lov_col_flag = false;

	public GetLovMessage() {
		lovObjects = new ArrayList();
	}

	public GetLovMessage(String inputFileName, String outputFileName,
			boolean lov_title, boolean lov_col) {
		this.inputFile = new File(inputFileName);
		this.outputDir = new File(outputFileName);
		this.lov_title_flag = lov_title;
		this.lov_col_flag = lov_col;
		lovObjects = new ArrayList();

	}

	public static void main(String args[]) {
		GetLovMessage getLovMessage = new GetLovMessage();
		getLovMessage.start();
	} // end main method

	private void getFormModule() {
		if (moduleName == null || moduleName.equals("")) {
			moduleName = formName.substring(0, 3);
		}
	}

	private void outputLovColumnToCmdConsole() {
		for (int i = 0; i < common_lov_col.length; i++) {
			String[] lovColumn = common_lov_col[i];
			System.out.println("," + moduleName + "," + formName + ","
					+ lovColumn[0] + "," + lovColumn[1] + ","
					+ lovColumn[2] + ",");

		}
		String lovCode = null;
		ArrayList columnTitles = null;
		for (int i = 0; i < lovObjects.size(); i++) {
			LovObject lovObject = (LovObject) lovObjects.get(i);
			lovCode = lovObject.getLovCode();
			columnTitles = lovObject.getColumnTitles();
			for (int j = 0; j < columnTitles.size(); j++) {
				System.out.println("," + moduleName + "," + formName
						+ "," + lovCode + "," + (j + 1) + ","
						+ columnTitles.get(j) + ",");
			}
		}
	}

	private void outputLovColumnToGUIPanel() {
		for (int i = 0; i < common_lov_col.length; i++) {
			String[] lovColumn = common_lov_col[i];
			GUI.outputContent.append("," + moduleName + "," + formName + ","
					+ lovColumn[0] + "," + lovColumn[1] + ","
					+ lovColumn[2] + ",");
			GUI.outputContent.append("\r\n");

		}
		String lovCode = null;
		ArrayList columnTitles = null;
		for (int i = 0; i < lovObjects.size(); i++) {
			LovObject lovObject = (LovObject) lovObjects.get(i);
			lovCode = lovObject.getLovCode();
			columnTitles = lovObject.getColumnTitles();
			for (int j = 0; j < columnTitles.size(); j++) {
				GUI.outputContent.append("," + moduleName + "," + formName
						+ "," + lovCode + "," + (j + 1) + ","
						+ columnTitles.get(j) + ",");
				GUI.outputContent.append("\r\n");
			}
		}
	}

	private void outputLovColumnToFile() {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(
					lovColumnFile, true));

			for (int i = 0; i < common_lov_col.length; i++) {
				String[] lovColumn = common_lov_col[i];
				out.println("," + moduleName + "," + formName + ","
						+ lovColumn[0] + "," + lovColumn[1] + ","
						+ lovColumn[2] + ",");

			}

			String lovCode = null;
			ArrayList columnTitles = null;
			for (int i = 0; i < lovObjects.size(); i++) {
				LovObject lovObject = (LovObject) lovObjects.get(i);
				lovCode = lovObject.getLovCode();
				columnTitles = lovObject.getColumnTitles();
				for (int j = 0; j < columnTitles.size(); j++) {
					out.println("," + moduleName + "," + formName + ","
							+ lovCode + "," + (j + 1) + ","
							+ columnTitles.get(j) + ",");

				}
			}
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void outputLovToCmdConsole() {
		for (int i = 0; i < common_lov.length; i++) {
			String[] lov = common_lov[i];
			if (lov[1] != null && !lov[1].equals(""))
				System.out.println("," + moduleName + "," + formName + ","
						+ lov[0] + "," + lov[1] + ",");
		}
		String lovCode = null;
		String lovTitle = null;
		for (int i = 0; i < lovObjects.size(); i++) {
			LovObject lovObject = (LovObject) lovObjects.get(i);
			lovCode = lovObject.getLovCode();
			lovTitle = lovObject.getLovTitle();
			if (lovTitle != null && !lovTitle.equals(""))
				System.out.println("," + moduleName + "," + formName + ","
						+ lovCode + "," + lovTitle + ",");
		}
	}

	private void outputLovToGUIPanel() {
		for (int i = 0; i < common_lov.length; i++) {
			String[] lov = common_lov[i];
			if (lov[1] != null && !lov[1].equals("")){
				GUI.outputContent.append("," + moduleName + "," + formName + ","
						+ lov[0] + "," + lov[1] + ",");
				GUI.outputContent.append("\r\n");	
			}
		}
		String lovCode = null;
		String lovTitle = null;
		for (int i = 0; i < lovObjects.size(); i++) {
			LovObject lovObject = (LovObject) lovObjects.get(i);
			lovCode = lovObject.getLovCode();
			lovTitle = lovObject.getLovTitle();
			if (lovTitle != null && !lovTitle.equals("")){
				GUI.outputContent.append("," + moduleName + "," + formName
						+ "," + lovCode + "," + lovTitle + ",");
				GUI.outputContent.append("\r\n");
			}
		}
	}

	private void outputLovToFile() {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(
					lovTitleFile, true));
			for (int i = 0; i < common_lov.length; i++) {
				String[] lov = common_lov[i];
				if (lov[1] != null && !lov[1].equals(""))
					out.println("," + moduleName + "," + formName + ","
							+ lov[0] + "," + lov[1] + ",");
			}

			String lovCode = null;
			String lovTitle = null;
			for (int i = 0; i < lovObjects.size(); i++) {
				LovObject lovObject = (LovObject) lovObjects.get(i);
				lovCode = lovObject.getLovCode();
				lovTitle = lovObject.getLovTitle();
				if (lovTitle != null && !lovTitle.equals(""))
					out.println("," + moduleName + "," + formName + ","
							+ lovCode + "," + lovTitle + ",");
			}
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void getContent(File level) {
		try {
			lovObjects.clear();
			String outContent;

			FileInputStream fis = new FileInputStream(level);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(fis));

			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("IDFOS_TYP = 36")) {
					LovObject(bufferedReader);
				} else if (outContent.trim().startsWith("MODNAME")) {
					outContent = bufferedReader.readLine();
					formName = pickUp(outContent, regEx);
					formName = pickUp(formName, FormRegEx);
					formName = formName.substring(0, formName.indexOf("."));
				}
			}
			fis.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		getFormModule();
		if (lov_title_flag) {

			if (GetFormProperty.outputTo == Common.outputToGUIPanel)
				outputLovToGUIPanel();
			else if (GetFormProperty.outputTo == Common.outputToCmdConsole)
				outputLovToCmdConsole();
			else
				outputLovToFile();
		}
		if (lov_col_flag) {
			if (GetFormProperty.outputTo == Common.outputToGUIPanel)
				outputLovColumnToGUIPanel();
			else if (GetFormProperty.outputTo == Common.outputToCmdConsole)
				outputLovColumnToCmdConsole();
			else
				outputLovColumnToFile();
		}
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
		if (outputDir == null)
			outputDir = inputFile;
		if (!inputFile.isDirectory())
			fileName = fileName.substring(0, fileName.indexOf("."));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String dateString = dateFormat.format(date);
		String lovFileName = fileName + "_" + lov_title_type + "_" + dateString
				+ ".csv";
		String lovColumnFileName = fileName + "_" + lov_column_type + "_"
				+ dateString + ".csv";
		if (lov_title_flag)
			lovTitleFile = creatFile(lovFileName);
		if (lov_col_flag)
			lovColumnFile = creatFile(lovColumnFileName);
	}

	private File creatFile(String lovFileName) {
		File newFile = null;
		try {
			if (outputDir.isDirectory()) {
				newFile = new File(outputDir + "/" + lovFileName);
			} else {
				newFile = new File(outputDir.getParent() + "/" + lovFileName);
			}
			if (!newFile.exists()) {
				newFile.createNewFile();
			} else {
				newFile.delete();
				newFile.createNewFile();
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return newFile;
	}

	private void LovObject(BufferedReader bufferedReader) {
		LovObject lovObject = null;
		String outContent;
		String lovCode;
		String lovName;
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("TN = 211")) {
					lovObject = new LovObject();
					lovObjects.add(lovObject);
					lovCode = getLovName(bufferedReader);
					lovObject.setLovCode(lovCode);
				} else if (outContent.trim().equals("TN = 307")) {
					lovName = getLovName(bufferedReader);
					lovObject.setLovTitle(lovName);
				} else if (outContent.trim().startsWith("IDFOS_TYP")) {
					String IDFOS_TYP = outContent.trim();
					if (IDFOS_TYP.equals("IDFOS_TYP = 13")) {
						LovColumnObject(lovObject, bufferedReader);

					} else if (IDFOS_TYP.equals("IDFOS_TYP = 14")) {

					} else if (IDFOS_TYP.equals("IDFOS_TYP = 36")) {

					} else if (IDFOS_TYP.equals("IDFOS_TYP = 52"))
						break;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void LovColumnObject(LovObject lovObject,
			BufferedReader bufferedReader) {
		String outContent;
		String lovName;
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("TI = 1")) {
				} else if (outContent.trim().equals("TN = 307")) {
					lovName = getLovName(bufferedReader);
					lovObject.addColumnTitle(lovName);
				} else if (outContent.trim().startsWith("IDFOS_TYP")) {
					String IDFOS_TYP = outContent.trim();
					if (!IDFOS_TYP.equals("IDFOS_TYP = 13")) {
						break;

					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getLovName(BufferedReader bufferedReader) {
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

	private int getLovWidthProperty(BufferedReader bufferedReader) {
		String outContent = null;
		int width = 0;
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("NN = 77")) {
					return getLovWidth(bufferedReader);
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return width;
	}

	private int getLovWidth(BufferedReader bufferedReader) {
		String outContent = null;
		int width = 0;
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().startsWith("NV")) {
					System.out.println(pickUp(outContent, ".*=\\s*(.*)\\s*"));
					return Integer.valueOf(
							pickUp(outContent, ".*=\\s*(.*)\\s*")).intValue();
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return width;
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

	String[][] common_lov = { { "PARAMETER", "系统参数列表" }, { "SAMPLE", "" } };
	String[][] common_lov_col = { { "PARAMETER", "1", "参数名称" },
			{ "PARAMETER", "2", "参数描述" } };
}
