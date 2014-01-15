package aurora.plugin.oracle.aq;

import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.REF;
import oracle.sql.STRUCT;

public class AuroraMessageRef implements ORAData, ORADataFactory
{
  public static final String _SQL_BASETYPE = "AURORA_MESSAGE";
  public static final int _SQL_TYPECODE = OracleTypes.REF;

  REF _ref;

private static final AuroraMessageRef _aurora_messageRefFactory = new AuroraMessageRef();

  public static ORADataFactory getORADataFactory()
  { return _aurora_messageRefFactory; }
  /* constructor */
  public AuroraMessageRef()
  {
  }

  /* ORAData interface */
  public Datum toDatum(Connection c) throws SQLException
  {
    return _ref;
  }

  /* ORADataFactory interface */
  public ORAData create(Datum d, int sqlType) throws SQLException
  {
    if (d == null) return null; 
    AuroraMessageRef r = new AuroraMessageRef();
    r._ref = (REF) d;
    return r;
  }

  public static AuroraMessageRef cast(ORAData o) throws SQLException
  {
     if (o == null) return null;
     try { return (AuroraMessageRef) getORADataFactory().create(o.toDatum(null), OracleTypes.REF); }
     catch (Exception exn)
     { throw new SQLException("Unable to convert "+o.getClass().getName()+" to aurora_messageRef: "+exn.toString()); }
  }

  public AuroraMessage getValue() throws SQLException
  {
     return (AuroraMessage) AuroraMessage.getORADataFactory().create(
       _ref.getSTRUCT(), OracleTypes.REF);
  }

  public void setValue(AuroraMessage c) throws SQLException
  {
    _ref.setValue((STRUCT) c.toDatum(_ref.getJavaSqlConnection()));
  }
}
