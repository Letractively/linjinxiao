package aurora.application.features.oraclestreams_bak;
import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.jpub.runtime.MutableArray;

public class Streams_dml_lcr_column_list implements ORAData, ORADataFactory
{
  public static final String _SQL_NAME = "STREAMS_DML_LCR_COLUMN_LIST";
  public static final int _SQL_TYPECODE = OracleTypes.ARRAY;

  MutableArray _array;

  private static final Streams_dml_lcr_column_list _StreamsDmlLcrColumnListFactory = new Streams_dml_lcr_column_list();

  public static ORADataFactory getORADataFactory()
  { return _StreamsDmlLcrColumnListFactory; }
  /* constructors */
  public Streams_dml_lcr_column_list()
  {
    this((Streams_dml_lcr_column[])null);
  }

  public Streams_dml_lcr_column_list(Streams_dml_lcr_column[] a)
  {
    _array = new MutableArray(2002, a, Streams_dml_lcr_column.getORADataFactory());
  }

  /* ORAData interface */
  public Datum toDatum(Connection c) throws SQLException
  {
    return _array.toDatum(c, _SQL_NAME);
  }

  /* ORADataFactory interface */
  public ORAData create(Datum d, int sqlType) throws SQLException
  {
    if (d == null) return null;
    Streams_dml_lcr_column_list a = new Streams_dml_lcr_column_list();
    a._array = new MutableArray(2002, (ARRAY) d, Streams_dml_lcr_column.getORADataFactory());
    return a;
  }

  public int length() throws SQLException
  {
    return _array.length();
  }

  public int getBaseType() throws SQLException
  {
    return _array.getBaseType();
  }

  public String getBaseTypeName() throws SQLException
  {
    return _array.getBaseTypeName();
  }

  public ArrayDescriptor getDescriptor() throws SQLException
  {
    return _array.getDescriptor();
  }

  /* array accessor methods */
  public Streams_dml_lcr_column[] getArray() throws SQLException
  {
    return (Streams_dml_lcr_column[]) _array.getObjectArray(
      new Streams_dml_lcr_column[_array.length()]);
  }

  public void setArray(Streams_dml_lcr_column[] a) throws SQLException
  {
    _array.setObjectArray(a);
  }

  public Streams_dml_lcr_column[] getArray(long index, int count) throws SQLException
  {
    return (Streams_dml_lcr_column[]) _array.getObjectArray(index,
      new Streams_dml_lcr_column[_array.sliceLength(index, count)]);
  }

  public void setArray(Streams_dml_lcr_column[] a, long index) throws SQLException
  {
    _array.setObjectArray(a, index);
  }

  public Streams_dml_lcr_column getElement(long index) throws SQLException
  {
    return (Streams_dml_lcr_column) _array.getObjectElement(index);
  }

  public void setElement(Streams_dml_lcr_column a, long index) throws SQLException
  {
    _array.setObjectElement(a, index);
  }

}
