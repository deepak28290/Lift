package indwin.c3.liftapp.pojos;

public class LockedLiftPojo {

	String userID;
	String source;
	String destination;
	String srcgeocode;
	String destgeocode;
	String isAccepted;
	String isCompleted;
	String starttime;
	boolean hasLockedRide;
	String userType;

	

	public boolean isHasLockedRide() {
		return hasLockedRide;
	}

	public void setHasLockedRide(boolean hasLockedRide) {
		this.hasLockedRide = hasLockedRide;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSrcgeocode() {
		return srcgeocode;
	}

	public void setSrcgeocode(String srcgeocode) {
		this.srcgeocode = srcgeocode;
	}

	public String getDestgeocode() {
		return destgeocode;
	}

	public void setDestgeocode(String destgeocode) {
		this.destgeocode = destgeocode;
	}

	public String getIsAccepted() {
		return isAccepted;
	}

	public void setIsAccepted(String isAccepted) {
		this.isAccepted = isAccepted;
	}

	public String getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(String isCompleted) {
		this.isCompleted = isCompleted;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

}
