package aurora.plugin.oracle.aq;

import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.Datum;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.jpub.runtime.MutableArray;

public class AuroraMessageProperties implements ORAData, ORADataFactory
{
  public static final String _SQL_NAME = "AURORA_MESSAGE_PROPERTIES";
  public static final int _SQL_TYPECODE = OracleTypes.ARRAY;

  MutableArray _array;

private static final AuroraMessageProperties _AuroraMessagePropertiesFactory = new AuroraMessageProperties();

  public static ORADataFactory getORADataFactory()
  { return _AuroraMessagePropertiesFactory; }
  /* constructors */
  public AuroraMessageProperties()
  {
    this((AuroraMessageProperty[])null);
  }

  public AuroraMessageProperties(AuroraMessageProperty[] a)
  {
    _array = new MutableArray(2002, a, AuroraMessageProperty.getORADataFactory());
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
    AuroraMessageProperties a = new AuroraMessageProperties();
    a._array = new MutableArray(2002, (ARRAY) d, AuroraMessageProperty.getORADataFactory());
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
  public AuroraMessageProperty[] getArray() throws SQLException
  {
    return (AuroraMessageProperty[]) _array.getObjectArray(
      new AuroraMessageProperty[_array.length()]);
  }

  public AuroraMessageProperty[] getArray(long index, int count) throws SQLException
  {
    return (AuroraMessageProperty[]) _array.getObjectArray(index,
      new AuroraMessageProperty[_array.sliceLength(index, count)]);
  }

  public void setArray(AuroraMessageProperty[] a) throws SQLException
  {
    _array.setObjectArray(a);
  }

  public void setArray(AuroraMessageProperty[] a, long index) throws SQLException
  {
    _array.setObjectArray(a, index);
  }

  public AuroraMessageProperty getElement(long index) throws SQLException
  {
    return (AuroraMessageProperty) _array.getObjectElement(index);
  }

  public void setElement(AuroraMessageProperty a, long index) throws SQLException
  {
    _array.setObjectElement(a, index);
  }

}
