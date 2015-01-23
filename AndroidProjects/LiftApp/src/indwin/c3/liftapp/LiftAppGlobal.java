package indwin.c3.liftapp;

import indwin.c3.liftapp.pojos.LockedLiftPojo;
import android.app.Application;

public class LiftAppGlobal extends Application {

	private boolean sessionActive;
	private boolean isGpsOn = false;
	private String regId = "regId";
	private String userId="userId";
	private String user_name;
	private MessageDetails msgdetails;
	private boolean isNotif=false;
	private String reqid="reqid";
	private boolean fblogout=false;
	private LockedLiftPojo llp;
	private String pubUserId;

	public String getPubUserId() {
		return pubUserId;
	}

	public void setPubUserId(String pubUserId) {
		this.pubUserId = pubUserId;
	}

	public LockedLiftPojo getLlp() {
		return llp;
	}

	public void setLlp(LockedLiftPojo llp) {
		this.llp = llp;
	}

	public boolean isFblogout() {
		return fblogout;
	}

	public void setFblogout(boolean fblogout) {
		this.fblogout = fblogout;
	}

	public String getReqid() {
		return reqid;
	}

	public void setReqid(String reqid) {
		this.reqid = reqid;
	}

	public boolean isNotif() {
		return isNotif;
	}

	public void setNotif(boolean isNotif) {
		this.isNotif = isNotif;
	}

	public MessageDetails getMsgdetails() {
		return msgdetails;
	}

	public void setMsgdetails(MessageDetails msgdetails) {
		this.msgdetails = msgdetails;
	}

	public String getUser_name() {
		return user_name;
	}
	
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}

	public boolean isGpsOn() {
		return isGpsOn;
	}

	public void setGpsOn(boolean isGpsOn) {
		this.isGpsOn = isGpsOn;
	}

	public boolean getSessionActive() {
		return sessionActive;
	}

	public void setSessionActive(boolean sessionActive) {
		this.sessionActive = sessionActive;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
