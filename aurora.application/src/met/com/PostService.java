package met.com;

import uncertain.composite.CompositeMap;

public class PostService {

	String strToken;
	String EACUrl;

	public void setStrToken(String strToken) {
		this.strToken = strToken;

	}

	public void setEACUrl(String cloud_sso_eacurl) {
		this.EACUrl = cloud_sso_eacurl;
	}

	/*
	 * <PostService xmlns="http://tempuri.org/">
	 * 		<EACUrl>string</EACUrl>
	 * <StrToken>string</StrToken> </PostService>
	 */
	public CompositeMap toXML() {
		CompositeMap postService = new CompositeMap("PostService");
		postService.setNameSpaceURI("http://tempuri.org/");
		CompositeMap eACUrl = new CompositeMap("EACUrl");
		eACUrl.setText(EACUrl);
		eACUrl.setNameSpaceURI("http://tempuri.org/");
		postService.addChild(eACUrl);

		CompositeMap StrToken = new CompositeMap("StrToken");
		StrToken.setText(strToken);
		StrToken.setNameSpaceURI("http://tempuri.org/");
		postService.addChild(StrToken);

		return postService;
	}

	/*
	 * <PostServiceResponse xmlns="http://tempuri.org/">
	 * 		<PostServiceResult>string</PostServiceResult>
	 * </PostServiceResponse>
	 */
	public String getResult(CompositeMap content) {
		return content.getChild("PostServiceResult").getText();
	}

}
