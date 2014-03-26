package met.com;
import uncertain.composite.CompositeMap;


public class ValidateEACToken {
	
	String iASID;
	String timeStamp;
	String appUrl;
	String userAccount;
	String authenticator;

	public void setIASID(String iASID) {
		this.iASID= iASID;
		
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp= timeStamp;
		
	}

	public void setAppUrl(String appUrl) {
		this.appUrl= appUrl;
		
		
	}

	public void setUserAccount(String userAccount) {
		this.userAccount= userAccount;
		
		
	}

	public void setAuthenticator(String authenticator) {
		this.authenticator= authenticator;
	}
	/*
	 <ValidateEACToken xmlns="http://tempuri.org/">
      <IASID>string</IASID>
      <TimeStamp>string</TimeStamp>
      <AppUrl>string</AppUrl>
      <UserAccount>string</UserAccount>
      <Authenticator>string</Authenticator>
    </ValidateEACToken>
	 */
	public CompositeMap toXML(){
		CompositeMap validateEACToken = new CompositeMap("ValidateEACToken");
		validateEACToken.setNameSpaceURI("http://tempuri.org/");
		CompositeMap IASID = new CompositeMap("IASID");
		IASID.setText(iASID);
		IASID.setNameSpaceURI("http://tempuri.org/");
		validateEACToken.addChild(IASID);
		
		CompositeMap TimeStamp = new CompositeMap("TimeStamp");
		TimeStamp.setText(timeStamp);
		TimeStamp.setNameSpaceURI("http://tempuri.org/");
		validateEACToken.addChild(TimeStamp);
		
		CompositeMap AppUrl = new CompositeMap("AppUrl");
		AppUrl.setText(appUrl);
		AppUrl.setNameSpaceURI("http://tempuri.org/");
		validateEACToken.addChild(AppUrl);
		
		CompositeMap UserAccount = new CompositeMap("UserAccount");
		UserAccount.setText(userAccount);
		UserAccount.setNameSpaceURI("http://tempuri.org/");
		validateEACToken.addChild(UserAccount);
		
		CompositeMap Authenticator = new CompositeMap("Authenticator");
		Authenticator.setText(authenticator);
		Authenticator.setNameSpaceURI("http://tempuri.org/");
		validateEACToken.addChild(Authenticator);
		
		return validateEACToken;
	}
	/*
	 <ValidateEACTokenResponse xmlns="http://tempuri.org/">
    	<ValidateEACTokenResult>boolean</ValidateEACTokenResult>
  	 </ValidateEACTokenResponse>
  	*/
	public String getResult(CompositeMap content){
		return content.getChild("ValidateEACTokenResult").getText();
	}

}
