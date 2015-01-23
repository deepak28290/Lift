package indwin.c3.liftapp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import indwin.c3.liftapp.pojos.VehicleDetails;
import indwin.c3.liftapp.utils.MarkerOptionsA;
import indwin.c3.liftapp.utils.ProfileHelper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class AcceptedActivity extends FragmentActivity {
	// Google Map
	public Dialog dialog;
	private GoogleMap googleMap;
	// Variable for storing current date and time
	SupportMapFragment mMapFragment;
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

			} else {

				try {
					// plot date and address
					long epoch = ((MarkerOptionsA) msg.obj).getTime();
					String username = ((MarkerOptionsA) msg.obj)
							.getAccepterusername();
					String userid = ((MarkerOptionsA) msg.obj)
							.getAccepteruserid();
					String phone = ((MarkerOptionsA) msg.obj).getPhone();
					TextView name = (TextView) findViewById(R.id.namefinal);
					TextView number = (TextView) findViewById(R.id.numberfinal);
					name.setText(username);
					number.setText(phone);

					popUserPic(userid);
					String meeting_address = ((MarkerOptionsA) msg.obj)
							.getAddress();
					Date date = new Date(epoch);
					DateFormat format = new SimpleDateFormat(
							"dd/MM/yyyy HH:mm:ss");
					String formatted = format.format(date);
					TextView address = (TextView) findViewById(R.id.finaladdresstext);
					TextView time = (TextView) findViewById(R.id.timertextfinal);
					// adding marker
					time.setText(" Meeting time: " + formatted);
					address.setText(" Meeting Landmark: " + meeting_address);

					// plot map
					String src_id = (googleMap
							.addMarker(((MarkerOptionsA) msg.obj)
									.getMarkerOptionsSrc())).getId();

					String dest_id = (googleMap
							.addMarker(((MarkerOptionsA) msg.obj)
									.getMarkerOptionsDest())).getId();
					double latitude = ((MarkerOptionsA) msg.obj).getSrcLat(), longitude = ((MarkerOptionsA) msg.obj)
							.getSrcLong();
					CameraPosition cameraPosition = new CameraPosition.Builder()
							.target(new LatLng(latitude, longitude)).zoom(10)
							.build();

					googleMap.animateCamera(CameraUpdateFactory
							.newCameraPosition(cameraPosition));
					
					populateVehicleDetails(((MarkerOptionsA) msg.obj).getSrcLat(),((MarkerOptionsA) msg.obj).getSrcLong(),((MarkerOptionsA) msg.obj).getDestLat(),((MarkerOptionsA) msg.obj).getDestLong());
				} catch (Exception e) {
					// Log.e("Lift", e.getMessage());
					Toast toast = Toast.makeText(getApplicationContext(),
							"Google Maps Error!", Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
				}
			}
			return false;
		}
	});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
		 * getLayoutInflater().inflate(R.layout.accept_activity, content, true);
		 */

		setContentView(R.layout.accept_activity);

		initilizeMap();

		//((LiftAppGlobal) this.getApplication()).setReqid("6");
		
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
		Editor editor = pref.edit();
		editor.putString("req_id", "6");
		editor.commit();
		/*
		 * Toast.makeText( getApplicationContext(), ((LiftAppGlobal)
		 * this.getApplication()).getReqid() + " request id",
		 * Toast.LENGTH_LONG).show();
		 */
		runCall(handler);
	}

	public void showprof(View v) {

	//	((LiftAppGlobal) this.getApplication()).setPubUserId(pubUserId);
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
		Editor editor = pref.edit();
		editor.putString("pub_user_id", pubUserId);
		editor.commit();
		
		Intent intent = new Intent(this, PublicProfileActivity.class);
		startActivity(intent);
	}

	public void callnumber(View v) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		TextView unItemVal = (TextView) findViewById(R.id.numberfinal);

		String phoneNumber = unItemVal.getText().toString();
		callIntent.setData(Uri.parse("tel:" + phoneNumber));
		startActivity(callIntent);

	}

	public void submit(final float rating, final String comment) {

		final String ratedByUser = ((LiftAppGlobal) getApplication())
				.getUserId();

		final String ratedUser = pubUserId;
		toggleServerLoading(true);
		final Handler finishHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.obj.equals("failed")) {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Server Communication Error. Please Try Again",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else if (msg.obj.equals("success")) {
					finishOnServer();

				}
				return false;
			}
		});
		new Thread() {
			public void run() {
				try {

					Thread.sleep(2000);
					JSONObject payload = new JSONObject();
					payload.put("ratedByUser", ratedByUser);
					payload.put("ratedUser", ratedUser);
					payload.put("rating", rating);
					payload.put("comment", comment);

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(
							httpParameters, 5000);
					HttpClient myClient = new DefaultHttpClient(
							httpParameters);

					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/rating/updaterating";

					HttpPost post = new HttpPost(call_url);
					post.setHeader("Content-type", "application/json");
					StringEntity entity = new StringEntity(payload.toString());

					post.setEntity(entity);
					HttpResponse response = myClient.execute(post);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");
					if (response.getStatusLine().getStatusCode() != 200) {

						Message m_req = new Message();
						m_req.obj = "failed";
						finishHandler.sendMessage(m_req);
					} else {

						if (responseString.contains("success")) {

							Message m_req = new Message();
							m_req.obj = "success";
							finishHandler.sendMessage(m_req);

						} else {

							Message m_req = new Message();
							m_req.obj = "failed";
							finishHandler.sendMessage(m_req);

						}
					}

				} catch (Exception e) {
					e.getMessage();
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void smsnumber(View v) {
		TextView unItemVal = (TextView) findViewById(R.id.numberfinal);
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		smsIntent.putExtra("address", unItemVal.getText().toString());
		smsIntent.putExtra("sms_body", "");
		startActivity(smsIntent);

	}

	public void finishride(View v) {
		// update finish status on server
		dialog = new Dialog(AcceptedActivity.this);
		dialog.setContentView(R.layout.rating_dialog);
		dialog.setTitle("Rate your LIFT experience");
		// validate values
		dialog.show();
		ImageButton cancelButton = (ImageButton) dialog
				.findViewById(R.id.submitRatingButton);
		// if button is clicked, close the custom dialog
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				submitratingButton();
			}
		});
		// update rating on server
	}

	public void cancelride(View v) {
		// TO-DO add cancel ride
		dialog = new Dialog(AcceptedActivity.this);
		dialog.setContentView(R.layout.cancel_dialog);
		dialog.setTitle("Sure you want to cancel now?");
		dialog.show();
		ImageButton cancelButton = (ImageButton) dialog
				.findViewById(R.id.cancelit);
		// if button is clicked, close the custom dialog
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelButton();
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

	public void nowaitButton(View v) {

		dialog.dismiss();

	}
	private void toggleServerLoading(boolean show) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.overlay);
		View fragmentCView = findViewById(R.id.mapfinal);
		
		if (show == true) {
			ll.setVisibility(View.VISIBLE);
			fragmentCView.setVisibility(View.GONE);

		} else {
			ll.setVisibility(View.GONE);
			fragmentCView.setVisibility(View.VISIBLE);
		}
	}
	public void finishOnServer() {

		final Handler finishHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.obj.equals("failed")) {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Server Communication Error. Please Try Again",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else if (msg.obj.equals("success")) {
					// give rating dialog box
					toggleServerLoading(false);
					Intent intent = new Intent(getApplicationContext(),
							DrawerHomeActivity.class);
					startActivity(intent);
					finish();

				}
				return false;
			}
		});
		/*final String reqid = ((LiftAppGlobal) getApplication()).getReqid();*/
		
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		final String reqid = pref.getString("req_id", null);
		final String selfuserid = ((LiftAppGlobal) getApplication())
				.getUserId();
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
					JSONObject payload = new JSONObject();
					payload.put("id", reqid);
					payload.put("userid", selfuserid);
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(
							httpParameters, 5000);
					HttpClient myClient = new DefaultHttpClient(
							httpParameters);


					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/request/finishride";
					HttpPost post = new HttpPost(call_url);
					post.setHeader("Content-type", "application/json");
					StringEntity entity = new StringEntity(payload.toString());

					post.setEntity(entity);
					HttpResponse response = myClient.execute(post);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");
					if (response.getStatusLine().getStatusCode() != 200) {

						Message m_req = new Message();
						m_req.obj = "failed";
						finishHandler.sendMessage(m_req);
					} else {
						if (responseString.contains("success")) {
							Message m_req = new Message();
							m_req.obj = "success";
							finishHandler.sendMessage(m_req);
						} else {
							Message m_req = new Message();
							m_req.obj = "failed";
							finishHandler.sendMessage(m_req);
						}
					}

				} catch (Exception e) {
					e.getMessage();
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void cancelButton() {

		final Handler finishHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.obj.equals("failed")) {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Server Communication Error. Please Try Again",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else if (msg.obj.equals("success")) {

					Intent intent = new Intent(getApplicationContext(),
							DrawerHomeActivity.class);
					startActivity(intent);
					finish();

				}
				return false;
			}
		});
	//	final String reqid = ((LiftAppGlobal) getApplication()).getReqid();
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		final String reqid = pref.getString("req_id", null);
		final String selfuserid = ((LiftAppGlobal) getApplication())
				.getUserId();
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
					JSONObject payload = new JSONObject();
					payload.put("id", reqid);
					payload.put("userid", selfuserid);
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(
							httpParameters, 5000);
					HttpClient myClient = new DefaultHttpClient(
							httpParameters);


					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/request/forfeitride";
					HttpPost post = new HttpPost(call_url);
					post.setHeader("Content-type", "application/json");
					StringEntity entity = new StringEntity(payload.toString());

					post.setEntity(entity);
					HttpResponse response = myClient.execute(post);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");
					if (response.getStatusLine().getStatusCode() != 200) {

						Message m_req = new Message();
						m_req.obj = "failed";
						finishHandler.sendMessage(m_req);
					} else {
						if (responseString.contains("success")) {
							Message m_req = new Message();
							m_req.obj = "success";
							finishHandler.sendMessage(m_req);
						} else {
							Message m_req = new Message();
							m_req.obj = "failed";
							finishHandler.sendMessage(m_req);
						}
					}

				} catch (Exception e) {
					e.getMessage();
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void submitratingButton() {
		final RatingBar rb = (RatingBar) dialog.findViewById(R.id.rating2);
		final EditText et = (EditText) dialog.findViewById(R.id.txtComment);
		// if button is clicked, close the custom dialog

		float rating = rb.getRating();

		String comment = et.getText().toString();
		final float PRECISION_LEVEL = 0.001f;

		if (Math.abs(rating - 0f) < PRECISION_LEVEL) {
			// not rated yet
			Toast.makeText(getApplicationContext(),
					"Please give a rating and optional comment!",
					Toast.LENGTH_SHORT).show();

		} else {
			dialog.dismiss();
			submit(rating, comment);
		}

	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.mapfinal)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
			googleMap.setMyLocationEnabled(false); // false to disable
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		}
	}

	@SuppressLint("SimpleDateFormat")
	public void runCall(final Handler handler) {

		try {
			new Thread() {
				public void run() {
					try {
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 5000);
						HttpClient myClient = new DefaultHttpClient(
								httpParameters);
						SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
						String reqid = pref.getString("req_id", null);
						String acc_req_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/request/getrequestdetails?id="
								+ reqid;//((LiftAppGlobal) getApplication()).getReqid();

						String phone;
						HttpGet httpget = new HttpGet(acc_req_url);
						HttpResponse response = myClient.execute(httpget);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
								"UTF-8");
						String userid = ((LiftAppGlobal) getApplication())
								.getUserId();
						if (response.getStatusLine().getStatusCode() != 200) {

							Log.e("LiftCommunication", "Server returned code "
									+ response.getStatusLine().getStatusCode());
							Message m_fail = new Message();
							m_fail.obj = "failed";

							handler.sendMessage(m_fail);
						} else {

							JSONObject respObj = new JSONObject(responseString);

							if (respObj.getString("status").equals("failure")) {
								Message m_norides = new Message();
								m_norides.obj = "failed";
								handler.sendMessage(m_norides); // if req id
																// returns a
																// failure from
																// server (which
																// is
																// impossibrooo
																// since server
																// sent this
																// req_id in
																// last call,
																// but you never
																// know :) )
							} else {
								JSONObject resultObj = respObj
										.getJSONObject("result");
							
								String otheruserid = resultObj
										.getString("otherFbUserID");
							
								Double srcLat,srcLong,destLat,destLong;
								String src_add="",dest_add="",otherusername="";
								if(userid.equals(resultObj
										.getString("otherFbUserID"))){ 
									
									otherusername = resultObj
											.getString("selfUserName");
									pubUserId = resultObj
											.getString("selfFbUserID");
									phone = resultObj								//change these details to selfuserid etc
											.getString("selfPhone");
									srcLat = Double.parseDouble(resultObj
											.getString("selfSrcGeoCode")
											.split(",")[0]);
									srcLong = Double.parseDouble(resultObj
											.getString("selfSrcGeoCode")
											.split(",")[1]);
									destLat = Double.parseDouble(resultObj
											.getString("selfDestGeoCode").split(
													",")[0]);
									destLong = Double.parseDouble(resultObj
											.getString("selfDestGeoCode").split(
													",")[1]);	
									src_add=resultObj
									.getString("selfSource");
									dest_add=resultObj
											.getString("selfDestination");
									
								}
								else{
									
									otherusername = resultObj
											.getString("otherUserName");
									pubUserId = resultObj
											.getString("otherFbUserID");
									phone = resultObj
											.getString("otherPhone");
									srcLat = Double.parseDouble(resultObj
											.getString("otherSrcGeoCode")
											.split(",")[0]);
									srcLong = Double.parseDouble(resultObj
											.getString("otherSrcGeoCode")
											.split(",")[1]);
									destLat = Double.parseDouble(resultObj
											.getString("otherDestGeoCode").split(
													",")[0]);
									destLong = Double.parseDouble(resultObj
											.getString("otherDestGeoCode").split(
													",")[1]);	
									
									src_add=resultObj
											.getString("otherSource");
											dest_add=resultObj
													.getString("otherDestination");
							
								}
								MarkerOptions markerSrcRider = new MarkerOptions()
										.position(new LatLng(srcLat, srcLong))
										.title(otherusername)
										.snippet(
												"Pickup Address: "
														+ src_add);
								markerSrcRider
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.passenger_marker_icon));

								MarkerOptions markerDstRider = new MarkerOptions()
										.position(new LatLng(destLat, destLong))
										.title(otherusername)
										// change to name when response returns
										// name
										.snippet(
												"Destination Address: "
														+dest_add)
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.home_marker_icon));
								
								long epoch = resultObj
										.getLong("otherStartTime");
								String meeting_address = resultObj
										.getString("otherSource");
								
								MarkerOptionsA me = new MarkerOptionsA();
								me.setMarkerOptionsSrc(markerSrcRider);
								me.setMarkerOptionsDest(markerDstRider);
								me.setSrcLat(srcLat);
								me.setSrcLong(srcLong);
								me.setTime(epoch);
								me.setPhone(phone);
								me.setAccepterusername(otherusername);
								me.setAccepteruserid(otheruserid); 
								me.setAddress(meeting_address);
								me.setDestLat(destLat);
								me.setDestLong(destLong);
								Message msg = new Message();
								msg.obj = me;

								handler.sendMessage(msg);

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

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void popUserPic(final String userid) {
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

				} else if (((ProfileHelper) msg.obj).getType().equals("image")) {

					if (((ProfileHelper) msg.obj).getBmp() != null) {

						ImageView iv = (ImageView) findViewById(R.id.imagefinal1);

						iv.setImageBitmap(DrawerHomeActivity.getCroppedBitmap(
								((ProfileHelper) msg.obj).getBmp(), 70));
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
							+ pubUserId
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

	}

	private Boolean exit = false;

	@Override
	public void onBackPressed() {
		if (exit)
			this.finish();
		else {
			Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_LONG)
					.show();
			exit = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					exit = false;
				}
			}, 3 * 1000);

		}

	}
	public void populateVehicleDetails(final double lat1,final double lon1,final double lat2,final double lon2) {

		final String userID = pubUserId;
		final Handler vehHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (((VehicleDetails) msg.obj).getStatus().equals("failed")) {
					Toast.makeText(getApplicationContext(), "Server Error!",
							Toast.LENGTH_LONG).show();

				} else if (((VehicleDetails) msg.obj).getStatus().equals("success")) {
					
				/*	TextView vehicle=(TextView)findViewById(R.id.vehicdetails);
					TextView costperkm=(TextView)findViewById(R.id.ridecostperkm);
					String vehicValue="Vehicle Type:  " +((VehicleDetails) msg.obj).getVehiclemodel() +" ( "+((VehicleDetails) msg.obj).getVehicletype() + " )";
					vehicle.setText(vehicValue);
					
					String cost="Approx cost per Km: Rs. "+ ((VehicleDetails) msg.obj).getCostperkm();
					
					costperkm.setText(cost);
			*/		
					getDistance(lat1, lon1, lat2, lon2, ((VehicleDetails) msg.obj).getCostperkm());
					
				} else if (((VehicleDetails) msg.obj).getStatus().equals("notfound"))  {

				/*	TextView vehicle=(TextView)findViewById(R.id.vehicdetails);
					TextView costperkm=(TextView)findViewById(R.id.ridecostperkm);
					String vehicValue="Vehicle Type:  N/A ";
					vehicle.setText(vehicValue);
					
					String cost="Approx cost per Km: N/A ";
					
					costperkm.setText(cost);*/
					
				}
				return false;
			}
		});
		new Thread() {
			public void run() {
				try {

					String url;
					Log.d("Lift", "Loading Vehicle Details");

					url = getApplicationContext()
							.getString(R.string.server_url)
							+ "/user/getvehicledetails?userID=" + pubUserId;

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(
							httpParameters, 5000);
					HttpClient client = new DefaultHttpClient(
							httpParameters);

					HttpGet request = new HttpGet(url);
					HttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();

					String responseString = EntityUtils.toString(entity,
							"UTF-8");

					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("LiftCommunication", "Server returned code "
								+ response.getStatusLine().getStatusCode());
						Message m_fail = new Message();

						VehicleDetails vd = new VehicleDetails();
						vd.setStatus("failed");
						m_fail.obj = vd;
						vehHandler.sendMessage(m_fail);

					} else {
						VehicleDetails vd = new VehicleDetails();
						if (responseString.contains("failure")) {
							vd.setStatus("notfound");
							
						} else if (responseString.contains("success")) {

							if (responseString.contains("{ }")) {
								
								vd.setStatus("notfound");

							} else {
								JSONObject result = new JSONObject(
										responseString);

								// if resultset is empty set values to N/A

								vd.setStatus("success");

								vd.setCostperkm(((JSONObject) result
										.get("result")).getString("costperkm"));

								vd.setVehiclenumber(((JSONObject) result
										.get("result"))
										.getString("vehiclenumber"));

								vd.setVehiclemodel(((JSONObject) result
										.get("result"))
										.getString("vehiclemodel"));

								vd.setVehicletype(((JSONObject) result
										.get("result"))
										.getString("vehicletype"));

								vd.setUserID(((JSONObject) result.get("result"))
										.getString("userID"));

							}
						}
						Message m_vd = new Message();
						m_vd.obj = vd;
						vehHandler.sendMessage(m_vd);
					}
				} catch (Exception e) {

					Log.d("TAG", "Loading Picture FAILED");
					e.printStackTrace();

				}

			}
		}.start();

	}
	public void getDistance(final double lat1, final double lon1,
			final double lat2, final double lon2,final String costperkm) {
		final Handler handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {

				if(((String)msg.obj).equals("NA")){

				}else{
					
					double distance = Double.parseDouble(	msg.obj.toString().split(" ")[0] );
					double costperkm_double=Double.parseDouble(costperkm);
					double doublecost=distance*costperkm_double;
					String cost=String.valueOf(doublecost).toString().split("\\.")[0];
					TextView ridecost=(TextView)findViewById(R.id.finalcost);
					ridecost.setText(" Approx trip cost: Rs. "+cost+" ( Distance- "+msg.obj.toString()+" )");
					
				}
				return false;
			}
		});
		if(costperkm.equals("N/A")){
			
		}else{
		new Thread() {
			public void run() {
				try {

					String result_in_kms = "";
					String url = "http://maps.google.com/maps/api/directions/xml?origin="
							+ lat1
							+ ","
							+ lon1
							+ "&destination="
							+ lat2
							+ ","
							+ lon2 + "&sensor=false&units=metric";
					String tag[] = { "text" };
					HttpResponse response = null;
					try {
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 5000);
						HttpClient httpClient = new DefaultHttpClient(
								httpParameters);

						HttpContext localContext = new BasicHttpContext();
						HttpPost httpPost = new HttpPost(url);
						response = httpClient.execute(httpPost, localContext);
						InputStream is = response.getEntity().getContent();
						DocumentBuilder builder = DocumentBuilderFactory
								.newInstance().newDocumentBuilder();
						org.w3c.dom.Document doc = builder.parse(is);
						if (doc != null) {
							NodeList nl;
							ArrayList args = new ArrayList();
							for (String s : tag) {
								nl = doc.getElementsByTagName(s);
								if (nl.getLength() > 0) {
									Node node = nl.item(nl.getLength() - 1);
									args.add(node.getTextContent());
								} else {
									args.add(" - ");
								}
							}
							result_in_kms = String.format("%s", args.get(0));

							Message m = new Message();
							m.obj = result_in_kms;
							handler.sendMessage(m);
						} else {
							Message m = new Message();
							m.obj = "NA";
							handler.sendMessage(m);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					Log.d("TAG", "Distance Calculation failed");
					e.printStackTrace();
				}
			}
		}.start();
		}
		// return result_in_kms;
	}
}
