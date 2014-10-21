package indwins.c3.lift.source;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VolvoUtil {

	public static String BUS_NUM_REQ="335E";
	public static String isUpStream(String loc1,String loc2){
		
		 Connection connection = null;
			Statement stmt = null;
			String lat="";
			String longt="";
			String coord;
			String sql = new String();
			String s1="";
			String s2="";
		 try {
				// the sql server driver string
				Class.forName("com.mysql.jdbc.Driver");

				// the sql server url
				String url = "jdbc:mysql://db4free.net:3306/volvolive";
				
				// get the sql server database connection
				connection = DriverManager.getConnection(url, "deepak111", "deepak");

				stmt = connection.createStatement();

				System.out.println(sql);
				String getSql = "SELECT * from volvo_bus_routes WHERE busstop_name='"+loc1+"'";
				//String getSql2 = "INSERT INTO volvo_table VALUES('"+userid+"','"+boardingpoint+"','"+droppoint+"','100,100','120,120','"+busnumb+"','0',null)";
			ResultSet rs=	stmt.executeQuery(getSql);
			if (rs.next()) {
			s1=rs.getString("Serial Number");
		
			}
			 getSql = "SELECT * from volvo_bus_routes WHERE busstop_name='"+loc2+"'";
			//String getSql2 = "INSERT INTO volvo_table VALUES('"+userid+"','"+boardingpoint+"','"+droppoint+"','100,100','120,120','"+busnumb+"','0',null)";
		rs=	stmt.executeQuery(getSql);
		if (rs.next()) {
		s2=rs.getString("Serial Number");
	
		}
		
		if(Integer.parseInt(s1)-Integer.parseInt(s2)>0){
			System.out.println(Integer.parseInt(s1)-Integer.parseInt(s2));
			return "1";
		}else{
			return "0";
		}
				// STEP 5: Extract data from result set
//				System.out.println(memcached.get(""));
			
						
					}catch(Exception e){
						e.printStackTrace();
						return "1";
					}
	
	}
	public static String getCoordFromLoc(String loc){
		 Connection connection = null;
			Statement stmt = null;
			String lat="";
			String longt="";
			String coord;
			String sql = new String();
		 try {
				// the sql server driver string
				Class.forName("com.mysql.jdbc.Driver");

				// the sql server url
				String url = "jdbc:mysql://db4free.net:3306/volvolive";
				
				// get the sql server database connection
				connection = DriverManager.getConnection(url, "deepak111", "deepak");

				stmt = connection.createStatement();

				System.out.println(sql);
				String getSql = "SELECT * from volvo_bus_routes WHERE busstop_name='"+loc+"'";
				//String getSql2 = "INSERT INTO volvo_table VALUES('"+userid+"','"+boardingpoint+"','"+droppoint+"','100,100','120,120','"+busnumb+"','0',null)";
			ResultSet rs=	stmt.executeQuery(getSql);
			if (rs.next()) {
			lat=rs.getString("latitude");
			lat.replaceAll("\r", "");
			lat.replaceAll("\n", "");
			longt=rs.getString("longitude");
			longt.replaceAll("\r", "");
			longt.replaceAll("\n", "");
			longt.trim();
			lat.trim();
			}
				// STEP 5: Extract data from result set
//				System.out.println(memcached.get(""));
			
						
					}catch(Exception e){
						e.printStackTrace();
						return "fail";
					}
		loc=lat+","+longt;
		return loc;
	}
	public static void main(String[] args) throws SQLException{
	//	System.out.println(getCoordFromLoc("Majestic"));
		onBoard("12.9569472,77.7015352", "deepak12345");
	}
			
	public static String onBoard(String currCoord,String userid) throws SQLException{
		Connection connection = null;
			Statement stmt = null;
			String lat="";
			String longt="";
			String coord;
			String sql = new String();
			String bp="";
		 try {
				// the sql server driver string
				Class.forName("com.mysql.jdbc.Driver");
				
				// the sql server url
				String url = "jdbc:mysql://db4free.net:3306/volvolive";
				
				// get the sql server database connection
				connection = DriverManager.getConnection(url, "deepak111", "deepak");
				
				stmt = connection.createStatement();
				
				System.out.println(sql);
				sql="SELECT * from volvo_table WHERE user_id='"+userid+"'";
				ResultSet rs=	stmt.executeQuery(sql);
				if (rs.next()) {
				bp=rs.getString("boarding_coord");
				}
				rs.close();
				if(bp.equals(currCoord)){
					
					sql="SELECT * from volvo_table WHERE current_coord='"+currCoord+"' AND is_onboard='1'";
					
					rs=	stmt.executeQuery(sql);
					while(rs.next()){
						int i=0;
						if(rs.getString("current_coord").equals(currCoord)&&rs.getString("bus_num_onb").equals(BUS_NUM_REQ)){
							for(i=0;rs.getString("current_coord").equals(currCoord)&&rs.getString("bus_num_onb").equals(BUS_NUM_REQ)&&i<60;i++){
								Thread.sleep(10000);
							}
							if(i==60){
								i=0;
								rs.close();
								
								sql="UPDATE volvo_table SET is_onboard='1',bus_numb_onboard WHERE user_id='"+userid+"'";
								stmt.execute(sql);
								stmt.close();
								System.out.println("aaaa");
								break;
							}
						}
						
					}
						
				
				}else{
					
					stmt.close();
				}
				//String getSql2 = "INSERT INTO volvo_table VALUES('"+userid+"','"+boardingpoint+"','"+droppoint+"','100,100','120,120','"+busnumb+"','0',null)";
		 }catch(Exception e){
			 e.printStackTrace();
			 stmt.close();
		 }
		
		return null;
	}
	
}
