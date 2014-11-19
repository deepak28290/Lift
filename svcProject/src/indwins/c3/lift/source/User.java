package indwins.c3.lift.source;

public class User 
{
	public String userName;
	public String fbuserID;
	public String emailID;
	public String phone;
	public String gender;
	User()
	{

	}
	public String getName()
	{
		return this.userName;
	}
	public void setName(String userName)
	{
		this.userName = userName;
	}
	public String getFbUserID()
	{
		return fbuserID;
	}
	public void setFbUserID(String fbUserID) 
	{
		this.fbuserID = fbUserID;
	}
	public String getEmailID() 
	{
		return emailID;
	}
	public void setEmailID(String emailID) 
	{
		this.emailID = emailID;
	}
	public String getPhone() 
	{
		return phone;
	}
	public void setPhone(String phone) 
	{
		this.phone = phone;
	}
	public String getGender() 
	{
		return gender;
	}
	public void setGender(String gender) 
	{
		this.gender = gender;
	}
}
