package indwin.c3.liftapp;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import indwin.c3.liftapp.R;
import indwin.c3.liftapp.utils.GPSTracker;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class FirstActivity extends SidePanel {

	// Google Map
	private GoogleMap googleMap;
	private MarkerOptions destMarker;
	private Marker mSource;
	private Marker mDestination;
	private int h, m, y, d, min;
	// Variable for storing current date and time
	private int mYear, mMonth, mDay, mHour, mMinute;
	SupportMapFragment mMapFragment;
	GPSTracker gps;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	//	setContentView(R.layout.firstactivity_main);
		 ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
	        getLayoutInflater().inflate(R.layout.firstactivity_main, content, true);   
	 
		/*
		 * TextView welcome = (TextView) findViewById(R.id.welcomemaps);
		 * welcome.setText("Hello " + prefs.getString("name", null) + "!");
		 */
		try {

			gps = new GPSTracker(FirstActivity.this);

			// Loading map
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
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			if (((LiftAppGlobal) this.getApplication()).isGpsOn()) {
				((LiftAppGlobal) this.getApplication()).setGpsOn(false);

				Thread.sleep(500);

				mapAction();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAttachedToWindow() {

		mapAction();

		// markerClicked = false;

	}

	public void mapAction() {
	
		try{
			googleMap.setMyLocationEnabled(true); // false to disable
		
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		// check if GPS enabled
		gps = new GPSTracker(FirstActivity.this);
		double latitude, longitude;
		// check if GPS enabled
		if (gps.canGetLocation()) {

			latitude = gps.getLatitude();
			longitude = gps.getLongitude();

		} else {
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings

			latitude = 12.960986;
			longitude = 77.638732;
			Toast.makeText(
					getApplicationContext(),
					"Unable to get current location. Turn on GPS or drag start location to your pick up point!",
					Toast.LENGTH_LONG).show();

		}
		// create marker
		MarkerOptions marker = new MarkerOptions()
				.position(new LatLng(latitude, longitude)).title("Pickup!")
				.draggable(true);
		marker.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.passenger_marker_icon));
		marker.snippet("My Pickup Address");
		// adding marker
		mSource = googleMap.addMarker(marker);

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(latitude, longitude)).zoom(12).build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));

		googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {

			@Override
			public void onMarkerDrag(Marker arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMarkerDragEnd(Marker arg0) {
				LatLng dragPosition = arg0.getPosition();
				double dragLat = dragPosition.latitude;
				double dragLong = dragPosition.longitude;
				Log.i("info", "on drag end :" + dragLat + " dragLong :"
						+ dragLong);
				Toast.makeText(getApplicationContext(), "Marker Dragged..!",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onMarkerDragStart(Marker arg0) {

			}

		});
		googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng point) {

				if (destMarker != null) { // if marker exists (not null or
											// whatever)
					mDestination.remove();
					destMarker = new MarkerOptions().title("Destination");
					destMarker.position(point);

					destMarker.draggable(true);
					destMarker.snippet("My Destination Address");
					destMarker.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.home_marker_icon));

					// adding marker
					mDestination = googleMap.addMarker(destMarker);
					googleMap.animateCamera(CameraUpdateFactory
							.newLatLng(point));

					googleMap.animateCamera(CameraUpdateFactory
							.newLatLng(point));
				} else {
					destMarker = new MarkerOptions().title("Destination");
					destMarker.position(point);
					destMarker.draggable(true);
					destMarker.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.home_marker_icon));
					// adding marker
					destMarker.snippet("My Destination Address");
					mDestination = googleMap.addMarker(destMarker);
					googleMap.animateCamera(CameraUpdateFactory
							.newLatLng(point));
				}

			}

		});
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "Google Play Services not enabled!",

					Toast.LENGTH_SHORT).show();

		}
	}

	public void datepickerClicked(View v) {
		try {
			final Calendar c = Calendar.getInstance();
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);

			mHour = c.get(Calendar.HOUR_OF_DAY);
			mMinute = c.get(Calendar.MINUTE);

			// Launch Time Picker Dialog
			TimePickerDialog tpd = new TimePickerDialog(this,
					new TimePickerDialog.OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {
							h = hourOfDay;
							min = minute;
							TextView txt = (TextView) findViewById(R.id.txtDate);
							txt.setText(d + "-" + m + "-" + y + " " + h + ":"
									+ min);
							txt.setVisibility(View.VISIBLE);
							txt.setWidth(100);
							Button bt = (Button) findViewById(R.id.btnCalendar);
							bt.setText("Change lift timings");

						}
					}, mHour, mMinute, false);
			tpd.show();
			// Launch Date Picker Dialog
			DatePickerDialog dpd = new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {

						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							// Display Selected date-time in textbox
							d = dayOfMonth;
							m = monthOfYear + 1;
							y = year;

						}
					}, mYear, mMonth, mDay);
			dpd.show();

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "App Error! Try Again.",

			Toast.LENGTH_SHORT).show();
		} finally {

		}
	}

	public void submitClicked(View v) {
		final Handler handler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				Toast toast = Toast.makeText(
						getApplicationContext(),
						msg.obj.toString(), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();

				return false;
			}
		});
		try {
			TextView s_address = (TextView) findViewById(R.id.sourceaddress);
			TextView d_address = (TextView) findViewById(R.id.destinationaddress);
			TextView f_date = (TextView) findViewById(R.id.txtDate);
			String str = f_date.getText().toString().trim();
				
			if(mDestination==null){
				
				Toast toast = Toast.makeText(getApplicationContext(),
						"Long Tap on your exact destination on the Map!  ", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
			}else if(mSource==null){
				
				Toast toast = Toast.makeText(getApplicationContext(),
						"Error. Please go back and retry! ", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
			}else	if (s_address.getText().toString().trim().equals("")
					|| d_address.getText().toString().trim().equals("")
					|| f_date.getText().toString().trim().equals("")) {
				
				Toast toast = Toast.makeText(getApplicationContext(),
						"Add Landmarks and choose time to Lock your ride!", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
			} else {
				// submit data to server
				try {
					SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
					Date date;
					date = df.parse(str);
					long epoch = date.getTime();

					final JSONObject riderPayload = new JSONObject();
					/*
					 * { "userID"="1", "userType"="passenger",
					 * "source"="888 West 16th Avenue, Vancouver, BC V5Z, Canada"
					 * , "destination"=
					 * "4430-4440 20th Street, San Francisco, CA 94114, USA",
					 * "srcgeocode"="49.2569777,-123.123904",
					 * "destgeocode"="37.7577,-122.4376", "starttime"="" }
					 */
					
					riderPayload.put("userID",((LiftAppGlobal)this.getApplication()).getUserId());
					riderPayload.put("userType", "rider");
					riderPayload.put("source", s_address.getText().toString()
							.trim());
					riderPayload.put("destination", d_address.getText()
							.toString().trim());
					riderPayload.put(
							"srcgeocode",
							mSource.getPosition().latitude + ","
									+ mSource.getPosition().longitude);
					riderPayload.put("destgeocode",
							mDestination.getPosition().latitude + ","
									+ mDestination.getPosition().longitude);
					riderPayload.put("starttime", epoch);

					Log.d("LIFT", mSource.getPosition().toString() + "  "
							+ mSource.getSnippet());
					Log.d("LIFT", mDestination.getPosition().toString() + "  "
							+ mSource.getSnippet());
					
					new Thread() {
						public void run() {
							try {
								HttpClient myClient = new DefaultHttpClient();
								
								String call_url =getApplicationContext().getString(R.string.server_url)+ "/user/addonlineuser";
								HttpPost post = new HttpPost(
										call_url);
								post.setHeader("Content-type",
										"application/json");
								StringEntity entity = new StringEntity(
										riderPayload.toString());

								post.setEntity(entity);
								HttpResponse response = myClient.execute(post);
								HttpEntity ent = response.getEntity();
								String responseString = EntityUtils.toString(
										ent, "UTF-8");
								if (response.getStatusLine().getStatusCode() != 200) {

									Log.e("Lift", "Call to Server Failed");
									Message m_fail = new Message();
									m_fail.obj = "failed";

									handler.sendMessage(m_fail);
								} else {
									Log.i("Lift", "Call to Server Success");
									Log.i("Lift", "Response" + responseString);
									Message msg = new Message();
									msg.obj = "Ride succesfully Locked!";
									//String a = responseString;

									handler.sendMessage(msg);
								}

							} catch (IOException e) {
								e.getMessage();
								e.printStackTrace();
								Message m_fail = new Message();
								m_fail.obj = "failed";

								handler.sendMessage(m_fail);
							}
						}
					}.start();

				} catch (Exception e) {
					Message m_fail = new Message();
					m_fail.obj = "failed";

					handler.sendMessage(m_fail);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			Message m_fail = new Message();
			m_fail.obj = "Please check your input details!";

			handler.sendMessage(m_fail);
			
		}
	}
}
