package aurora.plugin.oracle.aq;

import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.STRUCT;
import oracle.jpub.runtime.MutableStruct;

public class AuroraMessage implements ORAData, ORADataFactory
{
  public static final String _SQL_NAME = "AURORA_MESSAGE";
  public static final int _SQL_TYPECODE = OracleTypes.STRUCT;

  protected MutableStruct _struct;

  protected static int[] _sqlType =  { 12,2003 };
  protected static ORADataFactory[] _factory = new ORADataFactory[2];
  static
  {
    _factory[1] = AuroraMessageProperties.getORADataFactory();
  }
  protected static final AuroraMessage _aurora_messageFactory = new AuroraMessage();

  public static ORADataFactory getORADataFactory()
  { return _aurora_messageFactory; }
  /* constructors */
  protected void _init_struct(boolean init)
  { if (init) _struct = new MutableStruct(new Object[2], _sqlType, _factory); }
  public AuroraMessage()
  { _init_struct(true); }
  public AuroraMessage(String message, AuroraMessageProperties properties) throws SQLException
  { _init_struct(true);
    setMessage(message);
    setProperties(properties);
  }

  /* ORAData interface */
  public Datum toDatum(Connection c) throws SQLException
  {
    return _struct.toDatum(c, _SQL_NAME);
  }


  /* ORADataFactory interface */
  public ORAData create(Datum d, int sqlType) throws SQLException
  { return create(null, d, sqlType); }
  protected ORAData create(AuroraMessage o, Datum d, int sqlType) throws SQLException
  {
    if (d == null) return null; 
    if (o == null) o = new AuroraMessage();
    o._struct = new MutableStruct((STRUCT) d, _sqlType, _factory);
    return o;
  }
  /* accessor methods */
  public String getMessage() throws SQLException
  { return (String) _struct.getAttribute(0); }

  public void setMessage(String message) throws SQLException
  { _struct.setAttribute(0, message); }


  public AuroraMessageProperties getProperties() throws SQLException
  { return (AuroraMessageProperties) _struct.getAttribute(1); }

  public void setProperties(AuroraMessageProperties properties) throws SQLException
  { _struct.setAttribute(1, properties); }

}
