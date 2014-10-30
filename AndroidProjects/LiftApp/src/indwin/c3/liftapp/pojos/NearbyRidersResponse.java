package indwin.c3.liftapp.pojos;
public class NearbyRidersResponse {

	String userID;
	String srcgeocode;
	String source;
	String srcdistance;
	String destgeocode;
	String destination;
	String destdistance;
	String starttime;

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getSrcgeocode() {
		return srcgeocode;
	}

	public void setSrcgeocode(String srcgeocode) {
		this.srcgeocode = srcgeocode;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSrcdistance() {
		return srcdistance;
	}

	public void setSrcdistance(String srcdistance) {
		this.srcdistance = srcdistance;
	}

	public String getDestgeocode() {
		return destgeocode;
	}

	public void setDestgeocode(String destgeocode) {
		this.destgeocode = destgeocode;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDestdistance() {
		return destdistance;
	}

	public void setDestdistance(String destdistance) {
		this.destdistance = destdistance;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

}
