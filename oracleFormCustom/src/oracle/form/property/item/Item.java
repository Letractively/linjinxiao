/**
 * 
 */
package oracle.form.property.item;


/**
 * @author linjinxiao
 *
 */
public class Item {

	/**
	 * @param args
	 */
	private String blockCode ;
	private String itemCode ;
	private String itemTitle;
	public Item(){
		
	}
	public String getBlockCode() {
		return blockCode;
	}
	public void setBlockCode(String blockCode) {
		this.blockCode = blockCode;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	public String getItemTitle() {
		return itemTitle;
	}
	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}
	
}
