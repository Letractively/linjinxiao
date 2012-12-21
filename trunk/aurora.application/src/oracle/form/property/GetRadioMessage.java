/**
 * 
 */
package oracle.form.property;

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
public class GetRadioMessage {

	/**
	 * @param args
	 */
	// ACP ACR AST BGT CIMCINV CIMCORD CSH CST ENG EXP FND FRS GLD
	// INV MDM MWF ORD OTHER PLN PUR QMS SFC SYS
	File inputFile;
	File outputFile;

	private String regEx = ".*\"(.*)\".*";
	String FormRegEx = ".+\\\\(.+)$";
	private ArrayList radioGroups;
	private String formName;
	private String moduleName;
	String radioGroupCode = "";
	String blockCode = "";
	String type = Common.radio_type;
	public GetRadioMessage() {
		radioGroups = new ArrayList();

	}
	public GetRadioMessage(String inputFileName, String outputFileName) {
		this.inputFile = new File(inputFileName);
		this.outputFile = new File(outputFileName);
		radioGroups = new ArrayList();

	}

	public static void main(String args[]) {
		GetRadioMessage getRadioMessage = new GetRadioMessage();
		getRadioMessage.start();
	} 

	private void getFormModule() {
		if(moduleName ==null || moduleName.equals("")){
			moduleName = formName.substring(0, 3);
		}
	}

	private void outputToCmdConsole() {
		ArrayList RadioButtons = null;
		for (int i = 0; i < radioGroups.size(); i++) {
			RadioGroup radioGroup = (RadioGroup) radioGroups.get(i);
			RadioButtons = radioGroup.getRadioButtons();
			for (int j = 0; j < RadioButtons.size(); j++) {
				RadioButton radioButton = (RadioButton) RadioButtons.get(j);
				System.out.println("," + moduleName + "," + formName + ","
						+ radioGroup.getBlockCode() + ","
						+ radioGroup.getRadioCode() + ","
						+ radioButton.getRadioButtonCode() + ","
						+ radioButton.getRadioButtonTitle() + ",");

			}
		}
	}
	private void outputToGUIPanel() {
		ArrayList RadioButtons = null;
		for (int i = 0; i < radioGroups.size(); i++) {
			RadioGroup radioGroup = (RadioGroup) radioGroups.get(i);
			RadioButtons = radioGroup.getRadioButtons();
			for (int j = 0; j < RadioButtons.size(); j++) {
				RadioButton radioButton = (RadioButton) RadioButtons.get(j);
				GUI.outputContent.append("," + moduleName + "," + formName + ","
						+ radioGroup.getBlockCode() + ","
						+ radioGroup.getRadioCode() + ","
						+ radioButton.getRadioButtonCode() + ","
						+ radioButton.getRadioButtonTitle() + ",");
				GUI.outputContent.append("\r\n");
			}
		}
	}

	private void outputToFile() {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(outputFile,
					true));

			ArrayList RadioButtons = null;
			for (int i = 0; i < radioGroups.size(); i++) {
				RadioGroup radioGroup = (RadioGroup) radioGroups.get(i);
				RadioButtons = radioGroup.getRadioButtons();
				for (int j = 0; j < RadioButtons.size(); j++) {
					RadioButton radioButton = (RadioButton) RadioButtons.get(j);
					out.println("," + moduleName + "," + formName + ","
							+ radioGroup.getBlockCode() + ","
							+ radioGroup.getRadioCode() + ","
							+ radioButton.getRadioButtonCode() + ","
							+ radioButton.getRadioButtonTitle() + ",");

				}
			}
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void getContent(File level) {
		try {
			radioGroups.clear();
			String outContent;

			FileInputStream fis = new FileInputStream(level);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(fis));

			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("IDFOS_TYP = 7")) {
					blockCode = getName(bufferedReader);
				}
				if (outContent.trim().equals("IDFOS_TYP = 30")) {
					radioGroupCode = getName(bufferedReader);
				} else if (outContent.trim().equals("IDFOS_TYP = 68")) {
					String radio_group = getName(bufferedReader);
					if (radio_group.equals("WHEN-RADIO-CHANGED"))
						RadioObject(bufferedReader);
				}else if (outContent.trim().equals("IDFOS_TYP = 94")) {
					String radio_group = getName(bufferedReader);
					if (radio_group.equals("RADIO_GROUP"))
						RadioObject(bufferedReader);
				} 
				else if (outContent.trim().startsWith("MODNAME")) {
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
		if(GetFormProperty.outputTo == Common.outputToGUIPanel)
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

	private void RadioObject(BufferedReader bufferedReader) {
		RadioGroup radioGroup = null;
		String outContent;
		radioGroup = new RadioGroup();
		radioGroups.add(radioGroup);
		radioGroup.setBlockCode(blockCode);
		radioGroup.setRadioCode(radioGroupCode);
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().startsWith("IDFOS_TYP")) {
					String IDFOS_TYP = outContent.trim();
					if (IDFOS_TYP.equals("IDFOS_TYP = 62")) {
						RadioButton(radioGroup, bufferedReader);
					} 
				}else if (outContent.trim().equals("ON = 136")) {
					break;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private void RadioButton(RadioGroup radioGroup,
			BufferedReader bufferedReader) {
		String outContent;
		String radioButtonCode = "";
		String radioButtonDesc = "";
		RadioButton radioButton = new RadioButton();
		try {
			while ((outContent = bufferedReader.readLine()) != null) {
				if (outContent.trim().equals("TN = 211")) {
					radioButtonCode = getName(bufferedReader);
				} else if (outContent.trim().equals("TN = 151")) {
					radioButtonDesc = getName(bufferedReader);
					if (!radioButtonDesc.equals("")) {
						radioButton.setRadioButtonCode(radioButtonCode);
						radioButton.setRadioButtonTitle(radioButtonDesc);
						radioGroup.addRadioButton(radioButton);
						break;
					}
				} else if (outContent.trim().equals("TN = 394")) {
					radioButtonDesc = getName(bufferedReader);
					radioButton.setRadioButtonCode(radioButtonCode);
					radioButton.setRadioButtonTitle(radioButtonDesc);
					radioGroup.addRadioButton(radioButton);
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
		for (int i = 1; i <= m.groupCount(); i++) {
			selectedString = m.group(i);
		}
		return selectedString;

	}
}
