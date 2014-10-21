package a.service.proj;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import a.service.data.DistanceType;
import a.service.data.LocationDetails;
import a.service.data.PotentialGuideResponse;
import a.service.data.ResponseStatus;

public class BusServiceHelper{

	public static PotentialGuideResponse getAllNearestBuses(String busNumber, String currentStop,String destinationStop) throws SQLException{

		Set<String> potentialGuideSet = null;
		List<LocationDetails> locationDetailsList = new ArrayList<LocationDetails>();
		Boolean upstream = getUpstreamValue(currentStop,destinationStop);
		if(upstream == null)
			return null;

		try{
			potentialGuideSet = DBConnectionHelper.getPotentialGuides(upstream == true ? "1" : "0", busNumber);
			for(String string: potentialGuideSet){
				LocationDetails location = new LocationDetails();
				List<String> coOrdinateList = Arrays.asList(string.split(","));
				location.setLatitude(Double.valueOf(coOrdinateList.get(0)));
				location.setLongitude(Double.valueOf(coOrdinateList.get(1)));
				locationDetailsList.add(location);
			}

		}catch(Exception e){
			return null;
		}

		if(!locationDetailsList.isEmpty()){

			LocationDetails fromLocation = DBConnectionHelper.setLocationFromArea(currentStop);
			LocationDetails toLocation = DBConnectionHelper.setLocationFromArea(destinationStop);

			if(fromLocation != null && toLocation != null){
				return sortAndReturnNearest(locationDetailsList, fromLocation, toLocation);
			}			
		}

		return null;
	}


	private static PotentialGuideResponse sortAndReturnNearest(List<LocationDetails> locationDetailsList, LocationDetails fromLocation, LocationDetails toLocation){
	
		PotentialGuideResponse response = new PotentialGuideResponse();
		List<LocationDetails> finalLocationDetails = new ArrayList<LocationDetails>();
		
		for(LocationDetails location: locationDetailsList){

			String travelParameters = getDistanceTime(location, fromLocation, toLocation,DistanceType.BUS_TO_DESTINATION);
			if(travelParameters != null){
				String secondSetParameters = getDistanceTime(location, fromLocation, toLocation,DistanceType.BUS_TO_DESTINATION_WAYPOINT);
				if( getDistanceFromParameters(travelParameters) >= getDistanceFromParameters(secondSetParameters)){
					String thirdSetParameters = getDistanceTime(location, fromLocation, toLocation, DistanceType.BUS_TO_CURRENT);
					location.setDistanceParams(travelParameters + "|" + thirdSetParameters);
					finalLocationDetails.add(location);
				}
			}			
		}		
		response.setResponseList(finalLocationDetails);	
		response.setResponseStatus(ResponseStatus.SUCCESS);
		return response;
	}
	
	private static String getDistanceTime(LocationDetails busLocation, LocationDetails currentStop, LocationDetails destinationStop, DistanceType distanceType){

		String distance = null;
		String time = null;
		String urlString = "";
		switch(distanceType){
			
		case BUS_TO_DESTINATION: 
			urlString = "http://maps.googleapis.com/maps/api/directions/xml?sensor=false&origin="+busLocation.getLatitude() + "+" +busLocation.getLongitude()
			+"&destination="+destinationStop.getLatitude() + "+" + destinationStop.getLongitude();
			break;
			
		case BUS_TO_DESTINATION_WAYPOINT:
			urlString = "http://maps.googleapis.com/maps/api/directions/xml?sensor=false&origin="+busLocation.getLatitude() + "+" +busLocation.getLongitude()
			+"&destination="+destinationStop.getLatitude() + "+" + destinationStop.getLongitude() + "&waypoints=" +currentStop.getLatitude() + "+" + currentStop.getLongitude();
			break;
			
		case BUS_TO_CURRENT:
			urlString = "http://maps.googleapis.com/maps/api/directions/xml?sensor=false&origin="+busLocation.getLatitude() + "+" +busLocation.getLongitude()
			+"&destination="+currentStop.getLatitude() + "+" + currentStop.getLongitude();
			break;
		
		default:
			break;
		}
		
		try{
			URL urlGoogleDirService = new URL(urlString);

			HttpURLConnection urlGoogleDirCon = (HttpURLConnection)urlGoogleDirService.openConnection();

			urlGoogleDirCon.setAllowUserInteraction( false );
			urlGoogleDirCon.setDoInput( true );
			urlGoogleDirCon.setDoOutput( false );
			urlGoogleDirCon.setUseCaches( true );
			urlGoogleDirCon.setRequestMethod("GET");
			urlGoogleDirCon.connect();

			DocumentBuilderFactory factoryDir = DocumentBuilderFactory.newInstance();
			DocumentBuilder parserDirInfo = factoryDir.newDocumentBuilder();
			Document docDir = parserDirInfo.parse(urlGoogleDirCon.getInputStream());
			urlGoogleDirCon.disconnect();

			NodeList nodeList = docDir.getElementsByTagName("distance");
			if(nodeList != null && nodeList.getLength() > 0){
				Element element = (Element) nodeList.item(nodeList.getLength() - 1);
				if(element != null){
					NodeList n1 = element.getElementsByTagName("text");

					if(n1 != null && n1.getLength() > 0) {
						Element el = (Element)n1.item(0);
						distance = el.getFirstChild().getNodeValue();
					}
				}
			} 
			
			NodeList nodeListTime = docDir.getElementsByTagName("duration");
			if(nodeListTime != null && nodeListTime.getLength() > 0){
				Element element = (Element) nodeListTime.item(nodeList.getLength() - 1);
				if(element != null){
					NodeList n1 = element.getElementsByTagName("text");

					if(n1 != null && n1.getLength() > 0) {
						Element el = (Element)n1.item(0);
						time = el.getFirstChild().getNodeValue();
					}
				}
			} 
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return distance+ "+" + time;
	}
	
	private static Long getDistanceFromParameters(String travelParameters){
		
		Long distance = null;
		
		if(travelParameters != null){
			distance = (long) Double.parseDouble(Arrays.asList(travelParameters.split(" ")).get(0));
		}
		return distance;
	}
	private static Boolean getUpstreamValue(String currentStop,String destinationStop) throws SQLException{
		Boolean upstream = null;

		Integer currentStopNumber = DBConnectionHelper.getStopNumber(currentStop);
		Integer destinationStopNumber = DBConnectionHelper.getStopNumber(destinationStop);

		if(currentStopNumber != null && destinationStopNumber != null){
			return (currentStopNumber - destinationStopNumber > 0 ? true: false);
		}
		return upstream;
	}
}