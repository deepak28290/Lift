package a.service.data;

public class LocationDetails {
	
	private Double latitude;
	private Double longitude;
	private String distanceParams;
	
	public String getDistanceParams() {
		return distanceParams;
	}
	public void setDistanceParams(String distanceParams) {
		this.distanceParams = distanceParams;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
}
