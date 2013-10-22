/**
 * 
 */
package aurora.plugin.oracle.form.property;

import java.util.ArrayList;

/**
 * @author linjinxiao
 *
 */
public class Canvas {

	/**
	 * @param args
	 */
	private String canvasCode ;
	private ArrayList tabPageTitles;
	public Canvas(){
		tabPageTitles = new ArrayList();
	}
	public String getCanvasCode() {
		return canvasCode;
	}
	public void setCanvasCode(String canvasCode) {
		this.canvasCode = canvasCode;
	}
	public ArrayList getTabPageTitles() {
		return tabPageTitles;
	}
	public void setTabPageTitles(ArrayList tabPageTitles) {
		this.tabPageTitles = tabPageTitles;
	}	

}
