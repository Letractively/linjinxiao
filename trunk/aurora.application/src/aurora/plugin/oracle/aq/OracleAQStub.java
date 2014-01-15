package aurora.plugin.oracle.aq;


import java.sql.Connection;

import aurora.application.features.msg.IMessageStub;

public interface OracleAQStub extends IMessageStub{

	public Connection createConnection() throws Exception;

}
