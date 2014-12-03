package indwins.c3.lift.source;

import java.sql.SQLException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RatingManagement 
{

	public static JSONObject updateRating(Rating rating) throws JSONException 
	{
		JSONObject resObj = new JSONObject();
		String ratedByUserID = rating.getRatedByUser();
		String ratedUserID = rating.getRatedUser();
		String scoreString = rating.getRating();
		String comment = rating.getComment();
		double score = Math.round(Double.valueOf(scoreString)*10.0)/10.0;
		String ratingQuery = "insert into rating_details (fbuserID_rated, fbuserID_ratedby, rating_score, rating_comment, rating_time ) " +
							 "values (\""+ ratedUserID +"\", \"" + ratedByUserID + "\",\"" + score + "\",\"" + comment + "\"," + System.currentTimeMillis() +")";
		int rcount;
		try 
		{
			rcount = DBHelper.addDBSingle(ratingQuery);
			if(rcount > 0)
			{
				String scoreQuery = "";
				double oldScore = 5.0;
				int count = 1;
				String summaryQuery = "select rating_score, rating_count from rating_summary where fbuserID = \"" + ratedUserID + "\"";
				JSONObject summaryObj = DBHelper.getDBSingle(summaryQuery);
				if (summaryObj.length() > 0)
				{
					oldScore = summaryObj.getDouble("rating_score");
					count = summaryObj.getInt("rating_count");
					double newScore = Math.round((((oldScore*count)+score)/++count)*10000000000000.0)/10000000000000.0;
					scoreQuery = "update rating_summary set " +
								  "rating_score = \"" + newScore + "\", rating_count = " + count + " " +
							      "where fbuserID = \"" + ratedUserID + "\"";
					
				}
				else
				{
					scoreQuery = "insert into rating_summary (fbuserID, rating_score, rating_count) "+
								 "values (\"" + ratedUserID + "\",\"" + score + "\", 1)";
				}
				DBHelper.addDBSingle(scoreQuery);
				resObj.put("status", "success");
				resObj.put("message", "Rating successfully added.");
			}
			else
			{
				resObj.put("status", "failure");
				resObj.put("message", "Cannot update user rating due to server error. Please retry later.");
			}

		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			resObj.put("status", "failure");
			resObj.put("message", "Cannot update user rating due to server error. Please retry later.");
		}		
		return resObj;
	}

	public static JSONObject getRating(long userID) throws JSONException
	{
		JSONObject resObj = new JSONObject();
		String ratingQuery = "select rating_score, rating_count from rating_summary where fbuserId = \"" + userID + "\"";
		JSONObject ratingObj = DBHelper.getDBSingle(ratingQuery);
		if(ratingObj.length() > 0)
		{
			double score = ratingObj.getDouble("rating_score");
			int count = ratingObj.getInt("rating_count");
			resObj.put("status", "success");
			resObj.put("message", "rating found successfully");
			resObj.put("score", Math.round(score*10.0)/10.0);
			resObj.put("count", count);
		}
		else
		{
			resObj.put("status", "success");
			resObj.put("message", "user not rated yet.");
			resObj.put("score", "");
			resObj.put("count", "");
		}
		return resObj;
	}

}
