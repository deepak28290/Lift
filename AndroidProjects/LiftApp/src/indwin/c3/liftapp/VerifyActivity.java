package indwin.c3.liftapp;

import indwin.c3.liftapp.pojos.VehicleDetails;
import indwin.c3.liftapp.utils.ProfileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class VerifyActivity extends SidePanel {

	public static final int DL_FILE = 1;
	public static final int PAN_FILE = 2;
	private boolean phone_verified=true;
	private boolean email_verified=false;

	final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (((ProfileHelper) msg.obj).getType().equals("success")) {
				Toast.makeText(getApplicationContext(),
						"Image Succesfully Updated!", Toast.LENGTH_LONG).show();

			} else if (((ProfileHelper) msg.obj).getType().equals("failed")) {

				Toast.makeText(getApplicationContext(), "Server Error!",
						Toast.LENGTH_LONG).show();

			} else if (((ProfileHelper) msg.obj).getType().equals("wrongcode")) {

				Toast.makeText(getApplicationContext(),
						"Please enter correct code sent on email/phone",
						Toast.LENGTH_LONG).show();

			} else if (((ProfileHelper) msg.obj).getType().equals("verified")) {

				LinearLayout ll = (LinearLayout) findViewById(R.id.verifcontent);
				ll.setVisibility(View.GONE);
				LinearLayout ll2 = (LinearLayout) findViewById(R.id.accountverified);
				ll.setVisibility(View.VISIBLE);
				
				Toast.makeText(getApplicationContext(),
						"Account verified completed!", Toast.LENGTH_LONG)
						.show();
				Toast.makeText(getApplicationContext(),
						"You're now ready to ride!", Toast.LENGTH_LONG).show();

			} else if (((ProfileHelper) msg.obj).getType().equals("notsent")) {

				Toast.makeText(
						getApplicationContext(),
						"Activation code not sent yet! Check your email and number in Profile",
						Toast.LENGTH_LONG).show();

			}
			return false;
		}
	});
	
	public void checkEmailVerifStatus() {

		final Handler verifyHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				LinearLayout ll=(LinearLayout)findViewById(R.id.accountverified);
				LinearLayout ll2=(LinearLayout)findViewById(R.id.verifcontent);
				TextView tv=(TextView)findViewById(R.id.profverifstatus);
				if (((String) msg.obj).equals("failed")) {

					Toast.makeText(getApplicationContext(),
							"Error Contacting server!", Toast.LENGTH_LONG)
							.show();
				
				}else if(((String) msg.obj).equals("verified")){
				ll.setVisibility(View.VISIBLE);
				ll2.setVisibility(View.GONE);
				tv.setText("DL & PAN Verification Pending");
				}else if(((String) msg.obj).equals("not_verified")){
				
			
				}else if(((String) msg.obj).equals("only_email_verified")){
					ll.setVisibility(View.VISIBLE);
					ll2.setVisibility(View.GONE);
					tv.setText("DL & PAN Verification Pending");
				}else if(((String) msg.obj).equals("only_phone_verified")){
				
					ll.setVisibility(View.VISIBLE);
					ll2.setVisibility(View.GONE);
					tv.setText("DL & PAN Verification Pending");
				}
				
				return false;
			
			}
		});
		
		  SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		  final String userid =  pref.getString("user_id", null);
	
		  /*	final String userid = ((LiftAppGlobal) this.getApplication())
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
		getLayoutInflater().inflate(R.layout.verify_activity, content, true);
		// get vehicle details from server
		populateVehicleDetails();
		// check all verification status and flip views respectively or inflate
		checkEmailVerifStatus();
		// new views :)
	}

	public void populateVehicleDetails() {

		final Handler vehdetailhandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				if (((VehicleDetails) msg.obj).getStatus().equals("failed")) {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Error contacting server!", Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else if (((VehicleDetails) msg.obj).getStatus().equals(
						"nodata")) {

					// do nothing

				} else if (((VehicleDetails) msg.obj).getStatus().equals(
						"success")) {
					RadioGroup rg1 = (RadioGroup) findViewById(R.id.radioGroup1);
					RadioButton rb1 = (RadioButton) findViewById(R.id.radio1);
					RadioButton rb2 = (RadioButton) findViewById(R.id.radio2);

					if(((VehicleDetails) msg.obj).getVehicletype().equals("Car")){
						
						rb1.setChecked(false);
						rb2.setChecked(true);
				
					}else if(((VehicleDetails) msg.obj).getVehicletype().equals("Bike")){
					
						rb1.setChecked(true);
						rb2.setChecked(false);
					
					}
					
					EditText et1 = (EditText) findViewById(R.id.vehiclemodel);
					EditText et2 = (EditText) findViewById(R.id.vehiclenum);
					EditText et3 = (EditText) findViewById(R.id.liftcost);
					
					et1.setText(((VehicleDetails) msg.obj).getVehiclemodel());
					et2.setText(((VehicleDetails) msg.obj).getVehiclenumber());
					et3.setText(((VehicleDetails) msg.obj).getCostperkm());
					
				}

				return false;

			}
		});
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
						String userid =  pref.getString("user_id", null);	
						
						String acc_req_url = getApplicationContext().getString(
								R.string.server_url)
								+ "/user/getvehicledetails?userID=" + userid;

						HttpGet httpget = new HttpGet(acc_req_url);
						HttpResponse response = myClient.execute(httpget);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
								"UTF-8");
						VehicleDetails vd = new VehicleDetails();
						if (response.getStatusLine().getStatusCode() != 200) {

							Log.e("LiftCommunication", "Server returned code "
									+ response.getStatusLine().getStatusCode());
							Message m_fail = new Message();
							vd.setStatus("failed");
							m_fail.obj = vd;

							vehdetailhandler.sendMessage(m_fail);
						} else {

							JSONObject respObj = new JSONObject(responseString);

							if (respObj.getString("status").equals("failure")) {
								Message m_norides = new Message();
								vd.setStatus("nodata");
								m_norides.obj = vd;
								vehdetailhandler.sendMessage(m_norides);
							} else {
								JSONObject resultObj = respObj
										.getJSONObject("result");
								String userID = resultObj.getString("userID");
								String type = resultObj
										.getString("vehicletype");
								String model = resultObj
										.getString("vehiclemodel");
								String number = resultObj
										.getString("vehiclenumber");
								String cost = resultObj.getString("costperkm");
								vd.setStatus("success");
								vd.setCostperkm(cost);
								vd.setUserID(userID);
								vd.setVehiclemodel(model);
								vd.setVehiclenumber(number);
								vd.setVehicletype(type);
								Message msg = new Message();
								msg.obj = vd;

								vehdetailhandler.sendMessage(msg);

							}
						}
					} catch (Exception e) {
					
						e.printStackTrace();
						Message m_fail = new Message();
						VehicleDetails vd = new VehicleDetails();
						vd.setStatus("failed");
						m_fail.obj = vd;
						vehdetailhandler.sendMessage(m_fail);
				
					}
				}
			}.start();

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void verifysubmit(View v) {
		checkVerify();

	}

	public void panupload(View v) {
		openGallery(PAN_FILE);
	}

	public void dlupload(View v) {
		openGallery(DL_FILE);
	}

	public void updateVehicle(View v) {
		updateVehicleDetails();

	}

	public void updateVehicleDetails() {

		final Handler notifhandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				if (msg.obj.equals("failed")) {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Error contacting server!", Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

				} else {

					Toast toast = Toast.makeText(getApplicationContext(),
							"Vehicle Details updated!",
							Toast.LENGTH_LONG);
					toast.setGravity(
							Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();

					// change button text to ride locked

				}

				return false;

			}
		});

		// do validations
		try {

			RadioGroup rg1 = (RadioGroup) findViewById(R.id.radioGroup1);

			EditText et1 = (EditText) findViewById(R.id.vehiclemodel);
			EditText et2 = (EditText) findViewById(R.id.vehiclenum);
			EditText et3 = (EditText) findViewById(R.id.liftcost);

			String model = et1.getText().toString();
			String number = et2.getText().toString();
			String cost = et3.getText().toString();
			RadioButton rb1 = (RadioButton) findViewById(R.id.radio1);
			RadioButton rb2 = (RadioButton) findViewById(R.id.radio2);

			if (model.equals("") || number.equals("") || cost.equals("")) {
				Toast.makeText(getApplicationContext(),
						"Please complete vehicle details before updating.",
						Toast.LENGTH_LONG).show();
			} else {
				if (rb1.isChecked() || rb2.isChecked()) {
					String type = ((RadioButton) findViewById(rg1
							.getCheckedRadioButtonId())).getText().toString();
					final JSONObject riderPayload = new JSONObject();
					SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
					 String userid =  pref.getString("user_id", null);
					riderPayload
							.put("userID", userid);
					riderPayload.put("vehicletype", type);
					riderPayload.put("vehiclemodel", model);
					riderPayload.put("vehiclenumber", number);
					riderPayload.put("costperkm", cost);

					new Thread() {
						public void run() {
							try {
								HttpParams httpParameters = new BasicHttpParams();
								HttpConnectionParams.setConnectionTimeout(httpParameters,
										5000);
								HttpClient myClient = new DefaultHttpClient(httpParameters);

								String call_url = getApplicationContext()
										.getString(R.string.server_url)
										+ "/user/addvehicledetails";
								HttpPost post = new HttpPost(call_url);
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

									notifhandler.sendMessage(m_fail);
								} else {
									Log.i("Lift", "Call to Server Success");
									Log.i("Lift", "Response" + responseString);
									Message msg = new Message();
									msg.obj = "Success!";
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
				} else {
					Toast.makeText(getApplicationContext(),
							"Please enter all vehicle details",
							Toast.LENGTH_LONG).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Log.e("Lift", e.getMessage());

		}

	}

	public void openGallery(int req_code) {
		Intent intent = new Intent();

		intent.setType("image/*");

		intent.setAction(Intent.ACTION_GET_CONTENT);

		startActivityForResult(
				Intent.createChooser(intent, "Select file to upload "),
				req_code);

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			Uri selectedImageUri = data.getData();
			if (requestCode == DL_FILE)

			{
				String dl_path;
				if (Build.VERSION.RELEASE.compareTo(Build.VERSION_CODES.KITKAT
						+ "") >= 0) {
					dl_path = getPath(selectedImageUri);
				} else {
					dl_path = getPathNotKitKat(selectedImageUri);
				}

				System.out.println("selectedPath1 : " + dl_path);
				// call to server to upload DL
				uploadPicToServer("dl", dl_path);
			}

			if (requestCode == PAN_FILE)

			{

				String pan_path;

				if (Build.VERSION.RELEASE.compareTo(Build.VERSION_CODES.KITKAT
						+ "") >= 0) {

					pan_path = getPath(selectedImageUri);

				}

				else {

					pan_path = getPathNotKitKat(selectedImageUri);
				}

				System.out.println("selectedPath2 : " + pan_path);

				uploadPicToServer("pan", pan_path);
				// call to server to upload PAN

			}

		}

	}

	public void uploadPicToServer(final String type, final String path) {

		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		final String userID = 	 pref.getString("user_id", null);
		/*final String userID = ((LiftAppGlobal) this.getApplication())
				.getUserId();
*/
		// thread to upload photo

		new Thread() {

			@SuppressWarnings("deprecation")
			public void run() {
				try {
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient myClient = new DefaultHttpClient(httpParameters);

					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/updatephoto";

					// Convert bitmap to byte array
					File img = new File(path);
					FileInputStream fis = new FileInputStream(img);
					InputStreamBody inputStreamBody = new InputStreamBody(fis,
							userID + "_" + type + ".png");

					MultipartEntity reqEntity = new MultipartEntity(
							HttpMultipartMode.BROWSER_COMPATIBLE);
					reqEntity.addPart("imagefile", inputStreamBody);
					reqEntity.addPart("fbuserID", new StringBody(userID));
					reqEntity.addPart("docType", new StringBody(type));

					HttpPost post = new HttpPost(call_url);
					post.setEntity(reqEntity);
					HttpResponse response = myClient.execute(post);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");

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

						} else {
							Message m_req = new Message();
							ProfileHelper pe = new ProfileHelper();
							pe.setType("success");
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

	}

	public String getPathNotKitKat(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };

		CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null,
				null, null);
		Cursor cursor = cursorLoader.loadInBackground();

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		return path;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public String getPath(Uri uri) {
		String wholeID = DocumentsContract.getDocumentId(uri);

		// Split at colon, use second item in the array
		String id = wholeID.split(":")[1];

		String[] column = { MediaStore.Images.Media.DATA };

		// where id is equal to
		String sel = MediaStore.Images.Media._ID + "=?";

		Cursor cursor = getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
				new String[] { id }, null);

		String filePath = null;

		int columnIndex = cursor.getColumnIndex(column[0]);

		if (cursor.moveToFirst()) {
			filePath = cursor.getString(columnIndex);
		}

		cursor.close();
		return filePath;
	}

	public void checkVerify() {
		
		SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
		final String userid = pref.getString("user_id", null);
		
	/*	final String userid = ((LiftAppGlobal) this.getApplication())
				.getUserId();
*/
		TextView tv = (TextView) findViewById(R.id.emailverify);

		if (tv.getText().toString().equals("")) {

			Toast.makeText(getApplicationContext(),
					"Please enter verification code", Toast.LENGTH_LONG).show();
		}

		final String code = tv.getText().toString();

		new Thread() {
			public void run() {
				try {
					JSONObject payload = new JSONObject();
					payload.put("fbuserID", userid);
					payload.put("code", code);
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient myClient = new DefaultHttpClient(httpParameters);


					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/verifyemail";
					HttpPost post = new HttpPost(call_url);
					post.setHeader("Content-type", "application/json");
					StringEntity entity = new StringEntity(payload.toString());

					post.setEntity(entity);
					HttpResponse response = myClient.execute(post);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");
					if (response.getStatusLine().getStatusCode() != 200) {

						Message m_req = new Message();
						ProfileHelper pe = new ProfileHelper();
						pe.setType("failed");
						m_req.obj = pe;
						handler.sendMessage(m_req);
					} else {
						if (responseString
								.contains("No verification code found")) {
							Message m_req = new Message();
							ProfileHelper pe = new ProfileHelper();
							pe.setType("notsent");
							m_req.obj = pe;
							handler.sendMessage(m_req);
						} else if (responseString
								.contains("code doesnot match")) {

							Message m_req = new Message();
							ProfileHelper pe = new ProfileHelper();
							pe.setType("wrongcode");
							m_req.obj = pe;
							handler.sendMessage(m_req);
						} else if (responseString.contains("success")) {
							Message m_req = new Message();
							ProfileHelper pe = new ProfileHelper();
							pe.setType("verified");
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

}
