package indwin.c3.liftapp;

import indwin.c3.liftapp.pojos.LockedLiftPojo;
import indwin.c3.liftapp.utils.GPSTracker;
import indwin.c3.liftapp.utils.MapWrapperLayout;
import indwin.c3.liftapp.utils.MarkerOptionsE;
import indwin.c3.liftapp.utils.PlaceJSONParser;
import indwin.c3.liftapp.utils.ProfileHelper;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import de.keyboardsurfer.android.widget.crouton.Style;

@SuppressLint({ "SimpleDateFormat", "InflateParams" })
public class PassengerActivity extends SidePanel implements
		OnMarkerClickListener {
	private ViewGroup infoWindow;
	private TextView infoTitle;
	private TextView infoSnippet;
	private boolean isStarted = false;
	// private Button infoButton;
	// private OnInfoWindowElemTouchListener infoButtonListener;
	private HashMap<String, String> hmp = new HashMap<String, String>();
	private HashMap<String, MarkerOptionsE> h_map = new HashMap<String, MarkerOptionsE>();
	// Google Map
	private GoogleMap googleMap;
	private MarkerOptions destMarker;
	private boolean locked = false;
	private Marker mSource;
	private Marker mDestination;
	private static Dialog dialog;
	PlacesTask placesTask;
	ParserTask parserTask;
	AutoCompleteTextView srcPlace;
	AutoCompleteTextView destPlace;
	public static String srcadd="";
	public static String destadd="";
	private final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.obj.equals("failed")) {
				
				toggleServerLoading(false);
				toggleCroutons(PassengerActivity.this,
						"Network error!");

				
			} else if (msg.obj.equals("norides")) {
				toggleServerLoading(false);
				if(locked==false){
					
					toggleCroutons(PassengerActivity.this,
						"No Lifts available right now! Refresh or try on another route or time.\nYou can also tap on Lock icon on top to save this route and make yourself visible to LIFT givers.");
				}else{
					toggleCroutons(PassengerActivity.this,
							"No Lifts available right now! Refresh or try on another route or time.");
				
				}
			} else if (msg.obj.equals("req_success")) {
				toggleServerLoading(false);
				toggleCroutons(PassengerActivity.this,
						"Lift Request succesfully sent!\n View it in MyRequests tab in side panel.\n Keep refrshing to find new lift givers on this route");
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
				toggleCroutons(PassengerActivity.this,
						"Sorry, you're late. This user is no longer available!");

				String dest_address = destadd;/*getAddressFromLatLng(mDestination
						.getPosition());*/
				
				String src_address = srcadd;//getAddressFromLatLng(mSource.getPosition());
				regenMap(src_address, dest_address);

				runCall(mSource.getPosition().latitude + ","
						+ mSource.getPosition().longitude,
						mDestination.getPosition().latitude + ","
								+ mDestination.getPosition().longitude, handler);
				// clickNotifyMe(null);
				dialog.dismiss();
				System.out.println("come to dadddy");

			} else {
				toggleServerLoading(false);
				toggleCroutons(PassengerActivity.this,
						"Showing available lifts around you in the map!\nTap on marker window to view complete profile and to send them a LIFT request.");

				String id = (googleMap.addMarker(((MarkerOptionsE) msg.obj)
						.getMarkerOptions())).getId();
				hmp.put(id, ((MarkerOptionsE) msg.obj).getUserId());
				h_map.put(id, ((MarkerOptionsE) msg.obj));

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
		getLayoutInflater().inflate(R.layout.passenger_activity_main, content,
				true);

		srcPlace = (AutoCompleteTextView) findViewById(R.id.sourceaddressP);
		srcPlace.setThreshold(1);
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

		destPlace = (AutoCompleteTextView) findViewById(R.id.destinationaddressP);
		destPlace.setThreshold(1);
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
		try {
			// Loading map
			initilizeMap();
			// latitude and longitude
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isStarted == false) {
			checkLiftLocked();
		} else {
			toggleCroutons(this,
					"Tap on clock to select the time you want your lift at.");
		}
	}

	public void populateSavedDetails(LockedLiftPojo llp) {
		googleMap.setMyLocationEnabled(true); // false to disable
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		// shift states of all views to locked
		locked = true;
		indwin.c3.liftapp.CustomAutoCompleteTextView src = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddressP);
		indwin.c3.liftapp.CustomAutoCompleteTextView dest = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddressP);
		TextView f_date = (TextView) findViewById(R.id.txtDateP);
		ImageView iv = (ImageView) findViewById(R.id.notifyme);
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

		mSource = googleMap.addMarker(markerSrc);
		srcadd=getAddressFromLatLng(mSource.getPosition());
		MarkerOptions markerDest = new MarkerOptions()
				.position(new LatLng(destlatitude, destlongitude))
				.title("Destination").draggable(true);
		markerDest.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.home_marker_icon));
		markerDest.snippet(llp.getDestination());

		mDestination = googleMap.addMarker(markerDest);
		destadd=getAddressFromLatLng(mDestination.getPosition());
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(srclatitude, srclongitude)).zoom(12).build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
		llp.setHasLockedRide(false);

		/*
		 * int heightInPx = getResources() .getDimensionPixelOffset(12); Style
		 * style = new Style.Builder()
		 * .setBackgroundColor(R.color.horribly_pink) .setDuration(10000)
		 * .setGravity(Gravity.LEFT)
		 * .setTextColor(getResources().getColor(android.R.color.black))
		 * .setHeight(heightInPx) .build();
		 */

		toggleCroutons(
				this,
				"You're visible to lift givers on this route and time.\nYou will receive a notification when a lift is available.\n\nTap on Lock icon to change route details. "
						);

	
	}

	private void toggleCroutons(Activity activity, String text) {
		
		Crouton.cancelAllCroutons();
		if(activity==null){
			
		}else{
	final	Crouton crouton=	Crouton.makeText(
				activity,
				text+"\n\n\t[X] CLOSE\t",
				de.keyboardsurfer.android.widget.crouton.Style.INFO,(ViewGroup)findViewById(R.id.croutonid))
				.setConfiguration(
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

	MapWrapperLayout mapWrapperLayout;

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		try {

			if (googleMap == null) {
				googleMap = ((MapFragment) getFragmentManager()
						.findFragmentById(R.id.map_passenger)).getMap();
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
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),
					"Google Play Services not enabled!",

					Toast.LENGTH_SHORT).show();

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
			
			boolean isGps = pref.getBoolean("is_gps_on",false);
		//if (((LiftAppGlobal) this.getApplication()).isGpsOn()) {
			
			if(isGps){
		
				Editor editor = pref.edit();
				editor.putBoolean("is_gps_on", false);
				editor.commit();
				//((LiftAppGlobal) this.getApplication()).setGpsOn(false);

				Thread.sleep(500);
				mapAction();
				// check if ride already locked or not!

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
		try {
			googleMap.setMyLocationEnabled(true); // false to disable
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);
			googleMap.setPadding(0, 200, 0, 0);

			// create class object
			gps = new GPSTracker(PassengerActivity.this);

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
				toggleCroutons(
						this,
						"Drag your marker to the start location.\n\n Long tap on the marker and start dragging!");
			}

			LatLng pt = new LatLng(latitude, longitude);
			String src_address = getAddressFromLatLng(pt);
			srcadd=src_address;
			// create marker
			MarkerOptions marker = new MarkerOptions()
					.position(new LatLng(latitude, longitude)).title("Pickup!")
					.draggable(true);
			marker.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.passenger_marker_icon));
			marker.snippet(src_address);
			// adding marker
			mSource = googleMap.addMarker(marker);
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(latitude, longitude)).zoom(12).build();
			indwin.c3.liftapp.CustomAutoCompleteTextView srcView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddressP);

			srcView.setText(src_address);

			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));

			googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {

				@Override
				public void onMarkerDrag(Marker arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onMarkerDragEnd(Marker arg0) {
					String dest_address="";
					String src_address="";
					try {
						LatLng dragPosition = arg0.getPosition();
						double dragLat = dragPosition.latitude;
						double dragLong = dragPosition.longitude;
						Log.i("info", "on drag end :" + dragLat + " dragLong :"
								+ dragLong);
						// Getting the current position of the marker

						if (mSource.getPosition().equals(arg0.getPosition())) {
							 src_address = getAddressFromLatLng(arg0
									.getPosition());
							srcadd=src_address;
							indwin.c3.liftapp.CustomAutoCompleteTextView srcView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddressP);

							srcView.setText(src_address);

						}
						if (mDestination != null) {
							if (mDestination.getPosition().equals(
									arg0.getPosition())) {
								dest_address = getAddressFromLatLng(arg0
										.getPosition());
								destadd=dest_address;
								indwin.c3.liftapp.CustomAutoCompleteTextView destView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddressP);

								destView.setText(dest_address);
								runCall(mSource.getPosition().latitude + ","
										+ mSource.getPosition().longitude,
										mDestination.getPosition().latitude
												+ ","
												+ mDestination.getPosition().longitude,handler);
							}
						}

						if (mDestination != null
								&& (mSource.getPosition().equals(
										arg0.getPosition()) || mDestination
										.getPosition().equals(
												arg0.getPosition()))) {
						/*	String src_address = getAddressFromLatLng(mSource
									.getPosition());
							srcadd=src_address;
							String dest_address = getAddressFromLatLng(mDestination
									.getPosition());
						*/	regenMap(src_address, dest_address);
							runCall(mSource.getPosition().latitude + ","
									+ mSource.getPosition().longitude,
									mDestination.getPosition().latitude
											+ ","
											+ mDestination.getPosition().longitude,
									handler);
							// clickNotifyMe(null);

						}
					} catch (Exception e) {
						Log.e("Lift", "Error during marker drag ");
					/*	Toast.makeText(getApplicationContext(),
								"Error during marker drag! Reported. ",
								Toast.LENGTH_LONG).show();
*/
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

						if (locked == true) {
							
							toggleCroutons(PassengerActivity.this, "Drag your marker to the start location.\n\n Long tap on the marker to start dragging!");

						} else {
							if (destMarker != null) { // if marker exists (not
														// null
														// or
														// whatever)
								mDestination.remove();

								onLongClickHelper(point, handler);
							} else {
								onLongClickHelper(point, handler);
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
								toast.setGravity(
										Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
								toast.show();

							} else if (mDestination == null) {
								Toast toast = Toast.makeText(
										getApplicationContext(),
										" This is you!", Toast.LENGTH_LONG);
								toast.setGravity(
										Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
								toast.show();
							} else {
								final Marker marker_ = marker;

								// see if pending notification exists
								MarkerOptionsE me = h_map.get(marker.getId());
								final String pubUserId = me.getUserId();
								if (me != null)
									if (me.getHasActiveRequests().equals("1")) {
										toggleCroutons(PassengerActivity.this, 	"There is an existing request pending with this user.\n\n Check in MyRequests Tab.");

									} else {
										dialog = new Dialog(
												PassengerActivity.this);
										dialog.setContentView(R.layout.request_dialog);
										dialog.setTitle("You are almost there..");

										ImageButton reqButton = (ImageButton) dialog
												.findViewById(R.id.ReqButton);
										// if button is clicked, close the
										// custom dialog
										reqButton
												.setOnClickListener(new OnClickListener() {
													@Override
													public void onClick(View v) {
														sendRequestFlow(marker_);

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
														SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
														
														Editor editor = pref.edit();
														editor.putString("pub_user_id", pubUserId);
														editor.commit();
													
														/*((LiftAppGlobal) getApplication())
																.setPubUserId(pubUserId);*/
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

		}
	}

	public void requestlift(View v) {

	}

	public void sendRequestFlow(final Marker marker) {
		final Handler notifhandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				if (msg.obj.equals("failed")) {
					toggleServerLoading(false);
					Toast toast = Toast.makeText(getApplicationContext(),
							"Network error. Please try again!", Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else {
					/*
					 * ImageView iv = (ImageView) findViewById(R.id.notifyme);
					 * iv.setImageResource(R.drawable.locked);
					 */
					toggleServerLoading(false);
					disableFields();
					sendRequest(handler, marker.getId());

				}

				return false;

			}
		});
		try {
			TextView s_address = (TextView) findViewById(R.id.sourceaddressP);
			TextView d_address = (TextView) findViewById(R.id.destinationaddressP);
			TextView f_date = (TextView) findViewById(R.id.txtDateP);
			String str = f_date.getText().toString().trim();

			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			Date date;
			date = df.parse(str);
			long epoch = date.getTime();

			final JSONObject riderPayload = new JSONObject();
			SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
			
		
		/*	riderPayload.put("userID",
					((LiftAppGlobal) this.getApplication()).getUserId());
		*/	
			riderPayload.put("userID", pref.getString("user_id", null));
			riderPayload.put("userType", "passenger");
			riderPayload.put("source", s_address.getText().toString().trim());
			riderPayload.put("destination", d_address.getText().toString()
					.trim());
			riderPayload.put("srcgeocode", mSource.getPosition().latitude + ","
					+ mSource.getPosition().longitude);
			riderPayload.put("destgeocode", mDestination.getPosition().latitude
					+ "," + mDestination.getPosition().longitude);
			riderPayload.put("starttime", epoch);

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
						HttpConnectionParams.setConnectionTimeout(httpParameters,
								5000);
						HttpClient myClient = new DefaultHttpClient(httpParameters);
					

						String call_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/user/addonlineuser";
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

							notifhandler.sendMessage(m_fail);
						} else {
							/*
							 * JSONObject resObj=new JSONObject(responseString);
							 * 
							 * if(resObj.getString("detailschanged").equals("true"
							 * )){ Log.i("Lift", "Call to Server Success");
							 * Log.i("Lift", "Response" + responseString);
							 * Message msg = new Message(); msg.obj =
							 * "added_new"; // String a = responseString;
							 * 
							 * notifhandler.sendMessage(msg); }else{
							 */

							Log.i("Lift", "Call to Server Success");
							Log.i("Lift", "Response" + responseString);
							Message msg = new Message();
							msg.obj = "added_same";
							// String a = responseString;

							notifhandler.sendMessage(msg);
						}
						// }

					} catch (Exception e) {
						e.getMessage();
						e.printStackTrace();
						Message m_fail = new Message();
						m_fail.obj = "failed";

						notifhandler.sendMessage(m_fail);
					}
				}
			}.start();

		} catch (Exception e) {

			Log.e("Lift", e.getMessage());

		}
	}

	
	public void onLongClickHelper(LatLng point, final Handler handler) {

		final String srcgeocode = mSource.getPosition().latitude + ","
				+ mSource.getPosition().longitude;
		TextView f_datetime = (TextView) findViewById(R.id.txtDateP);

	
		String dest_address = getAddressFromLatLng(point);
		
		
		indwin.c3.liftapp.CustomAutoCompleteTextView destView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddressP);

		destView.setText(dest_address);
		destMarker = new MarkerOptions().title("Destination");
		destMarker.position(point);
		destMarker.draggable(true);
		//destMarker.snippet(address);
		destMarker.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.home_marker_icon));

		// adding marker
		mDestination = googleMap.addMarker(destMarker);
		final String destgeocode = mDestination.getPosition().latitude + ","
				+ mDestination.getPosition().longitude;

		googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));

		googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));
		String src =srcadd;// getAddressFromLatLng(mSource.getPosition());
		regenMap(src, dest_address);
		
		
		 if (f_datetime.getText().toString().trim().equals("")) { 
			 
			 toggleCroutons(this,  "Choose Lift Time by tapping on the Clock icon on top!\n" +
			 		"You can always long press on the pickup or destination markers to drag them and change routes. ");
		  
		  } else {
		 
		runCall(srcgeocode, destgeocode, handler);
		// clickNotifyMe(null);

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
					SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
					riderPayload.put("requesterID", pref.getString("user_id", null));
						
				/*	riderPayload.put("requesterID",
							((LiftAppGlobal) getApplication()).getUserId());*/
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
						+ mDestination.getPosition().longitude, handler);
		// clickNotifyMe(null);

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

			toggleServerLoading(true);
			new Thread() {
				public void run() {
					try {
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 5000);
						HttpClient myClient = new DefaultHttpClient(
								httpParameters);
						SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
						 
						
						String nrr_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/user/getnn?userType=passenger&userID="
								+ pref.getString("user_id", null)
								/*+ ((LiftAppGlobal) getApplication())
										.getUserId()pref.getString("user_id", null)*/
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
						
						String userid= pref.getString("user_id", null);
						
						/*
						String userid = ((LiftAppGlobal) getApplication())
								.getUserId();*/
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
									// "userID: "+
									// respObj.get("userID").toString()+
									// "( "+
									markerSrcRider.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
								/*	.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.car_marker_icon));*/

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
															+ " & is Headed here!").icon(BitmapDescriptorFactory
																	.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
										/*	.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.home_marker_2_icon));*/
									// "userID: " + respObj.get("userID")
									// .toString() + "( " +
									// adding marker
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
			toggleServerLoading(false);
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
							ImageView iv = (ImageView) findViewById(R.id.clock);
							iv.setVisibility(View.GONE);
							/*
							 * Button bt = (Button)
							 * findViewById(R.id.btnCalendarP);
							 * bt.setText("Change lift timings");
							 */if (mDestination != null) {

								String src = srcadd;/*getAddressFromLatLng(mSource
										.getPosition());*/
								String dest =destadd;/* getAddressFromLatLng(mDestination
										.getPosition());*/
								regenMap(src, dest);
								runCall(mSource.getPosition().latitude + ","
										+ mSource.getPosition().longitude,
										mDestination.getPosition().latitude
												+ ","
												+ mDestination.getPosition().longitude,
										handler);
								// clickNotifyMe(null);
							} else {
								toggleCroutons(PassengerActivity.this,
										"Long press to mark destination on Map");

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
		} finally {

		}
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
		if (mDestination != null) {
			mDestination = googleMap.addMarker(new MarkerOptions()
					.position(mDestination.getPosition())
					.draggable(true)
					.snippet(dest)
					.title("Destination")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.home_marker_icon)));
			
		destadd=dest;
		}
		
	}

	public void disableFields() {

		indwin.c3.liftapp.CustomAutoCompleteTextView src = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddressP);
		indwin.c3.liftapp.CustomAutoCompleteTextView dest = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddressP);
		TextView f_date = (TextView) findViewById(R.id.txtDateP);
		f_date.setEnabled(false);
		src.setInputType(0);
		dest.setInputType(0);
		ImageView iv = (ImageView) findViewById(R.id.notifyme);
		iv.setImageResource(R.drawable.locked);
		src.setEnabled(false);
		dest.setEnabled(false);
		locked = true;
		findViewById(R.id.map_passenger).setEnabled(false);
	}

	public void enableFields() {

		indwin.c3.liftapp.CustomAutoCompleteTextView src = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddressP);
		indwin.c3.liftapp.CustomAutoCompleteTextView dest = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddressP);
		TextView f_date = (TextView) findViewById(R.id.txtDateP);

		src.setInputType(InputType.TYPE_CLASS_TEXT);
		dest.setInputType(InputType.TYPE_CLASS_TEXT);
		src.setEnabled(true);
		dest.setEnabled(true);
		f_date.setEnabled(true);
		f_date.setClickable(true);
		ImageView iv = (ImageView) findViewById(R.id.notifyme);
		iv.setImageResource(R.drawable.unlocked);
		locked = false;
		findViewById(R.id.map_passenger).setEnabled(true);
	}

	public void clickNotifyMe(final View v) {

		final Handler notifhandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				
				toggleServerLoading(false);
				if (msg.obj.equals("failed")) {
					
					toggleCroutons(PassengerActivity.this,
							"Network error. Please try again!");

				} else {
					toggleCroutons(
							PassengerActivity.this,
							"You are now visible to lift givers. You will be notified when someone sends you a request.");

					// change button text to ride locked

				}

				// disable marker drag/long press/edit text fields

				// }

				return false;

			}
		});

		if (locked == true) {
			dialog = new Dialog(PassengerActivity.this);
			dialog.setContentView(R.layout.confirm_unlock_dialog);
			dialog.setTitle("Reset Lift Details?");
			ImageButton reqButton = (ImageButton) dialog
					.findViewById(R.id.ReqButton);
			// if button is clicked, close the custom dialog
			reqButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					toggleCroutons(
							PassengerActivity.this,
							"You're no longer visible to lift givers on this route.\n You can edit time or route of your LIFT now.\n Long press on marker to change location or use the input box");
					enableFields();
					// delete online user call
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

		} else {

			try {

				TextView s_address = (TextView) findViewById(R.id.sourceaddressP);
				TextView d_address = (TextView) findViewById(R.id.destinationaddressP);
				TextView f_date = (TextView) findViewById(R.id.txtDateP);
				String str = f_date.getText().toString().trim();

				if (mDestination == null) {
					toggleCroutons(this, 	"Enter pickup, destination and time before saving your lift.");
				} else if (mSource == null) {

					toggleCroutons(this, 	"Error! Please try again");

				} else if (s_address.getText().toString().trim().equals("")
						|| d_address.getText().toString().trim().equals("")
						|| f_date.getText().toString().trim().equals("")) {

					toggleCroutons(this, 	"Add landmarks and choose time before locking your lift details.");

				} else {
					// submit data to server
					disableFields();
					try {
						SimpleDateFormat df = new SimpleDateFormat(
								"dd-MM-yyyy HH:mm");
						Date date;
						date = df.parse(str);
						long epoch = date.getTime();

						final JSONObject riderPayload = new JSONObject();
						SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
						riderPayload.put("userID", pref.getString("user_id", null));
						
					/*	riderPayload.put("userID", ((LiftAppGlobal) this
								.getApplication()).getUserId());
					*/	riderPayload.put("userType", "passenger");
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
									HttpConnectionParams.setConnectionTimeout(httpParameters,
											5000);
									HttpClient myClient = new DefaultHttpClient(httpParameters);
								

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

						Log.e("Lift", e.getMessage());

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

	public void clickRefresh(View v) {

		EditText eTime = (EditText) findViewById(R.id.txtDateP);

		if (mSource == null || mDestination == null
				|| eTime.getText().toString().trim().equals("")) {
			toggleCroutons(this,
					"Enter source,destination and time and then try refresh again.");
		} else {
			String src = srcadd;//getAddressFromLatLng(mSource.getPosition());
			String dest = destadd;//getAddressFromLatLng(mDestination.getPosition());
			regenMap(src, dest);

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
		final Marker mark = arg0;

		LinearLayout ll = (LinearLayout) findViewById(R.id.profiledataP);
		if (ll != null)
			ll.setVisibility(View.GONE);
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
		// TODO Auto-generated method stub
		if (mDestination != null) {

			if (arg0.equals(mDestination) || arg0.equals(mSource)) {
				// update user logged in name in infowindow
				
				SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
				getUserDataFromID(pref.getString("user_id", null), arg0);
				/*getUserDataFromID(
						((LiftAppGlobal) getApplication()).getUserId(), arg0);*/
			} else {

				String userid = hmp.get(arg0.getId());
				getUserDataFromID(userid, arg0);

			}

		} else if (arg0.equals(mSource)) {
			// update user logged in name in infowindow
			
			SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
			getUserDataFromID(pref.getString("user_id", null), arg0);
			/*getUserDataFromID(((LiftAppGlobal) getApplication()).getUserId(),
					arg0);*/
		}
		return false;
	}

	private String getAddressFromLatLng(LatLng loc) {
		Geocoder geocoder;
		toggleServerLoading(true);
		List<Address> addresses;
		try {
			geocoder = new Geocoder(this, Locale.getDefault());
			addresses = geocoder
					.getFromLocation(loc.latitude, loc.longitude, 1);

			String address = addresses.get(0).getAddressLine(0);
			String city = addresses.get(0).getAddressLine(1);
			String country = addresses.get(0).getAddressLine(2);
			toggleServerLoading(false);
			return address + " , " + city;
		} catch (Exception e) {
			e.printStackTrace();
		}
		toggleServerLoading(false);
		return "";
	}

	private LatLng getLatLongFromAddress(String address) {
		toggleServerLoading(true);
		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocationName(address, 1);
			if (addresses.size() > 0) {
				LatLng coord = new LatLng(addresses.get(0).getLatitude(),
						addresses.get(0).getLongitude());
				toggleServerLoading(false);
				return coord;
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		toggleServerLoading(false);
		return null;
	}

	public void plotClickedP(View v) {
try{	
	
		toggleServerLoading(true);
		// get src dest values
		TextView f_datetime = (TextView) findViewById(R.id.txtDateP);
		indwin.c3.liftapp.CustomAutoCompleteTextView srcView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.sourceaddressP);
		indwin.c3.liftapp.CustomAutoCompleteTextView destView = (indwin.c3.liftapp.CustomAutoCompleteTextView) findViewById(R.id.destinationaddressP);
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

			mSource = googleMap.addMarker(new MarkerOptions()
					.position(srcLoc)
					.draggable(true)
					.snippet("My Start Point")
					.title(src_address)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.passenger_marker_icon)));
			srcadd=src_address;
			if (!dest_address.equals("")) {
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
			destadd=dest_address;

					if (f_datetime.getText().toString().trim().equals("")) {
						toggleServerLoading(false);
						toggleCroutons(this, "Choose time by tapping on clock icon to view nearby lifts\n\n");

					} else {
						// clickNotifyMe(null);
						runCall(mSource.getPosition().latitude + ","
								+ mSource.getPosition().longitude,
								mDestination.getPosition().latitude + ","
										+ mDestination.getPosition().longitude,
								handler);
					}
				} else {
					// Error fetching coordinates..
					toggleServerLoading(false);
					toggleCroutons(this,
							"Long Press to mark destination on Map!");

				}
			}
		}else{
			toggleServerLoading(false);
		}
		if (!dest_address.equals("")) {
			
			
			toggleServerLoading(true);
			googleMap.clear();

			mSource = googleMap.addMarker(new MarkerOptions()
					.position(mSource.getPosition())
					.draggable(true)
					.snippet("My Start Point")
					.title(src_address)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.passenger_marker_icon)));
			srcadd=src_address;
			LatLng destLoc = getLatLongFromAddress(dest_address);
			if (destLoc != null) {

				mDestination = googleMap.addMarker(new MarkerOptions()
						.position(destLoc)
						.draggable(true)
						.snippet("My Destination Point")
						.title(dest_address)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.home_marker_icon)));
				destadd=dest_address;
				if (f_datetime.getText().toString().trim().equals("")) {
					toggleServerLoading(false);
					toggleCroutons(this, "Choose time by tapping on clock icon to view nearby lifts\n\n");

				} else {
					runCall(mSource.getPosition().latitude + ","
							+ mSource.getPosition().longitude,
							mDestination.getPosition().latitude + ","
									+ mDestination.getPosition().longitude,
							handler);
					// clickNotifyMe(null);
				}

			} else if (mDestination != null) {

				mDestination = googleMap.addMarker(new MarkerOptions()
						.position(mDestination.getPosition())
						.draggable(true)
						.snippet("My Destination Point")
						.title(dest_address)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.home_marker_icon)));
				destadd=dest_address;
				if (f_datetime.getText().toString().trim().equals("")) {
					toggleServerLoading(false);
					toggleCroutons(this, "Choose time by tapping on clock icon to view nearby lifts\n\n");
					
				} else {
					runCall(mSource.getPosition().latitude + ","
							+ mSource.getPosition().longitude,
							mDestination.getPosition().latitude + ","
									+ mDestination.getPosition().longitude,
							handler);
					// clickNotifyMe(null);
				}

			} else {
				toggleServerLoading(false);
				toggleCroutons(this, "Long Press to mark destination on Map!");
			}
		}

		if (src_address.equals("") && dest_address.equals("")) {
			toggleServerLoading(false);
			toggleCroutons(this, "Long press on map to mark destination or long press on start pin to move it!");

		}

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(mSource.getPosition()).zoom(12).build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
		
	}finally{
		toggleServerLoading(false);
	}

	}

	
	
	String currUser;
	Bitmap currBmp;

	public void getUserDataFromID(final String userid, final Marker marker) {

		final Handler profHandler = new Handler(new Handler.Callback() {
			String username;
			String userId;

			@Override
			public boolean handleMessage(Message msg) {
				
				SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
				String user_id= pref.getString("user_id", null);
				
				if (((ProfileHelper) msg.obj).getType().equals("failed")) {

					toggleServerLoading(false);
					Toast toast = Toast.makeText(getApplicationContext(),
							"Network error. Please Try Again",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else if (((ProfileHelper) msg.obj).getType().equals("name")) {
					
					toggleServerLoading(false);
					username = ((ProfileHelper) msg.obj).getUsername();
					if (user_id.equals(
							((ProfileHelper) msg.obj).getUserid())) {
						currUser = username;
					}

				} else if (((ProfileHelper) msg.obj).getType().equals("image")) {
					toggleServerLoading(false);
					if (((ProfileHelper) msg.obj).getBmp() != null) {

						final Bitmap bmp = ((ProfileHelper) msg.obj).getBmp();
						userId = ((ProfileHelper) msg.obj).getUserid();
						if (user_id
								.equals(((ProfileHelper) msg.obj).getUserid())) {
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
				LinearLayout ll = (LinearLayout) findViewById(R.id.profiledataP);
				if (ll != null)
					ll.setVisibility(View.VISIBLE);
				return false;
			}
		});
		toggleServerLoading(true);
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
					Message m_fail = new Message();
					m_fail.obj = "failed";

					profHandler.sendMessage(m_fail);
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
					Message m_fail = new Message();
					m_fail.obj = "failed";

					profHandler.sendMessage(m_fail);
				}
			}
		}.start();
	}

	final Handler failhandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			
			toggleServerLoading(false);
			if (msg.obj.equals("failed")) {

				Toast toast = Toast.makeText(getApplicationContext(),
						"Network Error. Please Try Again",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			}
			return false;
		}
	});

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
					toggleCroutons(PassengerActivity.this,"1.Long press on the map to select destination or use the text box on top.\n" +
							"2. Long Press on your current marker and then drag it to change your start location.\n ");
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
					SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
					String userid= pref.getString("user_id", null);
					/*
					String userid = ((LiftAppGlobal) getApplication())
							.getUserId();*/
					String acc_req_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/haslockedride?userID="
							+ userid
							+ "&usertype=passenger";

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

	public void deleteEntryFromOnlineTable() {

		final Handler unlockhandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				toggleServerLoading(false);
				if (msg.obj.equals("failed")) {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Network error. Please try again!", Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else {

					// remove loading image

				}

				// disable marker drag/long press/edit text fields

				// }

				return false;

			}
		});

		try {
			final JSONObject riderPayload = new JSONObject();
			SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
			String user_id= pref.getString("user_id", null);
			
			riderPayload.put("userID", user_id);
		/*	riderPayload.put("userID",
					((LiftAppGlobal) this.getApplication()).getUserId());*/
			riderPayload.put("userType", "passenger");
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
	private void toggleServerLoading(boolean show) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.overlay);
		if (show == true) {
			ll.setVisibility(View.VISIBLE);

		} else {
			ll.setVisibility(View.GONE);
		}
	}
}
