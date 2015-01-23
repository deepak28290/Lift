package indwin.c3.liftapp.utils;

import indwin.c3.liftapp.pojos.ProfileData;
import android.graphics.Bitmap;

public class ProfileHelper {

	Bitmap bmp;
	ProfileData data;
	String username;
	String userid;
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public ProfileData getData() {
		return data;
	}
	public void setData(ProfileData data) {
		this.data = data;
	}
	public Bitmap getBmp() {
		return bmp;
	}
	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	String type;
	
}
