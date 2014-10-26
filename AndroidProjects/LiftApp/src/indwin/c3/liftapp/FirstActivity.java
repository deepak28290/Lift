package indwin.c3.liftapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import indwin.c3.liftapp.R;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class FirstActivity extends FragmentActivity {

	// Google Map
	private GoogleMap googleMap;
	private MarkerOptions destMarker;
	private Marker mSource;
	private Marker mDestination;

	// Variable for storing current date and time
	private int mYear, mMonth, mDay, mHour, mMinute;
	SupportMapFragment mMapFragment;
	GPSTracker gps;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.firstactivity_main);
		/*
		 * TextView welcome = (TextView) findViewById(R.id.welcomemaps);
		 * welcome.setText("Hello " + prefs.getString("name", null) + "!");
		 */
		try {
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
		
	}

	@Override
	public void onAttachedToWindow() {

		mapAction();

		// markerClicked = false;

	}
public void mapAction(){
	googleMap.setMyLocationEnabled(true); // false to disable
	googleMap.getUiSettings().setMyLocationButtonEnabled(true);

	// create class object
	gps = new GPSTracker(FirstActivity.this);

	// check if GPS enabled
	if (gps.canGetLocation()) {

		double latitude = gps.getLatitude();
		double longitude = gps.getLongitude();
		// create marker
		MarkerOptions marker = new MarkerOptions()
				.position(new LatLng(latitude, longitude)).title("Pickup!")
				.draggable(true);
		marker.snippet("My Pickup Address");
		// adding marker
		mSource = googleMap.addMarker(marker);

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(latitude, longitude)).zoom(12).build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	} else {
		// can't get location
		// GPS or Network is not enabled
		// Ask user to enable GPS/network in settings
		gps.showSettingsAlert();
	}

	googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

		@Override
		public void onMapLongClick(LatLng point) {
			// TODO Auto-generated method stub

			if (destMarker != null) { // if marker exists (not null or
										// whatever)
				mDestination.remove();
				destMarker = new MarkerOptions().title("Destination");
				destMarker.position(point);
				destMarker.draggable(true);
				destMarker.snippet("My Destination Address");
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
				// adding marker
				destMarker.snippet("My Destination Address");
				mDestination = googleMap.addMarker(destMarker);
				googleMap.animateCamera(CameraUpdateFactory
						.newLatLng(point));
			}

		}

	});
}
	public void datepickerClicked(View v) {
		Toast.makeText(getApplicationContext(), "DATE TIME PICKER",
				Toast.LENGTH_SHORT).show();
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		// Launch Date Picker Dialog
		DatePickerDialog dpd = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						// Display Selected date in textbox
						TextView txtDate = (TextView) findViewById(R.id.txtDate);

						txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1)
								+ "-" + year);

					}
				}, mYear, mMonth, mDay);
		dpd.show();
	}

	public void timepickerClicked(View v) {
		Toast.makeText(getApplicationContext(), "DATE TIME PICKER",
				Toast.LENGTH_SHORT).show();
		final Calendar c = Calendar.getInstance();
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);

		// Launch Time Picker Dialog
		TimePickerDialog tpd = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						// Display Selected time in textbox
						TextView txtTime = (TextView) findViewById(R.id.txtTime);

						txtTime.setText(hourOfDay + ":" + minute);
					}
				}, mHour, mMinute, false);
		tpd.show();
	}

	public void submitClicked(View v) {
		try {
			TextView s_address = (TextView) findViewById(R.id.sourceaddress);
			TextView d_address = (TextView) findViewById(R.id.destinationaddress);
			TextView f_date = (TextView) findViewById(R.id.txtDate);
			TextView f_time = (TextView) findViewById(R.id.txtTime);
			String str = f_date.getText().toString().trim() + " " + f_time.getText().toString().trim();
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			Date date;
			date = df.parse(str);
			long epoch = date.getTime();

			if (s_address.getText().toString().trim().equals("")
					|| d_address.getText().toString().trim().equals("")
					|| f_date.getText().toString().trim().equals("")
					|| f_time.getText().toString().trim().equals("")) {
				// Your piece of code for example
				Toast toast = Toast.makeText(getApplicationContext(),
						"Provide complete trip details!", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
			} else {
				// submit data to server
				try {

					final JSONObject riderPayload = new JSONObject();
					/*
					 * { "userID"="1", "userType"="passenger",
					 * "source"="888 West 16th Avenue, Vancouver, BC V5Z, Canada"
					 * , "destination"=
					 * "4430-4440 20th Street, San Francisco, CA 94114, USA",
					 * "srcgeocode"="49.2569777,-123.123904",
					 * "destgeocode"="37.7577,-122.4376", "starttime"="" }
					 */

					riderPayload.put("userID", new Random().nextInt(1000));
					riderPayload.put("userType", "rider");
					riderPayload.put("source", s_address.getText().toString().trim());
					riderPayload.put("destination", d_address.getText().toString().trim());
					riderPayload.put(
							"srcgeocode",
							mSource.getPosition().latitude + ","
									+ mSource.getPosition().longitude);
					riderPayload.put("destgeocode",
							mDestination.getPosition().latitude + ","
									+ mDestination.getPosition().longitude);
					riderPayload.put("starttime", epoch);
					/*
					 * Toast toast = Toast.makeText(getApplicationContext(),
					 * "Source: "
					 * +mSource.getPosition().latitude+","+mSource.getPosition
					 * ().longitude+" Destination: "+mDestination.getPosition().
					 * latitude+","+mDestination.getPosition().longitude,
					 * Toast.LENGTH_LONG); toast.setGravity(Gravity.CENTER |
					 * Gravity.CENTER_HORIZONTAL, 0, 0); toast.show();
					 */
					Log.d("LIFT", mSource.getPosition().toString() + "  "
							+ mSource.getSnippet());
					Log.d("LIFT", mDestination.getPosition().toString() + "  "
							+ mSource.getSnippet());

					new Thread() {
						public void run() {
							try {
								HttpClient myClient = new DefaultHttpClient();
								HttpPost post = new HttpPost(
										"http://192.168.1.107:8080/svcProject/user/addonlineuser");
								post.setHeader("Content-type",
										"application/json");
								StringEntity entity = new StringEntity(
										riderPayload.toString());

								post.setEntity(entity);
								HttpResponse response = myClient.execute(post);

								if (response.getStatusLine().getStatusCode() != 200) {
									
								System.out.println("Call to Server Failed");
								}else{
									System.out.println("Call to Server Success");
									System.out.println("Response"+response.getEntity().toString());
								}

							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}.start();

				} catch (Exception e) {

				}
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
