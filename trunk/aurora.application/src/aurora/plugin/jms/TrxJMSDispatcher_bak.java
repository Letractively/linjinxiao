package aurora.plugin.jms;

import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IMessageDispatcher;
import aurora.application.features.msg.TrxMessageDispatcher;

public class TrxJMSDispatcher_bak extends TrxMessageDispatcher{
	
	public TrxJMSDispatcher_bak(IObjectRegistry registry) {
		super(registry);
	}
	@Override
	protected IMessageDispatcher createMessageDispatcher(){
		return new MessageDispatcher(mRegistry);
	}
	

}
