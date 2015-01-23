package indwin.c3.liftapp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.facebook.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class NotifLanding extends FragmentActivity {
	public static final String EXTRA_MESSAGE = "message";
	private MainFragment mainFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();
		String reqid = "";
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			reqid = extras.getString("reqid");
		}

		// set reqid; reqid=-1 for ride request and generated reqid for accepted
		// requests

		SharedPreferences pref = getApplicationContext().getSharedPreferences(
				"MyPref", 0);
		Editor editor = pref.edit();
		editor.putString("req_id", reqid);
		editor.putBoolean("session_active", false);
		editor.putBoolean("is_notif", true);
		editor.commit();
		/*
		 * ((LiftAppGlobal) this.getApplication()).setReqid(reqid);
		 * 
		 * ((LiftAppGlobal) this.getApplication()).setNotif(true);
		 * 
		 * ((LiftAppGlobal) this.getApplication()).setSessionActive(false);
		 */
		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			mainFragment = new MainFragment();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, mainFragment).commit();
		} else {
			// Or set the fragment from restored state info
			mainFragment = (MainFragment) getSupportFragmentManager()
					.findFragmentById(android.R.id.content);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

}
