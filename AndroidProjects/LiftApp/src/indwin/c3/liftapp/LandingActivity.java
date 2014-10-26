package indwin.c3.liftapp;


import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LandingActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landing_main);
			

		Session s=Session.getActiveSession();
		
		  Request.newMeRequest(s, new Request.GraphUserCallback() {

        	  // callback after Graph API response with user object
        	  @Override
        	  public void onCompleted(GraphUser user, Response response) {
        		  if (user != null) {
        			  Toast.makeText(getApplicationContext(), user.getName(), Toast.LENGTH_SHORT).show();
        			  TextView welcome = (TextView) findViewById(R.id.textView1);
        			  welcome.setText("Hi, "+user.getName());

        			}
        	  }
        	}).executeAsync();
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	if(	Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data)){
		Session s=Session.getActiveSession();
		  Request.newMeRequest(s, new Request.GraphUserCallback() {

        	  // callback after Graph API response with user object
        	  @Override
        	  public void onCompleted(GraphUser user, Response response) {
        		  if (user != null) {
        			  Toast.makeText(getApplicationContext(), user.getName(), Toast.LENGTH_SHORT).show();
        			
        			}
        	  }
        	}).executeAsync();
	}
		

	}

	public void riderClicked(View v) {
		Intent i = new Intent(this, FirstActivity.class);
		startActivity(i);
	}

	public void passengerClicked(View v) {
		Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
	}

	public void profileClicked(View v) {
		Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
	}

	public void paymentsClicked(View v) {
		Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
	}

}
