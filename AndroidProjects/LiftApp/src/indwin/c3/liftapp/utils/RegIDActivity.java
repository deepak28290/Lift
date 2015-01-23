package indwin.c3.liftapp.utils;

import indwin.c3.liftapp.DrawerHomeActivity;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class RegIDActivity extends Activity{
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static String TAG = "LaunchActivity";
	protected String SENDER_ID = "112323008459";
	private GoogleCloudMessaging gcm =null;
	private String regid = null;
	private Context context= null;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
	     super.onCreate(savedInstanceState);
	     context = getApplicationContext();
	         if (checkPlayServices()) 
	     {
	            gcm = GoogleCloudMessaging.getInstance(this);
	            regid = getRegistrationId(context);

	            if (regid.isEmpty())
	            {
	                registerInBackground();
	            }
	            else
	            {
	            Log.d(TAG, "No valid Google Play Services APK found.");
	            }
	      }
	         
	         Intent i = new Intent(this, DrawerHomeActivity.class);
	 		startActivity(i);
	 }

	@Override protected void onResume()
	{
	       super.onResume();       checkPlayServices();
	}


	// Implement GCM Required methods (Add below methods in LaunchActivity)

	private boolean checkPlayServices() {
	        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	        if (resultCode != ConnectionResult.SUCCESS) {
	            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
	            } else {
	                Log.d(TAG, "This device is not supported - Google Play Services.");
	                finish();
	            }
	            return false;
	        }
	        return true;
	 }

	private String getRegistrationId(Context context) 
	{
	   final SharedPreferences prefs = getGCMPreferences(context);
	   String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	   if (registrationId.isEmpty()) {
	       Log.d(TAG, "Registration ID not found.");
	       return "";
	   }
	   int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	   int currentVersion = getAppVersion(context);
	   if (registeredVersion != currentVersion) {
	        Log.d(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}

	private SharedPreferences getGCMPreferences(Context context) 
	{
	    return getSharedPreferences(RegIDActivity.class.getSimpleName(),
	                Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) 
	{
	     try 
	     {
	         PackageInfo packageInfo = context.getPackageManager()
	                    .getPackageInfo(context.getPackageName(), 0);
	            return packageInfo.versionCode;
	      } 
	      catch (NameNotFoundException e) 
	      {
	            throw new RuntimeException("Could not get package name: " + e);
	      }
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerInBackground() 
	{     new AsyncTask() {
	     @Override
	     protected Object doInBackground(Object... params) 
	     {
	          String msg = "";
	          try 
	          {
	               if (gcm == null) 
	               {
	                        gcm = GoogleCloudMessaging.getInstance(context);
	               }
	               regid = gcm.register(SENDER_ID);               Log.d(TAG, "########################################");
	               Log.d(TAG, "Current Device's Registration ID is: "+msg);     
	               
	          } 
	          catch (IOException ex) 
	          {
	              msg = "Error :" + ex.getMessage();
	          }
	          return null;
	     }     protected void onPostExecute(Object result) 
	     { //to do here
	    	 
	     }
	  }.execute(null, null, null);
	}
}
