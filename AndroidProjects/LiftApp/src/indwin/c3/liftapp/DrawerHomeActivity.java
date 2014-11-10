package indwin.c3.liftapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

	public class DrawerHomeActivity extends SidePanel {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			 ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
		        getLayoutInflater().inflate(R.layout.drawer_activity, content, true);   
		// 	setContentView(R.layout.drawer_activity);


		}

		public void takeLiftClicked(View v){
			Intent startNewActivityOpen = new Intent(this,
					PassengerActivity.class);
			startActivity(startNewActivityOpen);
			
			
		}
		
		public void giveLiftClicked(View v){
			Intent startNewActivityOpen = new Intent(this,
					FirstActivity.class);
			startActivity(startNewActivityOpen);
			
		}
	}

