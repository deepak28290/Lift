package indwin.c3.liftapp;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.*;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment {
	private static final String TAG = "MainFragment";
	public static boolean LAND = false;
	final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			Toast toast = Toast.makeText(
					getActivity().getApplicationContext(),
					msg.obj.toString(), Toast.LENGTH_LONG);
			toast.show();

			return false;
		}
	});
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_main, container, false);
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			// onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
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
						boolean isNotif = ((LiftAppGlobal) getActivity()
								.getApplication()).isNotif();
					
						((LiftAppGlobal) getActivity().getApplication()).setUser_name(user.getName());
						((LiftAppGlobal) getActivity().getApplication())
								.setUserId(user.getId());
						String userid = ((LiftAppGlobal) getActivity()
								.getApplication()).getUserId();
						String regid = ((LiftAppGlobal) getActivity()
								.getApplication()).getRegId();
						//bug in notiflanding gcm.. see later
						if(!regid.equals("")){
						submitRegId(userid, regid);
					}
						if(isNotif==true){
							
							((LiftAppGlobal) getActivity()
									.getApplication()).setNotif(false);
							
							//check if it's a cancel or accept notif
							if(((LiftAppGlobal) getActivity().getApplication()).getReqid().equals("-1")){
							Intent startNewActivityOpen = new Intent(getActivity(),
									MyRequestsActivity.class);
							getActivity().startActivity(startNewActivityOpen);
							
							}else {
								
								Intent startNewActivityOpen = new Intent(getActivity(),
										AcceptedActivity.class);
								getActivity().startActivity(startNewActivityOpen);
								
							}
						}else{
							Intent startNewActivityOpen = new Intent(getActivity(),
									DrawerHomeActivity.class);
							getActivity().startActivity(startNewActivityOpen);
						}

					
						
					}
				}
			}).executeAsync();

		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");

			TextView welcome = (TextView) getView().findViewById(R.id.welcome);
			welcome.setText("You've been succesfully logged out!");
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

		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			boolean checkSessionState = ((LiftAppGlobal) this.getActivity()
					.getApplication()).getSessionActive();
			if (checkSessionState == true) {
				// exit
				AppExit();
			} else {
				((LiftAppGlobal) this.getActivity().getApplication())
						.setSessionActive(true);
				onSessionStateChange(session, session.getState(), null);
			}
			LoginButton authButton = (LoginButton) this.getActivity()
					.findViewById(R.id.authButton);
			authButton.setVisibility(View.GONE);
			ProgressBar load = (ProgressBar) this.getActivity().findViewById(
					R.id.progressBar1);
			load.setVisibility(View.VISIBLE);

		} else {

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

	public void submitRegId(final String userid,final String regid) {
		try {
			new Thread() {
				public void run() {
					try {
						
						
						JSONObject regPayload=new JSONObject();
						regPayload.put("userID",userid);
						regPayload.put("appRegID",regid);
						HttpClient myClient = new DefaultHttpClient();

						String call_url = getActivity().getApplicationContext().getString(
								R.string.server_url)
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
						if (response.getStatusLine().getStatusCode() != 200) {

							Log.e("Lift", "Call to Server Failed");
							Message m_fail = new Message();
							m_fail.obj = "failed";

							handler.sendMessage(m_fail);
						} else {
							Log.i("Lift", "Call to Server Success");
							Log.i("Lift", "Response" + responseString);
						/*	Message msg = new Message();
							msg.obj = responseString;
							// String a = responseString;

							handler.sendMessage(msg);*/
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
