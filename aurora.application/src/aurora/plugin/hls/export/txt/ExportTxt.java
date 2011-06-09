package aurora.plugin.hls.export.txt;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.database.service.DatabaseServiceFactory;
import aurora.database.service.SqlServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class ExportTxt extends AbstractEntry {
	public static final String LOGGING_TOPIC = "aurora.plugin.hls.export.txt";
	public String KEY_CHARSET = "GBK";
	private ILogger mLogger;
	public String FileName = "file";
	public String FileExtension = "txt";
	public String Separator = ",";
	public String Sql;
	private DatabaseServiceFactory databasefactory;
	public ExportTxt(IObjectRegistry registry) {
		databasefactory = (DatabaseServiceFactory) registry.getInstanceOfType(DatabaseServiceFactory.class);
	}
	@Override
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		initLogger(context);
		mLogger.log(Level.FINE, context.toXML());
		mLogger.log(Level.INFO, "ExportTxt");
		mLogger.log(Level.INFO, "===================================");
		validatePara(context);
		mLogger.log(Level.CONFIG, toString());

		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		// SqlServiceContext sqlServiceContext =
		// SqlServiceContext.createSqlServiceContext(context);
		mLogger.log(Level.INFO, "get Connection");
		SqlServiceContext ssc = null;
		Statement statment = null;
		ResultSet rs = null;
		PrintWriter pw = null;
		try {
			ssc = databasefactory.createContextWithConnection();
			Connection conn = ssc.getConnection();
			mLogger.log(Level.INFO, "get HttpServletResponse");
			HttpServletResponse response = serviceInstance.getResponse();
			// pw = new PrintWriter(new BufferedWriter(new
			// OutputStreamWriter(response.getOutputStream(), KEY_CHARSET)));
			response.setCharacterEncoding(KEY_CHARSET);
			String file_name = FileName;
			try {
				file_name = new String(FileName.getBytes(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				mLogger.log(Level.INFO, "", e);
			}
			pw = response.getWriter();
			statment = conn.createStatement();
			mLogger.log(Level.INFO, "executeQuery sql" + Sql);
			rs = statment.executeQuery(Sql);
			String[] columns = createColumnProperties(rs);
			int count = 0;
			while (rs.next()) {
				count++;
				for (int i = 0; i < columns.length; i++) {
					Object obj = rs.getObject(columns[i]);
					if (obj == null)
						obj = "";
					pw.append(obj.toString()).append(Separator);
				}
				pw.println();
			}
			if (count == 0) {
				mLogger.log(Level.INFO, "no data found.");
				alertMessage(pw,"没有数据");
			}else{
				response.setContentType("txt");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + file_name + "." + FileExtension+ "\"");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (statment != null)
				statment.close();
			if (ssc != null)
				ssc.freeConnection();
			if (pw != null)
				pw.close();

		}
		mLogger.log(Level.INFO, "done");
	}
	void validatePara(CompositeMap context) {
		if (Sql == null && "".equals(Sql))
			throw new IllegalArgumentException("Sql is undefined");
		Sql = TextParser.parse(Sql, context);
		if (FileName == null && "".equals(FileName))
			throw new IllegalArgumentException("file is undefined");
		FileName = TextParser.parse(FileName, context);
		if (Separator == null && "".equals(Separator))
			throw new IllegalArgumentException("Separator is undefined");
		Separator = TextParser.parse(Separator, context);
		FileExtension = TextParser.parse(FileExtension, context);
	}
	public String toString() {
		CompositeMap invoke = new CompositeMap("txt", "aurora.plugin.export.txt", "export-txt");
		invoke.put("FileName", this.FileName);
		invoke.put("FileExtension", this.FileExtension);
		invoke.put("Separator", this.Separator);
		invoke.put("Sql", this.Sql);
		return invoke.toXML();
	}
	public void initLogger(CompositeMap context) {
		CompositeMap m = context.getRoot();
		this.mLogger = LoggingContext.getLogger(m, LOGGING_TOPIC);
	}
	private String[] createColumnProperties(ResultSet resultSet) throws SQLException {
		ResultSetMetaData resultSetMetaData;
		String[] column_index = null;
		resultSetMetaData = resultSet.getMetaData();
		column_index = new String[resultSetMetaData.getColumnCount()];
		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			column_index[i - 1] = resultSetMetaData.getColumnName(i);
		}
		return column_index;
	}
	private void alertMessage(PrintWriter writer,String message){
		writer.println( "<script language='javascript'>");
		writer.println("alert('"+message+"');");
		writer.println("</script>");
	}
}
