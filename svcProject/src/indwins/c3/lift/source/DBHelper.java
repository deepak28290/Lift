package indwins.c3.lift.source;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class DBHelper
{

    public static Connection createConnection() throws Exception 
    {
        Connection con = null;
        try 
        {
            Class.forName(DBConstants.dbClass);
            con = DriverManager.getConnection(DBConstants.dbUrl, DBConstants.dbUser, DBConstants.dbPwd);
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
            throw e;
        } 
        return con;
    }

    
    public static JSONObject getDBSingle(String query)
    {
		Connection dbConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		dbConn 	= createConnection();
    		stmt = dbConn.prepareStatement(query);
    		System.out.println("query  input = " + query);
    		rs 	= stmt.executeQuery();
            if (rs.next()) 
            {
            	int total_rows = rs.getMetaData().getColumnCount();
            	for (int i = 0; i < total_rows; i++) 
            	{
            		jsonObject.put(rs.getMetaData().getColumnLabel(i + 1), rs.getObject(i + 1));
                }
            }
    	}
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    	finally 
    	{
    		try
    		{
    			if(rs != null)
    				rs.close();
    			if(stmt !=null)
    				stmt.close();
    			if(dbConn!=null)
    				dbConn.close();
    		}
    		catch (final Exception e) 
    		{
                e.printStackTrace();
            }
    	}
    	System.out.println("query output = " + jsonObject);
       	return jsonObject;
    }

    public static JSONArray getDBMultiple(String query)
    {
		Connection dbConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray jsonArray = new JSONArray();
    	try
    	{
    		dbConn 	= createConnection();
    		stmt = dbConn.prepareStatement(query);
    		System.out.println("query  input = " + query);
    		rs 	= stmt.executeQuery();
    		int total_cols = rs.getMetaData().getColumnCount();
            while (rs.next()) 
            {
            	JSONObject obj = new JSONObject();
            	for (int i = 0; i < total_cols; i++) 
            	{
                    obj.put(rs.getMetaData().getColumnLabel(i + 1), rs.getObject(i + 1));
                    
                }
                jsonArray.put(obj);            	
            }
    	}
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    	finally 
    	{
    		try
    		{
    			if(rs != null)
    				rs.close();
    			if(stmt !=null)
    				stmt.close();
    			if(dbConn!=null)
    				dbConn.close();
    		}
    		catch (final Exception e) 
    		{
                e.printStackTrace();
            }
    	}
    	System.out.println("query output = " + jsonArray);
       	return jsonArray;
    }
    
	public static int addDBSingle(String query) throws SQLException
	{
		Connection dbConn = null;
		PreparedStatement stmt = null;
		int count = -1;
		try
    	{
    		dbConn 	= createConnection();
    		stmt = dbConn.prepareStatement(query);
    		System.out.println("query  input = "+query);
    		count = stmt.executeUpdate();
    	}
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    	finally 
    	{
    		try
    		{
    			if(stmt !=null)
    				stmt.close();
    			if(dbConn!=null)
    				dbConn.close();
    		}
    		catch (final Exception e) 
    		{
                e.printStackTrace();
            }
    	}
		System.out.println("query output : " + count + "records affected");
       	return count;
	}

	public static JSONObject addReturnDBSingle(String query, String[] idcol) throws SQLException
    {
		Connection dbConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int idcolval = -1;
    	JSONObject obj = new JSONObject();
		try
    	{
    		dbConn 	= createConnection();
    		stmt = dbConn.prepareStatement(query,PreparedStatement.RETURN_GENERATED_KEYS);
    		System.out.println("query  input = " + query);
    		stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) 
            {
                idcolval = (int) rs.getInt(1);
            	obj.put(idcol[0], idcolval);
            }
    	}
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    	finally 
    	{
    		try
    		{
    			if(rs != null)
    				rs.close();
    			if(stmt !=null)
    				stmt.close();
    			if(dbConn!=null)
    				dbConn.close();
    		}
    		catch (final Exception e) 
    		{
                e.printStackTrace();
            }
    	}
    	System.out.println("query output = " + obj);
       	return obj;
    }
	public static JSONObject executePreparedStatement(PreparedStatement pre) throws SQLException
	{
		JSONObject resObj = new JSONObject();
		pre.executeUpdate();
		return resObj;
	}
}