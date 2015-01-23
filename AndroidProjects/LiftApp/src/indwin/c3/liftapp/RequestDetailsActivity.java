package indwin.c3.liftapp;

import java.io.BufferedInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import indwin.c3.liftapp.R;
import indwin.c3.liftapp.pojos.RatingHelper;
import indwin.c3.liftapp.utils.ProfileHelper;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class RequestDetailsActivity extends SidePanel {
	
	private String pubUserId;
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
				
				((LiftAppGlobal) getApplication()).setReqid(((LiftAppGlobal) getApplication()).getMsgdetails()
						.getRequestId());
				Intent i = new Intent(getApplicationContext(), AcceptedActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);                  
				i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(i);
				finish();

			}  else if (msg.obj.equals("success")) {

				Toast.makeText(
						getApplicationContext(),
						"Request Cancelled Succesfully!",
						Toast.LENGTH_LONG).show();
				Intent i = new Intent(getApplicationContext(), MyRequestsActivity.class);
				startActivity(i);
				finish();

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
		 ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
	        getLayoutInflater().inflate(R.layout.request_details, content, true);   
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
			String status=((LiftAppGlobal) getApplication()).getMsgdetails().getStatus();

			ImageView iv2=(ImageView) findViewById(R.id.cancelimage2);
			
			if(!status.equals("pending")){
				iv.setVisibility(View.GONE);
				iv2.setVisibility(View.GONE);
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

	/*@Override
	protected void onResume() {
		super.onResume();
		
		Intent i = new Intent(getApplicationContext(), MyRequestsActivity.class);
		startActivity(i);
		finish();
	
	}*/

	@Override
	public void onAttachedToWindow() {

		mapAction();

		// markerClicked = false;

	}

	/*final String reqId = ((LiftAppGlobal) getApplication()).getMsgdetails()
			.getRequestId();
*/
	public void mapAction() {
		
		//populate user details
		String username=((LiftAppGlobal) getApplication()).getMsgdetails().getName();
		TextView tv=(TextView)findViewById(R.id.name2);
		tv.setText(username);
	
		String userid=((LiftAppGlobal) getApplication()).getMsgdetails().getUserid();
		
		//populate photo
		popUserPic(userid);
		googleMap.setMyLocationEnabled(false); // false to disable
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
	
		String from = ((LiftAppGlobal) getApplication()).getMsgdetails()
				.getFrom();
		String to = ((LiftAppGlobal) getApplication()).getMsgdetails().getTo();

		String name = ((LiftAppGlobal) getApplication()).getMsgdetails()
				.getName();

		String time = ((LiftAppGlobal) getApplication()).getMsgdetails()
				.getTime();
		
		pubUserId= ((LiftAppGlobal) getApplication()).getMsgdetails().getUserid();
	
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
				.title(name+"'s Pickup point!").draggable(false);
		marker3.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.passenger_marker_icon));

		marker3.snippet(from);

		mSource = googleMap.addMarker(marker3);
		MarkerOptions marker4 = new MarkerOptions()
				.position(new LatLng(destOtherlatitude, destOtherlongitude))
				.title(name+"'s Destination!")
				.draggable(false)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.home_marker_icon))
				.snippet(to);

		mSource = googleMap.addMarker(marker4);

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(srcSelflatitude, srcSelflongitude)).zoom(12)
				.build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
		
	}
	
	public void popUserPic(final String userid){
		final Handler profHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				if (((ProfileHelper) msg.obj).getType().equals("failed")) {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Server Communication Error. Please Try Again",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				}  else if (((ProfileHelper) msg.obj).getType().equals("image")) {
					
					
					if (((ProfileHelper) msg.obj).getBmp() != null) {
				
								ImageView iv = (ImageView)findViewById(R.id.userimage2);

								iv.setImageBitmap(DrawerHomeActivity.getCroppedBitmap(((ProfileHelper) msg.obj).getBmp(), 70));
					}
				}
				return false;
			}
		});
		
		new Thread() {
			public void run() {
				try {

					String imageURL;
					Bitmap bitmap = null;
					Log.d("Lift", "Loading Picture");

					/*
					 * imageURL = fb ? "http://graph.facebook.com/" + userID +
					 * "/picture?type=large" : getApplicationContext()
					 * .getString(R.string.server_url) +
					 * "/user/getphoto?userID=" + userID + "&docType=photo";
					 */
					imageURL = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/getphoto?userID="
							+ userid
							+ "&docType=photo";

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(
							httpParameters, 5000);
					HttpClient client = new DefaultHttpClient(
							httpParameters);

					HttpGet request = new HttpGet(imageURL);
					HttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();

					bitmap = BitmapFactory
							.decodeStream(new BufferedInputStream(entity
									.getContent()));
					Message m_img = new Message();
					ProfileHelper pe = new ProfileHelper();
					pe.setBmp(bitmap);
					pe.setType("image");
					pe.setUserid(userid);
					m_img.obj = pe;
					profHandler.sendMessage(m_img);
				} catch (Exception e) {
					Log.d("TAG", "Loading Picture FAILED");
					e.printStackTrace();
				}
			}
		}.start();
		final Handler ratehandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				
				if(((RatingHelper)msg.obj).getStatus().equals("failed")){
					
					Toast.makeText(getApplicationContext(), "Error Contacting Server! Check your internet connection.",
							Toast.LENGTH_LONG).show();
					
				}else{
				RatingBar rating = (RatingBar)findViewById(R.id.rating2);
		
				
				rating.setRating(((RatingHelper)msg.obj).getRating());
				/*TextView rides=(TextView)findViewById(R.id.rides);
				rides.setText("Lifts so far: "+((RatingHelper)msg.obj).getLifts());
				*/
				}
				return false;
	
			}
		});
	
		
		//getRating
		
		new Thread() {
			public void run() {
				try {

					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/rating/getrating?userID=" + userid;
					
					Log.d("Lift", "Loading Rating ");
					
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(
							httpParameters, 5000);
					HttpClient client = new DefaultHttpClient(
							httpParameters);

					HttpGet request = new HttpGet(call_url);
					HttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();
					
					String responseString = EntityUtils.toString(entity,
							"UTF-8");

					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("LiftCommunication", "Server returned code "
								+ response.getStatusLine().getStatusCode());
						Message m_fail = new Message();
						
						RatingHelper rh=new RatingHelper();
						
						rh.setStatus("failed");
						m_fail.obj = rh;
						ratehandler.sendMessage(m_fail);
						
					} else {
						if (responseString.contains("failure")) {
						
	
						} else if (responseString.contains("success")) {
							JSONObject result=new JSONObject(responseString);
							RatingHelper rh=new RatingHelper();
							if(result.getString("message").contains("user not rated")){
								rh.setLifts("0");
								rh.setRating(0f);
							}else{
						
								rh.setLifts(result.getString("count").trim());
								rh.setRating(Float.parseFloat(result.getString("score")));
							
							}
							Message m1 = new Message();
							
							rh.setStatus("success");
							m1.obj = rh;
							ratehandler.sendMessage(m1);

						}
					}
				} catch (Exception e) {
					Log.d("TAG", "Loading Picture FAILED");
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void clickProf(View v) {
		
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		Editor editor = pref.edit();
		editor.putBoolean("pub_user_id", true);
		editor.commit();
		
	/*	((LiftAppGlobal) this.getApplication()).setPubUserId(pubUserId);*/
		Intent intent = new Intent(this, PublicProfileActivity.class);
		startActivity(intent);
	}
	public Dialog dialog;
	public void clickAdd(View v) {
			
		dialog = new Dialog(RequestDetailsActivity.this);
		dialog.setContentView(R.layout.accept_dialog);
		dialog.setTitle("Are you sure you want to accept this request?");

		
		ImageButton reqButton = (ImageButton) dialog
				.findViewById(R.id.acceptit);
		// if button is clicked, close the custom dialog
		reqButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				okAccept();
			}
		});
		ImageButton profileButton = (ImageButton) dialog
				.findViewById(R.id.nowait);
		// if button is clicked, close the custom dialog
		profileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();

			}
		});
		dialog.show();

	}
	
