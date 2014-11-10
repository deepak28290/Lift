package indwin.c3.liftapp;

import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import indwin.c3.liftapp.R;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class RequestDetailsActivity extends FragmentActivity {
	private final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.obj.equals("failed")) {

				Toast toast = Toast.makeText(getApplicationContext(),
						"Server Communication Error. Please Try Again",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			} else if (msg.obj.equals("success_acc")) {

				Toast.makeText(
						getApplicationContext(),
						"Request Accepted Succesfully!",
						Toast.LENGTH_LONG).show();
				Intent i = new Intent(getApplicationContext(), MyRequestsActivity.class);
				startActivity(i);

			}  else if (msg.obj.equals("success")) {

				Toast.makeText(
						getApplicationContext(),
						"Request Cancelled Succesfully!",
						Toast.LENGTH_LONG).show();
				Intent i = new Intent(getApplicationContext(), MyRequestsActivity.class);
				startActivity(i);

			} 
			return false;
		}
	});
	// Google Map
	private GoogleMap googleMap;
	private Marker mSource;
	// Variable for storing current date and time
	SupportMapFragment mMapFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.request_details);
		/*
		 * TextView welcome = (TextView) findViewById(R.id.welcomemaps);
		 * welcome.setText("Hello " + prefs.getString("name", null) + "!");
		 */
		try {

			// Loading map
			String type=	((LiftAppGlobal) getApplication()).getMsgdetails()
					.getType();
			ImageView iv=(ImageView) findViewById(R.id.addimage2);
			if(type.equals("sent")){
				
				iv.setVisibility(View.GONE);
			}else{
				iv.setVisibility(View.VISIBLE);
			}
			initilizeMap();
			// latitude and longitude
		} catch (Exception e) {
			e.printStackTrace();
		}
		// rest of the code
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map_req2)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps. Try again!",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Intent i = new Intent(getApplicationContext(), MyRequestsActivity.class);
		startActivity(i);

	
	}

	@Override
	public void onAttachedToWindow() {

		mapAction();

		// markerClicked = false;

	}

	/*final String reqId = ((LiftAppGlobal) getApplication()).getMsgdetails()
			.getRequestId();
*/
	public void mapAction() {
		googleMap.setMyLocationEnabled(false); // false to disable
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
	
		String from = ((LiftAppGlobal) getApplication()).getMsgdetails()
				.getFrom();
		String to = ((LiftAppGlobal) getApplication()).getMsgdetails().getTo();

		String name = ((LiftAppGlobal) getApplication()).getMsgdetails()
				.getName();

		String time = ((LiftAppGlobal) getApplication()).getMsgdetails()
				.getTime();

		/*
		 * String destcoord=((LiftAppGlobal) getApplication())
		 * .getMsgdetails().get();
		 */

		double srcSelflatitude, destSelflatitude, srcSelflongitude, destSelflongitude, srcOtherlatitude, destOtherlatitude, srcOtherlongitude, destOtherlongitude;

		srcSelflatitude = Double.parseDouble(((LiftAppGlobal) getApplication())
				.getMsgdetails().getSrcSelfCoord().split(",")[0]);
		srcSelflongitude = Double
				.parseDouble(((LiftAppGlobal) getApplication()).getMsgdetails()
						.getSrcSelfCoord().split(",")[1]);
		destSelflatitude = Double
				.parseDouble(((LiftAppGlobal) getApplication()).getMsgdetails()
						.getDesSelfCoord().split(",")[0]);
		destSelflongitude = Double
				.parseDouble(((LiftAppGlobal) getApplication()).getMsgdetails()
						.getDesSelfCoord().split(",")[1]);

		srcOtherlatitude = Double
				.parseDouble(((LiftAppGlobal) getApplication()).getMsgdetails()
						.getSrcOtherCoord().split(",")[0]);
		srcOtherlongitude = Double
				.parseDouble(((LiftAppGlobal) getApplication()).getMsgdetails()
						.getSrcOtherCoord().split(",")[1]);
		destOtherlatitude = Double
				.parseDouble(((LiftAppGlobal) getApplication()).getMsgdetails()
						.getDesOtherCoord().split(",")[0]);
		destOtherlongitude = Double
				.parseDouble(((LiftAppGlobal) getApplication()).getMsgdetails()
						.getDesOtherCoord().split(",")[1]);

		/*
		 * MarkerOptions marker1 = new MarkerOptions() .position(new
		 * LatLng(srcSelflatitude, srcSelflongitude))
		 * .title("Pickup!").draggable(false);
		 * marker1.icon(BitmapDescriptorFactory
		 * .fromResource(R.drawable.home_marker_icon));
		 * marker1.snippet("My Pickup Address");
		 * 
		 * // adding marker mSource = googleMap.addMarker(marker1);
		 * 
		 * MarkerOptions marker2 = new MarkerOptions() .position(new
		 * LatLng(destSelflatitude, destSelflongitude))
		 * .title("Destination!").draggable(false);
		 * marker2.icon(BitmapDescriptorFactory
		 * .fromResource(R.drawable.home_marker_icon));
		 * 
		 * marker2.snippet("My Destination Address");
		 * 
		 * mSource = googleMap.addMarker(marker2);
		 */
		MarkerOptions marker3 = new MarkerOptions()
				.position(new LatLng(srcOtherlatitude, srcOtherlongitude))
				.title("Rider's Pickup point!").draggable(false);
		marker3.icon(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

		marker3.snippet("Rider's Pickup Address");

		mSource = googleMap.addMarker(marker3);
		MarkerOptions marker4 = new MarkerOptions()
				.position(new LatLng(destOtherlatitude, destOtherlongitude))
				.title("Rider's Destination!")
				.draggable(false)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
				.snippet("Rider's destination address");

		mSource = googleMap.addMarker(marker4);

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(srcSelflatitude, srcSelflongitude)).zoom(12)
				.build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
		
	}

	public void clickProf(View v) {
		Toast toast = Toast.makeText(getApplicationContext(),
				"Nothing here yet!", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();

	}

	public void clickAdd(View v) {
			final Dialog dialog;
		dialog = new Dialog(RequestDetailsActivity.this);
		dialog.setContentView(R.layout.accept_dialog);
		dialog.setTitle("Are you sure you want to accept this request?");

		Button reqButton = (Button) dialog
				.findViewById(R.id.acceptButton);
		// if button is clicked, close the custom dialog
		reqButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				okAccept();
			}
		});
		Button profileButton = (Button) dialog
				.findViewById(R.id.backButton2);
		// if button is clicked, close the custom dialog
		profileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();

			}
		});
		dialog.show();

	}
	public void clickCancel(View v){
		final Dialog dialog;
		dialog = new Dialog(RequestDetailsActivity.this);
		dialog.setContentView(R.layout.cancel_dialog);
		dialog.setTitle("Are you sure you want to cancel this request?");

		Button reqButton = (Button) dialog
				.findViewById(R.id.cancelButton);
		// if button is clicked, close the custom dialog
		reqButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				okCancel();
			}
		});
		Button profileButton = (Button) dialog
				.findViewById(R.id.backButton1);
		// if button is clicked, close the custom dialog
		profileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();

			}
		});
		dialog.show();
	}
	
	
	public void okAccept() {
		new Thread() {
		public void run() {
			try {
				HttpClient myClient = new DefaultHttpClient();
				String call_url = getApplicationContext().getString(
						R.string.server_url)
						+ "/request/acceptrequest";

				final JSONObject riderPayload = new JSONObject();
				riderPayload.put("id",  ((LiftAppGlobal) getApplication()).getMsgdetails()
						.getRequestId());
			
				StringEntity entity = new StringEntity(
						riderPayload.toString());

				HttpPost post = new HttpPost(call_url);
				post.setHeader("Content-type", "application/json");

				post.setEntity(entity);
				HttpResponse response = myClient.execute(post);
				HttpEntity ent = response.getEntity();
				String responseString = EntityUtils.toString(ent, "UTF-8");
				if (response.getStatusLine().getStatusCode() != 200) {

					Log.e("Lift", "Call to Server Failed");
					Message m_fail = new Message();
					m_fail.obj = "failed";

					// handler.sendMessage(m_fail);
				} else {
					if(responseString.contains("failure")){
						Message m_fail = new Message();
						m_fail.obj = "failed";

						handler.sendMessage(m_fail);
					}else{
						Message m_cancel = new Message();
						m_cancel.obj = "success_acc";

						handler.sendMessage(m_cancel);
					}
				}

			} catch (Exception e) {
				e.getMessage();
				e.printStackTrace();
				Message m_fail = new Message();
				m_fail.obj = "failed";

				// handler.sendMessage(m_fail);
			}
		}
	}.start();

}
	public void okCancel() {
			new Thread() {
			public void run() {
				try {
					HttpClient myClient = new DefaultHttpClient();
					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/request/cancelrequest";

					final JSONObject riderPayload = new JSONObject();
					riderPayload.put("id",  ((LiftAppGlobal) getApplication()).getMsgdetails()
							.getRequestId());
					StringEntity entity = new StringEntity(
							riderPayload.toString());

				
					HttpPut put = new HttpPut(call_url);
					put.setHeader("Content-type", "application/json");

					put.setEntity(entity);
					HttpResponse response = myClient.execute(put);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");
					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("Lift", "Call to Server Failed");
						Message m_fail = new Message();
						m_fail.obj = "failed";

						// handler.sendMessage(m_fail);
					} else {
						if(responseString.contains("failure")){
							Message m_fail = new Message();
							m_fail.obj = "failed";

							handler.sendMessage(m_fail);
						}else{
							Message m_cancel = new Message();
							m_cancel.obj = "success";

							handler.sendMessage(m_cancel);
						}
					}

				} catch (Exception e) {
					e.getMessage();
					e.printStackTrace();
					Message m_fail = new Message();
					m_fail.obj = "failed";

					// handler.sendMessage(m_fail);
				}
			}
		}.start();

	}

	public void clickBack(View v) {
		Intent i = new Intent(getApplicationContext(), MyRequestsActivity.class);
		startActivity(i);

	}
}
