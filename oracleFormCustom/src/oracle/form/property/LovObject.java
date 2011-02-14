/**
 * 
 */
package oracle.form.property;

import java.util.ArrayList;

/**
 * @author linjinxiao
 *
 */
public class LovObject {

	/**
	 * @param args
	 */
	private String lovCode ;
	private String lovTitle ;
	private ArrayList columnTitles;
	public LovObject(){
		columnTitles = new ArrayList();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public void addColumnTitle(String columnTitle){
		columnTitles.add(columnTitle);
	}
	public void addColumnTitle(int index,String columnTitle){
		columnTitles.add(index, columnTitle);
	}
	public String getNextColumnTitle(int index){
		return (String)columnTitles.get(index);
	}
	public String getLovCode() {
		return lovCode;
	}
	public void setLovCode(String lovCode) {
		this.lovCode = lovCode;
	}
	public String getLovTitle() {
		return lovTitle;
	}
	public void setLovTitle(String lovTitle) {
		this.lovTitle = lovTitle;
	}
	public ArrayList getColumnTitles() {
		return columnTitles;
	}

}
