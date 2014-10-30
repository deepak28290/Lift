package indwin.c3.liftapp.utils;

import indwin.c3.liftapp.pojos.NearbyRidersResponse;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GoogleMapHelper {
	
	public static NearbyRidersResponse getNearbyUsers() {
		/* userID:
		       srcgeocode
		       source
		       srcdistance
		       destgeocode
		       destination
		       destdistance
		       startime
		       
		       *
		       12.9561205,77.6545907        12.974356578068898,77.6386334002018
		       */
		
		final NearbyRidersResponse nrr=new NearbyRidersResponse();
		
		return nrr;
	}

}