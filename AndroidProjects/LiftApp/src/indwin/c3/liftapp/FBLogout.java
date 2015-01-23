package indwin.c3.liftapp;

import com.facebook.Session;
import com.facebook.widget.LoginButton;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class FBLogout extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
		super.onCreate(savedInstanceState);
		/**
		 * Logout From Facebook 
		 */
		    Session session = Session.getActiveSession();
		    if (session != null) {

		        if (!session.isClosed()) {
		            session.closeAndClearTokenInformation();
		            //clear your preferences if saved
		        }
		    } else {

		        session = new Session(this.getApplicationContext());
		        Session.setActiveSession(session);

		        session.closeAndClearTokenInformation();
		            //clear your preferences if saved

		    }
			
		ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
	        getLayoutInflater().inflate(R.layout.activity_main, content, true);   
	        Intent startNewActivityOpen = new Intent(this,
					MainActivity.class);
	      /*  ((LiftAppGlobal) this.getApplication())
			.setFblogout(true);
	   */
	   
	        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
	        Editor editor = pref.edit();
			editor.putBoolean("is_fb_logout", true);
			editor.commit();
			
			
	        
	        startNewActivityOpen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        
			startActivity(startNewActivityOpen);
			
			finish();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		// rest of the code
	}
}
