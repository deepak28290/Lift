package indwin.c3.liftapp.utils;

import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerOptionsE {
	String userId;
	MarkerOptions markerOptions;
	String hasActiveRequests;
	public String getHasActiveRequests() {
		return hasActiveRequests;
	}

	public void setHasActiveRequests(String hasActiveRequests) {
		this.hasActiveRequests = hasActiveRequests;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public MarkerOptions getMarkerOptions() {
		return markerOptions;
	}

	public void setMarkerOptions(MarkerOptions markerOptions) {
		this.markerOptions = markerOptions;
	}
}
