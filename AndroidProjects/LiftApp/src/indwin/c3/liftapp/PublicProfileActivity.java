package indwin.c3.liftapp;

import indwin.c3.liftapp.pojos.ProfileData;
import indwin.c3.liftapp.pojos.RatingHelper;
import indwin.c3.liftapp.pojos.VehicleDetails;
import indwin.c3.liftapp.utils.ProfileHelper;

import java.io.BufferedInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class PublicProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pub_profile_activity);
	/*	ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
		getLayoutInflater().inflate(R.layout.pub_profile_activity, content,
				true);
	*/
	/* ((LiftAppGlobal) this.getApplication()).setUserId("123"); */
		getUserDetailsFromServer();

	}

	public void getUserDetailsFromServer() {
		
		/*
		 * ((LiftAppGlobal) this.getApplication())
		 * .setPubUserId("10205193200020273");
		 */
		
		  SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		  final String FBuserID =  pref.getString("pub_user_id", null);
	
	/*	final String FBuserID = ((LiftAppGlobal) this.getApplication())
				.getPubUserId();
	*/	
		final Handler ratehandler = new Handler(new Handler.Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {

				if (((RatingHelper) msg.obj).getStatus().equals("failed")) {

					Toast.makeText(
							getApplicationContext(),
							"Error Contacting Server! Check your internet connection.",
							Toast.LENGTH_LONG).show();

				} else {
					RatingBar rating = (RatingBar) findViewById(R.id.rating2);
					TextView rides = (TextView) findViewById(R.id.rides);

					rating.setRating(((RatingHelper) msg.obj).getRating());
					rides.setText("Lifts so far: "
							+ ((RatingHelper) msg.obj).getLifts());
				}
				return false;

			}
		});

		// getRating

		new Thread() {
			public void run() {
				try {

					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/rating/getrating?userID=" + FBuserID;

					Log.d("Lift", "Loading Rating ");

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

						RatingHelper rh = new RatingHelper();

						rh.setStatus("failed");
						m_fail.obj = rh;
						ratehandler.sendMessage(m_fail);

					} else {
						if (responseString.contains("failure")) {

						} else if (responseString.contains("success")) {
							JSONObject result = new JSONObject(responseString);
							RatingHelper rh = new RatingHelper();
							if (result.getString("message").contains(
									"user not rated")) {
								rh.setLifts("0");
								rh.setRating(0f);
							} else {

								rh.setLifts(result.getString("count").trim());
								rh.setRating(Float.parseFloat(result
										.getString("score")));

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

		// get profile details
		new Thread() {
			public void run() {
				try {

					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/getprofile?userID=" + FBuserID;

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

						} else if (responseString.contains("success")) {
							JSONObject result = new JSONObject(responseString);
							ProfileData pd = new ProfileData();
							pd.setEmail(((JSONObject) result.get("result"))
									.getString("emailID"));
							pd.setGender(((JSONObject) result.get("result"))
									.getString("gender"));
							pd.setPhone(((JSONObject) result.get("result"))
									.getString("phone"));
							pd.setUserName(((JSONObject) result.get("result"))
									.getString("userName"));
							Message m1 = new Message();
							ProfileHelper pe = new ProfileHelper();
							pe.setType("userdata");
							pe.setData(pd);
							m1.obj = pe;
							handler.sendMessage(m1);

						}
					}
				} catch (Exception e) {
					Log.d("TAG", "Loading Picture FAILED");
					e.printStackTrace();
				}
			}
		}.start();
	}

	final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (((ProfileHelper) msg.obj).getType().equals("image")) {

				ImageView iv = (ImageView) findViewById(R.id.profpic);
				iv.setImageBitmap(((ProfileHelper) msg.obj).getBmp());
				iv.setVisibility(View.VISIBLE);
				LinearLayout ll1 = (LinearLayout) findViewById(R.id.profcontent);
				ll1.setVisibility(View.VISIBLE);

				ProgressBar load = (ProgressBar) findViewById(R.id.progressBar2);
				load.setVisibility(View.GONE);

			} else if (((ProfileHelper) msg.obj).getType().equals("failed")) {

				Toast.makeText(getApplicationContext(), "Server Error!",
						Toast.LENGTH_LONG).show();

			} else if (((ProfileHelper) msg.obj).getType().equals("userdata")) {

				EditText nameField = (EditText) findViewById(R.id.profname);
				nameField.setText(((ProfileHelper) msg.obj).getData()
						.getUserName());

				EditText genField = (EditText) findViewById(R.id.profgender);
				genField.setText(((ProfileHelper) msg.obj).getData()
						.getGender());
				populateVehicleDetails();
				populateUserPic();

			}
			return false;
		}
	});

	public void populateVehicleDetails() {

		 SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		  final String userID =  pref.getString("pub_user_id", null);
		final Handler vehHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (((VehicleDetails) msg.obj).getStatus().equals("failed")) {
					Toast.makeText(getApplicationContext(), "Server Error!",
							Toast.LENGTH_LONG).show();

				} else if (((VehicleDetails) msg.obj).getStatus().equals("success")) {
					
					TextView vehicle=(TextView)findViewById(R.id.vehicdetails);
					TextView costperkm=(TextView)findViewById(R.id.ridecostperkm);
					String vehicValue="Vehicle Type:  " +((VehicleDetails) msg.obj).getVehiclemodel() +" ( "+((VehicleDetails) msg.obj).getVehicletype() + " )";
					vehicle.setText(vehicValue);
					
					String cost="Approx cost per Km: Rs. "+ ((VehicleDetails) msg.obj).getCostperkm();
					
					costperkm.setText(cost);
					
					
				} else if (((VehicleDetails) msg.obj).getStatus().equals("notfound"))  {

					TextView vehicle=(TextView)findViewById(R.id.vehicdetails);
					TextView costperkm=(TextView)findViewById(R.id.ridecostperkm);
					String vehicValue="Vehicle Type:  N/A ";
					vehicle.setText(vehicValue);
					
					String cost="Approx cost per Km: N/A ";
					
					costperkm.setText(cost);

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
							+ "/user/getvehicledetails?userID=" + userID;

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient client = new DefaultHttpClient(httpParameters);

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

	public void populateUserPic() {
		
		 SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		  final String userID =  pref.getString("pub_user_id", null);
	
	/*	final String userID = ((LiftAppGlobal) this.getApplication())
				.getPubUserId();*/

		new Thread() {
			public void run() {
				try {

					String imageURL;
					Bitmap bitmap = null;
					Log.d("Lift", "Loading Picture");

					imageURL = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/getphoto?userID="
							+ userID
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
					m_img.obj = pe;
					handler.sendMessage(m_img);

				} catch (Exception e) {

					Log.d("TAG", "Loading Picture FAILED");
					e.printStackTrace();

				}

			}
		}.start();
	}

	public void fblinkClicked(View v) {
		
		
		 SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		 final String FBuserID =  pref.getString("pub_user_id", null);
		
		 /*final String FBuserID = ((LiftAppGlobal) this.getApplication())
				.getPubUserId();*/
		
		String fbLink = "http://www.facebook.com/" + FBuserID;
		Intent fbIntent;
		
		try {
			v.getContext().getPackageManager()
					.getPackageInfo("com.facebook.katana", 0);
			fbIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("fb://profile/<id_here>"));
		} catch (Exception e) {
			fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fbLink));
		}

		startActivity(fbIntent);

	}

}
