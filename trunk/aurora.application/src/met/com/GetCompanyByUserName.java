package met.com;

import uncertain.composite.CompositeMap;

public class GetCompanyByUserName {
	
	String userName;
	
	public void setUserName(String userName){
		this.userName = userName;
	}
	/*
	 <GetCompanyByUserName xmlns="http://tempuri.org/">
      	<userName>string</userName>
    </GetCompanyByUserName>
	 */
	public CompositeMap toXML(){
		CompositeMap GetCompanyByUserName = new CompositeMap("GetCompanyByUserName");
		GetCompanyByUserName.setNameSpaceURI("http://tempuri.org/");
		CompositeMap user_name = new CompositeMap("userName");
		user_name.setText(userName);
		user_name.setNameSpaceURI("http://tempuri.org/");
		GetCompanyByUserName.addChild(user_name);
		return GetCompanyByUserName;
	}
	/*
	<GetCompanyByUserNameResponse xmlns="http://tempuri.org/">
       <GetCompanyByUserNameResult>string</GetCompanyByUserNameResult>
    </GetCompanyByUserNameResponse>
  	*/
	public String getResult(CompositeMap content){
		return content.getChild("GetCompanyByUserNameResult").getText();
	}

}
