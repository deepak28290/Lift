package indwin.c3.liftapp;

import indwin.c3.liftapp.pojos.LockedLiftPojo;
import indwin.c3.liftapp.utils.MarkerOptionsA;
import indwin.c3.liftapp.utils.NavDrawerItem;
import indwin.c3.liftapp.utils.NavDrawerListAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SidePanel extends FragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
		 * getLayoutInflater().inflate(R.layout.drawer_activity, content, true);
		 */setContentView(R.layout.slider_activity);

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons
				.getResourceId(0, -1)));
		// Give a lift
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons
				.getResourceId(1, -1)));
		// Find a lift
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons
				.getResourceId(2, -1)));
		// Requests with counter
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons
				.getResourceId(3, -1)));// navDrawerItems.add(new
										// NavDrawerItem(navMenuTitles[3],
										// navMenuIcons.getResourceId(3, -1),
										// true, "0"));
		//
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons
				.getResourceId(4, -1)));

		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons
				.getResourceId(5, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons
				.getResourceId(6, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons
				.getResourceId(7, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons
				.getResourceId(8, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			// displayView(0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Intent intent = null;
		Fragment f = null;
		switch (position) {
		case 0:
			intent = new Intent(this, AcceptedActivity.class);
			break;
		case 1:
			intent = new Intent(this, PassengerActivity.class);
			break;
		case 2:
			intent = new Intent(this, FirstActivity.class);
			break;
		case 3:
			intent = new Intent(this, MyRequestsActivity.class);
			break;
		case 4:
			intent = new Intent(this, ProfileActivity.class);
			break;
		case 5:
			intent = new Intent(this, VerifyActivity.class);
			break;
		case 6:
			intent = new Intent(this, DrawerHomeActivity.class);
			break;
		case 7:
			intent = new Intent(this, DrawerHomeActivity.class);
			break;
		case 8:
			intent = new Intent(this, FBLogout.class);
			break;
		default:
			break;
		}
		if (intent != null) {

				startActivity(intent);
			//	finish();
				// update selected item and title, then close the drawer
				mDrawerList.setItemChecked(position, true);
				mDrawerList.setSelection(position);
				setTitle(navMenuTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerList);

		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	final Handler failhandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.obj.equals("failed")) {

				Toast toast = Toast.makeText(getApplicationContext(),
						"Server Communication Error. Please Try Again",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();

			}
			return false;
		}
	});

	public void checkLiftLocked(final int position) {
		
		final Toast tag = Toast.makeText(getBaseContext(), "Loading....",Toast.LENGTH_SHORT);
		new CountDownTimer(6000, 1000)
		{

		    public void onTick(long millisUntilFinished) {tag.show();}
		    public void onFinish() {tag.show();}

		}.start();
		String type;
		if (position == 1) {
			type = "passenger";
		} else {
			type = "rider";
		}
		final String usertype = type;

		final Handler handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				Intent intent = null;

				((LiftAppGlobal) getApplication())
						.setLlp((LockedLiftPojo) msg.obj);

					if (position == 1) {

						intent = new Intent(getApplicationContext(),
								PassengerActivity.class);

					} else {

						intent = new Intent(getApplicationContext(),
								FirstActivity.class);

					}
					startActivity(intent);
					finish();
					// update selected item and title, then close the drawer
					mDrawerList.setItemChecked(position, true);
					mDrawerList.setSelection(position);
					setTitle(navMenuTitles[position]);
					mDrawerLayout.closeDrawer(mDrawerList);

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
					
					  SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); 
					  String userid =  pref.getString("user_id", null);
					
				/*	String userid = ((LiftAppGlobal) getApplication())
							.getUserId();*/
					String acc_req_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/user/haslockedride?userID="
							+ userid
							+ "&usertype=" + usertype;

					HttpGet httpget = new HttpGet(acc_req_url);
					HttpResponse response = myClient.execute(httpget);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");

					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("LiftCommunication", "Server returned code "
								+ response.getStatusLine().getStatusCode());
						Message m_fail = new Message();
						m_fail.obj = "failed";

						failhandler.sendMessage(m_fail);

					} else {

						JSONObject respObj = new JSONObject(responseString);

						if (respObj.getString("haslockedride").equals("false")) {
							Message m_fail = new Message();
							LockedLiftPojo llp = new LockedLiftPojo();
							llp.setHasLockedRide(false);
							m_fail.obj = llp;

							handler.sendMessage(m_fail);

						} else {

							JSONObject resultObj = respObj
									.getJSONObject("data");

							LockedLiftPojo llp = new LockedLiftPojo();
							llp.setDestgeocode(resultObj
									.getString("destgeocode"));
							llp.setDestination(resultObj
									.getString("destination"));
							llp.setIsAccepted(resultObj.getString("isAccepted"));
							llp.setIsCompleted(resultObj
									.getString("isCompleted"));
							llp.setSource(resultObj.getString("source"));
							llp.setUserID(resultObj.getString("userID"));
							llp.setSrcgeocode(resultObj.getString("srcgeocode"));
							llp.setStarttime(resultObj.getString("starttime"));

							Message m1 = new Message();
							llp.setHasLockedRide(true);
							m1.obj = llp;

							handler.sendMessage(m1);

						}
					}

				} catch (Exception e) {

					e.printStackTrace();
					Message m_fail = new Message();
					m_fail.obj = "failed";

					failhandler.sendMessage(m_fail);

				}
			}
		}.start();

	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void takeLiftClicked(View v) {
		Intent startNewActivityOpen = new Intent(this, PassengerActivity.class);
		startActivity(startNewActivityOpen);

	}

	public void giveLiftClicked(View v) {
		Intent startNewActivityOpen = new Intent(this, FirstActivity.class);
		startActivity(startNewActivityOpen);

	}
}
