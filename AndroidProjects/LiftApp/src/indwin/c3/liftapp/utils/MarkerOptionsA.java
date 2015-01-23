package indwin.c3.liftapp.utils;

import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerOptionsA {
	String senderusername;
	String accepterusername;
	MarkerOptions markerOptionsSrc;
	MarkerOptions markerOptionsDest;
	String senderuserid;
	String accepteruserid;
	String phone;
	String address;
	double srcLat;
	double srcLong;
	double destLat;
	double destLong;
	long time;

	public double getDestLat() {
		return destLat;
	}

	public void setDestLat(double destLat) {
		this.destLat = destLat;
	}

	public double getDestLong() {
		return destLong;
	}

	public void setDestLong(double destLong) {
		this.destLong = destLong;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getSrcLat() {
		return srcLat;
	}

	public void setSrcLat(double srcLat) {
		this.srcLat = srcLat;
	}

	public double getSrcLong() {
		return srcLong;
	}

	public void setSrcLong(double srcLong) {
		this.srcLong = srcLong;
	}

	public String getSenderusername() {
		return senderusername;
	}

	public void setSenderusername(String senderusername) {
		this.senderusername = senderusername;
	}

	public String getAccepterusername() {
		return accepterusername;
	}

	public void setAccepterusername(String accepterusername) {
		this.accepterusername = accepterusername;
	}

	public String getSenderuserid() {
		return senderuserid;
	}

	public void setSenderuserid(String senderuserid) {
		this.senderuserid = senderuserid;
	}

	public String getAccepteruserid() {
		return accepteruserid;
	}

	public void setAccepteruserid(String accepteruserid) {
		this.accepteruserid = accepteruserid;
	}

	public MarkerOptions getMarkerOptionsSrc() {
		return markerOptionsSrc;
	}

	public void setMarkerOptionsSrc(MarkerOptions markerOptionsSrc) {
		this.markerOptionsSrc = markerOptionsSrc;
	}

	public MarkerOptions getMarkerOptionsDest() {
		return markerOptionsDest;
	}

	public void setMarkerOptionsDest(MarkerOptions markerOptionsDest) {
		this.markerOptionsDest = markerOptionsDest;
	}

}
