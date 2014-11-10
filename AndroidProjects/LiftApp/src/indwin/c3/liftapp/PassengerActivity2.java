package indwin.c3.liftapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
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
import indwin.c3.liftapp.utils.MapWrapperLayout;
import indwin.c3.liftapp.utils.MarkerOptionsE;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

@SuppressLint({ "SimpleDateFormat", "InflateParams" })
public class PassengerActivity2 extends DrawerHomeActivity implements
		OnMarkerClickListener {
	private ViewGroup infoWindow;
	private TextView infoTitle;
	private TextView infoSnippet;
	// private Button infoButton;
	// private OnInfoWindowElemTouchListener infoButtonListener;
	private HashMap<String, String> hmp = new HashMap<String, String>();
	private HashMap<String, MarkerOptionsE> h_map = new HashMap<String, MarkerOptionsE>();
	// Google Map
	private GoogleMap googleMap;
	private MarkerOptions destMarker;
	private Marker mSource;
	private Marker mDestination;
	private static Dialog dialog;
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

			} else if (msg.obj.equals("norides")) {

				Toast.makeText(
						getApplicationContext(),
						"Sorry, No Rides available around you! Please try after some time or choose a different location",
						Toast.LENGTH_LONG).show();

			} else if (msg.obj.equals("req_success")) {
				Toast toast = Toast
						.makeText(
								getApplicationContext(),
								"Request Successfully Sent! Modify in My Requests Tab!",
								Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
				dialog.dismiss();
				// TO-DO redirect to my requests tab

			} else if (msg.obj.equals("req_server_error")) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"Unable to contact GCM Server. Try again!",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			} else if (msg.obj.equals("reg_id_error")) {
				Toast toast = Toast
						.makeText(
								getApplicationContext(),
								"Registered ID not found on server. Go back and try again",
								Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
				dialog.dismiss();

			} else if (msg.obj.equals("accepter_unavailable")) {
				Toast toast = Toast
						.makeText(
								getApplicationContext(),
								"Sorry, you're late. This user is no longer available!",
								Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
				regenMap();
				runCall(mSource.getPosition().latitude + ","
						+ mSource.getPosition().longitude,
						mDestination.getPosition().latitude + ","
								+ mDestination.getPosition().longitude, handler);
				dialog.dismiss();
				System.out.println("come to dadddy");
			} else {
				String id=(googleMap.addMarker(((MarkerOptionsE) msg.obj)
						.getMarkerOptions())).getId();
				hmp.put(id,
						((MarkerOptionsE) msg.obj).getUserId());
				h_map.put(id,
						((MarkerOptionsE) msg.obj));
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
		 ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
	        getLayoutInflater().inflate(R.layout.passenger_activity_main, content, true);   
	        
	//	setContentView(R.layout.passenger_activity_main);
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
	try{
		
	
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map_passenger)).getMap();
			final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
			mapWrapperLayout.init(googleMap, getPixelsFromDp(this, 0));// 39 +20
			this.infoWindow = (ViewGroup) getLayoutInflater().inflate(
					R.layout.rider_infowindow_layout, null);
			// update user logged in name in infowindow
			this.infoTitle = (TextView) infoWindow.findViewById(R.id.title);
			this.infoSnippet = (TextView) infoWindow.findViewById(R.id.snippet);

			googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
				@Override
				public View getInfoWindow(Marker marker) {
					// Setting up the infoWindow with current's marker info
					infoTitle.setText(marker.getTitle());
					infoSnippet.setText(marker.getSnippet());
					// infoButtonListener.setMarker(marker);
					// We must call this to set the current marker and
					// infoWindow references
					// to the MapWrapperLayout
					mapWrapperLayout
							.setMarkerWithInfoWindow(marker, infoWindow);
					return infoWindow;

				}

				@Override
				public View getInfoContents(Marker marker) {
					return null;
				}
			});
			// 20

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}catch(Exception e){
		e.printStackTrace();
		Toast.makeText(getApplicationContext(), "Google Play Services not enabled!",

				Toast.LENGTH_SHORT).show();

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

	}

	public void mapAction() {
try{
		googleMap.setMyLocationEnabled(true); // false to disable
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		// create class object
		gps = new GPSTracker(PassengerActivity2.this);

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
						regenMap();
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
		googleMap.setOnMarkerClickListener((OnMarkerClickListener) this);
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
		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {

				if (mDestination != null
						&& (mDestination.equals(marker) || mSource
								.equals(marker))) {
					Toast toast = Toast.makeText(getApplicationContext(),
							" This is you!", Toast.LENGTH_LONG);
					toast.show();

				} else if (mDestination == null) {
					Toast toast = Toast.makeText(getApplicationContext(),
							" This is you!", Toast.LENGTH_LONG);
					toast.show();
				} else {
					final Marker marker_ = marker;

					// see if pending notification exists
					Set<String> s=h_map.keySet();
					String marker_id=marker.getId();
					MarkerOptionsE me=h_map.get(marker.getId());
					if(me!=null)
					if(me.getHasActiveRequests().equals("1")){
						Toast toast = Toast.makeText(
								getApplicationContext(),
								" You have pending requests from this user. Check My Requests Tab!",
								Toast.LENGTH_LONG);
						toast.show();

						
					}else{
					dialog = new Dialog(PassengerActivity2.this);
					dialog.setContentView(R.layout.request_dialog);
					dialog.setTitle("You are almost there..");

					Button reqButton = (Button) dialog
							.findViewById(R.id.ReqButton);
					// if button is clicked, close the custom dialog
					reqButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							sendRequest(handler, marker_.getId());
						}
					});
					Button profileButton = (Button) dialog
							.findViewById(R.id.profileButton);
					// if button is clicked, close the custom dialog
					profileButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast toast = Toast.makeText(
									getApplicationContext(),
									" Wanna View Profile? Nothing here yet!",
									Toast.LENGTH_LONG);
							toast.show();

						}
					});
					dialog.show();
					}

				}
			}
		});
}catch(Exception e){
	e.printStackTrace();
	Toast.makeText(getApplicationContext(), "Google Play Services not enabled!",

			Toast.LENGTH_SHORT).show();

}
	}

	public void onLongClickHelper(LatLng point, final Handler handler) {

		final String srcgeocode = mSource.getPosition().latitude + ","
				+ mSource.getPosition().longitude;
		TextView f_datetime = (TextView) findViewById(R.id.txtDateP);

		if (f_datetime.getText().toString().trim().equals("")) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Choose Lift Time! ", Toast.LENGTH_LONG);
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

			regenMap();
			runCall(srcgeocode, destgeocode, handler);

		}
	}

	public void sendRequest(final Handler handler, final String markerId) {
		new Thread() {
			public void run() {
				try {
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient myClient = new DefaultHttpClient(httpParameters);

					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/request/addrequest";
					// get Requester ID for marker
					String acceptorID;
					acceptorID = hmp.get(markerId);

					final JSONObject riderPayload = new JSONObject();
					riderPayload.put("requesterType", "passenger");
					riderPayload.put("requesterID",
							((LiftAppGlobal) getApplication()).getUserId());
					riderPayload.put("accepterID", acceptorID);
					StringEntity entity = new StringEntity(
							riderPayload.toString());
					HttpPost post = new HttpPost(call_url);
					post.setHeader("Content-type", "application/json");

					post.setEntity(entity);
					HttpResponse response = myClient.execute(post);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");

					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("LiftCommunication", "Server returned code "
								+ response.getStatusLine().getStatusCode());
						Message m_fail = new Message();
						m_fail.obj = "failed";

						handler.sendMessage(m_fail);

					} else {
						if (responseString.contains("success")) {
							Message m_req = new Message();
							m_req.obj = "req_success";
							handler.sendMessage(m_req);

						} else if (responseString.contains("server error")) {
							Message m_serv_err = new Message();
							m_serv_err.obj = "req_server_error";
							handler.sendMessage(m_serv_err);
						} else if (responseString.contains("registration")) {
							Message m_serv_err = new Message();
							m_serv_err.obj = "reg_id_error";
							handler.sendMessage(m_serv_err);
						} else if (responseString.contains("not available now")) {
							Message m_serv_err = new Message();
							m_serv_err.obj = "accepter_unavailable";
							handler.sendMessage(m_serv_err);
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
		dialog.dismiss();
		regenMap();
		runCall(mSource.getPosition().latitude + ","
				+ mSource.getPosition().longitude,
				mDestination.getPosition().latitude + ","
						+ mDestination.getPosition().longitude, handler);

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
						HttpClient myClient = new DefaultHttpClient(
								httpParameters);

						String nrr_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/user/getnn?userType=passenger&userID="
								+ ((LiftAppGlobal) getApplication())
										.getUserId()
								+ "&srcgeocode="
								+ srcgeocode
								+ "&destgeocode="
								+ destgeocode
								+ "&starttime=" + datetime;

						HttpGet httpget = new HttpGet(nrr_url);
						HttpResponse response = myClient.execute(httpget);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
								"UTF-8");
						String userid=((LiftAppGlobal) getApplication()).getUserId();
						if (response.getStatusLine().getStatusCode() != 200) {

							Log.e("LiftCommunication", "Server returned code "
									+ response.getStatusLine().getStatusCode());
							Message m_fail = new Message();
							m_fail.obj = "failed";

							handler.sendMessage(m_fail);
						} else {

							JSONArray respArr = new JSONArray(responseString);

							JSONObject respObj;
							if (respArr.length() == 0) {
								Message m_norides = new Message();
								m_norides.obj = "norides";
								handler.sendMessage(m_norides);
							}

							for (int i = 0; i < respArr.length(); i++) {
								respObj = (JSONObject) respArr.get(i);
								if(respObj.getString("userID").equals(userid)){
									//do nothing
								}else{
								// handle already sent/received req case
								String hasActiveRequests = respObj
										.getString("hasactiverequest");

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
										.title(respObj.getString("srcdistance")
												+ " from your start location")
										.snippet(
												"Can Pickup from here & is Headed to "
														+ respObj
																.getString("destination"));
								// "userID: "+ respObj.get("userID").toString()+
								// "( "+
								markerSrcRider
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

								MarkerOptions markerDstRider = new MarkerOptions()
										.position(new LatLng(destLat, destLong))
										.title(respObj
												.getString("destdistance")
												+ " from your destination")
										.snippet(
												"Pickup from: "
														+ respObj
																.getString("source")
														+ " & is Headed here!")
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
								// "userID: " + respObj.get("userID")
								// .toString() + "( " +
								// adding marker
								MarkerOptionsE me_src = new MarkerOptionsE();
								me_src.setMarkerOptions(markerSrcRider);
								me_src.setUserId(respObj.getString("userID"));
								me_src.setHasActiveRequests(hasActiveRequests);
								Message msg = new Message();
								msg.obj = me_src;

								handler.sendMessage(msg);

								MarkerOptionsE me_dest = new MarkerOptionsE();
								me_dest.setMarkerOptions(markerDstRider);
								me_dest.setUserId(respObj.getString("userID"));
								me_dest.setHasActiveRequests(hasActiveRequests);
								Message m2 = new Message();
								m2.obj = me_dest;

								handler.sendMessage(m2);
							}
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
							ImageView iv=(ImageView)findViewById(R.id.clock);
							iv.setVisibility(View.GONE);
							/*Button bt = (Button) findViewById(R.id.btnCalendarP);
							bt.setText("Change lift timings");
						*/	if (mDestination != null) {
								regenMap();
								runCall(mSource.getPosition().latitude + ","
										+ mSource.getPosition().longitude,
										mDestination.getPosition().latitude
												+ ","
												+ mDestination.getPosition().longitude,
										handler);
							} else {
								Toast.makeText(
										getApplicationContext(),
										"Long Press to Mark Destination on Map",

										Toast.LENGTH_LONG).show();

							}
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

	public void regenMap() {
		googleMap.clear();

		mSource = googleMap.addMarker(new MarkerOptions()
				.position(mSource.getPosition())
				.draggable(true)
				.snippet("My Start Point")
				.title("You!")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.passenger_marker_icon)));

		if (mDestination != null) {
			mDestination = googleMap.addMarker(new MarkerOptions()
					.position(mDestination.getPosition())
					.draggable(true)
					.snippet("My Destination Point")
					.title("Destination")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.home_marker_icon)));
		}
	}

	public void clickNotifyMe(View v) {
		Toast.makeText(getApplicationContext(), "Sorry! Nothing here yet!",
				Toast.LENGTH_LONG).show();
	}
	
	public void clickRefresh(View v){
	
		EditText eTime=(EditText)findViewById(R.id.txtDateP);
		
		if(mSource==null||mDestination==null||eTime.getText().toString().trim().equals("")){
			Toast toast = Toast.makeText(getApplicationContext(),
					"Select time and mark destination on map!",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
					0);
			toast.show();
		}else{
			
			regenMap();
		runCall(mSource.getPosition().latitude + ","
				+ mSource.getPosition().longitude,
				mDestination.getPosition().latitude + ","
						+ mDestination.getPosition().longitude, handler);
		
		}
	}
	public static int getPixelsFromDp(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		if (mDestination != null) {

			if (arg0.equals(mDestination) || arg0.equals(mSource)) {
				// update user logged in name in infowindow
				TextView name = (TextView) infoWindow
						.findViewById(R.id.username);
				name.setText(((LiftAppGlobal) this.getApplication())
						.getUser_name());

			}

		} else if (arg0.equals(mSource)) {
			// update user logged in name in infowindow
			TextView name = (TextView) infoWindow.findViewById(R.id.username);
			name.setText(((LiftAppGlobal) this.getApplication()).getUser_name());

		}
		return false;
	}
}