/*	public void nowaitButton(View v){
			
		dialog.dismiss();
			
			
	}*/
	/*public void acceptitButton(View v){
		
		okAccept();
			
			
	}*/
	/*public void cancelitButton(View v){
		
		okCancel();
			
			
	}*/
	public void clickCancel(View v){
		final Dialog dialog;
		dialog = new Dialog(RequestDetailsActivity.this);
		dialog.setContentView(R.layout.cancel_dialog);
		dialog.setTitle("Are you sure you want to cancel this request?");

		dialog.show();
		ImageButton cancelButton = (ImageButton) dialog
				.findViewById(R.id.cancelit);
		// if button is clicked, close the custom dialog
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				okCancel();
			}
		});
		ImageButton profileButton = (ImageButton) dialog
				.findViewById(R.id.nowait);
		// if button is clicked, close the custom dialog
		profileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();

			}
		});
	}
	
	
	public void okAccept() {
		new Thread() {
		public void run() {
			try {
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(
						httpParameters, 5000);
				HttpClient myClient = new DefaultHttpClient(
						httpParameters);

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
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(
							httpParameters, 5000);
					HttpClient myClient = new DefaultHttpClient(
							httpParameters);

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
		/*Intent i = new Intent(getApplicationContext(), MyRequestsActivity.class);
		startActivity(i);*/
		finish();
	}
}
