package met.com;
import uncertain.composite.CompositeMap;


public class CreateToken {
	
	String appUrl;
	String timeStamp;
	String iASID;

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
		
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
		
	}

	public void setIASID(String iASID) {
		this.iASID = iASID;
	}
	/*
	 <CreateToken xmlns="http://tempuri.org/">
	      <IASID>string</IASID>
	      <TimeStamp>string</TimeStamp>
	      <AppUrl>string</AppUrl>
     </CreateToken>
	 */
	public CompositeMap toXML(){
		CompositeMap createToken = new CompositeMap("CreateToken");
		createToken.setNameSpaceURI("http://tempuri.org/");
		CompositeMap IASID = new CompositeMap("IASID");
		IASID.setText(iASID);
		IASID.setNameSpaceURI("http://tempuri.org/");
		createToken.addChild(IASID);
		
		CompositeMap TimeStamp = new CompositeMap("TimeStamp");
		TimeStamp.setText(timeStamp);
		TimeStamp.setNameSpaceURI("http://tempuri.org/");
		createToken.addChild(TimeStamp);
		
		CompositeMap AppUrl = new CompositeMap("AppUrl");
		AppUrl.setText(appUrl);
		AppUrl.setNameSpaceURI("http://tempuri.org/");
		createToken.addChild(AppUrl);
		return createToken;
	}
	/* 
	 <CreateTokenResponse xmlns="http://tempuri.org/">
    	<CreateTokenResult>string</CreateTokenResult>
  	 </CreateTokenResponse>
  	*/
	public String getResult(CompositeMap content){
		return content.getChild("CreateTokenResult").getText();
	}

}
