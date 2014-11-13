package indwins.c3.lift.source;

public class OnlineUser 
{
	public  long userID;
	public  String userType;
	public  String source;
	public  String destination;
	public  String srcgeocode;
	public  String destgeocode;
	public 	String starttime;
	OnlineUser()
	{
		
	}
	public long getUserID() 
	{
		return userID;
	}
	public void setUserID(long userID) 
	{
		this.userID = userID;
	}
	public String getUserType() 
	{
		return userType;
	}
	public void setUserType(String userType) 
	{
		this.userType = userType;
	}
	public String getSource() 
	{
		return source;
	}
	public void setSource(String source) 
	{
		this.source = source;
	}
	public String getDestination() 
	{
		return destination;
	}
	public void setDestination(String destination) 
	{
		this.destination = destination;
	}
	public String getSrcGeoCode() 
	{
		return srcgeocode;
	}
	public void setSrcGeoCode(String srcGeoCode) 
	{
		this.srcgeocode = srcGeoCode;
	}
	public String getDestGeoCode() 
	{
		return destgeocode;
	}
	public void setDestGeoCode(String destGeoCode) 
	{
		this.destgeocode = destGeoCode;
	}
	public String getStarttime() 
	{
		return starttime;
	}
	public void setStarttime(String starttime) 
	{
		this.starttime = starttime;
	}
}
