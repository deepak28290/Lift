package indwins.c3.lift.source;

public class Request 
{
	public String requesterType;
	public long requesterID;
	public long accepterID;
	Request()
	{
		
	}
	public String getRequesterType() 
	{
		return requesterType;
	}
	public void setRequesterType(String requesterType) 
	{
		this.requesterType = requesterType;
	}
	public long getRequesterID() 
	{
		return requesterID;
	}
	public void setRequesterID(long requesterID) 
	{
		this.requesterID = requesterID;
	}
	public long getAccepterID() 
	{
		return accepterID;
	}
	public void setAccepterID(long accepterID) 
	{
		this.accepterID = accepterID;
	}
}
