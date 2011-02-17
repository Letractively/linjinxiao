/**
 * 
 */
package oracle.form.property.radio;

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
public class GetRadioMessage {

	/**
	 * @param args
	 */
	// ACP ACR AST BGT CIMCINV CIMCORD CSH CST ENG EXP FND FRS GLD
	// INV MDM MWF ORD OTHER PLN PUR QMS SFC SYS
	File inputFile = new File(
			"E:/workspace/zj/set_property/radio_button/INV708/PUR362.fmt");
	File outputFile;

	private String regEx = ".*\"(.*)\".*";
	String FormRegEx = ".+\\\\(.+)$";
	private ArrayList radioGroups;
	private String formName;
	private String moduleName;
	String radioGroupCode = "";
	String blockCode = "";
	String type = Constant.radio_type;
	public static final String InterfaceTable = "sys_radio_label_interface";

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
	} // end main method

	private void getFormModule() {
		if (moduleName == null || moduleName.equals("")) {
			moduleName = formName.substring(0, 3);
		}
	}

	private void outputToCmdConsole() {
		ArrayList RadioButtons = null;
		for (int i = 0; i < radioGroups.size(); i++) {
			RadioGroup radioGroup = (RadioGroup) radioGroups.get(i);
			RadioButtons = radioGroup.getRadioButtons();
			for (int j = 0; j < RadioButtons.size(); j++) {
				// System.out.println("ACP,ACP306,"+radioCode+","+radioTitle+","+columnTitles.get(j));
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
				// System.out.println("ACP,ACP306,"+radioCode+","+radioTitle+","+columnTitles.get(j));
				RadioButton radioButton = (RadioButton) RadioButtons.get(j);
				GUI.outputContent.append("," + moduleName + "," + formName
						+ "," + radioGroup.getBlockCode() + ","
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
					// System.out.println("ACP,ACP306,"+radioCode+","+radioTitle+","+columnTitles.get(j));
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
			e.printStackTrace();
		}

	}

	private void outputToDataBase() throws SQLException {
		String insertSQL = "insert into sys_radio_label_interface (batch_id,module_code,function_code,block_code,item_code,radio_code,label_zh) values (?,?,?,?,?,?,?)";
		Connection dbConnection = DBManager.getDBConnection();
		PreparedStatement statement = dbConnection.prepareStatement(insertSQL);
		dbConnection.setAutoCommit(false);
		ArrayList RadioButtons = null;
		for (int i = 0; i < radioGroups.size(); i++) {
			RadioGroup radioGroup = (RadioGroup) radioGroups.get(i);
			RadioButtons = radioGroup.getRadioButtons();
			for (int j = 0; j < RadioButtons.size(); j++) {
				// System.out.println("ACP,ACP306,"+radioCode+","+radioTitle+","+columnTitles.get(j));
				RadioButton radioButton = (RadioButton) RadioButtons.get(j);
				statement.setInt(1, FormPropertyHandler.batch_id);
				statement.setString(2, moduleName);
				statement.setString(3, formName);
				statement.setString(4, radioGroup.getBlockCode());
				statement.setString(5, radioGroup.getRadioCode());
				statement.setString(6, radioButton.getRadioButtonCode());
				statement.setString(7, radioButton.getRadioButtonTitle());
				statement.executeQuery();
			}
		}
		dbConnection.commit();
		dbConnection.setAutoCommit(true);

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
				} else if (outContent.trim().equals("IDFOS_TYP = 94")) {
					String radio_group = getName(bufferedReader);
					if (radio_group.equals("RADIO_GROUP"))
						RadioObject(bufferedReader);
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
		if (FormPropertyHandler.outputTo == Constant.outputToGUIPanel)
			outputToGUIPanel();
		else if (FormPropertyHandler.outputTo == Constant.outputToCmdConsole)
			outputToCmdConsole();
		else if (FormPropertyHandler.outputTo == Constant.outputToFile) {
			outputToFile();
		}else{
			try {
				outputToDataBase();
			} catch (SQLException e) {
				e.printStackTrace();
				FormPropertyHandler.log(e.getMessage());
			}
		}
	}
		

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
				} else if (nextLevel[i].isDirectory()) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
					// else if (IDFOS_TYP.equals("IDFOS_TYP = 63")) {
					//
					// } else if (IDFOS_TYP.equals("IDFOS_TYP = 68")) {
					//
					// } else if (IDFOS_TYP.equals("IDFOS_TYP = 69")) {
					//
					// } else
					// break;
				} else if (outContent.trim().equals("ON = 136")) {
					break;
				}
			}
		} catch (Exception e) {
			// TODO �Զ���� catch ��
			e.printStackTrace();
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
			// TODO �Զ���� catch ��
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

		// boolean rs = m.find();
		m.find();
		for (int i = 1; i <= m.groupCount(); i++) {
			selectedString = m.group(i);
			// System.out.println(m.group(i));

		}
		return selectedString;

	}
}
