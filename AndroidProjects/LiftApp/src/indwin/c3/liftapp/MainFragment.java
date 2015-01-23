package indwin.c3.liftapp;

import indwin.c3.liftapp.utils.ConnectionDetector;

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
import org.json.JSONObject;

import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.*;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainFragment extends Fragment {
	private static final String TAG = "MainFragment";
	public static boolean LAND = false;
	final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			try {
				if (((String) msg.obj).equals("newuser")) {
					Intent startNewActivityOpen = new Intent(getActivity(),
							ProfileActivity.class);
					getActivity().startActivity(startNewActivityOpen);
					getActivity().finish();
				} else if (((String) msg.obj).equals("olduser")) {
					// check if user has accepted request

					checkAcceptedRequest();

				} else {
					checkAcceptedRequest();
				}
			} catch (Exception e) {
				 Log.e("Lift"," "+ e.getMessage().toString()); 
				e.printStackTrace();
			}
			// commented cos o/w fails with IO error

			/*
			 * Toast toast = Toast.makeText(
			 * getActivity().getApplicationContext(), msg.obj.toString(),
			 * Toast.LENGTH_LONG); toast.show();
			 */
			return false;
		}
	});

	public void checkAcceptedRequest() {

		SharedPreferences pref = getActivity().getApplicationContext()
				.getSharedPreferences("MyPref", 0);
		final String userid = pref.getString("user_id", null);
		// final String userid = ((LiftAppGlobal)
		// getActivity().getApplication()).getUserId();

		final Handler thandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				try {
					if (((String) msg.obj).equals("failed")) {

					} else if (((String) msg.obj).equals("false")) {

						Intent startNewActivityOpen = new Intent(getActivity(),
								DrawerHomeActivity.class);
						getActivity().startActivity(startNewActivityOpen);
						getActivity().finish();
					} else {

						SharedPreferences pref = getActivity()
								.getApplicationContext().getSharedPreferences(
										"MyPref", 0); // 0 - for private mode
						Editor editor = pref.edit();
						editor.putString("req_id", (String) msg.obj);
						editor.commit();
						// ((LiftAppGlobal)
						// getActivity().getApplication()).setReqid((String)msg.obj);
						Intent startNewActivityOpen = new Intent(getActivity(),
								AcceptedActivity.class);
						getActivity().startActivity(startNewActivityOpen);
						getActivity().finish();
					}
				} catch (Exception e) {
					Log.e("Lift", "Call to Server Failed");
					e.printStackTrace();
				}

				return false;
			}
		});

		new Thread() {
			public void run() {
				try {
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							5000);
					HttpClient myClient = new DefaultHttpClient(httpParameters);

					String call_url = getActivity().getApplicationContext()
							.getString(R.string.server_url)
							+ "/user/hasacceptedride?userID=" + userid;
					HttpGet httpget = new HttpGet(call_url);
					HttpResponse response = myClient.execute(httpget);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");
					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("Lift", "Call to Server Failed");
						Message m_fail = new Message();
						m_fail.obj = "failed";

						handler.sendMessage(m_fail);
					} else {
						Log.i("Lift", "Call to Server Success");
						JSONObject respObj = new JSONObject(responseString);
						String resp;
						if (respObj.getString("status").equals("success")) {
							if (respObj.getString("hasacceptedride").equals(
									"true")) {
								resp = respObj.getString("requestid");
							} else {
								resp = "false";
							}
						} else {
							resp = "failed";
						}
						Message msg = new Message();
						msg.obj = resp;

						// String a = responseString;

						thandler.sendMessage(msg);
					}

				} catch (Exception e) {
					e.getMessage();
					e.printStackTrace();
					Message m_fail = new Message();
					m_fail.obj = "failed";

					handler.sendMessage(m_fail);
				}
			}
		}.start();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_main, container, false);

		ConnectionDetector cd = new ConnectionDetector(getActivity()
				.getApplicationContext());

		Boolean isInternetPresent = cd.isConnectingToInternet();
		if (isInternetPresent == false) {
			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					"No Internet Connection", Toast.LENGTH_LONG);
			toast.show();

		} else {
			Session session = Session.getActiveSession();
			if (session != null && (session.isOpened() || session.isClosed())) {
				// onSessionStateChange(session, session.getState(), null);
			}

			uiHelper.onResume();
		}
		return view;
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
			Request.newMeRequest(session, new Request.GraphUserCallback() {

				// callback after Graph API response with user object
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (user != null) {
						LoginButton authButton = (LoginButton) getActivity()
								.findViewById(R.id.authButton);
						authButton.setVisibility(View.GONE);
						ProgressBar load = (ProgressBar) getActivity()
								.findViewById(R.id.progressBar1);
						load.setVisibility(View.VISIBLE);
						
						SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); 
						boolean isNotif= pref.getBoolean("is_notif", false);
						/*boolean isNotif = ((LiftAppGlobal) getActivity()
								.getApplication()).isNotif();*/

						if (isNotif == true) {
							Editor editor = pref.edit();
							editor.putBoolean("is_notif", false);
							editor.commit();
							
							String req_id=pref.getString("req_id", "-1");
							// check if it's a cancel or accept notif
							/*if (((LiftAppGlobal) getActivity().getApplication())
									.getReqid().equals("-1")) {*/
								
									if(req_id.equals("-1")){
								Intent startNewActivityOpen = new Intent(
										getActivity(), MyRequestsActivity.class);
								getActivity().startActivity(
										startNewActivityOpen);
								getActivity().finish();

							} else {

								Intent startNewActivityOpen = new Intent(
										getActivity(), AcceptedActivity.class);
								getActivity().startActivity(
										startNewActivityOpen);
								getActivity().finish();
							}
						} else {
							
							Editor editor = pref.edit();
							editor.putString("user_name", user.getName());
							editor.putString("user_id", user.getId());
							editor.commit();
					/*		((LiftAppGlobal) getActivity().getApplication())
									.setUser_name(user.getName());
							((LiftAppGlobal) getActivity().getApplication())
									.setUserId(user.getId());
					*/		
							String userid =	pref.getString("user_id", null);	
							
							String regid= pref.getString("reg_id", null);
					/*		String userid = ((LiftAppGlobal) getActivity()
									.getApplication()).getUserId();
				String regid = ((LiftAppGlobal) getActivity()
									.getApplication()).getRegId();*/			
							// bug in notiflanding gcm.. see later
							if (!regid.equals("")) {
								submitRegId(userid, regid);
							}
							
						}

					} else {
						LoginButton authButton = (LoginButton) getActivity()
								.findViewById(R.id.authButton);
						authButton.setVisibility(View.VISIBLE);
						ProgressBar load = (ProgressBar) getActivity()
								.findViewById(R.id.progressBar1);
						load.setVisibility(View.GONE);
					}
				}
			}).executeAsync();

		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");

			/*
			 * TextView welcome = (TextView)
			 * getView().findViewById(R.id.welcome);
			 * welcome.setText("You've been succesfully logged out!");
			 */
		}
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			// onSessionStateChange(session, state, exception);
		}
	};

	private UiLifecycleHelper uiHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		ConnectionDetector cd = new ConnectionDetector(getActivity()
				.getApplicationContext());

		Boolean isInternetPresent = cd.isConnectingToInternet();
		if (isInternetPresent == false) {
			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					"No Internet Connection", Toast.LENGTH_LONG);
			toast.show();

		} else {
			// For scenarios where the main activity is launched and user
			// session is not null, the session state change notification
			// may not be triggered. Trigger it if it's open/closed.
			Session session = Session.getActiveSession();
			if (session != null && (session.isOpened() || session.isClosed())) {
		
				
				SharedPreferences pref = getActivity().getApplicationContext()
						.getSharedPreferences("MyPref", 0);
				boolean checkSessionState = pref.getBoolean("session_active",false);
				boolean isFbLogout = pref.getBoolean("is_fb_logout", false);
				/*		boolean checkSessionState = ((LiftAppGlobal) this.getActivity()
						.getApplication()).getSessionActive();*/
				if (checkSessionState == true) {
					
					Editor editor = pref.edit();
					editor.putBoolean("session_active", true);
					editor.commit();
					
					/*((LiftAppGlobal) this.getActivity().getApplication())
							.setSessionActive(true);*/
					
					onSessionStateChange(session, session.getState(), null);

				}else if(isFbLogout == true){	 
				
				/*else if (((LiftAppGlobal) getActivity().getApplication())
						.isFblogout() == true) {*/
				/*	((LiftAppGlobal) getActivity().getApplication())
							.setFblogout(false);*/
				//	
					Editor editor = pref.edit();
					editor.putBoolean("is_fb_logout", true);
					editor.commit();
					LoginButton authButton = (LoginButton) getActivity()
							.findViewById(R.id.authButton);
					authButton.setVisibility(View.VISIBLE);
					ProgressBar load = (ProgressBar) getActivity()
							.findViewById(R.id.progressBar1);
					load.setVisibility(View.GONE);

				} else {
					/*((LiftAppGlobal) this.getActivity().getApplication())
							.setSessionActive(true);*/
					Editor editor = pref.edit();
					editor.putBoolean("session_active", true);
					editor.commit();
				
					onSessionStateChange(session, session.getState(), null);

				}
			} else {
				LoginButton authButton = (LoginButton) getActivity()
						.findViewById(R.id.authButton);
				authButton.setVisibility(View.VISIBLE);
				ProgressBar load = (ProgressBar) getActivity().findViewById(
						R.id.progressBar1);
				load.setVisibility(View.GONE);
			}

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	public void AppExit() {

		this.getActivity().finish();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

		/*
		 * int pid = android.os.Process.myPid();=====> use this if you want to
		 * kill your activity. But its not a good one to do.
		 * android.os.Process.killProcess(pid);
		 */

	}

	public void submitRegId(final String userid, final String regid) {
		try {
			new Thread() {
				public void run() {
					try {

						JSONObject regPayload = new JSONObject();
						regPayload.put("userID", userid);
						regPayload.put("appRegID", regid);
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 5000);
						HttpClient myClient = new DefaultHttpClient(
								httpParameters);

						String call_url = getActivity().getApplicationContext()
								.getString(R.string.server_url)
								+ "/user/addappregid";
						HttpPost post = new HttpPost(call_url);
						post.setHeader("Content-type", "application/json");
						StringEntity entity = new StringEntity(
								regPayload.toString());

						post.setEntity(entity);
						HttpResponse response = myClient.execute(post);
						HttpEntity ent = response.getEntity();
						String responseString = EntityUtils.toString(ent,
								"UTF-8");

						String checknew_call = getActivity()
								.getApplicationContext().getString(
										R.string.server_url)
								+ "/user/isnewuser?userID=" + userid;
						HttpGet httpget = new HttpGet(checknew_call);
						HttpResponse getResponse = myClient.execute(httpget);
						HttpEntity entit = getResponse.getEntity();
						String respoString = EntityUtils.toString(entit,
								"UTF-8");
						if (getResponse.getStatusLine().getStatusCode() != 200) {

							Log.e("LiftCommunication", "Server returned code "
									+ getResponse.getStatusLine()
											.getStatusCode());
							Message m_fail1 = new Message();
							m_fail1.obj = "failed";

							handler.sendMessage(m_fail1);
						} else {

							JSONObject jObj = new JSONObject(respoString);
							if (jObj.getString("status").contains("success")
									&& jObj.getString("newuser").equals("1")) {

								Message msg = new Message();
								msg.obj = "newuser";// sending newuser if user
													// is logging in for the
													// first time
								handler.sendMessage(msg);

							} else {
								Message msg = new Message();
								msg.obj = "olduser";// sending newuser if user
													// is logging in for the
													// first time
								handler.sendMessage(msg);
							}

						}
						if (response.getStatusLine().getStatusCode() != 200) {
							Log.e("LiftCommunication", "Server returned code "
									+ getResponse.getStatusLine()
											.getStatusCode());
							Message m_fail1 = new Message();
							m_fail1.obj = "failed";
						} else {
							Log.i("Lift", "Call to Server Success");
							Log.i("Lift", "Response" + responseString);
							/*
							 * Message msg = new Message(); msg.obj =
							 * responseString; // String a = responseString;
							 * 
							 * handler.sendMessage(msg);
							 */
						}

					} catch (Exception e) {
						e.getMessage();
						e.printStackTrace();
						Message m_fail = new Message();
						m_fail.obj = "Server Error!";

						handler.sendMessage(m_fail);
					}
				}
			}.start();
		} catch (Exception e) {
			Log.e("Lift", "Call to Server Failed");
			Message m_fail = new Message();
			m_fail.obj = "failed";

			handler.sendMessage(m_fail);
		}
	}
}
