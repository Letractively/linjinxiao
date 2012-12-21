/**
 * 
 */
package oracle.form.property;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * @author linjinxiao
 * 
 */
public class GUI extends JFrame implements ActionListener {

	/**
	 * @param args
	 */
	JButton inputFileButton;
	TextField inputFileField;

	 
	String[] switchContol = new String[] { Common.outputToFile, Common.outputToGUIPanel };
	JComboBox outputSwitchBox;

	JButton outputFileButton;
	TextField outputFileField;

	JLabel blankLable = new JLabel(
			"                                                                                                 "
					+ "                                                                                                 ");

	private JCheckBox getItemDesc = new JCheckBox("获取字段描述");
	private JCheckBox getWindowTitle = new JCheckBox("获取窗口描述");
	private JCheckBox getTabPageTitle = new JCheckBox("获取标签页描述");
	private JCheckBox getLovTitle = new JCheckBox("获取LOV标题描述");
	private JCheckBox getLovColumnTitle = new JCheckBox("获取LOV栏位标题描述");
	private JCheckBox getRadioDesc = new JCheckBox("获取单选框描述");

	private String[][] formProperties = { { "getItemDesc", "false" },
			{ "getWindowTitle", "false" }, { "getTabPageTitle", "false" },
			{ "getLovTitle", "false" }, { "getLovColumnTitle", "false" },
			{ "getRadioDesc", "false" } };

	JButton start = new JButton("开始执行");
	public static TextArea outputContent;

	boolean outputToFile = false;
	Container container;

	public static void main(String[] args) {
		GUI gui = new GUI();
		gui.showSelf();
	}

	private void showSelf() {
		container = this.getContentPane();

		container.setLayout(new FlowLayout());

		inputFileButton = new JButton("输入FMT文件");
		inputFileField = new TextField(80);
		inputFileButton.addActionListener(this);

		outputFileButton = new JButton("输出CSV目录");
		outputFileField = new TextField(64);
		outputFileButton.addActionListener(this);

		outputSwitchBox = new JComboBox(switchContol);
		outputSwitchBox.setSelectedIndex(0);
		outputSwitchBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((JComboBox) e.getSource()).getSelectedItem().equals(
						Common.outputToFile)) {
					outputFileButton.setVisible(true);
					outputFileField.setVisible(true);
					outputContent.setVisible(false);
					blankLable.setVisible(false);
					GetFormProperty.outputTo = Common.outputToFile; 
				}
				if (((JComboBox) e.getSource()).getSelectedItem().equals(
						Common.outputToGUIPanel)) {
					outputFileButton.setVisible(false);
					outputFileField.setVisible(false);
					outputContent.setVisible(true);
					blankLable.setVisible(true);
					GetFormProperty.outputTo = Common.outputToGUIPanel; 
				}
			}
		});
		start.addActionListener(this);

		outputContent = new TextArea(10, 100);

		container.add(inputFileButton);
		container.add(inputFileField);

		container.add(outputSwitchBox);

		container.add(outputFileButton);
		container.add(outputFileField);

		container.add(blankLable);

		container.add(getItemDesc);
		container.add(getWindowTitle);
		container.add(getTabPageTitle);
		container.add(getLovTitle);
		container.add(getLovColumnTitle);
		container.add(getRadioDesc);

		container.add(start);
		container.add(outputContent);

		outputContent.setVisible(false);
		blankLable.setVisible(false);
		GetFormProperty.outputTo = Common.outputToFile; 
		
		setSize(800, 400);
		setVisible(true);
		setLocation(150, 150);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}
		});

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(inputFileButton)) {
			creatFileChooser(new MyFileFilter(), "输入FMT文件", inputFileField,
					JFileChooser.FILES_AND_DIRECTORIES);
		} else if (e.getSource().equals(outputFileButton)) {
			creatFileChooser(null, "选择CSV输出目录", outputFileField,
					JFileChooser.DIRECTORIES_ONLY);
		} else if (e.getSource().equals(start)) {
			String inputFileName = inputFileField.getText();
			String outputFileName = outputFileField.getText();
			if (inputFileName == null || inputFileName.equals("")) {
				JOptionPane.showMessageDialog(this, "fmt文件或目录不能为空！", "错误",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			File inputFile = new File(inputFileName);
			if ((!inputFile.isDirectory()&&!inputFileName.toLowerCase().endsWith(".fmt"))
					|| !inputFile.exists()) {
				JOptionPane.showMessageDialog(this, "请指定正确的fmt文件或目录", "错误",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (outputSwitchBox.getSelectedIndex() == 0) {
				if (outputFileName == null || outputFileName.equals("")) {
					JOptionPane.showMessageDialog(null, "请指定输出目录", "错误",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				File outputDir = new File(outputFileName);
				if (!outputDir.isDirectory() || !outputDir.exists()) {
					JOptionPane.showMessageDialog(null, "请指定正确的输出目录", "错误",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (getItemDesc.isSelected()) {
				formProperties[0][1] = "true";
			}else
				formProperties[0][1] = "false";
			if (getWindowTitle.isSelected()) {
				formProperties[1][1] = "true";
			}else
				formProperties[1][1] = "false";
			if (getTabPageTitle.isSelected()) {
				formProperties[2][1] = "true";
			}else
				formProperties[2][1] = "false";
			if (getLovTitle.isSelected()) {
				formProperties[3][1] = "true";
			}else
				formProperties[3][1] = "false";
			if (getLovColumnTitle.isSelected()) {
				formProperties[4][1] = "true";
			}else
				formProperties[4][1] = "false";
			if (getRadioDesc.isSelected()) {
				formProperties[5][1] = "true";
			}else
				formProperties[5][1] = "false";
			outputContent.setText("");
			GetFormProperty GetFormProperty = new GetFormProperty(
					inputFileName, outputFileName, formProperties);
			GetFormProperty.start();
			JOptionPane.showMessageDialog(null, "执行结束！", "OK",
					JOptionPane.PLAIN_MESSAGE);
		}

	}

	public void creatFileChooser(FileFilter fileFilter, String DialogTitle,
			TextField textfield, int FileType) {
		JFileChooser fileChooser = new JFileChooser();
		if (fileFilter != null)
			fileChooser.setFileFilter(fileFilter);
		fileChooser.setFileSelectionMode(FileType);
		fileChooser.setDialogTitle(DialogTitle);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			textfield.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
		if (rVal == JFileChooser.CANCEL_OPTION) {
			textfield.setText("");
		}
	}

}
