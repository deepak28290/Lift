package indwins.c3.lift.source;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

@Path("/user")
public class UserAPI 
{
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() 
	{
		return "Lift Version 1.0";
	}

    @GET
    @Path("/isnewuser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isNewUser(@QueryParam("userID") String userId) throws Exception
    {
    	System.out.println("user Id = " + userId);
		JSONObject result = UserManagement.isNewUser(userId);
		return Response.status(200).entity(result).build();
    }
    
    @POST
    @Path("/updateprofile")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserProfile(User newUser) throws Exception
    {
		JSONObject result = UserManagement.updateUserProfile(newUser);
		return Response.status(200).entity(result).build();
    }
    
    @GET
    @Path("/getprofile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@QueryParam("userID") String userId) throws Exception
    {
    	JSONObject output = UserManagement.getUser(userId);
		return Response.status(200).entity(output).build();
    }
    
    @GET
    @Path("/getnn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNearestUsers(@QueryParam("userType") String userType, 
    		                        @QueryParam("userID") long userID,
    		                        @QueryParam("srcgeocode") String srcgeocode,
    		                        @QueryParam("destgeocode") String destgeocode,
    		                        @QueryParam("starttime") String starttime) throws Exception
    {
    	JSONArray output = UserManagement.getNearestUsers(userType, userID, srcgeocode, destgeocode, starttime  );
		return Response.status(200).entity(output).build();
    }
    
    @POST
    @Path("/addonlineuser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addOnlineUser(OnlineUser olusr) throws Exception
    {
    	long userId = olusr.getUserID();
    	String source = olusr.getSource();
    	String destination = olusr.getDestination();
    	String srcGeoCode = olusr.getSrcGeoCode();
    	String destGeoCode = olusr.getDestGeoCode();
    	String startTime = olusr.getStarttime();
    	String userType = olusr.getUserType();
		JSONObject result = UserManagement.addOnlineUser(userType, userId, source, destination, 
														 srcGeoCode, destGeoCode, startTime);
		return Response.status(200).entity(result).build();
    }
    
    @POST
    @Path("/addappregid")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAppRegID(AppIDRegistration appReg) throws Exception
    {
    	String output;
    	System.out.println("addappregid : userId = " + appReg.getUserID());
    	System.out.println("addappregid : appRegId = " + appReg.getAppRegID());
		output = UserManagement.addRegID(appReg.getUserID(), appReg.getAppRegID());
		return Response.status(200).entity(output).build();
    }
}
