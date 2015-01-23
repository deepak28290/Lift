package indwin.c3.liftapp;

import java.io.BufferedInputStream;

import indwin.c3.liftapp.pojos.ProfileData;
import indwin.c3.liftapp.pojos.RatingHelper;
import indwin.c3.liftapp.utils.ProfileHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.facebook.Session;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
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

public class DrawerHomeActivity extends SidePanel {
	final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (((ProfileHelper) msg.obj).getType().equals("image")) {
				LinearLayout ll = (LinearLayout) findViewById(R.id.landingprof);
				ll.setVisibility(View.VISIBLE);

				ImageView iv = (ImageView) findViewById(R.id.profpicD);
				iv.setImageBitmap(getCroppedBitmap(
						((ProfileHelper) msg.obj).getBmp(), 200));
				iv.setVisibility(View.VISIBLE);
				ProgressBar load = (ProgressBar) findViewById(R.id.progressBar3);
				load.setVisibility(View.GONE);
			} else if (((ProfileHelper) msg.obj).getType().equals("failed")) {

				Toast.makeText(getApplicationContext(), "Server Error!",
						Toast.LENGTH_LONG).show();

			} else if (((ProfileHelper) msg.obj).getType().equals("userdata")) {

				TextView nameField = (TextView) findViewById(R.id.profnameD);
				nameField.setText(((ProfileHelper) msg.obj).getData()
						.getUserName());

				populateUserPic(false);// pic from db
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
		getLayoutInflater().inflate(R.layout.drawer_activity, content, true);
		toggleServerLoading(false);
		populateUserDetails();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		toggleServerLoading(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		toggleServerLoading(false);
	}

	public void takeLiftClicked(View v) {

		// checkLiftLocked(1);
		toggleServerLoading(true);
		Intent startNewActivityOpen = new Intent(this, PassengerActivity.class);
		startActivity(startNewActivityOpen);

	}

	public void giveLiftClicked(View v) {
		toggleServerLoading(true);
		Intent startNewActivityOpen = new Intent(this, FirstActivity.class);
		startActivity(startNewActivityOpen);

	}

	public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
		Bitmap sbmp;

		if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
			float smallest = Math.min(bmp.getWidth(), bmp.getHeight());
			float factor = smallest / radius;
			sbmp = Bitmap.createScaledBitmap(bmp,
					(int) (bmp.getWidth() / factor),
					(int) (bmp.getHeight() / factor), false);
		} else {
			sbmp = bmp;
		}

		Bitmap output = Bitmap.createBitmap(radius, radius, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, radius, radius);

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		canvas.drawCircle(radius / 2 + 0.7f, radius / 2 + 0.7f,
				radius / 2 + 0.1f, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(sbmp, rect, rect, paint);

		return output;
	}

	public void populateUserPic(final boolean fb) {
		
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		final String userID = pref.getString("user_id", null);
		 
		/*final String userID = ((LiftAppGlobal) this.getApplication())
				.getUserId();
		*/final Handler ratehandler = new Handler(new Handler.Callback() {

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
							+ "/rating/getrating?userID=" + userID;

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
					imageURL = "http://graph.facebook.com/" + userID
							+ "/picture?type=large";
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

	public void populateUserDetails() {
		/*final String userID = ((LiftAppGlobal) this.getApplication())
				.getUserId();
		*/
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		final String userID = pref.getString("user_id", null);
		
		new Thread() {
			public void run() {
				try {

					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/getprofile?userID=" + userID;
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
							pe.setType("firstuser");
							mfirst.obj = pe;
							handler.sendMessage(mfirst);
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

	private void toggleServerLoading(boolean show) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.overlay);
		if (show == true) {
			ll.setVisibility(View.VISIBLE);

		} else {
			ll.setVisibility(View.GONE);
		}
	}
}
