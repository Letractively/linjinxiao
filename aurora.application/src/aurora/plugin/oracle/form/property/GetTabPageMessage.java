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
public class GetTabPageMessage {

	/**
	 * @param args
	 */
	// ACP ACR AST BGT CIMCINV CIMCORD CSH CST ENG EXP FND FRS GLD
	// INV MDM MWF ORD OTHER PLN PUR QMS SFC SYS
	File inputFile;
	File outputFile;

	private String regEx = ".*\"(.*)\".*";
	String FormRegEx = ".+\\\\(.+)$";
	private ArrayList canvases;
	private String formName;
	private String moduleName;

	String canvasCode = "";
	String type = Common.form_tab_type;

	public GetTabPageMessage() {
		canvases = new ArrayList();
	}

	public GetTabPageMessage(String inputFileName, String outputFileName) {
		canvases = new ArrayList();
		this.inputFile = new File(inputFileName);
		this.outputFile = new File(outputFileName);
	}

	public static void main(String args[]) {
		GetTabPageMessage getLovMessage = new GetTabPageMessage();
		getLovMessage.start();
	} // end main method

	private void getFormModule() {
		if (moduleName == null || moduleName.equals("")) {
			moduleName = formName.substring(0, 3);
		}
	}

	private void outputToCmdConsole() {
		String canvasCode = null;
		ArrayList tabPages = null;
		for (int i = 0; i < canvases.size(); i++) {
			Canvas canvas = (Canvas) canvases.get(i);
			canvasCode = canvas.getCanvasCode();
			tabPages = canvas.getTabPageTitles();
			for (int j = 0; j < tabPages.size(); j++) {
				TabPage tabPage = (TabPage) tabPages.get(j);
				System.out.println("," + moduleName + "," + formName + ","
						+ canvasCode + "," + tabPage.getCode() + ","
						+ tabPage.getTitle() + ",");

			}
		}
	}

	private void outputToGUIPanel() {
		String canvasCode = null;
		ArrayList tabPages = null;
		for (int i = 0; i < canvases.size(); i++) {
			Canvas canvas = (Canvas) canvases.get(i);
			canvasCode = canvas.getCanvasCode();
			tabPages = canvas.getTabPageTitles();
			for (int j = 0; j < tabPages.size(); j++) {
				TabPage tabPage = (TabPage) tabPages.get(j);
				GUI.outputContent.append("," + moduleName + "," + formName
						+ "," + canvasCode + "," + tabPage.getCode() + ","
						+ tabPage.getTitle() + ",");
				GUI.outputContent.append("\r\n");
			}

		}
	}

	private void outputToFile() {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(outputFile,
					true));

			String canvasCode = null;
			ArrayList tabPages = null;
			for (int i = 0; i < canvases.size(); i++) {
				Canvas canvas = (Canvas) canvases.get(i);
				canvasCode = canvas.getCanvasCode();
				tabPages = canvas.getTabPageTitles();
				for (int j = 0; j < tabPages.size(); j++) {
					TabPage tabPage = (TabPage) tabPages.get(j);
					out.println("," + moduleName + "," + formName + ","
							+ canvasCode + "," + tabPage.getCode() + ","
							+ tabPage.getTitle() + ",");

				}
			}
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void getContent(File level) {
		try {
			canvases.clear();
			String outContent;

			FileInputStream fis = new FileInputStream(level);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(fis));

			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("IDFOS_TYP = 11")) {
					canvasCode = getName(bufferedReader);
				} else if (outContent.trim().equals("IDFOS_TYP = 88")) {
					canvas(bufferedReader);
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
		if (GetFormProperty.outputTo == Common.outputToGUIPanel)
			outputToGUIPanel();
		else if (GetFormProperty.outputTo == Common.outputToCmdConsole)
			outputToCmdConsole();
		else
			outputToFile();
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
		String outputFileName = fileName + "_" + type + "_" + dateString
				+ ".csv";
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

	private void canvas(BufferedReader bufferedReader) {
		String outContent;
		String pageCode;
		String pageTitle;
		Canvas canvas = new Canvas();
		canvases.add(canvas);
		canvas.setCanvasCode(canvasCode);
		TabPage tabPage = new TabPage();
		canvas.getTabPageTitles().add(tabPage);
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("TN = 211")) {
					pageCode = getName(bufferedReader);
					tabPage.setCode(pageCode);
				} else if (outContent.trim().equals("TN = 151")) {
					pageTitle = getName(bufferedReader);
					tabPage.setTitle(pageTitle);
				} else if (outContent.trim().startsWith("IDFOS_TYP")) {
					String IDFOS_TYP = outContent.trim();
					if (IDFOS_TYP.equals("IDFOS_TYP = 88")) {
						tabPage = new TabPage();
						canvas.getTabPageTitles().add(tabPage);

					} else if (IDFOS_TYP.equals("IDFOS_TYP = 90")) {
					} else if (IDFOS_TYP.equals("IDFOS_TYP = 93")) {
					} else if (IDFOS_TYP.equals("IDFOS_TYP = 94")) {
					} else
						break;

				}
			}
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
}
