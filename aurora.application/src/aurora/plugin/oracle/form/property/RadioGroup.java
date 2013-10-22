/**
 * 
 */
package aurora.plugin.oracle.form.property;

import java.util.ArrayList;

/**
 * @author linjinxiao
 *
 */
public class RadioGroup {

	/**
	 * @param args
	 */
	private String blockCode;
	private String radioGroupCode ;
	private ArrayList radioButtons;
	public RadioGroup(){
		radioButtons = new ArrayList();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public void addRadioButton(RadioButton radioButton){
		radioButtons.add(radioButton);
	}
	public void addRadioButton(int index,RadioButton radioButton){
		radioButtons.add(index, radioButton);
	}
	public RadioButton getRadioButton(int index){
		return (RadioButton)radioButtons.get(index);
	}
	public String getRadioCode() {
		return radioGroupCode;
	}
	public void setRadioCode(String radioGroupCode) {
		this.radioGroupCode = radioGroupCode;
	}
	public ArrayList getRadioButtons() {
		return radioButtons;
	}
	public String getBlockCode() {
		return blockCode;
	}
	public void setBlockCode(String blockCode) {
		this.blockCode = blockCode;
	}
	public String getRadioGroupCode() {
		return radioGroupCode;
	}
	public void setRadioGroupCode(String radioGroupCode) {
		this.radioGroupCode = radioGroupCode;
	}
	public void setRadioButtons(ArrayList radioButtons) {
		this.radioButtons = radioButtons;
	}

}
