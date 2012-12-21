/**
 * 
 */
package oracle.form.property;

/**
 * @author linjinxiao
 * 
 */
public class Constant {

	/**
	 * @param args
	 */
	public static final String outputToFile = "toFile";
	public static final String outputToDataBase = "toDB";
	public static final String outputToGUIPanel = "outputToGUIPanel";
	public static final String outputToCmdConsole = "outputToCmdConsole";
	public static final String item_desc_type = "item_desc";
	public static final String form_title_type = "form_title";
	public static final String form_tab_type = "form_tab";
	public static final String lov_title_type = "lov_title";
	public static final String lov_col_type = "lov_col";
	public static final String radio_type = "form_radio";

	public static void main(String[] args) {
		String splitExp = "\"|>>|\\.|\\\\|\\/|fmt";
		String a ="<<\"E:\\workspace\\zj\\set_property\\FMT\\CSH529.fmt\">>";
		String[] strs = a.split(splitExp);
		System.out.println(strs[strs.length-1]);
		a ="CSH529.fmt";
		strs = a.split(splitExp);
		System.out.println(strs[strs.length-1]);
		a ="<<\"/u01/aabc/CSH529.fmt\">>";
		strs = a.split(splitExp);
		System.out.println(strs[strs.length-1]);
	}

	public static void printException(Throwable e) {
		StackTraceElement[] messages = e.getStackTrace();
		if (messages == null)
			return;
		int length = messages.length;
		for (int i = 0; i < length; i++) {
			System.out.println("ClassName:" + messages[i].getClassName());
			System.out.println("getFileName:" + messages[i].getFileName());
			System.out.println("getLineNumber:" + messages[i].getLineNumber());
			System.out.println("getMethodName:" + messages[i].getMethodName());
			System.out.println("toString:" + messages[i].toString());
		}

	}

}
