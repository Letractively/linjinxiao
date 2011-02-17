/**
 * 
 */
package oracle.form.property;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oracle.form.property.item.GetItemDesc;
import oracle.form.property.lov.GetLovMessage;
import oracle.form.property.radio.GetRadioMessage;
import oracle.form.property.tabpage.GetTabPageMessage;
import oracle.form.property.window.GetWindowMessage;


/**
 * @author linjinxiao
 * 
 */
public class FormPropertyHandler {

	/**
	 * @param args
	 */
	public static String outputTo = Constant.outputToDataBase;
	public static int batch_id ;
	public String[][] formProperties = { { "getItemDesc", "true" },
			{ "getWindowTitle", "true" }, { "getTabPageTitle", "true" },
			{ "getLovTitle", "true" }, { "getLovColumnTitle", "true" },
			{ "getRadioDesc", "true" } };
	private static FileOutputStream logFOS;
	// ACP ACR AST BGT CIMCINV CIMCORD CSH CST ENG EXP FND FRS GLD
	// INV MDM MWF ORD OTHER PLN PUR QMS SFC SYS
	private static final String LineSeparator = System.getProperty("line.separator");
	private static final String fmtFileExtension = "fmt";
	private List fmbList;
	public static final String formNameSpilt = "\"|>>|\\.|\\\\|\\/|fmt";
	public static void main(String[] args) {
		
//		if(args == null||args.length==0)
//			args = new String[]{"jdbc:oracle:thin:mas9i/mas9i@192.168.11.65:1521:masdev","1","CSH526.fmt","ACP301.fmt"};
		
		try {
			logFOS = createFile(createFileName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(args == null ||args.length <=0){
			log("no fmt ");
			return ;
		}
		List fmbList = new LinkedList();
		if(args[0].toLowerCase().equals(Constant.outputToFile.toLowerCase())){
			outputTo = Constant.outputToFile;
		}else {
			String dataBaseUrl = args[0];
			DBManager dbManager = new DBManager(dataBaseUrl);
			try {
				dbManager.init();
			} catch (ClassNotFoundException e) {
				log("driver is wrong! "+e.getMessage());
			} catch (SQLException e) {
				log("url is wrong! "+e.getMessage());
			}
			String batch_id_str = args[1];
			batch_id = Integer.parseInt(batch_id_str);
		}
		
		for(int i=2;i<args.length;i++){
			if(args[i].toLowerCase().endsWith("."+fmtFileExtension))
					fmbList.add(args[i]);
		}
		if(fmbList.size()==0){
			log("no fmt ");
			return ;
		}
		FormPropertyHandler formProperty = new FormPropertyHandler(fmbList);
		formProperty.start();
		try {
			DBManager.getDBConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
			log(e.getMessage());
		}
		System.out.println("done..");

	}
	private static FileOutputStream createFile(String fileName) throws IOException{
		if(fileName == null)
			return null;
		File newFile = new  File(fileName);
		if(!newFile.exists()){
			newFile.createNewFile();
		}
		return new FileOutputStream(newFile, true);
	}
	public static void log(String message){
		try {
			logFOS.write(Calendar.getInstance().getTime().toString().getBytes());
			logFOS.write(message.getBytes());
			logFOS.write(LineSeparator.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static String createFileName(){
		String seperator ="-"; 
		String time = Calendar.getInstance().get(Calendar.YEAR)+seperator+Calendar.getInstance().get(Calendar.MONTH)
		+seperator+Calendar.getInstance().get(Calendar.DATE)+"_"+Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+seperator+Calendar.getInstance().get(Calendar.MINUTE)+
		seperator+Calendar.getInstance().get(Calendar.SECOND);
		return "FormPropertyHandler_"+time+".log";
	}

	public FormPropertyHandler() {

	}

	public FormPropertyHandler(List fmbList) {
		this.fmbList = fmbList;
	}

	public void start() {
		boolean lov_title = false;
		boolean lov_col = false;
		if (formProperties == null)
			return;
		for(Iterator it = fmbList.iterator();it.hasNext();){
			String inputFileName = (String)it.next();
			if (formProperties[0][1].equals("true")) {
				GetItemDesc itemDesc = new GetItemDesc(inputFileName,
						"a.txt");
				itemDesc.start();
			}
			if (formProperties[1][1].equals("true")) {
				GetWindowMessage window = new GetWindowMessage(inputFileName,
						"a.txt");
				window.start();
			}
			if (formProperties[2][1].equals("true")) {
				GetTabPageMessage tabPage = new GetTabPageMessage(inputFileName,
						"a.txt");
				tabPage.start();
			}
			if (formProperties[3][1].equals("true")) {
				lov_title = true;
			}
			if (formProperties[4][1].equals("true")) {
				lov_col = true;
			}
			if (lov_title || lov_col) {
				GetLovMessage lov = new GetLovMessage(inputFileName,
						"a.txt", lov_title, lov_col);
				lov.start();
			}
			if (formProperties[5][1].equals("true")) {
				GetRadioMessage radio = new GetRadioMessage(inputFileName,
						"a.txt");
				radio.start();
			}
			}
	}
}
