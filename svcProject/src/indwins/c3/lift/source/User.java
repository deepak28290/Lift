package indwins.c3.lift.source;

public class User 
{
	public String userName;
	public String docType;
	public String docID;	
	public String fbuserID;
	User()
	{

	}
	public void setName(String userName)
	{
		this.userName = userName;
	}	
	public String getName()
	{
		return this.userName;
	}
	public void setDocType(String docType)
	{
		this.docType = docType;
	}
	public String getDocType()
	{
		return this.docType;
	}
	public void setDocId(String docID)
	{
		this.docID = docID;
	}
	public String getDocId()
	{
		return this.docID;
	}
	public String getFbUserID()
	{
		return fbuserID;
	}
	public void setFbUserID(String fbUserID) 
	{
		this.fbuserID = fbUserID;
	}
}
