package indwin.c3.liftapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PassengerFragment extends Fragment {
	
	public PassengerFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
         
        return rootView;
    }
	
	
	
	public void takeLiftClicked(View v){
		Intent startNewActivityOpen = new Intent(getActivity(),
				PassengerActivity.class);
		getActivity().startActivity(startNewActivityOpen);
		
		
	}
	
	public void giveLiftClicked(View v){
		Intent startNewActivityOpen = new Intent(getActivity(),
				FirstActivity.class);
		getActivity().startActivity(startNewActivityOpen);
		
	}
	
}
