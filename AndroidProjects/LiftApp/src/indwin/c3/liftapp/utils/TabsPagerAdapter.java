package indwin.c3.liftapp.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
public class TabsPagerAdapter extends FragmentPagerAdapter {

public TabsPagerAdapter(FragmentManager fm) {
 super(fm);
}

@Override
public Fragment getItem(int index) {
	 switch (index) {
     case 0:
         // Top Rated fragment activity
         return new SentRequestsFragment();
     case 1:
         // Games fragment activity
         return new ReceivedRequestsFragment();
     case 2:
         // Movies fragment activity
         return new PastRequestsFragment();
     }

     return null;
}

@Override
public int getCount() {
	// TODO Auto-generated method stub
	return 3;
}
}