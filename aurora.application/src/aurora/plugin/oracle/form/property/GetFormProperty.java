/**
 * 
 */
package aurora.plugin.oracle.form.property;


/**
 * @author linjinxiao
 * 
 */
public class GetFormProperty {

	/**
	 * @param args
	 */
	public static String outputTo = Common.outputToCmdConsole;
	String outputFileName;
	String inputFileName;
	public String[][] formProperties = { { "getItemDesc", "false" },
			{ "getWindowTitle", "false" }, { "getTabPageTitle", "false" },
			{ "getLovTitle", "false" }, { "getLovColumnTitle", "false" },
			{ "getRadioDesc", "false" } };

	// ACP ACR AST BGT CIMCINV CIMCORD CSH CST ENG EXP FND FRS GLD
	// INV MDM MWF ORD OTHER PLN PUR QMS SFC SYS
	public static void main(String[] args) {
		GetFormProperty formProperty = new GetFormProperty();
		formProperty.start();

	}

	public GetFormProperty() {

	}

	public GetFormProperty(String inputFileName, String outputFileName,
			String[][] formProperties) {
		this.inputFileName = inputFileName;
		this.outputFileName = outputFileName;
		this.formProperties = formProperties;
	}

	public void start() {
		boolean lov_title = false;
		boolean lov_col = false;
		if (formProperties == null)
			return;
		if (formProperties[0][1].equals("true")) {
			GetItemDesc itemDesc = new GetItemDesc(inputFileName,
					outputFileName);
			itemDesc.start();
		}
		if (formProperties[1][1].equals("true")) {
			GetWindowMessage window = new GetWindowMessage(inputFileName,
					outputFileName);
			window.start();
		}
		if (formProperties[2][1].equals("true")) {
			GetTabPageMessage tabPage = new GetTabPageMessage(inputFileName,
					outputFileName);
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
					outputFileName, lov_title, lov_col);
			lov.start();
		}
		if (formProperties[5][1].equals("true")) {
			GetRadioMessage radio = new GetRadioMessage(inputFileName,
					outputFileName);
			radio.start();
		}
	}

}
