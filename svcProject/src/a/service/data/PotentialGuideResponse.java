package a.service.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PotentialGuideResponse {
	
	private List<LocationDetails> responseList;
	private Integer responseStatus;
	
	public List<LocationDetails> getResponseList() {
		return responseList;
	}
	public void setResponseList(List<LocationDetails> responseList) {
		this.responseList = responseList;
	}
	public Integer getResponseStatus() {
		return responseStatus;
	}
	public void setResponseStatus(Integer responseStatus) {
		this.responseStatus = responseStatus;
	}	
}
