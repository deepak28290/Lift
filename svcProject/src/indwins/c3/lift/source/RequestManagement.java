package indwins.c3.lift.source;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RequestManagement 
{
	public static JSONObject addRequest(String requesterType, long requesterID, long accepterID) throws JSONException, SQLException
	{
		
		String accepterTable = "";
		JSONObject obj = new JSONObject();
		if(requesterType.equals("passenger"))
		{
			accepterTable = "online_rider";
		}
		else if(requesterType.equals("rider"))
		{
			accepterTable = "online_passenger";
		}
		String accepterQuery = "select * from " + accepterTable + " where userID ="+accepterID;
		JSONObject accepterObj = DBHelper.getDBSingle(accepterQuery);
		if(accepterObj.length() > 0 && accepterObj.get("isAccepted").toString().equalsIgnoreCase("N"))
		{				
			String tableName = "active_requests";
			String query = "replace into "+tableName+"(requesterType, requesterID, accepterID, requesttime, requeststatus) values (\"" + 
							requesterType + "\",\"" + requesterID+"\",\"" + accepterID+"\", "+ System.currentTimeMillis() +",\"pending\")";
			String[] returncols = {"requestID"};
			JSONObject requestJSON = DBHelper.addReturnDBSingle(query, returncols);
			if( requestJSON.length() > 0)
			{
				String regCol = "registrationID";
				String regIdQuery = "select "+ regCol +" from app_registration_keys where userID = "+ accepterID;
				JSONObject jObj = DBHelper.getDBSingle(regIdQuery);
				if(jObj.length() > 0)
				{
					String regIDS = jObj.getString(regCol).toString();
					JSONArray jsonArray = new JSONArray();
					jsonArray.put(regIDS);
					JSONObject gcmJObj = new JSONObject();
					gcmJObj.put("collapse_key", requesterType+" is available");
					gcmJObj.put("registration_ids", jsonArray);
					JSONObject dataObj = new JSONObject();
					dataObj.put("requestID", requestJSON.getInt(returncols[0]));
					dataObj.put("requesterType", requesterType);
					dataObj.put("requesterID", requesterID);
					dataObj.put("accepterID", accepterID);
					gcmJObj.put("data", dataObj);
					try 
					{
						JSONObject resultJSONObj = pushNotify(gcmJObj);
						if(resultJSONObj.getString("success").equals("1"))
						{
							obj.put("status", "success");
							obj.put("message", resultJSONObj);
						}
						else
						{
							String delQuery = "delete from active_requests where requesterID = "+ requesterID;
							DBHelper.addDBSingle(delQuery);	
							obj.put("status", "failure");
							obj.put("message", "Unable to send push Notification due to server error");
						}
					} 
					catch (HttpException e) 
					{
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				else
				{
					obj.put("status", "failure");
					obj.put("message", "registrationID not found in DB");
				}
			}
			else
			{
				obj.put("status", "failure");
				obj.put("message", "unable to push request in DB");
			}
		}
		else
		{
			obj.put("status", "failure");
			obj.put("message", "accepter not available now");
		}
		return obj;
	}
	
	public static JSONObject pushNotify(JSONObject gcmJObj) throws HttpException, IOException, JSONException
	{
		String gcmUrl = "https://android.googleapis.com/gcm/send";
		JSONObject obj = new JSONObject();
		PostMethod method = new PostMethod(gcmUrl);
		method.addRequestHeader("Content-Type", "application/json");
		method.addRequestHeader("Authorization", "key = AIzaSyDTE65FLjtlVV_hXVG5c60vuEtvFcKpv60");
		method.setRequestEntity(new StringRequestEntity(gcmJObj.toString(), "application/json", "UTF-8"));
		//System.out.println("Sending 'POST' request to URL : " + gcmUrl);
		obj = HelperUtil.sendPostRequest(method);
		return obj;
	}
	
	public static JSONObject acceptRequest(String reqID) throws JSONException, SQLException 
	{
		JSONObject resObj = new JSONObject();
		String requesterStatus = "";
		String accepterStatus = "";
		String riderId = "" ;
		String passengerId = "";
		String requesterType ="";
		String requesterId = "";
		String accepterId = "";
		String requestQuery = "select requesterType, requesterID, accepterID from active_requests where requestID = " + reqID;
		JSONObject reqObj = DBHelper.getDBSingle(requestQuery);
		if(reqObj.length() > 0)
		{
			requesterType = reqObj.getString("requesterType");
			requesterId = reqObj.getString("requesterID");
			accepterId = reqObj.getString("accepterID");
			if(requesterType.equals("rider"))
			{
				JSONObject reqStatusObj = DBHelper.getDBSingle("select isAccepted from online_rider where userID = "+ requesterId);
				requesterStatus = reqStatusObj.getString("isAccepted");
				JSONObject accStatusObj = DBHelper.getDBSingle("select isAccepted from online_passenger where userID = "+ accepterId);
				accepterStatus = accStatusObj.getString("isAccepted");
				riderId = requesterId;
				passengerId = accepterId;
			}
			else if(requesterType.equals("passenger"))
			{
				JSONObject reqStatusObj = DBHelper.getDBSingle("select isAccepted from online_passenger where userID = "+ requesterId);
				requesterStatus = reqStatusObj.getString("isAccepted");
				JSONObject accStatusObj = DBHelper.getDBSingle("select isAccepted from online_rider where userID = "+ accepterId);
				accepterStatus = accStatusObj.getString("isAccepted");
				passengerId = requesterId;
				riderId = accepterId;
			}
			
			if(requesterStatus.toUpperCase().equals("N") && accepterStatus.toUpperCase().equals("N"))
			{
				String expireRequetQuery = "update active_requests set requeststatus = \"expired\" where "+
										   "requestID != " + reqID + " and "+
										   "requeststatus = 'pending' and ("+
									 	   "requesterId in (" + requesterId + "," + accepterId +") or " +
									 	   "accepterId in (" + requesterId + "," + accepterId +"))";
				int expireRequestStatus = DBHelper.addDBSingle(expireRequetQuery);
				if (expireRequestStatus >= 0)
				{	
					String acceptRequestQuery = "update active_requests set requeststatus = \"accepted\" where "+
							  			        "requestID = "+ reqID;
					int accpetRequestStatus = DBHelper.addDBSingle(acceptRequestQuery);
					if(accpetRequestStatus > 0)
					{
						int riderAcceptStatus = DBHelper.addDBSingle("update online_rider set isAccepted = 'Y' where userID = " + riderId);
						int passengerAcceptStatus = DBHelper.addDBSingle("update online_passenger set isAccepted = 'Y' where userID = " + passengerId);
						if(riderAcceptStatus > 0 && passengerAcceptStatus > 0)
						{
							String regCol = "registrationID";
							String regIdQuery = "select "+ regCol +" from app_registration_keys where userID = "+ requesterId;
							JSONObject jObj = DBHelper.getDBSingle(regIdQuery);
							if(jObj.length() > 0)
							{
								String regIDS = jObj.getString(regCol).toString();
								JSONArray jsonArray = new JSONArray();
								jsonArray.put(regIDS);
								JSONObject gcmJObj = new JSONObject();
								gcmJObj.put("collapse_key", "Congrats!! Your request is accepted");
								gcmJObj.put("registration_ids", jsonArray);
								JSONObject dataObj = new JSONObject();
								dataObj.put("requestID", reqID);
								gcmJObj.put("data", dataObj);
								try 
								{
									JSONObject resultJSONObj = pushNotify(gcmJObj);
									if(resultJSONObj.getString("success").equals("1"))
									{
										System.out.println("Push Notify response:" + resultJSONObj);
										resObj.put("status", "success");
										resObj.put("message", "Successfully accepted request.");
									}
									else
									{
										String expireRequetQuery1 = "update active_requests set requeststatus = \"pending\" where "+
												   				    "requestID != " + reqID + " and "+
												   				    "requeststatus = 'expired' and ("+
												   				    "requesterId in (" + requesterId + "," + accepterId +") or " +
												   				    "accepterId in (" + requesterId + "," + accepterId +"))";
										int expireRequestStatus1 = DBHelper.addDBSingle(expireRequetQuery1);
										if (expireRequestStatus1 >= 0)
										{	
											String acceptRequestQuery1 = "update active_requests set requeststatus = \"pending\" where "+
									  			        				"requestID = "+ reqID;
											int accpetRequestStatus1 = DBHelper.addDBSingle(acceptRequestQuery1);
											if(accpetRequestStatus1 > 0)
											{
												DBHelper.addDBSingle("update online_rider set isAccepted = 'N' where userID = " + riderId);
												DBHelper.addDBSingle("update online_passenger set isAccepted = 'N' where userID = " + passengerId);
											}
										}
										resObj.put("status", "failure");
										resObj.put("message", "Unable to send push Notification due to server error");

									}
								} 
								catch (HttpException e) 
								{
									e.printStackTrace();
								} 
								catch (IOException e) 
								{
									e.printStackTrace();
								}
							}
							else
							{
								resObj.put("status", "failure");
								resObj.put("message", "Unable to send push notification, registrationID not found in DB.");
							}
						}
						else
						{
							resObj.put("status", "failure");
							resObj.put("message", "Cannot accept request due to server error.");
						}
					}
					else
					{
						resObj.put("status", "failure");
						resObj.put("message", "Cannot accept request due to server error.");
					}
				}
				else
				{
					resObj.put("status", "failure");
					resObj.put("message", "Cannot accept request due to server error.");
				}
			}
			else
			{
				resObj.put("status", "failure");
				resObj.put("message", "Sorry user is no longer available.");
			}
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "Sorry invalid requestID.");
		}
		return resObj;
	}
	
	public static JSONObject cancelRequest(String reqID) throws JSONException, SQLException 
	{
		JSONObject resObj = new JSONObject();
		String cancelRequestQuery = "update active_requests set requeststatus = \"cancelled\" where "+
							        "requestID = " + reqID;
		int cancelRequestStatus = DBHelper.addDBSingle(cancelRequestQuery);
		if(cancelRequestStatus > 0)
		{
			resObj.put("status", "success");
			resObj.put("message", "Succesfully cancelled request");
		}
		else if(cancelRequestStatus == 0)
		{
			resObj.put("status", "failure");
			resObj.put("message", "Request not found.");
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "Cannot cancel request due to server error.");
		}
		return resObj;
	}
	
	public static JSONArray getMyRequest(long id, String reqType) throws JSONException 
	{
		String senderIdCol = "";
		String receiverIdCol = "";
		String requestQuery = "";
		JSONArray reqObj = new JSONArray();
		if(reqType.equals("sent"))
		{
			senderIdCol = "requesterID";
			receiverIdCol = "accepterID";
		}
		else if (reqType.equals("received"))
		{
			senderIdCol = "accepterID";
			receiverIdCol = "requesterID";
		}
		String userTypeQuery = "select requesterType from active_requests where "+ senderIdCol +" = "+ id;
		JSONObject userTypeObj = DBHelper.getDBSingle(userTypeQuery);
		if(userTypeObj.length() > 0)
		{
			String userType = userTypeObj.getString("requesterType");
			if(userType.equals("rider") && reqType.equals("sent"))
			{
				requestQuery =  "select olr.source selfSource, olr.destination selfDestination, "+
								"olr.srcgeocode selfSrcGeoCode, olr.destgeocode selfDestGeoCode, "+
								"olr.starttime selfStartTime, "+
								"opr.source otherSource, opr.destination otherDestination, "+
								"opr.srcgeocode otherSrcGeoCode, opr.destgeocode otherDestGeoCode, "+
								"opr.starttime otherStartTime, "+
								"ar.requesttime requestTime, ar.requestID requestId, ar.requeststatus requestStatus "+
								"from active_requests ar, online_rider olr, online_passenger opr " +
								"where ar."+ senderIdCol +" = " + "olr.userID " +
							 	"and ar."+ receiverIdCol +" = " + "opr.userID " +
							 	"and ar." + senderIdCol +" = "+ id;
			}
			else if(userType.equals("passenger") && reqType.equals("sent"))
			{
				requestQuery =  "select olp.source selfSource, olp.destination selfDestination, "+
								"olp.srcgeocode selfSrcGeoCode, olp.destgeocode selfDestGeoCode, "+
								"olp.starttime selfStartTime, "+
								"olr.source otherSource, olr.destination otherDestination, "+
								"olr.srcgeocode otherSrcGeoCode, olr.destgeocode otherDestGeoCode, "+
								"olr.starttime otherStartTime, "+
								"ar.requesttime requestTime, ar.requestID requestId, ar.requeststatus requestStatus "+
								"from active_requests ar, online_passenger olp, online_rider olr "+
								"where ar."+ senderIdCol +" = " + "olp.userID "+
								"and ar."+ receiverIdCol +" = " + "olr.userID "+
								"and ar." + senderIdCol +" = "+ id;
			}
			else if(userType.equals("rider") && reqType.equals("received"))
			{
				requestQuery =  "select olp.source selfSource, olp.destination selfDestination, "+
								"olp.srcgeocode selfSrcGeoCode, olp.destgeocode selfDestGeoCode, "+
								"olp.starttime selfStartTime, "+
								"olr.source otherSource , olr.destination otherDestination , "+
								"olr.srcgeocode otherSrcGeoCode, olr.destgeocode otherDestGeoCode, "+
								"olr.starttime otherStartTime, "+
								"ar.requesttime requestTime, ar.requestID requestId, ar.requeststatus requestStatus "+
								"from active_requests ar, online_passenger  olp, online_rider olr "+
								"where ar."+ senderIdCol +" = " + "olp.userID "+
								"and ar."+ receiverIdCol +" = " + "olr.userID "+
								"and ar." + senderIdCol +" = "+ id;
			}
			else if(userType.equals("passenger") && reqType.equals("received"))
			{
				requestQuery = 	"select olr.source selfSource, olr.destination selfDestination, "+
								"olr.srcgeocode selfSrcGeoCode, olr.destgeocode selfDestGeoCode, "+
								"olr.starttime selfStartTime, "+
								"olp.source otherSource, olp.destination otherDestination, "+
								"olp.srcgeocode otherSrcGeoCode, olp.destgeocode otherDestGeoCode, "+
								"olp.starttime otherStartTime, "+
								"ar.requesttime requestTime, ar.requestID requestId, ar.requeststatus requestStatus "+
								"from active_requests ar, online_rider olr, online_passenger olp "+
								"where ar."+ senderIdCol +" = " + "olr.userID "+
								"and ar."+ receiverIdCol +" = " + "olp.userID "+
								"and ar." + senderIdCol +" = "+ id;
			}
			reqObj = DBHelper.getDBMultiple(requestQuery);
		}
		return reqObj;
	}
}
