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

@Path("/rating")
public class RatingAPI 
{
    @POST
    @Path("/updaterating")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRating(Rating rating) throws Exception
    {
		JSONObject result = RatingManagement.updateRating(rating);
		return Response.status(200).entity(result).build();
    }
    
    @GET
    @Path("/getrating")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDLStatus(@QueryParam("userID") long userID) throws Exception
    {
    	System.out.println("request Id =" + userID);
		JSONObject result = RatingManagement.getRating(userID);
		return Response.status(200).entity(result).build();
    }
    
}
