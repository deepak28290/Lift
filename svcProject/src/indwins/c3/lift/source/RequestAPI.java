package indwins.c3.lift.source;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import indwins.c3.lift.source.Request;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

@Path("/request")
public class RequestAPI 
{  
    @POST
    @Path("/addrequest")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRequest(Request req) throws Exception
    {
    	String requesterType = req.getRequesterType();
    	long requesterID = req.getRequesterID();
    	long accepterID = req.getAccepterID();
		JSONObject result = RequestManagement.addRequest(requesterType, requesterID, accepterID);
		return Response.status(200).entity(result).build();
    }
    
    @POST
    @Path("/acceptrequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response acceptRequest(EntityId req) throws Exception
    {
    	System.out.println("request Id =" + req.getId());
		JSONObject result = RequestManagement.acceptRequest(req.getId());
		return Response.status(200).entity(result).build();
    }
    
    @PUT
    @Path("/cancelrequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelRequest(EntityId request) throws Exception
    {
    	System.out.println("request Id =" + request.getId());
		JSONObject result = RequestManagement.cancelRequest(request.getId());
		return Response.status(200).entity(result).build();
    }
    
    @GET
    @Path("/myrequests")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyRequest(@QueryParam("userID") long uid,
    							 @QueryParam("requestType") String reqType) throws Exception
    {
    	System.out.println("request Id =" + uid);
    	System.out.println("requestType =" + reqType);
		JSONArray result = RequestManagement.getMyRequest(uid, reqType);
		return Response.status(200).entity(result).build();
    }
    
    @GET
    @Path("/getrequestdetails")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestDetails(@QueryParam("id") long reqId) throws Exception
    {
    	System.out.println("request Id =" + reqId);
		JSONObject result = RequestManagement.getRequestDetails(reqId);
		return Response.status(200).entity(result).build();
    }
    
    @POST
    @Path("/finishride")
    @Produces(MediaType.APPLICATION_JSON)
    public Response finishRide(EntityId req) throws Exception
    {
    	System.out.println("request Id =" + req.getId());
		JSONObject result = RequestManagement.finishRide(req.getId(),req.getUserid());
		return Response.status(200).entity(result).build();
    }
    @POST
    @Path("/forfeitride")
    @Produces(MediaType.APPLICATION_JSON)
    public Response forfeitRide(EntityId req) throws Exception
    {
    	System.out.println("request Id =" + req.getId());
		JSONObject result = RequestManagement.forfeitRide(req.getId(),req.getUserid());
		return Response.status(200).entity(result).build();
    }
    
}
