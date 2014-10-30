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
import java.util.Objects;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
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
import indwin.c3.liftapp.pojos.NearbyRidersResponse;
import indwin.c3.liftapp.utils.GPSTracker;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class PassengerActivity extends FragmentActivity {

	// Google Map
	private GoogleMap googleMap;
	private MarkerOptions destMarker;
	private Marker mSource;
	private Marker mDestination;
	private final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.obj.equals("fail")) {

				Toast toast = Toast.makeText(getApplicationContext(),
						"Server Communication Error. Please Try Again",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			} else if (msg.obj.equals("norides")) {

				Toast.makeText(
						getApplicationContext(),
						"Sorry, No Rides available around you! Please try after some time or choose a different location",
						Toast.LENGTH_LONG).show();

			} else {
				googleMap.addMarker((MarkerOptions) msg.obj);
			}
			return false;
		}
	});
	// Variable for storing current date and time
	private int mYear, mMonth, mDay, mHour, mMinute;
	SupportMapFragment mMapFragment;
	GPSTracker gps;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.passenger_activity_main);
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
					R.id.map_passenger)).getMap();

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

	}

	public void mapAction() {
		googleMap.setMyLocationEnabled(true); // false to disable
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		// create class object
		gps = new GPSTracker(PassengerActivity.this);

		// check if GPS enabled
		if (gps.canGetLocation()) {

			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();
			// create marker
			MarkerOptions marker = new MarkerOptions()
					.position(new LatLng(latitude, longitude)).title("You!")
					.draggable(true);
			marker.snippet("My Current Location");
			marker.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.passenger_marker_icon));
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
		googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {

			@Override
			public void onMarkerDrag(Marker arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMarkerDragEnd(Marker arg0) {
				try {
					LatLng dragPosition = arg0.getPosition();
					double dragLat = dragPosition.latitude;
					double dragLong = dragPosition.longitude;
					Log.i("info", "on drag end :" + dragLat + " dragLong :"
							+ dragLong);
					// Getting the current position of the marker

					if (mDestination != null
							&& (mSource.getPosition()
									.equals(arg0.getPosition()) || mDestination
									.getPosition().equals(arg0.getPosition()))) {
						googleMap.clear();
					
						mSource = googleMap.addMarker(new MarkerOptions()
								.position(mSource.getPosition())
								.draggable(true)
								.snippet("My Start Point")
								.title("You!")
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.passenger_marker_icon)));

						mDestination = googleMap.addMarker(new MarkerOptions()
								.position(mDestination.getPosition())
								.draggable(true)
								.snippet("My Destination Point")
								.title("Destination")
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.home_marker_icon)));
						runCall(mSource.getPosition().latitude + ","
								+ mSource.getPosition().longitude,
								mDestination.getPosition().latitude + ","
										+ mDestination.getPosition().longitude,
								handler);

					}
				} catch (Exception e) {
					Log.e("Lift", "Error during marker drag ");
					Toast.makeText(getApplicationContext(),
							"Error during marker drag! Reported. ",
							Toast.LENGTH_LONG).show();

				}
			}

			@Override
			public void onMarkerDragStart(Marker arg0) {
				// TODO Auto-generated method stub

			}

		});

		// handler code

		googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng point) {
				// TODO Auto-generated method stub
				try {

					if (destMarker != null) { // if marker exists (not null or
												// whatever)
						mDestination.remove();
						onLongClickHelper(point, handler);
					} else {
						onLongClickHelper(point, handler);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}

	public void onLongClickHelper(LatLng point, final Handler handler) {

		final String srcgeocode = mSource.getPosition().latitude + ","
				+ mSource.getPosition().longitude;
		TextView f_datetime = (TextView) findViewById(R.id.txtDateP);

		if (f_datetime.getText().toString().trim().equals("")) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Choose Lift Time! ", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();

		} else {
			destMarker = new MarkerOptions().title("Destination");
			destMarker.position(point);
			destMarker.draggable(true);
			destMarker.snippet("My Destination Address");
			destMarker.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.home_marker_icon));
	
			// adding marker
			mDestination = googleMap.addMarker(destMarker);

			final String destgeocode = mDestination.getPosition().latitude
					+ "," + mDestination.getPosition().longitude;

			googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));

			googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));

			runCall(srcgeocode, destgeocode, handler);
		}
	}

	public void runCall(final String srcgeocode, final String destgeocode,
			final Handler handler) {

		try {
			TextView f_datetime = (TextView) findViewById(R.id.txtDateP);

			String str = f_datetime.getText().toString().trim();
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");

			Date date;

			date = df.parse(str);

			long epoch = date.getTime();
			final String datetime = String.valueOf(epoch);

			new Thread() {
				public void run() {
					try {
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 5000);
						NearbyRidersResponse nrr = new NearbyRidersResponse();
						HttpClient myClient = new DefaultHttpClient(
								httpParameters);
						String nrr_url = "http://192.168.1.6:8080/svcProject/user/getnn?userType=passenger&userID=11&srcgeocode="
								+ srcgeocode
								+ "&destgeocode="
								+ destgeocode
								+ "&starttime=" + datetime;

						HttpGet httpget = new HttpGet(nrr_url);
						HttpResponse response = myClient.execute(httpget);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
								"UTF-8");

						if (response.getStatusLine().getStatusCode() != 200) {

							Log.e("LiftCommunication", "Server returned code "
									+ response.getStatusLine().getStatusCode());
						} else {

							JSONArray respArr = new JSONArray(responseString);

							JSONObject respObj, respObj1, respObj2;
							if (respArr.length() == 0) {
								Message m_norides = new Message();
								m_norides.obj = "norides";
								handler.sendMessage(m_norides);
							}
							for (int i = 0; i < respArr.length(); i++) {
								respObj = (JSONObject) respArr.get(i);

								Double srcLat = Double.parseDouble(respObj
										.getString("srcgeocode").split(",")[0]);
								Double srcLong = Double.parseDouble(respObj
										.getString("srcgeocode").split(",")[1]);
								Double destLat = Double
										.parseDouble(respObj.getString(
												"destgeocode").split(",")[0]);
								Double destLong = Double
										.parseDouble(respObj.getString(
												"destgeocode").split(",")[1]);
								MarkerOptions markerSrcRider = new MarkerOptions()
										.position(new LatLng(srcLat, srcLong))
										.title("userID: "
												+ respObj.get("userID")
														.toString()
												+ "( "
												+ respObj
														.getString("srcdistance")
												+ " from your start location)")
										.snippet(
												"Can Pickup from here & is Headed to "
														+ respObj
																.getString("destination"));

								markerSrcRider
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

								MarkerOptions markerDstRider = new MarkerOptions()
										.position(new LatLng(destLat, destLong))
										.title("userID: "
												+ respObj.get("userID")
														.toString()
												+ "( "
												+ respObj
														.getString("destdistance")
												+ " from your destination)")
										.snippet(
												"Pickup from: "
														+ respObj
																.getString("source")
														+ " & is Headed here!")
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
								// adding marker
								Message msg = new Message();
								msg.obj = markerSrcRider;

								handler.sendMessage(msg);
								Message m2 = new Message();
								m2.obj = markerDstRider;

								handler.sendMessage(m2);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						Message m_fail = new Message();
						m_fail.obj = "failed";

						handler.sendMessage(m_fail);
					}
				}
			}.start();

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public int d, m, y, h, min;

	public void datepickerClicked(View v) {
		try {
			Toast.makeText(getApplicationContext(), "DATE TIME PICKER",

			Toast.LENGTH_SHORT).show();
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
							TextView txt = (TextView) findViewById(R.id.txtDateP);
							txt.setText(d + "-" + m + "-" + y + " " + h + ":"
									+ min);
							txt.setVisibility(View.VISIBLE);
							txt.setWidth(100);
							Button bt = (Button) findViewById(R.id.btnCalendarP);
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
		Toast.makeText(getApplicationContext(), "Sorry! Nothing here yet!",
				Toast.LENGTH_LONG).show();
	}

}
