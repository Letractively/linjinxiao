package aurora.application.features.oraclestreams_bak;
import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.REF;
import oracle.sql.STRUCT;

public class Streams_dml_lcr_column_ref implements ORAData, ORADataFactory
{
  public static final String _SQL_BASETYPE = "STREAMS_DML_LCR_COLUMN";
  public static final int _SQL_TYPECODE = OracleTypes.REF;

  REF _ref;

  private static final Streams_dml_lcr_column_ref _StreamsDmlLcrColumnRefFactory = new Streams_dml_lcr_column_ref();

  public static ORADataFactory getORADataFactory()
  { return _StreamsDmlLcrColumnRefFactory; }
  /* constructor */
  public Streams_dml_lcr_column_ref()
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
    Streams_dml_lcr_column_ref r = new Streams_dml_lcr_column_ref();
    r._ref = (REF) d;
    return r;
  }

  public Streams_dml_lcr_column getValue() throws SQLException
  {
     return (Streams_dml_lcr_column) Streams_dml_lcr_column.getORADataFactory().create(
       _ref.getSTRUCT(), OracleTypes.REF);
  }

  public void setValue(Streams_dml_lcr_column c) throws SQLException
  {
    _ref.setValue((STRUCT) c.toDatum(_ref.getJavaSqlConnection()));
  }
}
