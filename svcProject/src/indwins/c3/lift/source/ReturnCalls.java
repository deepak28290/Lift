package indwins.c3.lift.source;


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
@Path("/v1")
public class ReturnCalls {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() {
		return "Volvo Live 1.0";
	}
	
	@GET
	@Path("/riderAvail")
	@Produces(MediaType.APPLICATION_JSON)
	public String updateRiderAvailability(@QueryParam(value = "user_id") String user_id,
			@QueryParam(value = "source_coord") String source_coord, @QueryParam(value = "destination_coord") String destination_coord,@QueryParam(value = "vehicle_type") String vehicle_type,@QueryParam(value = "start_time") String start_time,@QueryParam(value = "end_time") String end_time){
		
		
		return null;
	
	}
	 
	
}