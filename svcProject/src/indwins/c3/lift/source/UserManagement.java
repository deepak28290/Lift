package indwins.c3.lift.source;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@XmlRootElement
public class UserManagement 
{
	public static String addUser(String userName, String docType, String docID, String fbUserID)
	{
		String query = 	"INSERT INTO user_details "
					    +"(userName, docType, docID , fbuserID)"
						+"VALUES (\""+userName+"\",\""
									 +docType +"\",\""
									 +docID +"\",\""
									 +fbUserID+"\")";
		String[] returncols = {"userID"};
		try 
		{
			return DBHelper.addReturnDBSingle(query, returncols).toString();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			return (new StringBuffer("Cannot Create Record due to DB Issue").toString());
		}
	}
	
	public static JSONObject getUser(String uid) throws JSONException
	{
		JSONObject resObj = new JSONObject();
		String userQuery = "select * from user_details where fbuserID = \"" + uid + "\"";
		JSONObject userObj = DBHelper.getDBSingle(userQuery);
		if(userObj.length() > 0)
		{
			resObj.put("status", "success");
			resObj.put("message", "Successfully fetched user details.");
			resObj.put("result", userObj);
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "User details not found");
			resObj.put("result", userObj);
		}
		return resObj;
	}
	
	public static JSONObject addOnlineUser(String userType, long userID, String source, String destination, 
										   String srcGeoCode, String destGeoCode, String startTime) throws JSONException, SQLException
	{
		String tableName = "";
		if(userType.equals("rider"))
		{
			tableName = "online_rider";
		}
		else if(userType.equals("passenger"))
		{
			tableName ="online_passenger";
		}
		String query = "replace into "+tableName+"(userID, source, destination, srcgeocode, destgeocode, starttime)"+
						            " values (\""+userID+"\",\""
									             +source+"\",\""
												 +destination+"\",\""
												 +srcGeoCode+"\",\""
												 +destGeoCode+"\",\""
												 +startTime+"\")";
		
		int status = DBHelper.addDBSingle(query);
		JSONObject obj = new JSONObject();
		if(status > 0)
		{
			obj.put("status", "success");
			obj.put("message", "Successfully added user in online " + userType + " list.");
		}
		else
		{
			obj.put("status", "failure");
			obj.put("message", "Cannot add user in online " + userType + " list due to server error.");
		}
		
		return obj;
	}
	
	public static JSONArray getNearestUsers(String userType , long userID, String userSrcGeoCode, 
											String userDestGeoCode, String userStartTime) throws JSONException, HttpException, IOException
	{
		long minutes = 30;
		int distRangeSP = 2000;
		int distRangeEP = 2000;
		String originsSP = "origins=";
		String originsEP = "origins=";
		String tableNameNN = "";
		String destinationsSP = "destinations=";
		String destinationsEP = "destinations=";
		String distMatUrlSP = "https://maps.googleapis.com/maps/api/distancematrix/json?";
		String distMatUrlEP = "https://maps.googleapis.com/maps/api/distancematrix/json?";
		Date startTime = new Date(Long.valueOf(userStartTime).longValue() - minutes * 60 * 1000);
		Date endTime = new Date(Long.valueOf(userStartTime).longValue() + minutes * 60 * 1000);
		JSONArray nnJSONArray = new JSONArray();
		JSONArray responseJSONArray = new JSONArray();
		if (userType.equals("passenger")) 
		{
			tableNameNN = "online_rider";
		} 
		else if (userType.equals("rider")) 
		{
			tableNameNN = "online_passenger";
		}
		originsSP = originsSP + userSrcGeoCode;
		originsEP = originsEP + userDestGeoCode;
		String nnQuery = "select userID, source, srcgeocode, destination, destgeocode, starttime "
				         + "from " + tableNameNN + " where isAccepted = \'N\' and isCompleted = \'N\'";
		JSONArray nJSONArray = DBHelper.getDBMultiple(nnQuery);
		if (nJSONArray.length() > 0) 
		{
			for (int i = 0; i < nJSONArray.length(); i++) 
			{
				Date nStartTime = new Date(Long.valueOf(nJSONArray.getJSONObject(i).getString("starttime")).longValue());
				if (nStartTime.after(startTime) && nStartTime.before(endTime)) 
				{
					nnJSONArray.put(nJSONArray.getJSONObject(i));
				}
			}
			if (nnJSONArray.length() > 0) 
			{
				for (int j = 0; j < nnJSONArray.length(); j++) 
				{
					if (destinationsSP.equals("destinations=") && destinationsEP.equals("destinations=")) 
					{
						destinationsSP = destinationsSP + nnJSONArray.getJSONObject(j).getString("srcgeocode");
						destinationsEP = destinationsEP	+ nnJSONArray.getJSONObject(j).getString("destgeocode");
					} 
					else 
					{
						destinationsSP = destinationsSP	+ "%7C"	+ nnJSONArray.getJSONObject(j).getString("srcgeocode");
						destinationsEP = destinationsEP	+ "%7C"	+ nnJSONArray.getJSONObject(j).getString("destgeocode");
					}
				}

				distMatUrlSP = distMatUrlSP + originsSP + "&" + destinationsSP + "&mode=walking&language=en";
				distMatUrlEP = distMatUrlEP + originsEP + "&" + destinationsEP + "&mode=walking&language=en";
				//JSONObject nnSPJSONObj = ExternalApiHelper.sendHttpPost(distMatUrlSP);
				//JSONObject nnEPJOSNObj = ExternalApiHelper.sendHttpPost(distMatUrlEP);
				
				PostMethod methodSP = new PostMethod(distMatUrlSP);
				//System.out.println("Sending 'POST' request to URL : " + distMatUrlSP);
				JSONObject nnSPJSONObj = HelperUtil.sendPostRequest(methodSP);
				PostMethod methodEP = new PostMethod(distMatUrlEP);
				//System.out.println("Sending 'POST' request to URL : " + distMatUrlEP);
				JSONObject nnEPJOSNObj = HelperUtil.sendPostRequest(methodEP);
				
				JSONArray elementSPJSON = nnSPJSONObj.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
				JSONArray elementEPJSON = nnEPJOSNObj.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
				
				for (int i = 0; i < elementSPJSON.length(); i++) 
				{
					if (elementSPJSON.getJSONObject(i).getString("status").equals("OK")	&& elementEPJSON.getJSONObject(i).getString("status").equals("OK")) 
					{
						if (elementSPJSON.getJSONObject(i).getJSONObject("distance").getLong("value") <= distRangeSP
						 && elementEPJSON.getJSONObject(i).getJSONObject("distance").getLong("value") <= distRangeEP) 
						{
							JSONObject obj = new JSONObject();
							String sentReqQuery = "select * from active_requests where requesterID = " + userID +
									              " and accepterID = " + nnJSONArray.getJSONObject(i).getString("userID") +
									              " and requeststatus = 'pending' ";
							JSONObject sentReqObj = DBHelper.getDBSingle(sentReqQuery);
							if(sentReqObj.length() > 0)
							{
								obj.put("hasactiverequest", "1");
							}
							else
							{
								obj.put("hasactiverequest", "0");
							}							
							obj.put("userID", nnJSONArray.getJSONObject(i).getString("userID"));
							obj.put("starttime", nnJSONArray.getJSONObject(i).getString("starttime"));
							obj.put("srcgeocode", nnJSONArray.getJSONObject(i).getString("srcgeocode"));
							obj.put("source", nnJSONArray.getJSONObject(i).getString("source"));
							obj.put("srcdistance",elementSPJSON.getJSONObject(i).getJSONObject("distance").getString("text"));
							obj.put("srcduration",elementSPJSON.getJSONObject(i).getJSONObject("duration").getString("text"));
							obj.put("destgeocode", nnJSONArray.getJSONObject(i).getString("destgeocode"));
							obj.put("destination", nnJSONArray.getJSONObject(i).getString("destination"));
							obj.put("destdistance", elementEPJSON.getJSONObject(i).getJSONObject("distance").getString("text"));
							obj.put("destduration", elementEPJSON.getJSONObject(i).getJSONObject("duration").getString("text"));
							responseJSONArray.put(obj);
							System.out.println(responseJSONArray);
						}
					}
				}
/*				try 
				{
					addOnlineUser(userType, userID, "", "", userSrcGeoCode, userDestGeoCode, userStartTime);
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}*/
			} 
			else 
			{
				System.out.println("No nearest " + userType + " found");
			}
		} 
		else 
		{
			System.out.println("No online neighbhour found");
		}
		return responseJSONArray;
	}

	public static String addRegID(long userID, String regID) throws JSONException, SQLException 
	{
		JSONObject obj = new JSONObject();
		if(regID != null)
		{
			String query = "replace into app_registration_keys (userID, registrationID) values("+userID+",\""+regID+"\")";
			int status = DBHelper.addDBSingle(query);
			if(status > 0)
			{
				obj.put("status", "success");
				obj.put("message", "Successfully added app RegistrationID");
			}
			else
			{
				obj.put("status", "failure");
				obj.put("message", "Cannot add app RegistrationID due to server error");
			}
		}
		else
		{
			
		}		
		return obj.toString();
	}

	public static JSONObject isNewUser(String userId) throws JSONException 
	{
		JSONObject resObj = new JSONObject();
		String userQuery = "select fbuserID from user_details where fbuserID = \"" + userId + "\"";
		JSONObject userObj = DBHelper.getDBSingle(userQuery);
		if(userObj.length() > 0)
		{
			resObj.put("status", "success");
			resObj.put("newuser","0");
			resObj.put("message", "user already exists");
		}
		else
		{
			resObj.put("status", "success");
			resObj.put("newuser","1");
			resObj.put("message", "user not reqistered");
		}
		return resObj;
	}

	public static JSONObject updateUserProfile(User newUser) throws SQLException, JSONException 
	{
		JSONObject resObj = new JSONObject();
		String name = newUser.getName();
		String fbUserID = newUser.getFbUserID();
		String emailID = newUser.getEmailID();
		String gender = newUser.getGender();
		String phone = newUser.getPhone();
		
		String userQuery =  "replace into user_details (fbuserID, userName, emailID, gender, phone) values (\""+
							fbUserID + "\",\"" + name + "\",\"" + emailID + "\",\"" + gender + "\",\"" + phone + "\")";
		int rows = DBHelper.addDBSingle(userQuery);
		if( rows > 0)
		{
			resObj.put("status", "success");
			resObj.put("message", "profile successfully updated");
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "cannot update profile dure to server error, please try later");
		}
		return resObj;
	}

	public static JSONObject updateUserPhoto(String fbuserID, String docType, InputStream imageFileInputStream) throws JSONException, SQLException 
	{
		JSONObject resObj = new JSONObject();
		String tableName = "";
		String colName = "";
		if(docType.toUpperCase().equals("PHOTO"))
		{
			tableName = colName = "user_photo";
		}
		else if(docType.toUpperCase().equals("PAN"))
		{
			tableName = colName = "user_pancard";
		}
		else if(docType.toUpperCase().equals("DL"))
		{
			tableName = colName = "user_dl";
		}
		Connection con = null;
		PreparedStatement pre = null;
		try
		{
			con = DBHelper.createConnection();
			pre = con.prepareStatement("replace into " + tableName + " (fbuserID, " + colName + ") values(?,?)");
			pre.setString(1,fbuserID);
			pre.setBinaryStream(2,imageFileInputStream);
			int count = DBHelper.executePreparedStatement(pre);
			if( count > 0)
			{
				resObj.put("status", "success");
				resObj.put("message", "profile successfully updated");
			}
			else
			{
				resObj.put("status", "failure");
				resObj.put("message", "cannot update profile due to server error.");
			}
		}
		catch (Exception e1)
		{
			resObj.put("status", "failure");
			resObj.put("message", "cannot update profile due to server error.");
			System.out.println("error 1 ="+e1.getMessage());
		}
		finally
		{
			pre.close();
			con.close(); 
		}
		return resObj;
	}

	public static InputStream getPhoto(String userID, String docType) throws SQLException 
	{
		String tableName = "";
		String colName = "";
		InputStream ipStream = null;
		PreparedStatement pre = null;
		
		if(docType.toUpperCase().equals("PHOTO"))
		{
			tableName = colName = "user_photo";
		}
		else if(docType.toUpperCase().equals("PAN"))
		{
			tableName = colName = "user_pancard";
		}
		else if(docType.toUpperCase().equals("DL"))
		{
			tableName = colName = "user_dl";
		}
		Connection con = null;
		try
		{
			con = DBHelper.createConnection();
			pre = con.prepareStatement("select " + colName + " from " + tableName + " where fbuserID" +
					" = ?");
			pre.setString(1,userID);
			ipStream = DBHelper.selectPreparedStatement(pre, colName);
		}
		catch (Exception e1)
		{
			System.out.println("error 1 ="+e1.getMessage());
		}
		finally
		{
			pre.close();
			con.close(); 
		}
		return ipStream;
	}

	public static JSONObject sendEmailVerifyCode(String id) throws JSONException, SQLException
	{
		JSONObject resObj = new JSONObject();
		String from = "admin@elift.in";
		String subject = "Lift: verify your email";
		String to = "";
		String body = "";
		String emailQuery = "select emailID from user_details where fbuserID = \"" + id +"\"";
		JSONObject emailObj = DBHelper.getDBSingle(emailQuery);
		if(emailObj.length() > 0)
		{
			Random rand = new Random();
		    int randomNum = rand.nextInt((99999 - 10000) + 1) + 10000;
			to = emailObj.getString("emailID");
			body = "Hi \n\nYour email verificatoin code is " + randomNum +" \n\nRegards\nLift Admin";
			String codeQuery = "replace into verify_email (fbuserID, emailID, verification_code, code_validity) " +
								"values (\""+ id + "\", \"" + to + "\", \"" + randomNum + "\", " + System.currentTimeMillis() +")";
			int rows = DBHelper.addDBSingle(codeQuery);
			if (rows > 0)
			{
				System.out.println("To = " + to);
				System.out.println("From = " + from);
				System.out.println("Subject = " + subject);
				System.out.println("Body = " + body);
				//HelperUtil.sendMail(from, to, subject, body );
				resObj.put("status", "success");
				resObj.put("message", "Email verification code sent successfully");
			}
			else
			{
				resObj.put("status", "failure");
				resObj.put("message", "Could not generate email verification code. Please try later");
			}
		    
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "Email Id not found. Please update email Id before verification");
		}
		return resObj;
	}

	public static JSONObject verifyEmail(String id, String userCode) throws JSONException, SQLException 
	{
		JSONObject resObj = new JSONObject();
		String from = "admin@elift.in";
		String subject = "Congrats: Your email verified successfully";
		String to = "";
		String body = "";
		String internalCode = "";
		String emailQuery = "select emailID, verification_code from verify_email where fbuserID = \"" + id +"\"";
		JSONObject emailObj = DBHelper.getDBSingle(emailQuery);
		if(emailObj.length() > 0)
		{
			internalCode = emailObj.getString("verification_code");
			to = emailObj.getString("emailID");
			if(internalCode.equals(userCode))
			{
				body = "Hi \n\nCongrats!! Your email verified successfully \n\nRegards\nLift Admin";
				String codeQuery = "delete from verify_email where fbuserID = \"" + id + "\"";
				int rows = DBHelper.addDBSingle(codeQuery);
				if (rows > 0)
				{
					System.out.println("To = " + to);
					System.out.println("From = " + from);
					System.out.println("Subject = " + subject);
					System.out.println("Body = " + body);
					//HelperUtil.sendMail(from, to, subject, body );
					String verifyStatusQuery = "insert into verification_status (fbuserID, email_status) " +
												"values ( " + id + ", 1) on duplicate key update email_status = 1";
					int rcount = DBHelper.addDBSingle(verifyStatusQuery);
					if (rcount > 0)
					{
						resObj.put("status", "success");
						resObj.put("message", "Email verified successfully");
					}
					else
					{
						resObj.put("status", "failure");
						resObj.put("message", "Error occured while verifying email, Please try again");
					}

				}
				else
				{
					resObj.put("status", "failure");
					resObj.put("message", "Could not flush the email verification code.");
				}
			}
			else
			{
				resObj.put("status", "failure");
				resObj.put("message", "Sorry code doesnot match. Please retry");
			}
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "No verification code found. Please sent verication code before verification");
		}		
		return resObj;
	}
	
	public static JSONObject sendPhoneVerifyCode(String id) throws JSONException, SQLException 
	{
		JSONObject resObj = new JSONObject();
		String to = "";
		String message = "";
		String phoneQuery = "select phone from user_details where fbuserID = \"" + id +"\"";
		JSONObject phoneObj = DBHelper.getDBSingle(phoneQuery);
		if(phoneObj.length() > 0)
		{
			Random rand = new Random();
		    int randomNum = rand.nextInt((99999 - 10000) + 1) + 10000;
			to = phoneObj.getString("phone");
			message = "Hi \n\nYour phone verificatoin code is " + randomNum +" \n\nRegards\nLift Admin";
			String codeQuery = "replace into verify_phone (fbuserID, phone, verification_code, code_validity) " +
								"values (\""+ id + "\", \"" + to + "\", \"" + randomNum + "\", " + System.currentTimeMillis() +")";
			int rows = DBHelper.addDBSingle(codeQuery);
			if (rows > 0)
			{
				System.out.println("To = " + to);
				System.out.println("Body = " + message);
				HelperUtil.sendSMS(to, message );
				resObj.put("status", "success");
				resObj.put("message", "Phone verification code sent successfully");
			}
			else
			{
				resObj.put("status", "failure");
				resObj.put("message", "Could not generate phone verification code. Please try later");
			}   
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "Phone number not found. Please update phone number before verification");
		}
		return resObj;
	}

	public static JSONObject verifyPhone(String id, String userCode) throws JSONException, SQLException 
	{
		JSONObject resObj = new JSONObject();
		String message = "Congrats: Your Phone number verified successfully";
		String to = "";
		String internalCode = "";
		String emailQuery = "select phone, verification_code from verify_phone where fbuserID = \"" + id +"\"";
		JSONObject emailObj = DBHelper.getDBSingle(emailQuery);
		if(emailObj.length() > 0)
		{
			internalCode = emailObj.getString("verification_code");
			to = emailObj.getString("phone");
			if(internalCode.equals(userCode))
			{
				String codeQuery = "delete from verify_phone where fbuserID = \"" + id + "\"";
				int rows = DBHelper.addDBSingle(codeQuery);
				if (rows > 0)
				{
					System.out.println("To = " + to);
					System.out.println("Body = " + message);
					HelperUtil.sendSMS(to, message );
					String verifyStatusQuery = "insert into verification_status (fbuserID, phone_status) " +
												"values ( " + id + ", 1) on duplicate key update phone_status = 1";
					int rcount = DBHelper.addDBSingle(verifyStatusQuery);
					if (rcount > 0)
					{
						resObj.put("status", "success");
						resObj.put("message", "Phone verified successfully");
					}
					else
					{
						resObj.put("status", "failure");
						resObj.put("message", "Error occured while verifying phone number, Please try again");
					}

				}
				else
				{
					resObj.put("status", "failure");
					resObj.put("message", "Could not flush the phone verification code.");
				}
			}
			else
			{
				resObj.put("status", "failure");
				resObj.put("message", "Sorry code doesnot match. Please retry");
			}
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "No verification code found. Please sent verication code before verification");
		}		
		return resObj;
	}
	
	public static JSONObject getVerificatonStatus(long userID) throws JSONException 
	{
		JSONObject resObj = new JSONObject();
		String verifyQuery = "select email_status, phone_status from verification_status where fbuserID = \"" + userID + "\"";
		JSONObject verifyObj = DBHelper.getDBSingle(verifyQuery);
		if(verifyObj.length() > 0)
		{
			if(verifyObj.getInt("email_status") == 1 && verifyObj.getInt("phone_status") == 1)
			{
				resObj.put("status", "verified");
				resObj.put("message", "verification complete");
			}
			else if (verifyObj.getInt("email_status") == 1)
			{
				resObj.put("status", "pending");
				resObj.put("message", "partially verified. Phone number verification pending");
			}
			else if(verifyObj.getInt("phone_status") == 1)
			{
				resObj.put("status", "pending");
				resObj.put("message", "partially verified. Email verification pending");
			}
			else
			{
				resObj.put("status", "pending");
				resObj.put("message", "Not verified");
			}
		}
		else
		{
			resObj.put("status", "pending");
			resObj.put("message", "Not verified");
		}
		System.out.println("result = " +  resObj);
		return resObj;
	}

	public static JSONObject hasLockedRide(String userID, String userType) throws JSONException 
	{
		JSONObject resObj = new JSONObject();
		String lockQuery = "";
		if(userType.toUpperCase().equals("RIDER"))
		{
			lockQuery = "select * from online_rider where userID = \"" + userID + "\"";
		}
		else if (userType.toUpperCase().equals("PASSENGER"))
		{
			lockQuery = "select * from online_passenger where userID = \"" + userID + "\"";
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("haslockedride", "");
			resObj.put("data", new JSONObject());
			return resObj;
		}
		
		JSONObject lockObj = DBHelper.getDBSingle(lockQuery);
		if(lockObj.length() > 0 )
		{
			resObj.put("status", "success");
			resObj.put("haslockedride", "true");
			resObj.put("data", lockObj);
		}
		else
		{
			resObj.put("status", "success");
			resObj.put("haslockedride", "false");
			resObj.put("data", new JSONObject());
		}
		return resObj;
	}

	public static JSONObject getPANStatus(long userID) throws JSONException 
	{
		JSONObject resObj = new JSONObject();
		String panQuery = "select pan_status from user_pancard where fbuserId = \"" + userID + "\"";
		JSONObject panObj = DBHelper.getDBSingle(panQuery);
		if (panObj.length() > 0)
		{
			int panStatus = panObj.getInt("pan_status");
			if (panStatus == 1)
			{
				resObj.put("staus", "success");
				resObj.put("message", "verified");
			}
			else
			{
				resObj.put("staus", "success");
				resObj.put("message", "verification pending");
			}	
		}
		else
		{
			resObj.put("staus", "success");
			resObj.put("message", "upload pending");
		}		
		return resObj;
	}

	public static JSONObject verifyPAN(String fbuserID, String code) throws SQLException, JSONException 
	{
		JSONObject resObj = new JSONObject();
		String panQuery = "update user_pancard set pan_status = " + code + " where fbuserID = \"" + fbuserID + "\"";
		int rcount = DBHelper.addDBSingle(panQuery);
		if(rcount > 0)
		{
			resObj.put("stauts", "success");
			resObj.put("message", "pan verification status updated successfully");
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "Trying to update status without uploading pan. Please upload PAN before verifying");
		}
		
		return resObj;
	}

	public static JSONObject getDLStatus(long userID) throws JSONException 
	{
		JSONObject resObj = new JSONObject();
		String dlQuery = "select dl_status from user_dl where fbuserId = \"" + userID + "\"";
		JSONObject dlObj = DBHelper.getDBSingle(dlQuery);
		if (dlObj.length() > 0)
		{
			int dlStatus = dlObj.getInt("dl_status");
			if (dlStatus == 1)
			{
				resObj.put("staus", "success");
				resObj.put("message", "verified");
			}
			else
			{
				resObj.put("staus", "success");
				resObj.put("message", "verification pending");
			}	
		}
		else
		{
			resObj.put("staus", "success");
			resObj.put("message", "upload pending");
		}		
		return resObj;
	}

	public static JSONObject verifyDL(String fbuserID, String code) throws SQLException, JSONException 
	{
		JSONObject resObj = new JSONObject();
		String dlQuery = "update user_dl set dl_status = " + code + " where fbuserID = \"" + fbuserID + "\"";
		int rcount = DBHelper.addDBSingle(dlQuery);
		if(rcount > 0)
		{
			resObj.put("stauts", "success");
			resObj.put("message", "DL verification status updated successfully");
		}
		else
		{
			resObj.put("status", "failure");
			resObj.put("message", "Trying to update status without uploading DL. Please upload DL before verifying");
		}
		
		return resObj;
	}
}