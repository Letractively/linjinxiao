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

	private JCheckBox getItemDesc = new JCheckBox("��ȡ�ֶ�����");
	private JCheckBox getWindowTitle = new JCheckBox("��ȡ��������");
	private JCheckBox getTabPageTitle = new JCheckBox("��ȡ��ǩҳ����");
	private JCheckBox getLovTitle = new JCheckBox("��ȡLOV��������");
	private JCheckBox getLovColumnTitle = new JCheckBox("��ȡLOV8λ��������");
	private JCheckBox getRadioDesc = new JCheckBox("��ȡ��ѡ������");

	private String[][] formProperties = { { "getItemDesc", "false" },
			{ "getWindowTitle", "false" }, { "getTabPageTitle", "false" },
			{ "getLovTitle", "false" }, { "getLovColumnTitle", "false" },
			{ "getRadioDesc", "false" } };

	JButton start = new JButton("��ʼִ��");
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

		inputFileButton = new JButton("����FMT�ļ�");
		inputFileField = new TextField(80);
		inputFileButton.addActionListener(this);

		outputFileButton = new JButton("���CSVĿ¼");
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
			creatFileChooser(new MyFileFilter(), "����FMT�ļ�", inputFileField,
					JFileChooser.FILES_AND_DIRECTORIES);
		} else if (e.getSource().equals(outputFileButton)) {
			creatFileChooser(null, "ѡ��CSV���Ŀ¼", outputFileField,
					JFileChooser.DIRECTORIES_ONLY);
		} else if (e.getSource().equals(start)) {
			String inputFileName = inputFileField.getText();
			String outputFileName = outputFileField.getText();
			if (inputFileName == null || inputFileName.equals("")) {
				JOptionPane.showMessageDialog(this, "fmt�ļ���Ŀ¼����Ϊ�գ�", "����",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			File inputFile = new File(inputFileName);
			if ((!inputFile.isDirectory()&&!inputFileName.toLowerCase().endsWith(".fmt"))
					|| !inputFile.exists()) {
				JOptionPane.showMessageDialog(this, "��ָ����ȷ��fmt�ļ���Ŀ¼", "����",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (outputSwitchBox.getSelectedIndex() == 0) {
				if (outputFileName == null || outputFileName.equals("")) {
					JOptionPane.showMessageDialog(null, "��ָ�����Ŀ¼", "����",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				File outputDir = new File(outputFileName);
				if (!outputDir.isDirectory() || !outputDir.exists()) {
					JOptionPane.showMessageDialog(null, "��ָ����ȷ�����Ŀ¼", "����",
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
			JOptionPane.showMessageDialog(null, "ִ�н���", "OK",
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
