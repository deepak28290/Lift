package indwin.c3.liftapp;

import indwin.c3.liftapp.pojos.ProfileData;
import indwin.c3.liftapp.utils.ProfileHelper;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends SidePanel {
	
	private boolean phone_verified=true;
	private boolean email_verified=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
		getLayoutInflater().inflate(R.layout.profile_activity, content, true);
		/* ((LiftAppGlobal) this.getApplication()).setUserId("123"); */
		getUserDetailsFromServer();
		checkVerifStatus();
	}

	public void checkVerifStatus() {

		final Handler verifyHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
			
				TextView tv = (TextView)findViewById(R.id.dlverifstatus);
				ImageView iv=(ImageView) findViewById(R.id.verificon);
				if (((String) msg.obj).equals("failed")) {

					Toast.makeText(getApplicationContext(),
							"Error Contacting server!", Toast.LENGTH_LONG)
							.show();
				
				}else if(((String) msg.obj).equals("verified")){
					
					tv.setText("You're all set to use Lift!");
					iv.setImageResource(R.drawable.tick);
				
				}else if(((String) msg.obj).equals("not_verified")){
				
					tv.setText("Email and Phone Verification Pending! Update Profile to resend code!");
					phone_verified=false;
					email_verified=false;
			
				}else if(((String) msg.obj).equals("only_email_verified")){
				
					tv.setText("Phone Verification Pending!  Update Profile to resend code!");
					phone_verified=false;
					email_verified=true;
				
				}else if(((String) msg.obj).equals("only_phone_verified")){
				
					tv.setText("Email Verification Pending! Update Profile to resend code!");
					phone_verified=true;
					email_verified=true;
				
				}
				
				return false;
			
			}
		});
	
		 SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		  final String userid =  pref.getString("user_id", null);
		
		  /*final String userid = ((LiftAppGlobal) this.getApplication())
				.getUserId();*/

		new Thread() {
			public void run() {
				try {

					String url;
					Log.d("Lift", "Loading Picture");

					url = getApplicationContext()
							.getString(R.string.server_url)
							+ "/user/getverificationstatus?userID=" + userid;
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
						m_fail.obj = "failed";
						verifyHandler.sendMessage(m_fail);

					} else {

						JSONObject result = new JSONObject(responseString);
						if (result.getString("status").equals("verified")) {

							Message m_pass = new Message();
							m_pass.obj = "verified";
							verifyHandler.sendMessage(m_pass);

						} else if (result.getString("status").equals("pending")) {

							if (result.getString("message").contains(
									"Not verified")) {
								Message m_pass = new Message();
								m_pass.obj = "not_verified";
								verifyHandler.sendMessage(m_pass);

							} else if (result.getString("message").contains(
									"Phone number")) { // Phone number
														// verification pending
								Message m_pass = new Message();
								m_pass.obj = "only_email_verified";
								verifyHandler.sendMessage(m_pass);

							} else if (result.getString("message").contains(
									"Email")) { // Email verification pending

								Message m_pass = new Message();
								m_pass.obj = "only_phone_verified";
								verifyHandler.sendMessage(m_pass);

							}

						}

					}

				} catch (Exception e) {
					Log.d("TAG", "Loading Picture FAILED");
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void getUserDetailsFromServer() {

		 SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		  final String userID =  pref.getString("user_id", null);
		
		/*final String userID = ((LiftAppGlobal) this.getApplication())
				.getUserId();*/
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

	final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (((ProfileHelper) msg.obj).getType().equals("success")) {
				Toast.makeText(getApplicationContext(),
						"Profile Succesfully Updated!", Toast.LENGTH_LONG)
						.show();

			} else if (((ProfileHelper) msg.obj).getType().equals("image")) {

				ImageView iv = (ImageView) findViewById(R.id.profpic);
		
				if(((ProfileHelper) msg.obj).getBmp()!=null){
		
				iv.setImageBitmap(getCroppedBitmap(
						((ProfileHelper) msg.obj).getBmp(), 200));
				}
				iv.setVisibility(View.VISIBLE);
				LinearLayout ll1 = (LinearLayout) findViewById(R.id.profcontent);
				ll1.setVisibility(View.VISIBLE);
				LinearLayout ll2 = (LinearLayout) findViewById(R.id.profverifcontent);
				ll2.setVisibility(View.VISIBLE);

				ProgressBar load = (ProgressBar) findViewById(R.id.progressBar2);
				load.setVisibility(View.GONE);

			} else if (((ProfileHelper) msg.obj).getType().equals("failed")) {

				Toast.makeText(getApplicationContext(), "Server Error!",
						Toast.LENGTH_LONG).show();

			} else if (((ProfileHelper) msg.obj).getType().equals("firstuser")) {

				populateFBDetails();
				populateEmailFromDevice();
				populatePhoneFromDevice();
				populateUserPic(true);// pic from fb

			} else if (((ProfileHelper) msg.obj).getType().equals("userdata")) {

				EditText nameField = (EditText) findViewById(R.id.profname);
				nameField.setText(((ProfileHelper) msg.obj).getData()
						.getUserName());

				EditText emailField = (EditText) findViewById(R.id.profemail);
				emailField.setText(((ProfileHelper) msg.obj).getData()
						.getEmail());

				EditText phField = (EditText) findViewById(R.id.profphone);
				phField.setText(((ProfileHelper) msg.obj).getData().getPhone());

				EditText genField = (EditText) findViewById(R.id.profgender);
				genField.setText(((ProfileHelper) msg.obj).getData()
						.getGender());
				populateUserPic(false);// pic from db
			} else if (((ProfileHelper) msg.obj).getType().equals("sent")) {

				Intent startNewActivityOpen = new Intent(
						getApplicationContext(), DrawerHomeActivity.class);
				startActivity(startNewActivityOpen);
			}
			return false;
		}
	});

	public void populateEmailFromDevice() {
		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(this.getApplicationContext())
				.getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				String possibleEmail = account.name;

				EditText emailField = (EditText) findViewById(R.id.profemail);
				emailField.setText(possibleEmail);
			}
		}
	}

	public void populatePhoneFromDevice() {
		TelephonyManager tMgr = (TelephonyManager) this.getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tMgr.getLine1Number() != null) {
			EditText phField = (EditText) findViewById(R.id.profphone);
			phField.setText(tMgr.getLine1Number());
		}

	}

	public void populateUserPic(final boolean fb) {
	
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		  final String userID =  pref.getString("user_id", null);
		/*
		final String userID = ((LiftAppGlobal) this.getApplication())
				.getUserId();
*/
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

	public void populateFBDetails() {
		Session session = Session.getActiveSession();
		Request.newMeRequest(session, new Request.GraphUserCallback() {

			// callback after Graph API response with user object
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (user != null) {
					EditText nameField = (EditText) findViewById(R.id.profname);
					nameField.setText(user.getName());

					JSONObject jsonUser = user.getInnerJSONObject();
					try {
						String gender = jsonUser.getString("gender");

						if ("male".equals(gender)) {
							gender = "Male";
						} else {
							gender = "Female";
						}
						EditText genField = (EditText) findViewById(R.id.profgender);
						genField.setText(gender);

					} catch (JSONException e) {
						Log.e("Lift", e.toString());
						e.printStackTrace();
					}
				}

			}
		}).executeAsync();
	}

	public void profsubmit(View v) {

		// update data and image in seperate calls
		profUpdAsync();
	}

	public void profVerif(View v) {
		Intent startNewActivityOpen = new Intent(this, VerifyActivity.class);
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

	public void profUpdAsync() {
		// do validation
		EditText nameView = (EditText) findViewById(R.id.profname);
		EditText emailView = (EditText) findViewById(R.id.profemail);
		EditText phoneView = (EditText) findViewById(R.id.profphone);
		EditText genderView = (EditText) findViewById(R.id.profgender);
		ImageView iprofile = (ImageView) findViewById(R.id.profpic);

		final String name = nameView.getText().toString().trim();
		final String email = emailView.getText().toString().trim();
		final String phone = phoneView.getText().toString().trim();
		final String gender = genderView.getText().toString().trim();
		final Bitmap bitmap = ((BitmapDrawable) iprofile.getDrawable())
				.getBitmap();

		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher m = p.matcher(email);
		boolean matchFound = m.matches();
		if (name.equals("") || email.equals("") || phone.equals("")
				|| gender.equals("")) {

			Toast toast = Toast.makeText(getApplicationContext(),
					"Please fill all fields", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();

		} else if (!matchFound) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Please enter a valid email address!", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		} else if (phone.length() != 10) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Ensure mobile number has 10 digits!", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		} else {

			new Thread() {
				public void run() {
					try {
						SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
						String userID =  pref.getString("user_id", null);
						
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 5000);
						HttpClient myClient = new DefaultHttpClient(
								httpParameters);

						String call_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/user/updateprofile";

						final JSONObject riderPayload = new JSONObject();
						riderPayload.put("userName", name);
						riderPayload.put("emailID", email);
						riderPayload.put("fbuserID",
								userID);
						riderPayload.put("gender", gender);
						riderPayload.put("phone", phone);

						StringEntity entity = new StringEntity(
								riderPayload.toString());
						HttpPost post = new HttpPost(call_url);
						post.setHeader("Content-type", "application/json");

						post.setEntity(entity);
						HttpResponse response = myClient.execute(post);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
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
							if (responseString.contains("success")) {
								Message m_req = new Message();
								ProfileHelper pe = new ProfileHelper();
								pe.setType("code_sent");
								m_req.obj = pe;
								handler.sendMessage(m_req);

							}

						}

					} catch (Exception e) {
						e.printStackTrace();
						/*
						 * Message m_fail = new Message(); ((ProfileHelper)
						 * m_fail.obj).setType("failed");
						 * 
						 * handler.sendMessage(m_fail);
						 */
					}
				}
			}.start();
			
			SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
			  final String userID =  pref.getString("user_id", null);
			/*
			final String userID = ((LiftAppGlobal) this.getApplication())
					.getUserId();*/
			// thread to upload photo
			new Thread() {
				@SuppressWarnings("deprecation")
				public void run() {
					try {
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 5000);
						HttpClient myClient = new DefaultHttpClient(
								httpParameters);

						String call_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/user/updatephoto";

						// Convert bitmap to byte array

						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.PNG,
								0 /* ignored for PNG */, bos);
						byte[] bitmapdata = bos.toByteArray();

						InputStreamBody inputStreamBody = new InputStreamBody(
								new ByteArrayInputStream(bitmapdata), userID
										+ ".png");

						MultipartEntity reqEntity = new MultipartEntity(
								HttpMultipartMode.BROWSER_COMPATIBLE);
						reqEntity.addPart("imagefile", inputStreamBody);
						reqEntity.addPart("fbuserID", new StringBody(userID));
						reqEntity.addPart("docType", new StringBody("photo"));

						HttpPost post = new HttpPost(call_url);
						post.setEntity(reqEntity);
						HttpResponse response = myClient.execute(post);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
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
								Message m_req = new Message();
								ProfileHelper pe = new ProfileHelper();
								pe.setType("failed");
								m_req.obj = pe;
								handler.sendMessage(m_req);

							}

						}

					} catch (Exception e) {
						e.printStackTrace();
						Message m_fail = new Message();
						((ProfileHelper) m_fail.obj).setType("failed");

						handler.sendMessage(m_fail);
					}
				}
			}.start();

			// check if account verified or not. if not send verification code
			
			if(email_verified==false){
			
			sendVerificationCodeEmail(userID); // TO-DO do this only if account is
											// not verified yet!
			}
		}

	}

	public void sendVerificationCodeEmail(final String userid) {

		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
					JSONObject payload = new JSONObject();
					payload.put("id", userid);
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient client = new DefaultHttpClient(httpParameters);


					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/sendemailverifycode";
					HttpPost post = new HttpPost(call_url);
					post.setHeader("Content-type", "application/json");
					StringEntity entity = new StringEntity(payload.toString());

					post.setEntity(entity);
					HttpResponse response = client.execute(post);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");
					if (response.getStatusLine().getStatusCode() != 200) {

						Message m_req = new Message();
						ProfileHelper pe = new ProfileHelper();
						pe.setType("failed");
						m_req.obj = pe;
						handler.sendMessage(m_req);
					} else {
						if (responseString.contains("success")) {
							Message m_req = new Message();
							ProfileHelper pe = new ProfileHelper();
							pe.setType("sent");
							m_req.obj = pe;
							handler.sendMessage(m_req);
						}
					}

				} catch (Exception e) {
					e.getMessage();
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	
/*	private void toggleServerLoading(boolean show) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.overlay);
		if (show == true) {
			ll.setVisibility(View.VISIBLE);

		} else {
			ll.setVisibility(View.GONE);
		}
	}*/
}
