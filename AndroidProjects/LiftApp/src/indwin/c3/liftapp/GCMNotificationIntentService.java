package indwin.c3.liftapp;

import indwin.c3.liftapp.MainActivity;
import indwin.c3.liftapp.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMNotificationIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GCMNotificationIntentService() {
		super("GcmIntentService");
	}

	public void onCreate() {
		super.onCreate();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, startId, startId);
		Log.i("LocalService", "Received start id " + startId + ": " + intent);

		return START_STICKY;
	}

	public static final String TAG = "GCMNotificationIntentService";

	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent.getExtras()!=null){
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString(),"0");
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString(),"0");
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				for (int i = 0; i < 3; i++) {
					Log.i(TAG,
							"Working... " + (i + 1) + "/5 @ "
									+ SystemClock.elapsedRealtime());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}

				}
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				if(extras.getString("collapse_key").contains("accepted")){
				sendNotification("You have a new Ride request. "
						+ extras.get("collapse_key"),extras.getString("requestID"));
				Log.i(TAG, "Received: " + extras.toString());
				}else{
					sendNotification("You have a new Ride request. "
							+ extras.get("collapse_key"),"-1");
					Log.i(TAG, "Received: " + extras.toString());
				}
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
		}
	}

	private void sendNotification(String msg,String req_id) {
		Log.d(TAG, "Preparing to send notification...: " + msg);
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notifIntent=new Intent(this, NotifLanding.class);
		notifIntent.putExtra("reqid",req_id);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notifIntent,PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setContentTitle("Lift!")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setDefaults(Notification.DEFAULT_SOUND |
					      Notification.DEFAULT_VIBRATE).setSmallIcon(R.drawable.notif);
		mBuilder.setAutoCancel(true);
		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		Log.d(TAG, "Notification sent successfully.");
	}
}