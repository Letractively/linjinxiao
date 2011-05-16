package hls;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import aurora.plugin.quartz.ApplicationException;
import aurora.plugin.quartz.SchedulerConfig;

public class SendPhoneMsg implements Job {

	/**
	 * @param args
	 */
	public static final String PLUGIN = "aurora.plugin.quartz.application.hls";
	public static Integer running = new Integer(0);
	public String SERVICE_URL;
	public String SN;// 序列号
	public String PASSWORD;// 密码

	private Connection conn;
	private ILogger logger;
	private Client client;
	public void execute(JobExecutionContext context) throws JobExecutionException {
		synchronized (running) {
			if (running.intValue() == 1) {
				// System.out.println("prev job still running");
				return;
			}
			running = new Integer(1);
		}
		try {
			IObjectRegistry os = SchedulerConfig.getObjectRegistry(context.getJobDetail().getJobDataMap());
			logger = LoggingContext.getLogger(PLUGIN, os);
			conn = initConnection(os);
//			logger.log(Level.CONFIG,"SERVICE_URL:"+SERVICE_URL);
//			logger.log(Level.CONFIG,"SN:"+SN);
			client = new Client(SERVICE_URL,SN,PASSWORD);
			sendMessages();
			conn.close();
		} catch (Exception ex) {
			throw new JobExecutionException(ex);
		} finally {
			running = new Integer(0);
			try {
				if(conn != null && !conn.isClosed()){
					conn.rollback();
					conn.close();
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "close dbconnection failed.", e);
			}
		}
	}
	private Connection initConnection(IObjectRegistry registry) throws ApplicationException {
		DataSource ds = (DataSource) registry.getInstanceOfType(DataSource.class);
		try {
			if (ds == null)
				throw new ApplicationException("Can not get DataSource from registry " + registry);
			return ds.getConnection();
		} catch (SQLException e) {
			throw new ApplicationException("Can not get Connection from DataSource", e);
		}
	}
	private void sendMessages() throws SQLException{
		String query_header_sql = " select h.message_id,to_char(h.send_date, 'yyyy-mm-dd hh24:mi:ss'),h.send_content" +
				" from HLS_PRJ_MESSAGE h where h.status = 'NEW' order by h.message_id";
		String query_line_sql = " select l.phone_number from HLS_PRJ_MESSAGE_SENDER l where l.message_id=?";
		Statement statement = conn.createStatement();
		ResultSet header_rs = statement.executeQuery(query_header_sql);
		String mobile = "";
		int messageId = -1;
		try {
			while (header_rs.next()){
				messageId = header_rs.getInt(1);
				PreparedStatement ps = conn.prepareStatement(query_line_sql);
				ps.setInt(1, messageId);
				ResultSet line_rs = ps.executeQuery();
				while (line_rs.next()){
					mobile = mobile+line_rs.getString(1)+",";
				}
				line_rs.close();
				ps.close();
				String stime=header_rs.getString(2);
				if(stime == null)
					stime = "";
				String content = header_rs.getString(3);
				logger.log("sendMessage "+"messageId:"+messageId+" mobile："+mobile+" content:"+content+" stime:"+stime);
				sendMessage(messageId,mobile,content,"",stime,"");
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "handle messageId:"+messageId+" failed.", e);
			updateErrorMessage(messageId,e);
		} catch (ApplicationException e) {
			logger.log(Level.SEVERE, "handle messageId:"+messageId+" failed.", e);
			updateErrorMessage(messageId,e);
		}
		header_rs.close();
		statement.close();
	}
	private void sendMessage(int message_id,String mobile, String content, String ext, String stime, String rrid) throws SQLException, ApplicationException{
//		String retrunRrid = "retrunRrid";
		String retrunRrid = client.mt(mobile, content, ext, stime, rrid);
		if(retrunRrid != null && retrunRrid.startsWith("-")){
			throw new ApplicationException("send message error:"+retrunRrid);
		}
		logger.log("message_id:"+message_id+" send,return rrid:"+retrunRrid);
		String update_sql = "update HLS_PRJ_MESSAGE h set h.status='SEND'";
		Statement statement = conn.createStatement();
		statement.executeUpdate(update_sql);
		logger.log("update message_id:"+message_id+" status successfull.");
		statement.close();
	}
	private void updateErrorMessage(int message_id,Throwable e) throws SQLException{
		logger.log("update error message from message_id:"+message_id);
		String update_sql = "update HLS_PRJ_MESSAGE h set h.status='ERROR' and h.error_msg="+e.getLocalizedMessage();
		Statement statement = conn.createStatement();
		statement.executeUpdate(update_sql);
		logger.log("update message_id:"+message_id+" status successfull.");
		statement.close();
	}
}
