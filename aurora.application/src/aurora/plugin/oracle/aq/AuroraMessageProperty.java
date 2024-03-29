package aurora.plugin.oracle.aq;

import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.STRUCT;
import oracle.jpub.runtime.MutableStruct;

public class AuroraMessageProperty implements ORAData, ORADataFactory
{
  public static final String _SQL_NAME = "AURORA_MESSAGE_PROPERTY";
  public static final int _SQL_TYPECODE = OracleTypes.STRUCT;

  protected MutableStruct _struct;

  protected static int[] _sqlType =  { 12,12 };
  protected static ORADataFactory[] _factory = new ORADataFactory[2];
  protected static final AuroraMessageProperty _AuroraMessagePropertyFactory = new AuroraMessageProperty();

  public static ORADataFactory getORADataFactory()
  { return _AuroraMessagePropertyFactory; }
  /* constructors */
  protected void _init_struct(boolean init)
  { if (init) _struct = new MutableStruct(new Object[2], _sqlType, _factory); }
  public AuroraMessageProperty()
  { _init_struct(true); }
  public AuroraMessageProperty(String key, String value) throws SQLException
  { _init_struct(true);
    setKey(key);
    setValue(value);
  }

  /* ORAData interface */
  public Datum toDatum(Connection c) throws SQLException
  {
    return _struct.toDatum(c, _SQL_NAME);
  }


  /* ORADataFactory interface */
  public ORAData create(Datum d, int sqlType) throws SQLException
  { return create(null, d, sqlType); }
  protected ORAData create(AuroraMessageProperty o, Datum d, int sqlType) throws SQLException
  {
    if (d == null) return null; 
    if (o == null) o = new AuroraMessageProperty();
    o._struct = new MutableStruct((STRUCT) d, _sqlType, _factory);
    return o;
  }
  /* accessor methods */
  public String getKey() throws SQLException
  { return (String) _struct.getAttribute(0); }

  public void setKey(String key) throws SQLException
  { _struct.setAttribute(0, key); }


  public String getValue() throws SQLException
  { return (String) _struct.getAttribute(1); }

  public void setValue(String value) throws SQLException
  { _struct.setAttribute(1, value); }

}
