package indwins.c3.lift.source;

import indwin.c3.lift.util.PotentialGuideResponse;
import indwin.c3.lift.util.ResponseStatus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jettison.json.JSONArray;


@XmlRootElement
@Path("/volvolive")
public class ReturnCalls {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() {
		return "Volvo Live 1.0";
	}
	
	@GET
	@Path("/nearestBus")
	@Produces(MediaType.APPLICATION_JSON)
	public PotentialGuideResponse getNearestBusesForLocation(@QueryParam(value = "busnum") String busNumber,
			@QueryParam(value = "currentStop") String currentStop, @QueryParam(value = "destinationStop") String destinationStop){
		
		try{
			return BusServiceHelper.getAllNearestBuses(busNumber, currentStop,destinationStop);
		}catch(Exception e){
			PotentialGuideResponse response = new PotentialGuideResponse();
			response.setResponseStatus(ResponseStatus.FAILURE);
			return response;
		}
	}
	 
	 @GET
	  @Path("/firstUser")
	  @Produces(MediaType.APPLICATION_JSON)
	  @Consumes(MediaType.APPLICATION_JSON)
	  public String crtUserData(@QueryParam(value = "userid") String userid,@QueryParam(value = "boardingpoint") String boardingpoint,@QueryParam(value = "droppoint") String droppoint,@QueryParam(value = "busnumb") String busnumb,@QueryParam(value = "currentcoord") String currentcoord) throws ClientProtocolException, IOException, SQLException {
		String boardcoord;
		String dropcoord;
		boardcoord=VolvoUtil.getCoordFromLoc(boardingpoint);
		dropcoord=VolvoUtil.getCoordFromLoc(droppoint);
		
		 Connection connection = null;
			Statement stmt = null;
			
			String sql = new String();
		 try {
				// the sql server driver string
				Class.forName("com.mysql.jdbc.Driver");

				// the sql server url
				String url = "jdbc:mysql://db4free.net:3306/volvolive";
				
				// get the sql server database connection
				connection = DriverManager.getConnection(url, "deepak111", "deepak");
				String getSql;
				stmt = connection.createStatement();
				String up=VolvoUtil.isUpStream(boardingpoint, droppoint);
				System.out.println(sql);
				//String getSql = "INSERT INTO volvo_table VALUES('deepakId','Marathalli','Kadugodi','100,100','120,120','111,111','22E','0','','')";
				sql="SELECT * from volvo_table WHERE user_id='"+userid+"'";
				ResultSet rs=stmt.executeQuery(sql);
				if(!rs.next()){
				getSql = "INSERT INTO volvo_table VALUES('"+userid+"','"+boardingpoint+"','"+droppoint+"','"+boardcoord+"','"+dropcoord+"','"+currentcoord+"','"+busnumb+"','0',null,'"+up+"')";
				System.out.println(getSql);
				stmt.executeUpdate(getSql);
			
				}else{
					
					getSql = "DELETE FROM volvo_table WHERE user_id='"+userid+"'";
					stmt.execute(getSql);
					getSql="INSERT INTO volvo_table VALUES('"+userid+"','"+boardingpoint+"','"+droppoint+"','"+boardcoord+"','"+dropcoord+"','"+currentcoord+"','"+busnumb+"','0',null,'"+up+"')";
					stmt.execute(getSql);
					stmt.close();
				}
						
					}catch(Exception e){
						e.printStackTrace();
						stmt.close();
						return "fail";
					}
		 System.out.println(userid);	
		  	 return userid;
	 }
	 
	 @GET
	  @Path("/updateCoords")
	  @Produces(MediaType.APPLICATION_JSON)
	  @Consumes(MediaType.APPLICATION_JSON)
	  public String updCurrCoord(@QueryParam(value = "coord") String coord,@QueryParam(value = "userid") String userid) throws ClientProtocolException, IOException {
			
			 Connection connection = null;
				Statement stmt = null;
				
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
					String getSql = "UPDATE volvo_table SET current_coord='"+coord+"' WHERE user_id='"+userid+"'";
					//String getSql2 = "INSERT INTO volvo_table VALUES('"+userid+"','"+boardingpoint+"','"+droppoint+"','100,100','120,120','"+busnumb+"','0',null)";
					stmt.executeUpdate(getSql);
					// STEP 5: Extract data from result set
//					System.out.println(memcached.get(""));
				
							
						}catch(Exception e){
							e.printStackTrace();
							return "fail";
						}
			 
			 System.out.println(userid);	
		  	 return userid;
	 }
	  
}