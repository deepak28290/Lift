package a.service.proj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import a.service.data.LocationDetails;


public class DBConnectionHelper {


    @SuppressWarnings("finally")
    public static Connection createConnection() throws Exception {
        Connection con = null;
        try {
            Class.forName(DataBaseConstants.dbClass);
            con = DriverManager.getConnection(DataBaseConstants.dbUrl, DataBaseConstants.dbUser, DataBaseConstants.dbPwd);
        } catch (Exception e) {
            throw e;
        } finally {
            return con;
        }
    }
    
    /*
     * 
     * Method to return list of same stream potential guides
     */
    
    public static Set<String> getPotentialGuides(String upstream, String busNumber) throws SQLException{
    	Set<String> potentailGuideSet = new HashSet<String>();
    	 Connection dbConn = null;
    	try{
    		dbConn = createConnection();
    	} catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    	try{
    		Statement stmt = dbConn.createStatement();
            String query = "SELECT * FROM volvo_table WHERE bus_num_req ='" +busNumber +  "' and upstream ='" +upstream +"' and is_onboard='" + "1';";
            System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
            	potentailGuideSet.add(rs.getString("current_coord"));
            }
    	}catch(Exception e){
    		if (dbConn != null) {
                dbConn.close();
            }
    	} finally{
    		if (dbConn != null) {
                dbConn.close();
            }
    	}
    	
    	return potentailGuideSet;
    }
    
    public static LocationDetails setLocationFromArea(String stopName) throws SQLException{
    	
    	LocationDetails location = null;
    	Connection dbConn = null;
    	try{
    		dbConn = createConnection();
    	} catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    	
    	try{
    		Statement stmt = dbConn.createStatement();
            String query = "SELECT * FROM volvo_bus_routes WHERE busstop_name ='" +stopName +"' ;";
            System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
            	location = new LocationDetails();
            	location.setLatitude(Double.valueOf(rs.getString("latitude")));
            	location.setLongitude(Double.valueOf(rs.getString("longitude")));
            }
    	}catch(Exception e){
    		if (dbConn != null) {
                dbConn.close();
            }
    	} finally{
    		if (dbConn != null) {
                dbConn.close();
            }
    	}
    	return location;

    }
    
    public static Integer getStopNumber(String stopValue) throws SQLException{

    	Integer stopNumber = null;

    	Connection dbConn = null;
    	try{
    		dbConn = createConnection();
    	} catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    	try{
    		Statement stmt = dbConn.createStatement();
    		String query = "SELECT * FROM volvo_bus_routes WHERE busstop_name ='" +stopValue +"' ;";
    		System.out.println(query);
    		ResultSet rs = stmt.executeQuery(query);
    		while(rs.next()){
    			stopNumber = Integer.parseInt(rs.getString("Serial Number"));
    		}
    	}catch(Exception e){
    		if (dbConn != null) {
    			dbConn.close();
    		}
    	} finally{
    		if (dbConn != null) {
    			dbConn.close();
    		}
    	}
    	return stopNumber;
    }
}
