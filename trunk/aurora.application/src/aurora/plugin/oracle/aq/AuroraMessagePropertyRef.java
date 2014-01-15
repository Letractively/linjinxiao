package aurora.plugin.oracle.aq;

import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.REF;
import oracle.sql.STRUCT;

public class AuroraMessagePropertyRef implements ORAData, ORADataFactory
{
  public static final String _SQL_BASETYPE = "AURORA_MESSAGE_PROPERTY";
  public static final int _SQL_TYPECODE = OracleTypes.REF;

  REF _ref;

private static final AuroraMessagePropertyRef _AuroraMessagePropertyRefFactory = new AuroraMessagePropertyRef();

  public static ORADataFactory getORADataFactory()
  { return _AuroraMessagePropertyRefFactory; }
  /* constructor */
  public AuroraMessagePropertyRef()
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
    AuroraMessagePropertyRef r = new AuroraMessagePropertyRef();
    r._ref = (REF) d;
    return r;
  }

  public static AuroraMessagePropertyRef cast(ORAData o) throws SQLException
  {
     if (o == null) return null;
     try { return (AuroraMessagePropertyRef) getORADataFactory().create(o.toDatum(null), OracleTypes.REF); }
     catch (Exception exn)
     { throw new SQLException("Unable to convert "+o.getClass().getName()+" to AuroraMessagePropertyRef: "+exn.toString()); }
  }

  public AuroraMessageProperty getValue() throws SQLException
  {
     return (AuroraMessageProperty) AuroraMessageProperty.getORADataFactory().create(
       _ref.getSTRUCT(), OracleTypes.REF);
  }

  public void setValue(AuroraMessageProperty c) throws SQLException
  {
    _ref.setValue((STRUCT) c.toDatum(_ref.getJavaSqlConnection()));
  }
}
