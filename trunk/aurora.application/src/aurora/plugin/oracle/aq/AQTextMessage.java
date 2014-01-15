package aurora.plugin.oracle.aq;

import uncertain.composite.CompositeMap;
import aurora.application.features.msg.IMessage;
public class AQTextMessage implements IMessage{
	AuroraMessage aqMessage;
	public AQTextMessage(AuroraMessage message){
		this.aqMessage = message;
	}
	public String getText() throws Exception{
		return aqMessage.getMessage();
	}
	public CompositeMap getProperties() throws Exception{
		AuroraMessageProperties amp =  aqMessage.getProperties();
		if(amp != null && amp.getArray() != null){
			CompositeMap cm = new CompositeMap();
			AuroraMessageProperty[] propertiesArray =  amp.getArray();
			for(int i=0;i<propertiesArray.length;i++){
				AuroraMessageProperty property = propertiesArray[i];
				cm.put(property.getKey(),property.getValue());
			}
			return cm;
		}
		return null;
	}
}
