/**
 * Created on: 2009-11-09 14:56:50
 * Author:     zhoufan
 */
package uncertain.datatype;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.io.IOException;
import java.io.Reader;

import org.xml.sax.SAXException;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.XMLOutputter;

/**
 * 
 */
public class CompositeMapType extends AbstractDataType implements DataType {

	/**
	 * Constructor for IntegerType.
	 */
	public CompositeMapType() {
	}

	/**
	 * @see uncertain.datatype.DataType#getJavaType()
	 */
	public Class getJavaType() {
	    return CompositeMap.class;
	}

	/**
	 * @see uncertain.datatype.DataType#getSqlType()
	 */
	public int getSqlType() {
	    return Types.VARCHAR;
	}
	
	public String clobToString(Clob clob) throws SQLException {
	    if( clob == null) return null;
	    try{
	        StringBuffer buf = new StringBuffer();
	        Reader reader = clob.getCharacterStream();
	        if( reader == null) return null;
	        int n;
	        while( ( n = reader.read()) != -1) buf.append((char)n);
	        return buf.toString();
	    } catch(IOException ex){
	        ex.printStackTrace();
	        return null;
	    }
	}

	/**
	 * @see uncertain.datatype.DataType#getObject(CallableStatement, int)
	 */
	public Object getObject(CallableStatement stmt, int id)
		throws SQLException {
		String content = stmt.getString(id);
		if(content == null)
			return null;
		CompositeLoader cl = new CompositeLoader();
	    try {
			return cl.loadFromString(content,"UTF-8");
		} catch (IOException e) {
			throw new SQLException(e.getLocalizedMessage());
		} catch (SAXException e) {
			throw new SQLException(e.getLocalizedMessage());
		}
	}

	/**
	 * @see uncertain.datatype.DataType#getObject(ResultSet, int)
	 */

	public Object getObject(ResultSet rs, int id) throws SQLException {
//	    Clob clob = rs.getClob(id);
	    String content = rs.getString(id);
//	    String content = clobToString(clob);
		if(content == null)
			return null;
		CompositeLoader cl = new CompositeLoader();
	    try {
			return cl.loadFromString(content,"UTF-8");
		} catch (IOException e) {
			throw new SQLException(e.getLocalizedMessage());
		} catch (SAXException e) {
			throw new SQLException(e.getLocalizedMessage());
		}
	}

	/**
	 * @see uncertain.datatype.DataType#registerParameter(CallableStatement, int)
	 */
	public void registerParameter(CallableStatement stmt, int id)
		throws SQLException {
		stmt.registerOutParameter(id, Types.VARCHAR);
	}

    public void setParameter (PreparedStatement stmt, int id, Object value ) throws SQLException{
        if(value==null)
            stmt.setNull(id, getSqlType());
        else{
            if(value instanceof CompositeMap)
                stmt.setString(id, XMLOutputter.defaultInstance().toXML((CompositeMap)value));
            else
                stmt.setString(id, value.toString());
        }
    }     
	/**
	 * @see uncertain.datatype.DataType#convert(Object)
	 */
	public Object convert(Object value)
        throws ConvertionException
    {
		if(value == null)
			return null;
		if(value instanceof String){
			CompositeLoader cl = new CompositeLoader();
		    try {
				return cl.loadFromString((String)value,"UTF-8");
			} catch (IOException e) {
				throw new ConvertionException(e.getLocalizedMessage());
			} catch (SAXException e) {
				throw new ConvertionException(e.getLocalizedMessage());
			}
		}
        else
            return value;
	}

}
