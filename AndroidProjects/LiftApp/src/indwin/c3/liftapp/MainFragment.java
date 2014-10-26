package indwin.c3.liftapp;


import java.util.Arrays;

import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.*;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainFragment extends Fragment {
	private static final String TAG = "MainFragment";
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.activity_main, container, false);
	    /*LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
	    authButton.setFragment(this);
	    authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));
	   */ Session session = Session.getActiveSession();  
	    if (session != null &&
        (session.isOpened() || session.isClosed()) ) {
     onSessionStateChange(session, session.getState(), null);
 }

 uiHelper.onResume();
	    return view;
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        Request.newMeRequest(session, new Request.GraphUserCallback() {

	        	  // callback after Graph API response with user object
	        	  @Override
	        	  public void onCompleted(GraphUser user, Response response) {
	        		  if (user != null) {
/*	        			  sharedpreferences=getActivity().getSharedPreferences("FB_REFERENCE", Context.MODE_PRIVATE);
	        			  Editor editor=sharedpreferences.edit();
	        			  editor.putString("name",user.getName());
	        			  editor.commit();
*/	          			  Intent startNewActivityOpen = new Intent(getActivity(), LandingActivity.class);
      			  getActivity().startActivity(startNewActivityOpen);
	        			
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
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
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
	    
	boolean checkSessionState=	 ((LiftAppGlobal) this.getActivity().getApplication()).getSessionActive();
	if(checkSessionState==true){
		//exit
		AppExit();
	}else{
		 ((LiftAppGlobal) this.getActivity().getApplication()).setSessionActive(true);
		 //do nothing else
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
	public void AppExit()
	{

	 this.getActivity().finish();
	 Intent intent = new Intent(Intent.ACTION_MAIN);
	 intent.addCategory(Intent.CATEGORY_HOME);
	 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 startActivity(intent);

	/*int pid = android.os.Process.myPid();=====> use this if you want to kill your activity. But its not a good one to do.
	 android.os.Process.killProcess(pid);*/

	 }
}
