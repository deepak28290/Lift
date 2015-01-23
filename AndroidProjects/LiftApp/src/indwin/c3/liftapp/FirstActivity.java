package indwin.c3.liftapp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import indwin.c3.liftapp.CustomAutoCompleteTextView.CustomAutoCompleteInterface;
import indwin.c3.liftapp.R;
import indwin.c3.liftapp.pojos.LockedLiftPojo;
import indwin.c3.liftapp.utils.GPSTracker;
import indwin.c3.liftapp.utils.MapWrapperLayout;
import indwin.c3.liftapp.utils.MarkerOptionsE;
import indwin.c3.liftapp.utils.PlaceJSONParser;
import indwin.c3.liftapp.utils.ProfileHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class FirstActivity extends SidePanel implements
		CustomAutoCompleteInterface, OnMarkerClickListener {
	private HashMap<String, String> hmp = new HashMap<String, String>();
	private HashMap<String, MarkerOptionsE> h_map = new HashMap<String, MarkerOptionsE>();
	private boolean locked = false;
	private ViewGroup infoWindow;
	private TextView infoTitle;
	private TextView infoSnippet;
	private static Dialog dialog;
	// Google Map
	private GoogleMap googleMap;
	private MarkerOptions destMarker;
	private Marker mSource;
	private Marker mDestination;
	private int h, m, y, d, min;
	private boolean isStarted = false;

	private int mYear, mMonth, mDay, mHour, mMinute;
	public static String srcadd="";
	public static String destadd="";
	
	SupportMapFragment mMapFragment;
	GPSTracker gps;
	AutoCompleteTextView srcPlace;
	AutoCompleteTextView destPlace;
	PlacesTask placesTask;
	ParserTask parserTask;

	final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.obj.equals("failed")) {
				toggleServerLoading(false);
				Toast toast = Toast.makeText(getApplicationContext(),
						"Network Error. Please Try Again", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			} else if (msg.obj.equals("norides")) {

				toggleServerLoading(false);
				if (locked == true) {
					toggleCroutons(
							FirstActivity.this,
							"No lift seekers available around you right now! Refresh or try on another route or time.");
				} else {
					toggleCroutons(
							FirstActivity.this,
							"No lift seekers available around you right now! Refresh or try on another route or time.\nYou can also tap on Lock icon on top to save this route and make yourself visible to LIFT seekers");
				}
			} else if (msg.obj.equals("req_server_error")) {
				toggleServerLoading(false);
				Toast toast = Toast.makeText(getApplicationContext(),
						"Unable to contact GCM Server. Try again!",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			} else if (msg.obj.equals("req_success")) {
				toggleCroutons(FirstActivity.this,
						"Request succesfully sent! View details in my requests tab in the side panel.");
				dialog.dismiss();
				// TO-DO redirect to my requests tab

			} else if (msg.obj.equals("req_server_error")) {
				toggleServerLoading(false);
				Toast toast = Toast.makeText(getApplicationContext(),
						"Unable to contact GCM Server. Try again!",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			} else if (msg.obj.equals("reg_id_error")) {
				toggleServerLoading(false);
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
				toggleServerLoading(false);
				toggleCroutons(
						FirstActivity.this,
						"Sorry, you're late. This user is no longer available!\nPlease refresh to find other lift seekers around you.");

				String dest_address = destadd;/* getAddressFromLatLng(mDestination
						.getPosition());*/
				String src_address = srcadd;/*getAddressFromLatLng(mSource.getPosition());
*/
				regenMap(src_address, dest_address);

				runCall(mSource.getPosition().latitude + ","
						+ mSource.getPosition().longitude,
						mDestination.getPosition().latitude + ","
								+ mDestination.getPosition().longitude);
				dialog.dismiss();
				System.out.println("come to dadddy");

			} else {
				toggleServerLoading(false);
				String id = (googleMap.addMarker(((MarkerOptionsE) msg.obj)
						.getMarkerOptions())).getId();
				hmp.put(id, ((MarkerOptionsE) msg.obj).getUserId());
				h_map.put(id, ((MarkerOptionsE) msg.obj));
				toggleCroutons(
						FirstActivity.this,
						"Showing available lift seekers in the map!\nTap on marker window to view complete profile and to send them a LIFT request.");

			}

			return false;
		}
	});

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// setContentView(R.layout.firstactivity_main);
		ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
		getLayoutInflater().inflate(R.layout.firstactivity_main, content, true);

		srcPlace = (AutoCompleteTextView) findViewById(R.id.sourceaddress);
		srcPlace.setThreshold(1);
		// ((CustomAutoCompleteTextView) srcPlace).setCallback(this);
		srcPlace.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				placesTask = new PlacesTask();
				placesTask.execute(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

		});

		destPlace = (AutoCompleteTextView) findViewById(R.id.destinationaddress);
		destPlace.setThreshold(1);
		// ((CustomAutoCompleteTextView) destPlace).setCallback(this);
		destPlace.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				placesTask = new PlacesTask();
				placesTask.execute(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

		});
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
		if (isStarted == false) {
			checkLiftLocked();
		} else {
			final Toast tag = Toast.makeText(getBaseContext(),
					"Tap on clock to choose Lift time!", Toast.LENGTH_SHORT);

			new CountDownTimer(10000, 1000) {

				public void onTick(long millisUntilFinished) {
					tag.show();
				}

				public void onFinish() {
					tag.show();
				}

			}.start();

		}
	}

	MapWrapperLayout mapWrapperLayout;

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
			mapWrapperLayout.init(googleMap, getPixelsFromDp(this, 0));// 39
																		// +20

			infoWindow = (ViewGroup) getLayoutInflater().inflate(
					R.layout.rider_infowindow_layout, null);
			// update user logged in name in infowindow
			infoTitle = (TextView) infoWindow.findViewById(R.id.title);
			infoSnippet = (TextView) infoWindow.findViewById(R.id.snippet);
			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	public static int getPixelsFromDp(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {

			SharedPreferences pref = getApplicationContext()
					.getSharedPreferences("MyPref", 0);
			/*
			 * if (((LiftAppGlobal) this.getApplication()).isGpsOn()) {
			 * ((LiftAppGlobal) this.getApplication()).setGpsOn(false);
			 */
			if (pref.getBoolean("is_gps_on", false)) {
				pref.getBoolean("is_gps_on", false);
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

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	// Fetches all places from GooglePlaces AutoComplete Web Service
	private class PlacesTask extends AsyncTask<String, Void, String> {

		@SuppressWarnings("deprecation")
		@Override
		protected String doInBackground(String... place) {
			// For storing data from web service
			String data = "";

			// Obtain browser key from https://code.google.com/apis/console
			String key = "key=AIzaSyAgs7VF1yBTuDyz5Oi85SWswtHQ10Cg7ko";

			String input = "";

			try {
				input = "input=" + URLEncoder.encode(place[0]);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			// place type to be searched
			String types = "types=geocode";

			// Sensor enabled
			String sensor = "sensor=false";

			// Building the parameters to the web service
			String parameters = input + "&" + types + "&" + sensor + "&" + key;

			// Output format
			String output = "json";

			// Building the url to the web service
			String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"
					+ output + "?" + parameters;

			try {
				// Fetching the data from we service
				data = downloadUrl(url);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			// Creating ParserTask
			parserTask = new ParserTask();

			// Starting Parsing the JSON string returned by Web Service
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;

			PlaceJSONParser placeJsonParser = new PlaceJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				// Getting the parsed data as a List construct
				places = placeJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return places;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {

			String[] from = new String[] { "description" };
			int[] to = new int[] { android.R.id.text1 };

			// Creating a SimpleAdapter for the AutoCompleteTextView
			SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result,
					android.R.layout.simple_list_item_1, from, to);

			// Setting the adapter
			srcPlace.setAdapter(adapter);
			destPlace.setAdapter(adapter);
		}
	}

	public void mapAction() {

		try {

			googleMap.setMyLocationEnabled(true); // false to disable
			googleMap.setPadding(0, 200, 0, 0);
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

				Toast toast = Toast
						.makeText(
								getApplicationContext(),
								"Unable to get current location. Turn on GPS or drag start location to your pick up point!",
								Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			}
			// create marker
			LatLng pt = new LatLng(latitude, longitude);
			String src_address = getAddressFromLatLng(pt);
			MarkerOptions marker = new MarkerOptions()
					.position(new LatLng(latitude, longitude)).title("Pickup!")
					.draggable(true);
			marker.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.passenger_marker_icon));
			marker.snippet(src_address);
			// adding marker
			mSource = googleMap.addMarker(marker);
			srcadd= src_address;
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(latitude, longitude)).zoom(12).build();

			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			// update edittext with new address

			indwin.c3.liftapp.CustomAutoCompleteTextView srcView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddress);

			srcView.setText(src_address);
			googleMap.setOnMarkerClickListener((OnMarkerClickListener) this);
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
					if (mSource.getPosition().equals(arg0.getPosition())) {
						String src_address = getAddressFromLatLng(arg0
								.getPosition());
						indwin.c3.liftapp.CustomAutoCompleteTextView srcView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddress);

						srcView.setText(src_address);
						srcadd=src_address;
					}
					if (mDestination != null) {
						if (mDestination.getPosition().equals(
								arg0.getPosition())) {
							String dest_address = getAddressFromLatLng(arg0
									.getPosition());
							indwin.c3.liftapp.CustomAutoCompleteTextView destView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddress);
							destadd=dest_address;
							destView.setText(dest_address);
							runCall(mSource.getPosition().latitude + ","
									+ mSource.getPosition().longitude,
									mDestination.getPosition().latitude
											+ ","
											+ mDestination.getPosition().longitude);
						}
					}
				}

				@Override
				public void onMarkerDragStart(Marker arg0) {

				}

			});
			googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(LatLng point) {

					if (locked == false) {
						String dest_address = getAddressFromLatLng(point);
						destadd=dest_address;
						if (destMarker != null || mDestination != null) { 
							// whatever)
							mDestination.remove();

						}
						destMarker = new MarkerOptions().title("Destination");
						destMarker.position(point);

						destMarker.draggable(true);
						destMarker.snippet(dest_address);
						destMarker.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.home_marker_icon));

						// adding marker
						mDestination = googleMap.addMarker(destMarker);
						googleMap.animateCamera(CameraUpdateFactory
								.newLatLng(point));

						googleMap.animateCamera(CameraUpdateFactory
								.newLatLng(point));

						indwin.c3.liftapp.CustomAutoCompleteTextView destView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddress);

						destView.setText(dest_address);
						TextView f_date = (TextView) findViewById(R.id.txtDate);
						if (f_date.getText().toString().trim().equals("")) {
							toggleCroutons(FirstActivity.this,
									"Tap on clock to select your LIFT time.");
						} else {

							runCall(mSource.getPosition().latitude + ","
									+ mSource.getPosition().longitude,
									mDestination.getPosition().latitude
											+ ","
											+ mDestination.getPosition().longitude);

						}

					} else {

						toggleCroutons(FirstActivity.this,
								"Please tap on the Lock icon if you wish to modify your ride details!");
					}

				}

			});

			googleMap
					.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
						@Override
						public void onInfoWindowClick(Marker marker) {

							if (mDestination != null
									&& (mDestination.equals(marker) || mSource
											.equals(marker))) {
								Toast toast = Toast.makeText(
										getApplicationContext(),
										" This is you!", Toast.LENGTH_LONG);
								toast.show();

							} else if (mDestination == null) {
								Toast toast = Toast.makeText(
										getApplicationContext(),
										" This is you!", Toast.LENGTH_LONG);
								toast.show();
							} else {
								final Marker marker_ = marker;

								// see if pending notification exists
								MarkerOptionsE me = h_map.get(marker.getId());
								final String pubUserId = me.getUserId();
								if (me != null)
									if (me.getHasActiveRequests().equals("1")) {
										toggleCroutons(
												FirstActivity.this,
												" You have pending requests from this user. Check My Requests Tab!\n You can still send request to other lift seekers on this route.");

									} else {
										dialog = new Dialog(FirstActivity.this);
										dialog.setContentView(R.layout.offer_dialog);
										dialog.setTitle("You are almost there..");

										ImageButton reqButton = (ImageButton) dialog
												.findViewById(R.id.ReqButton);
										// if button is clicked, close the
										// custom dialog
										reqButton
												.setOnClickListener(new OnClickListener() {
													@Override
													public void onClick(View v) {
														// add online user
														acceptRequestFlow(marker_);

													}
												});
										ImageButton profileButton = (ImageButton) dialog
												.findViewById(R.id.profileButton);
										// if button is clicked, close the
										// custom dialog
										profileButton
												.setOnClickListener(new OnClickListener() {
													@Override
													public void onClick(View v) {

														SharedPreferences pref = getApplicationContext()
																.getSharedPreferences(
																		"MyPref",
																		0);
														Editor editor = pref
																.edit();
														editor.putString(
																"pub_user_id",
																pubUserId);
														editor.commit();
														/*
														 * ((LiftAppGlobal)
														 * getApplication())
														 * .setPubUserId
														 * (pubUserId);
														 */
														Intent intent = new Intent(
																getApplicationContext(),
																PublicProfileActivity.class);
														startActivity(intent);

													}
												});
										dialog.show();
									}

							}
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),
					"Google Play Services not enabled!",

					Toast.LENGTH_SHORT).show();

		}
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		final Marker mark = arg0;

		if (arg0.isInfoWindowShown()) {
			arg0.hideInfoWindow();
		}
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mark.showInfoWindow();

			}
		}, 2000);
		SharedPreferences pref = getApplicationContext().getSharedPreferences(
				"MyPref", 0);
		// TODO Auto-generated method stub
		if (mDestination != null) {

			if (arg0.equals(mDestination) || arg0.equals(mSource)) {
				// update user logged in name in infowindow
				getUserDataFromID(pref.getString("user_id", null), arg0);
				/*
				 * getUserDataFromID( ((LiftAppGlobal)
				 * getApplication()).getUserId(), arg0);
				 */
			} else {

				String userid = hmp.get(arg0.getId());
				getUserDataFromID(userid, arg0);

			}

		} else if (arg0.equals(mSource)) {
			// update user logged in name in infowindow
			getUserDataFromID(pref.getString("user_id", null), arg0);
			// getUserDataFromID(((LiftAppGlobal)
			// getApplication()).getUserId(),arg0);
		}
		return false;
	}

	String currUser;
	Bitmap currBmp;

	public void getUserDataFromID(final String userid, final Marker marker) {

		final Handler profHandler = new Handler(new Handler.Callback() {
			String username;
			String userId;

			@Override
			public boolean handleMessage(Message msg) {
				if (((ProfileHelper) msg.obj).getType().equals("failed")) {
					toggleServerLoading(false);
					Toast toast = Toast.makeText(getApplicationContext(),
							"Server Communication Error. Please Try Again",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else if (((ProfileHelper) msg.obj).getType().equals("name")) {
					toggleServerLoading(false);
					username = ((ProfileHelper) msg.obj).getUsername();
					SharedPreferences pref = getApplicationContext()
							.getSharedPreferences("MyPref", 0);
					if (pref.getString("user_id", null).equals(
							((ProfileHelper) msg.obj).getUserid())) {
						currUser = username;
					}

				} else if (((ProfileHelper) msg.obj).getType().equals("image")) {

					if (((ProfileHelper) msg.obj).getBmp() != null) {

						final Bitmap bmp = ((ProfileHelper) msg.obj).getBmp();
						userId = ((ProfileHelper) msg.obj).getUserid();
						SharedPreferences pref = getApplicationContext()
								.getSharedPreferences("MyPref", 0);
						if (pref.getString("user_id", null).equals(
								((ProfileHelper) msg.obj).getUserid())) {
							currBmp = DrawerHomeActivity.getCroppedBitmap(bmp,
									70);
						}
						// if (!marker.isInfoWindowShown())
						// marker.showInfoWindow();

						googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
							@Override
							public View getInfoWindow(Marker marker) {
								// Setting up the infoWindow with current's
								// marker info

								infoTitle.setText(marker.getTitle());
								infoSnippet.setText(marker.getSnippet());
								String useridmarker = hmp.get(marker.getId());
								ImageView iv = (ImageView) infoWindow
										.findViewById(R.id.profileimage);

								TextView name = (TextView) infoWindow
										.findViewById(R.id.username);

								if (useridmarker != null) {
									if (useridmarker.equals(userId)) {
										name.setText(username);
										iv.setImageBitmap(DrawerHomeActivity
												.getCroppedBitmap(bmp, 70));

									} else {

										name.setText("");

										iv.setImageResource(android.R.color.transparent);

									}
								} else {
									if (currBmp != null) {
										name.setText(currUser);
										iv.setImageBitmap(currBmp);
									} else {
										name.setText("");

										iv.setImageResource(android.R.color.transparent);

									}

								}

								// infoButtonListener.setMarker(marker);
								// We must call this to set the current marker
								// and
								// infoWindow references
								// to the MapWrapperLayout
								mapWrapperLayout.setMarkerWithInfoWindow(
										marker, infoWindow);
								return infoWindow;

							}

							@Override
							public View getInfoContents(Marker marker) {
								return null;
							}
						});
					}
					// iv.setVisibility(View.VISIBLE);
				}

				return false;
			}
		});

		// get name of user
		new Thread() {
			public void run() {
				try {

					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/getprofile?userID=" + userid;
					Log.d("Lift", "Loading User ");
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient client = new DefaultHttpClient(httpParameters);

					HttpGet request = new HttpGet(call_url);
					HttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity,
							"UTF-8");

					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("LiftCommunication", "Server returned code "
								+ response.getStatusLine().getStatusCode());
						Message m_fail = new Message();
						ProfileHelper pe = new ProfileHelper();
						pe.setType("failed");
						m_fail.obj = pe;
						handler.sendMessage(m_fail);

					} else {
						if (responseString.contains("failure")) {
							Message mfirst = new Message();
							ProfileHelper pe = new ProfileHelper();
							pe.setType("failed");
							mfirst.obj = pe;
							profHandler.sendMessage(mfirst);
						} else if (responseString.contains("success")) {
							JSONObject result = new JSONObject(responseString);
							ProfileHelper pe = new ProfileHelper();
							pe.setUsername(((JSONObject) result.get("result"))
									.getString("userName"));
							pe.setType("name");
							pe.setUserid(userid);
							Message m_name = new Message();
							m_name.obj = pe;
							profHandler.sendMessage(m_name);

						}
					}
				} catch (Exception e) {
					Log.d("TAG", "Loading Picture FAILED");
					e.printStackTrace();
				}
			}
		}.start();

		// get pic of user

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
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient client = new DefaultHttpClient(httpParameters);

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
	}

	public void sendRequest(final Handler handler, final String markerId) {
		toggleServerLoading(true);
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
					riderPayload.put("requesterType", "rider");

					SharedPreferences pref = getApplicationContext()
							.getSharedPreferences("MyPref", 0);

					riderPayload.put("requesterID",
							pref.getString("user_id", null));
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

				}
			}
		}.start();
		dialog.dismiss();
		String src = srcadd;//getAddressFromLatLng(mSource.getPosition());
		String dest = destadd;//getAddressFromLatLng(mDestination.getPosition());
		regenMap(src, dest);
		runCall(mSource.getPosition().latitude + ","
				+ mSource.getPosition().longitude,
				mDestination.getPosition().latitude + ","
						+ mDestination.getPosition().longitude);

	}

	public void regenMap(String src, String dest) {
		googleMap.clear();

		mSource = googleMap.addMarker(new MarkerOptions()
				.position(mSource.getPosition())
				.draggable(true)
				.snippet(src)
				.title("Pickup")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.passenger_marker_icon)));
		srcadd=src;
		destadd=dest;
		if (mDestination != null) {
			mDestination = googleMap.addMarker(new MarkerOptions()
					.position(mDestination.getPosition())
					.draggable(true)
					.snippet(dest)
					.title("Destination")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.home_marker_icon)));
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
							ImageView iv = (ImageView) findViewById(R.id.clock);
							iv.setVisibility(View.GONE);

							indwin.c3.liftapp.CustomAutoCompleteTextView dest = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddress);
							if (dest.getText().toString().equals("")) {
								toggleCroutons(FirstActivity.this,
										"Long tap at your destination location on Map!");

							} else {
								runCall(mSource.getPosition().latitude + ","
										+ mSource.getPosition().longitude,
										mDestination.getPosition().latitude
												+ ","
												+ mDestination.getPosition().longitude);
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
			dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
			dpd.show();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void disableFields() {

		indwin.c3.liftapp.CustomAutoCompleteTextView src = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddress);
		indwin.c3.liftapp.CustomAutoCompleteTextView dest = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddress);
		TextView f_date = (TextView) findViewById(R.id.txtDate);
		f_date.setEnabled(false);
		src.setInputType(0);
		dest.setInputType(0);
		src.setEnabled(false);
		dest.setEnabled(false);
		locked = true;
		ImageView iv = (ImageView) findViewById(R.id.lockride);
		iv.setImageResource(R.drawable.locked);

	}

	public void enableFields() {

		indwin.c3.liftapp.CustomAutoCompleteTextView src = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddress);
		indwin.c3.liftapp.CustomAutoCompleteTextView dest = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddress);
		TextView f_date = (TextView) findViewById(R.id.txtDate);
		ImageView iv = (ImageView) findViewById(R.id.lockride);
		iv.setImageResource(R.drawable.unlocked);
		src.setInputType(InputType.TYPE_CLASS_TEXT);
		src.setEnabled(true);
		dest.setEnabled(true);
		dest.setInputType(InputType.TYPE_CLASS_TEXT);
		f_date.setEnabled(true);
		f_date.setClickable(true);
		locked = false;

	}

	public void submitClicked(View v) {
		final Handler notifhandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				if (msg.obj.equals("failed")) {
					toggleServerLoading(false);
					Toast toast = Toast.makeText(getApplicationContext(),
							"Server Communication Error. Please Try Again",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else {

					toggleServerLoading(false);
					// change button text to ride locked

					disableFields();

					// disable marker drag/long press/edit text fields

				}

				return false;

			}
		});

		final Handler handler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				toggleServerLoading(false);
				toggleCroutons(
						FirstActivity.this,
						"Lift details saved! You are now visible to lift seekers.\n We'll notify you if someone wishes to ride with you.");

				// change button text to ride locked
				findViewById(R.id.map).setEnabled(false);

				// call nearest passenger api

				/*
				 * runCall(mSource.getPosition().latitude + "," +
				 * mSource.getPosition().longitude,
				 * mDestination.getPosition().latitude + "," +
				 * mDestination.getPosition().longitude);
				 */
				// disable marker drag/long press/edit text fields

				disableFields();
				return false;
			}
		});

		try {

			if (locked == false) {

				TextView s_address = (TextView) findViewById(R.id.sourceaddress);
				TextView d_address = (TextView) findViewById(R.id.destinationaddress);
				TextView f_date = (TextView) findViewById(R.id.txtDate);
				String str = f_date.getText().toString().trim();

				if (mDestination == null) {

					toggleCroutons(FirstActivity.this,
							"Long Tap on your exact destination on the Map!  ");

				} else if (mSource == null) {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Error. Please go back and retry! ",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
				} else if (s_address.getText().toString().trim().equals("")
						|| d_address.getText().toString().trim().equals("")
						|| f_date.getText().toString().trim().equals("")) {

					toggleCroutons(FirstActivity.this,
							"Add Landmarks and choose time to Lock your ride!");

				} else {
					// submit data to server
					try {
						SimpleDateFormat df = new SimpleDateFormat(
								"dd-MM-yyyy HH:mm");
						Date date;
						date = df.parse(str);
						long epoch = date.getTime();

						final JSONObject riderPayload = new JSONObject();

						SharedPreferences pref = getApplicationContext()
								.getSharedPreferences("MyPref", 0);
						riderPayload.put("userID",
								pref.getString("user_id", null));

						riderPayload.put("userType", "rider");

						riderPayload.put("source", s_address.getText()
								.toString().trim());

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
						Log.d("LIFT", mDestination.getPosition().toString()
								+ "  " + mSource.getSnippet());
						toggleServerLoading(true);
						new Thread() {
							public void run() {
								try {
									HttpParams httpParameters = new BasicHttpParams();
									HttpConnectionParams.setConnectionTimeout(
											httpParameters, 5000);
									HttpClient myClient = new DefaultHttpClient(
											httpParameters);

									String call_url = getApplicationContext()
											.getString(R.string.server_url)
											+ "/user/addonlineuser";
									HttpPost post = new HttpPost(call_url);
									post.setHeader("Content-type",
											"application/json");
									StringEntity entity = new StringEntity(
											riderPayload.toString());

									post.setEntity(entity);
									HttpResponse response = myClient
											.execute(post);
									HttpEntity ent = response.getEntity();
									String responseString = EntityUtils
											.toString(ent, "UTF-8");
									if (response.getStatusLine()
											.getStatusCode() != 200) {

										Log.e("Lift", "Call to Server Failed");
										Message m_fail = new Message();
										m_fail.obj = "failed";

										handler.sendMessage(m_fail);
									} else {
										Log.i("Lift", "Call to Server Success");
										Log.i("Lift", "Response"
												+ responseString);
										Message msg = new Message();
										msg.obj = "Lift succesfully Locked!";
										// String a = responseString;

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

						// call addonline for rider
						final JSONObject riderPayload2 = new JSONObject();
						  
						riderPayload2.put("userID", pref.getString("user_id", null));
						riderPayload2.put("userType", "rider");
						riderPayload2.put("source", s_address.getText()
								.toString().trim());
						riderPayload2.put("destination", d_address.getText()
								.toString().trim());
						riderPayload2.put(
								"srcgeocode",
								mSource.getPosition().latitude + ","
										+ mSource.getPosition().longitude);
						riderPayload2.put("destgeocode",
								mDestination.getPosition().latitude + ","
										+ mDestination.getPosition().longitude);
						riderPayload2.put("starttime", epoch);

						Log.d("LIFT", mSource.getPosition().toString() + "  "
								+ mSource.getSnippet());
						Log.d("LIFT", mDestination.getPosition().toString()
								+ "  " + mSource.getSnippet());

						new Thread() {
							public void run() {
								try {
									HttpParams httpParameters = new BasicHttpParams();
									HttpConnectionParams.setConnectionTimeout(
											httpParameters, 5000);
									HttpClient myClient = new DefaultHttpClient(
											httpParameters);

									String call_url = getApplicationContext()
											.getString(R.string.server_url)
											+ "/user/addonlineuser";
									HttpPost post = new HttpPost(call_url);
									post.setHeader("Content-type",
											"application/json");
									StringEntity entity = new StringEntity(
											riderPayload2.toString());

									post.setEntity(entity);
									HttpResponse response = myClient
											.execute(post);
									HttpEntity ent = response.getEntity();
									String responseString = EntityUtils
											.toString(ent, "UTF-8");

									if (response.getStatusLine()
											.getStatusCode() != 200) {

										Log.e("Lift", "Call to Server Failed");
										Message m_fail = new Message();
										m_fail.obj = "failed";

										notifhandler.sendMessage(m_fail);
									} else {
										Log.i("Lift", "Call to Server Success");
										Log.i("Lift", "Response"
												+ responseString);
										Message msg = new Message();
										msg.obj = "Notifications on!";
										// String a = responseString;

										notifhandler.sendMessage(msg);
									}

								} catch (IOException e) {
									e.getMessage();
									e.printStackTrace();
									Message m_fail = new Message();
									m_fail.obj = "failed";

									notifhandler.sendMessage(m_fail);
								}
							}
						}.start();

					} catch (Exception e) {
						Message m_fail = new Message();
						m_fail.obj = "failed";

						handler.sendMessage(m_fail);
					}
				}
			} else {

				dialog = new Dialog(FirstActivity.this);
				dialog.setContentView(R.layout.confirm_unlock_dialog);
				dialog.setTitle("Reset Lift Details?");
				ImageButton reqButton = (ImageButton) dialog
						.findViewById(R.id.ReqButton);
				// if button is clicked, close the custom dialog
				reqButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						toggleServerLoading(false);
						enableFields();
						toggleCroutons(
								FirstActivity.this,
								"You're no longer visible to lift seekers on this route.\n You can edit time or route of your LIFT now.");
						dialog.dismiss();
						deleteEntryFromOnlineTable();
					}
				});
				ImageButton profileButton = (ImageButton) dialog
						.findViewById(R.id.nowaitButton);
				// if button is clicked, close the custom dialog
				profileButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();

					}
				});
				dialog.show();

			}
		} catch (Exception e1) {
			e1.printStackTrace();

		}
	}

	private void acceptRequestFlow(final Marker marker) {
		final Handler notifhandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				if (msg.obj.equals("failed")) {
					toggleServerLoading(false);
					Toast toast = Toast.makeText(getApplicationContext(),
							"Server Communication Error. Please Try Again",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else {

					// change button text to ride locked

					// disableFields();
					toggleServerLoading(false);
					ImageView iv = (ImageView) findViewById(R.id.lockride);
					iv.setImageResource(R.drawable.locked);

					sendRequest(handler, marker.getId());
					// disable marker drag/long press/edit text fields

				}

				return false;

			}
		});
		try {

			TextView s_address = (TextView) findViewById(R.id.sourceaddress);
			TextView d_address = (TextView) findViewById(R.id.destinationaddress);
			TextView f_date = (TextView) findViewById(R.id.txtDate);
			String str = f_date.getText().toString().trim();
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			Date date;
			date = df.parse(str);
			long epoch = date.getTime();

			final JSONObject riderPayload2 = new JSONObject();
			  SharedPreferences pref = getApplicationContext()
						.getSharedPreferences("MyPref", 0);
			riderPayload2.put("userID",
					pref.getString("user_id", null));
			riderPayload2.put("userType", "rider");
			riderPayload2.put("source", s_address.getText().toString().trim());
			riderPayload2.put("destination", d_address.getText().toString()
					.trim());
			riderPayload2.put("srcgeocode", mSource.getPosition().latitude
					+ "," + mSource.getPosition().longitude);
			riderPayload2.put(
					"destgeocode",
					mDestination.getPosition().latitude + ","
							+ mDestination.getPosition().longitude);
			riderPayload2.put("starttime", epoch);

			Log.d("LIFT",
					mSource.getPosition().toString() + "  "
							+ mSource.getSnippet());
			Log.d("LIFT", mDestination.getPosition().toString() + "  "
					+ mSource.getSnippet());
			toggleServerLoading(true);
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
								+ "/user/addonlineuser";
						HttpPost post = new HttpPost(call_url);
						post.setHeader("Content-type", "application/json");
						StringEntity entity = new StringEntity(
								riderPayload2.toString());

						post.setEntity(entity);
						HttpResponse response = myClient.execute(post);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
								"UTF-8");

						if (response.getStatusLine().getStatusCode() != 200) {

							Log.e("Lift", "Call to Server Failed");
							Message m_fail = new Message();
							m_fail.obj = "failed";

							notifhandler.sendMessage(m_fail);
						} else {
							Log.i("Lift", "Call to Server Success");
							Log.i("Lift", "Response" + responseString);
							Message msg = new Message();
							msg.obj = "success";
							// String a = responseString;

							notifhandler.sendMessage(msg);
						}

					} catch (IOException e) {
						e.getMessage();
						e.printStackTrace();
						Message m_fail = new Message();
						m_fail.obj = "failed";

						notifhandler.sendMessage(m_fail);
					}
				}
			}.start();
		} catch (Exception e) {

		}
	}

	private String getAddressFromLatLng(LatLng loc) {
		Geocoder geocoder;

		List<Address> addresses;
		try {
			geocoder = new Geocoder(this, Locale.getDefault());
			addresses = geocoder
					.getFromLocation(loc.latitude, loc.longitude, 1);

			String address = addresses.get(0).getAddressLine(0);
			String city = addresses.get(0).getAddressLine(1);
			String country = addresses.get(0).getAddressLine(2);

			return address + " , " + city;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private LatLng getLatLongFromAddress(String address) {
		double lat = 0.0, lng = 0.0;

		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocationName(address, 1);
			if (addresses.size() > 0) {
				LatLng coord = new LatLng(addresses.get(0).getLatitude(),
						addresses.get(0).getLongitude());
				return coord;
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;
	}

	public void plotClicked(View v) {
		// TODO Auto-generated method stub\
		// get src dest values
		try {
			toggleServerLoading(true);
			indwin.c3.liftapp.CustomAutoCompleteTextView srcView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddress);
			indwin.c3.liftapp.CustomAutoCompleteTextView destView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddress);
			String src_address = srcView.getText().toString();
			String dest_address = destView.getText().toString();
			if (!src_address.equals("")) {
				googleMap.clear();
				// get src dest geo codes
				LatLng srcLoc = getLatLongFromAddress(src_address);
				if (srcLoc == null) {
					srcLoc = mSource.getPosition();
				}
				// set msrc and mdest to those geo codes
				srcadd=src_address;
				mSource = googleMap
						.addMarker(new MarkerOptions()
								.position(srcLoc)
								.draggable(true)
								.snippet("My Start Point")
								.title(src_address)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.passenger_marker_icon)));
				if (!dest_address.equals("")) {
					destadd=dest_address;
					LatLng destLoc = getLatLongFromAddress(dest_address);
					if (destLoc != null) {

						mDestination = googleMap
								.addMarker(new MarkerOptions()
										.position(destLoc)
										.draggable(true)
										.snippet("My Destination Point")
										.title(dest_address)
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.home_marker_icon)));
					} else {
						// Error fetching coordinates..
						toggleCroutons(FirstActivity.this,
								"Long Press to mark destination on Map!");
					}
				}
			}
			if (!dest_address.equals("")) {
				googleMap.clear();
				destadd=dest_address;
				mSource = googleMap
						.addMarker(new MarkerOptions()
								.position(mSource.getPosition())
								.draggable(true)
								.snippet("My Start Point")
								.title(src_address)
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.passenger_marker_icon)));

				LatLng destLoc = getLatLongFromAddress(dest_address);
				if (destLoc != null) {

					mDestination = googleMap
							.addMarker(new MarkerOptions()
									.position(destLoc)
									.draggable(true)
									.snippet("My Destination Point")
									.title(dest_address)
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.home_marker_icon)));
				} else {
					// Error fetching coordinates..
					toggleCroutons(FirstActivity.this,
							"Long Press to mark destination on Map!");

				}
			}

			if (src_address.equals("") && dest_address.equals("")) {

				toggleCroutons(FirstActivity.this,
						"Long Press to mark destination on Map or long press on start pin to move it!");

			}

			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(mSource.getPosition()).zoom(12).build();

			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		} finally {
			toggleServerLoading(false);
		}
	}

	public void runCall(final String srcgeocode, final String destgeocode) {
		try {
			TextView f_datetime = (TextView) findViewById(R.id.txtDate);

			String str = f_datetime.getText().toString().trim();
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");

			Date date;

			date = df.parse(str);

			long epoch = date.getTime();
			final String datetime = String.valueOf(epoch);
			toggleServerLoading(true);
			new Thread() {
				public void run() {
					try {
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 5000);
						HttpClient myClient = new DefaultHttpClient(
								httpParameters);
						SharedPreferences pref = getApplicationContext()
								.getSharedPreferences("MyPref", 0);
						String nrr_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/user/getnn?userType=rider&userID="
								+ pref.getString("user_id", null)
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
						String userid = pref.getString("user_id", null);
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
								if (respObj.getString("userID").equals(userid)) {

									if (respArr.length() == 1) {
										Message m_norides = new Message();
										m_norides.obj = "norides";
										handler.sendMessage(m_norides);
									}
									// do nothing
								} else {
									// handle already sent/received req case
									String hasActiveRequests = respObj
											.getString("hasactiverequest");

									Double srcLat = Double
											.parseDouble(respObj.getString(
													"srcgeocode").split(",")[0]);
									Double srcLong = Double
											.parseDouble(respObj.getString(
													"srcgeocode").split(",")[1]);
									Double destLat = Double.parseDouble(respObj
											.getString("destgeocode")
											.split(",")[0]);
									Double destLong = Double
											.parseDouble(respObj.getString(
													"destgeocode").split(",")[1]);
									MarkerOptions markerSrcRider = new MarkerOptions()
											.position(
													new LatLng(srcLat, srcLong))
											.title(respObj
													.getString("srcdistance")
													+ " from your start location")
											.snippet(
													"Can Pickup from here & is Headed to "
															+ respObj
																	.getString("destination"));
									markerSrcRider
											.icon(BitmapDescriptorFactory
													.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

									MarkerOptions markerDstRider = new MarkerOptions()
											.position(
													new LatLng(destLat,
															destLong))
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

									MarkerOptionsE me_src = new MarkerOptionsE();
									me_src.setMarkerOptions(markerSrcRider);
									me_src.setUserId(respObj
											.getString("userID"));
									me_src.setHasActiveRequests(hasActiveRequests);
									Message msg = new Message();
									msg.obj = me_src;

									handler.sendMessage(msg);

									MarkerOptionsE me_dest = new MarkerOptionsE();
									me_dest.setMarkerOptions(markerDstRider);
									me_dest.setUserId(respObj
											.getString("userID"));
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

	@Override
	public void focusChanged() {
		// TODO Auto-generated method stub

	}

	@SuppressLint("ShowToast")
	public void checkLiftLocked() {
		isStarted = true;
		/*
		 * final Toast tag = Toast.makeText(getBaseContext(),
		 * "Loading....",Toast.LENGTH_SHORT); new CountDownTimer(4000, 1000) {
		 * 
		 * public void onTick(long millisUntilFinished) {tag.show();} public
		 * void onFinish() {tag.show();}
		 * 
		 * }.start();
		 */

		final Handler handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				toggleServerLoading(false);
				if (((LockedLiftPojo) msg.obj).isHasLockedRide() == false) {
					toggleCroutons(
							FirstActivity.this,
							"1.Long press on the map to select destination or use the text box on top.\n"
									+ "2. Long Press on your current marker and then drag it to change your start location.\n ");
				} else {
					googleMap.clear();
					populateSavedDetails(((LockedLiftPojo) msg.obj));
				}
				return false;
			}
		});
		toggleServerLoading(true);
		new Thread() {
			public void run() {
				try {

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient myClient = new DefaultHttpClient(httpParameters);
					
					SharedPreferences pref = getApplicationContext()
							.getSharedPreferences("MyPref", 0);
					String userid = pref.getString("user_id", null);
					
					String acc_req_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/haslockedride?userID="
							+ userid
							+ "&usertype=rider";

					HttpGet httpget = new HttpGet(acc_req_url);
					HttpResponse response = myClient.execute(httpget);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");

					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("LiftCommunication", "Server returned code "
								+ response.getStatusLine().getStatusCode());
						Message m_fail = new Message();
						m_fail.obj = "failed";

						failhandler.sendMessage(m_fail);

					} else {

						JSONObject respObj = new JSONObject(responseString);

						if (respObj.getString("haslockedride").equals("false")) {
							Message m_fail = new Message();
							LockedLiftPojo llp = new LockedLiftPojo();
							llp.setHasLockedRide(false);
							m_fail.obj = llp;

							handler.sendMessage(m_fail);

						} else {

							JSONObject resultObj = respObj
									.getJSONObject("data");

							LockedLiftPojo llp = new LockedLiftPojo();
							llp.setDestgeocode(resultObj
									.getString("destgeocode"));
							llp.setDestination(resultObj
									.getString("destination"));
							llp.setIsAccepted(resultObj.getString("isAccepted"));
							llp.setIsCompleted(resultObj
									.getString("isCompleted"));
							llp.setSource(resultObj.getString("source"));
							llp.setUserID(resultObj.getString("userID"));
							llp.setSrcgeocode(resultObj.getString("srcgeocode"));
							llp.setStarttime(resultObj.getString("starttime"));

							Message m1 = new Message();
							llp.setHasLockedRide(true);
							m1.obj = llp;

							handler.sendMessage(m1);

						}
					}

				} catch (Exception e) {

					e.printStackTrace();
					Message m_fail = new Message();
					m_fail.obj = "failed";

					failhandler.sendMessage(m_fail);

				}
			}
		}.start();

	}

	public void populateSavedDetails(LockedLiftPojo llp) {
		googleMap.setMyLocationEnabled(true); // false to disable
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		// shift states of all views to locked
		locked = true;
		indwin.c3.liftapp.CustomAutoCompleteTextView src = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddress);
		indwin.c3.liftapp.CustomAutoCompleteTextView dest = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddress);
		TextView f_date = (TextView) findViewById(R.id.txtDate);
		ImageView iv = (ImageView) findViewById(R.id.lockride);
		ImageView ivClock = (ImageView) findViewById(R.id.clock);
		f_date.setVisibility(View.VISIBLE);
		f_date.setEnabled(false);
		src.setInputType(0);
		dest.setInputType(0);
		ivClock.setVisibility(View.GONE);
		iv.setImageResource(R.drawable.locked);
		iv.setClickable(true);
		src.setEnabled(false);
		dest.setEnabled(false);

		// populate addresses
		src.setText(llp.getSource());
		dest.setText(llp.getDestination());

		// convert time to viewable format from epoch and populate field
		long epoch = Long.parseLong(llp.getStarttime());
		Date date = new Date(epoch);
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String formatted = format.format(date);
		f_date.setText(formatted);

		// populate Map
		// plot map
		double srclatitude = Double
				.parseDouble(llp.getSrcgeocode().split(",")[0]);
		double srclongitude = Double
				.parseDouble(llp.getSrcgeocode().split(",")[1]);
		double destlatitude = Double.parseDouble(llp.getDestgeocode()
				.split(",")[0]);
		double destlongitude = Double.parseDouble(llp.getDestgeocode().split(
				",")[1]);

		// adding source marker

		MarkerOptions markerSrc = new MarkerOptions()
				.position(new LatLng(srclatitude, srclongitude))
				.title("Pickup!").draggable(true);
		markerSrc.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.passenger_marker_icon));
		markerSrc.snippet(llp.getSource());
		srcadd=llp.getSource();
		mSource = googleMap.addMarker(markerSrc);

		MarkerOptions markerDest = new MarkerOptions()
				.position(new LatLng(destlatitude, destlongitude))
				.title("Destination").draggable(true);
		markerDest.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.home_marker_icon));
		markerDest.snippet(llp.getDestination());
		destadd=llp.getDestination();
		mDestination = googleMap.addMarker(markerDest);

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(srclatitude, srclongitude)).zoom(12).build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
		llp.setHasLockedRide(false);
		toggleCroutons(
				FirstActivity.this,
				"Showing your saved Lift details. Tap on Lock icon to edit!\nHit Refresh to find nearby lift seekers for this route.");
	}

	public void clickRefresh(View v) {

		EditText eTime = (EditText) findViewById(R.id.txtDate);

		if (mSource == null || mDestination == null
				|| eTime.getText().toString().trim().equals("")) {
			toggleCroutons(FirstActivity.this,
					"Select time and mark destination on map before Refreshing!");
	/*		Toast toast = Toast
					.makeText(
							getApplicationContext(),
							"Select time and mark destination on map before Refreshing!",
							Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();*/
		} else {
			runCall(mSource.getPosition().latitude + ","
					+ mSource.getPosition().longitude,
					mDestination.getPosition().latitude + ","
							+ mDestination.getPosition().longitude);

		}
	}

	public void deleteEntryFromOnlineTable() {

		final Handler unlockhandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				if (msg.obj.equals("failed")) {
					toggleServerLoading(false);
					Toast toast = Toast.makeText(getApplicationContext(),
							"Error contacting server!", Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else {

					// remove loading image
					toggleServerLoading(false);
				}

				// disable marker drag/long press/edit text fields

				// }

				return false;

			}
		});

		try {
			final JSONObject riderPayload = new JSONObject();
			SharedPreferences pref = getApplicationContext()
					.getSharedPreferences("MyPref", 0);
			riderPayload.put("userID",
					pref.getString("user_id", null));
			riderPayload.put("userType", "rider");
			toggleServerLoading(true);
			new Thread() {
				public void run() {
					try {
						HttpClient myClient = new DefaultHttpClient();

						String call_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/user/deleteonlineuser";
						HttpPost post = new HttpPost(call_url);
						post.setHeader("Content-type", "application/json");
						StringEntity entity = new StringEntity(
								riderPayload.toString());

						post.setEntity(entity);
						HttpResponse response = myClient.execute(post);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
								"UTF-8");
						if (response.getStatusLine().getStatusCode() != 200) {

							Log.e("Lift", "Call to Server Failed");
							Message m_fail = new Message();
							m_fail.obj = "failed";

							unlockhandler.sendMessage(m_fail);
						} else if (responseString.contains("failure")) {

							Log.e("Lift", "Call to Server Failed");
							Message m_fail = new Message();
							m_fail.obj = "failed";

							unlockhandler.sendMessage(m_fail);

						} else {

							Log.i("Lift", "Call to Server Success");
							Log.i("Lift", "Response" + responseString);
							Message msg = new Message();
							msg.obj = "Notifications off!";
							// String a = responseString;

							unlockhandler.sendMessage(msg);
						}

					} catch (IOException e) {
						e.getMessage();
						e.printStackTrace();
						Message m_fail = new Message();
						m_fail.obj = "failed";

						unlockhandler.sendMessage(m_fail);
					}
				}
			}.start();

		} catch (Exception e) {

			Log.e("Lift", e.getMessage());

		}
	}

	private void toggleCroutons(Activity activity, String text) {

		Crouton.cancelAllCroutons();
		if (activity == null) {

		} else {
			final Crouton crouton = Crouton.makeText(activity,
					text + "\n\n\t[X] CLOSE\t",
					de.keyboardsurfer.android.widget.crouton.Style.INFO,
					(ViewGroup) findViewById(R.id.croutonid)).setConfiguration(
					new Configuration.Builder().setDuration(
							Configuration.DURATION_INFINITE).build());
			crouton.show();

			crouton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Crouton.hide(crouton);
				}
			});
		}
	}

	private void toggleServerLoading(boolean show) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.overlay);
		if (show == true) {
			ll.setVisibility(View.VISIBLE);

		} else {
			ll.setVisibility(View.GONE);
		}
	}
	/*
	 * public void checkVerifStatus() {
	 * 
	 * final Handler verifyHandler = new Handler(new Handler.Callback() {
	 * 
	 * @Override public boolean handleMessage(Message msg) {
	 * 
	 * TextView tv = (TextView)findViewById(R.id.dlverifstatus); ImageView
	 * iv=(ImageView) findViewById(R.id.verificon); if (((String)
	 * msg.obj).equals("failed")) {
	 * 
	 * Toast.makeText(getApplicationContext(), "Error Contacting server!",
	 * Toast.LENGTH_LONG) .show();
	 * 
	 * }else if(((String) msg.obj).equals("verified")){
	 * 
	 * tv.setText("You're all set to use Lift!");
	 * iv.setImageResource(R.drawable.tick);
	 * 
	 * }else if(((String) msg.obj).equals("not_verified")){
	 * 
	 * tv.setText(
	 * "Email and Phone Verification Pending! Update Profile to resend code!");
	 * 
	 * }else if(((String) msg.obj).equals("only_email_verified")){
	 * 
	 * tv.setText("Phone Verification Pending!  Update Profile to resend code!");
	 * 
	 * }else if(((String) msg.obj).equals("only_phone_verified")){
	 * 
	 * tv.setText("Email Verification Pending! Update Profile to resend code!");
	 * 
	 * }
	 * 
	 * return false;
	 * 
	 * } });
	 * 
	 * final String userid = ((LiftAppGlobal) this.getApplication())
	 * .getUserId();
	 * 
	 * new Thread() { public void run() { try {
	 * 
	 * String url; Log.d("Lift", "Loading Picture");
	 * 
	 * url = getApplicationContext() .getString(R.string.server_url) +
	 * "/user/getverificationstatus?userID=" + userid; HttpClient client = new
	 * DefaultHttpClient(); HttpGet request = new HttpGet(url); HttpResponse
	 * response = client.execute(request); HttpEntity entity =
	 * response.getEntity(); String responseString =
	 * EntityUtils.toString(entity, "UTF-8");
	 * 
	 * if (response.getStatusLine().getStatusCode() != 200) {
	 * 
	 * Log.e("LiftCommunication", "Server returned code " +
	 * response.getStatusLine().getStatusCode()); Message m_fail = new
	 * Message(); m_fail.obj = "failed"; verifyHandler.sendMessage(m_fail);
	 * 
	 * } else {
	 * 
	 * JSONObject result = new JSONObject(responseString); if
	 * (result.getString("status").equals("verified")) {
	 * 
	 * Message m_pass = new Message(); m_pass.obj = "verified";
	 * verifyHandler.sendMessage(m_pass);
	 * 
	 * } else if (result.getString("status").equals("pending")) {
	 * 
	 * if (result.getString("message").contains( "Not verified")) { Message
	 * m_pass = new Message(); m_pass.obj = "not_verified";
	 * verifyHandler.sendMessage(m_pass);
	 * 
	 * } else if (result.getString("message").contains( "Phone number")) { //
	 * Phone number // verification pending Message m_pass = new Message();
	 * m_pass.obj = "only_email_verified"; verifyHandler.sendMessage(m_pass);
	 * 
	 * } else if (result.getString("message").contains( "Email")) { // Email
	 * verification pending
	 * 
	 * Message m_pass = new Message(); m_pass.obj = "only_phone_verified";
	 * verifyHandler.sendMessage(m_pass);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * } catch (Exception e) { Log.d("TAG", "Loading Picture FAILED");
	 * e.printStackTrace(); } } }.start(); }
	 */
}
