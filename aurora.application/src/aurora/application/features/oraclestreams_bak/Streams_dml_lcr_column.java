package aurora.application.features.oraclestreams_bak;
import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.STRUCT;
import oracle.jpub.runtime.MutableStruct;

public class Streams_dml_lcr_column implements ORAData, ORADataFactory
{
  public static final String _SQL_NAME = "STREAMS_DML_LCR_COLUMN";
  public static final int _SQL_TYPECODE = OracleTypes.STRUCT;

  protected MutableStruct _struct;

  private static int[] _sqlType =  { 12,12,12,12 };
  private static ORADataFactory[] _factory = new ORADataFactory[4];
  protected static final Streams_dml_lcr_column _StreamsDmlLcrColumnFactory = new Streams_dml_lcr_column(false);

  public static ORADataFactory getORADataFactory()
  { return _StreamsDmlLcrColumnFactory; }
  /* constructor */
  protected Streams_dml_lcr_column(boolean init)
  { if(init) _struct = new MutableStruct(new Object[4], _sqlType, _factory); }
  public Streams_dml_lcr_column()
  { this(true); }

  /* ORAData interface */
  public Datum toDatum(Connection c) throws SQLException
  {
    return _struct.toDatum(c, _SQL_NAME);
  }

  /* ORADataFactory interface */
  public ORAData create(Datum d, int sqlType) throws SQLException
  { return create(null, d, sqlType); }
  protected ORAData create(Streams_dml_lcr_column o, Datum d, int sqlType) throws SQLException
  {
    if (d == null) return null; 
    if (o == null) o = new Streams_dml_lcr_column(false);
    o._struct = new MutableStruct((STRUCT) d, _sqlType, _factory);
    return o;
  }
  /* accessor methods */
  public String getColumnName() throws SQLException
  { return (String) _struct.getAttribute(0); }

  public void setColumnName(String columnName) throws SQLException
  { _struct.setAttribute(0, columnName); }


  public String getColumnType() throws SQLException
  { return (String) _struct.getAttribute(1); }

  public void setColumnType(String columnType) throws SQLException
  { _struct.setAttribute(1, columnType); }


  public String getValue() throws SQLException
  { return (String) _struct.getAttribute(2); }

  public void setValue(String value) throws SQLException
  { _struct.setAttribute(2, value); }


  public String getNewValueFlag() throws SQLException
  { return (String) _struct.getAttribute(3); }

  public void setNewValueFlag(String newValueFlag) throws SQLException
  { _struct.setAttribute(3, newValueFlag); }

}
