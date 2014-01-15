package aurora.application.features.oraclestreams_bak;
import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.STRUCT;
import oracle.jpub.runtime.MutableStruct;

public class Streams_dml_lcr implements ORAData, ORADataFactory
{
  public static final String _SQL_NAME = "STREAMS_DML_LCR";
  public static final int _SQL_TYPECODE = OracleTypes.STRUCT;

  protected MutableStruct _struct;

  private static int[] _sqlType =  { 12,12,12,2003 };
  private static ORADataFactory[] _factory = new ORADataFactory[4];
  static
  {
    _factory[3] = Streams_dml_lcr_column_list.getORADataFactory();
  }
  protected static final Streams_dml_lcr _streams_dml_lcrFactory = new Streams_dml_lcr(false);

  public static ORADataFactory getORADataFactory()
  { return _streams_dml_lcrFactory; }
  /* constructor */
  protected Streams_dml_lcr(boolean init)
  { if(init) _struct = new MutableStruct(new Object[4], _sqlType, _factory); }
  public Streams_dml_lcr()
  { this(true); }

  /* ORAData interface */
  public Datum toDatum(Connection c) throws SQLException
  {
    return _struct.toDatum(c, _SQL_NAME);
  }

  /* ORADataFactory interface */
  public ORAData create(Datum d, int sqlType) throws SQLException
  { return create(null, d, sqlType); }
  protected ORAData create(Streams_dml_lcr o, Datum d, int sqlType) throws SQLException
  {
    if (d == null) return null; 
    if (o == null) o = new Streams_dml_lcr(false);
    o._struct = new MutableStruct((STRUCT) d, _sqlType, _factory);
    return o;
  }
  /* accessor methods */
  public String getOwner() throws SQLException
  { return (String) _struct.getAttribute(0); }

  public void setOwner(String owner) throws SQLException
  { _struct.setAttribute(0, owner); }


  public String getObjectName() throws SQLException
  { return (String) _struct.getAttribute(1); }

  public void setObjectName(String objectName) throws SQLException
  { _struct.setAttribute(1, objectName); }


  public String getCommandType() throws SQLException
  { return (String) _struct.getAttribute(2); }

  public void setCommandType(String commandType) throws SQLException
  { _struct.setAttribute(2, commandType); }


  public Streams_dml_lcr_column_list getPkColumnList() throws SQLException
  { return (Streams_dml_lcr_column_list) _struct.getAttribute(3); }

  public void setPkColumnList(Streams_dml_lcr_column_list pkColumnList) throws SQLException
  { _struct.setAttribute(3, pkColumnList); }

}
