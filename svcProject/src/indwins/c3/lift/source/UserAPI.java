package indwins.c3.lift.source;

import java.io.InputStream;
import java.sql.SQLException;

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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

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
    
    @POST
    @Path("/updatephoto")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updatePhoto(@FormDataParam("fbuserID") String fbuserID,
    							@FormDataParam("docType") String docType,
    							@FormDataParam("imagefile") InputStream imageFileInputStream,
    							@FormDataParam("imagefile") FormDataContentDisposition fileDetail) throws Exception
    {
    	System.out.println("fbuserId = "+ fbuserID);
    	System.out.println("docType = "+ docType);
    	System.out.println("InputStream = "+ imageFileInputStream);
    	System.out.println("FormDataContentDisposition = "+ fileDetail);
		JSONObject result = UserManagement.updateUserPhoto(fbuserID, docType, imageFileInputStream);
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
    @Path("/getphoto")
    @Produces("image/*")
    public Response getImage(@QueryParam("userID") String userID,
    						 @QueryParam("docType") String docType) throws SQLException 
    {
    	InputStream output = UserManagement.getPhoto(userID, docType);
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
    
    @POST
    @Path("/sendemailverifycode")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendEmailVerifyCode(EntityId user) throws Exception
    {
    	JSONObject output;
    	System.out.println("verifyEmail : userId = " + user.getId());
		output = UserManagement.sendEmailVerifyCode(user.getId());
		return Response.status(200).entity(output).build();
    }
    
    @POST
    @Path("/verifyemail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyEmail(VerifyUser user) throws Exception
    {
    	JSONObject output;
    	System.out.println("verifyEmail : userId = " + user.getFbuserID());
    	System.out.println("verifyEmail : code   = " + user.getCode());
		output = UserManagement.verifyEmail(user.getFbuserID(), user.getCode());
		return Response.status(200).entity(output).build();
    }
    
    @POST
    @Path("/sendphoneverifycode")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendPhoneVerifyCode(EntityId user) throws Exception
    {
    	JSONObject output;
    	System.out.println("verifyEmail : userId = " + user.getId());
		output = UserManagement.sendPhoneVerifyCode(user.getId());
		return Response.status(200).entity(output).build();
    }
    
    @POST
    @Path("/verifyphone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyPhone(VerifyUser user) throws Exception
    {
    	JSONObject output;
    	System.out.println("verifyEmail : userId = " + user.getFbuserID());
    	System.out.println("verifyEmail : code   = " + user.getCode());
		output = UserManagement.verifyPhone(user.getFbuserID(), user.getCode());
		return Response.status(200).entity(output).build();
    }
    
    @GET
    @Path("/getverificationstatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVerificatonStatus(@QueryParam("userID") long userID) throws Exception
    {
    	System.out.println("request Id =" + userID);
		JSONObject result = UserManagement.getVerificatonStatus(userID);
		return Response.status(200).entity(result).build();
    }
    
    @GET
    @Path("/ispanverified")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPANStatus(@QueryParam("userID") long userID) throws Exception
    {
    	System.out.println("request Id =" + userID);
		JSONObject result = UserManagement.getPANStatus(userID);
		return Response.status(200).entity(result).build();
    }
    
    @POST
    @Path("/verifypan")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyPAN(VerifyUser user) throws Exception
    {
    	JSONObject output;
    	System.out.println("verifyEmail : userId = " + user.getFbuserID());
    	System.out.println("verifyEmail : code   = " + user.getCode());
		output = UserManagement.verifyPAN(user.getFbuserID(), user.getCode());
		return Response.status(200).entity(output).build();
    }
    
    @GET
    @Path("/isdlverified")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDLStatus(@QueryParam("userID") long userID) throws Exception
    {
    	System.out.println("request Id =" + userID);
		JSONObject result = UserManagement.getDLStatus(userID);
		return Response.status(200).entity(result).build();
    }
    
    @POST
    @Path("/verifydl")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyDL(VerifyUser user) throws Exception
    {
    	JSONObject output;
    	System.out.println("verifyEmail : userId = " + user.getFbuserID());
    	System.out.println("verifyEmail : code   = " + user.getCode());
		output = UserManagement.verifyDL(user.getFbuserID(), user.getCode());
		return Response.status(200).entity(output).build();
    }
    
    @GET
    @Path("/haslockedride")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hasLockedRide(@QueryParam("userID") String userID,
    							  @QueryParam("usertype") String userType) throws Exception
    {
    	System.out.println("request Id =" + userID);
		JSONObject result = UserManagement.hasLockedRide(userID, userType);
		return Response.status(200).entity(result).build();
    }
}
