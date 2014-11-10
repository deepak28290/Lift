package indwin.c3.liftapp;

import indwin.c3.liftapp.utils.GPSTracker;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AcceptedActivity extends Activity {
	GPSTracker gps;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accept_activity);
		
		  Toast.makeText(getApplicationContext(), ((LiftAppGlobal) this.getApplication()).getReqid()+" request id", Toast.LENGTH_LONG).show();

		 
	}




	public void riderClicked(View v) {
		Intent i = new Intent(this, FirstActivity.class);
		startActivity(i);
	}

	public void passengerClicked(View v) {
		Intent i = new Intent(this, PassengerActivity.class);
		startActivity(i);
	}

	public void profileClicked(View v) {
		Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
	}

	public void paymentsClicked(View v) {
		Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
	}
	
	public void requestsClicked(View v){
		Intent i = new Intent(this, MyRequestsActivity.class);
		startActivity(i);
	}
}
