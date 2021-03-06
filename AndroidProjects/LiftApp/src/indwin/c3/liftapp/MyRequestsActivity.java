package indwin.c3.liftapp;

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
import org.json.JSONArray;
import org.json.JSONObject;

import indwin.c3.liftapp.pojos.MessageListE;
import indwin.c3.liftapp.utils.GPSTracker;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
public class MyRequestsActivity extends SidePanel {
	GPSTracker gps;
	ListView msgList;
	ArrayList<MessageDetails> details;
	AdapterView.AdapterContextMenuInfo info;
	final Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.obj.equals("failed")) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"Server Communication Error. Please Try Again",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
				Intent i = new Intent(getApplicationContext(),
						DrawerHomeActivity.class);
				startActivity(i);
				finish();
			} else if (msg.obj.equals("norequests")) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"No new requests", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
			} else if (((MessageListE) msg.obj).getMessage().equals(
					"myrequests")) {

				msgList.setAdapter(new CustomAdapter(((MessageListE) msg.obj)
						.getMessageList(), getApplicationContext()));

			}

			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	setContentView(R.layout.my_requests_layout);
		 ViewGroup content = (ViewGroup) findViewById(R.id.frame_container);
	        getLayoutInflater().inflate(R.layout.my_requests_layout, content, true);   
	 
		populateList("received");

	}

	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			populateList("sent");
		} else {
			populateList("received");
		}
	}

	public void populateList(final String requestType) {

		details = new ArrayList<MessageDetails>();
		msgList = (ListView) findViewById(R.id.MessageList);
		msgList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				TextView tv = (TextView) findViewById(R.id.reqid);
		/*		for (int n = 0; n < details.size(); n++) {
					String req=details.get(n).getRequestId();
					String r2=tv.getText().toString();*/
		//			if (details.get(n).getRequestId().equals(tv.getText().toString())) { 
			((LiftAppGlobal) getApplication())
								.setMsgdetails(details.get(position));

						Intent i = new Intent(getApplicationContext(),
								RequestDetailsActivity.class);
						startActivity(i);
				//		finish();
				/*		break;
					}
				}*/
			
			}

		});
		new Thread() {
			public void run() {
				try {
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(
							httpParameters, 5000);
					HttpClient myClient = new DefaultHttpClient(
							httpParameters);

					MessageDetails Detail;
					SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
					String userid= pref.getString("user_id", null);
					
					/*String userid = ((LiftAppGlobal) getApplication())
							.getUserId();*/
					String call_url = getApplicationContext().getString(
							R.string.server_url)
							+ "/request/myrequests?userID="
							+ userid
							+ "&requestType=" + requestType;
					HttpGet httpget = new HttpGet(call_url);
					HttpResponse response = myClient.execute(httpget);
					HttpEntity ent = response.getEntity();
					String responseString = EntityUtils.toString(ent, "UTF-8");
					if (response.getStatusLine().getStatusCode() != 200) {

						Log.e("Lift", "Call to Server Failed");
						Message m_fail = new Message();
						m_fail.obj = "failed";

						handler.sendMessage(m_fail);
					} else {
						Log.i("Lift", "Call to Server Success");
						// Log.i("Lift", "Response" + responseString);
						JSONArray respArr = new JSONArray(responseString);

						JSONObject respObj;
						
						if (respArr.length() == 0) {
							Message m_norequests = new Message();
							m_norequests.obj = "norequests";
							handler.sendMessage(m_norequests);
						}

						for (int i =( respArr.length()-1); i >-1; i--) {
							
							respObj = (JSONObject) respArr.get(i);
							/*
							 * selfSource: "", selfDestination: "",
							 * selfStartTime: 1420050600000, otherSource:
							 * "indiranagar", otherDestination:
							 * "murugeshpalaya", otherStartTime: 1420050600000,
							 * requestTime: 1415052885832
							 */// handle already sent/received req case
							if(respObj.getString("requestStatus").equals("accepted")){
								//do nothing
							}else{
							String selfSource = respObj.getString("selfSource");
							String selfDestination = respObj
									.getString("selfDestination");
							String selfStartTime = respObj
									.getString("selfStartTime");
							String otherSource = respObj
									.getString("otherSource");
							String otherDestination = respObj
									.getString("otherDestination");
							Long otherStartTime = respObj
									.getLong("otherStartTime");
							Long requestTime = respObj.getLong("requestTime");
							String srcSelfCoord=respObj.getString("selfSrcGeoCode");
							String destSelfCoord=respObj.getString("selfDestGeoCode");
							String srcOtherCoord=respObj.getString("otherSrcGeoCode");
							String destOtherCoord=respObj.getString("otherDestGeoCode");
							String requestId=respObj.getString("requestId");
							String status=respObj.getString("requestStatus");
							String name=respObj.getString("otherUserName");
							String otherUserID=respObj.getString("otherFbUserID");
							Date reqTime = new Date(requestTime);
							Date requestDate = new Date(requestTime);
								Detail = new MessageDetails();
								Detail.setUserimage(R.drawable.boy);
								Detail.setName(name);
								Detail.setRidessofar("10");
								Detail.setDesc1("500m from your start location");
								Detail.setFrom("From " + otherSource);
								Detail.setTime("" + reqTime);
								Detail.setTo("to " + otherDestination);
								Detail.setReqtime("" + requestDate);
								Detail.setSrcSelfCoord(srcSelfCoord);
								Detail.setSrcOtherCoord(srcOtherCoord);
								Detail.setDesSelfCoord(destSelfCoord);
								Detail.setDesOtherCoord(destOtherCoord);
								Detail.setUserid(otherUserID);  
								Detail.setRequestId(requestId);
								Detail.setStatus(status);
								Detail.setType(requestType);
								
								details.add(Detail);
			}

						Message msg = new Message();
						MessageListE msgListE = new MessageListE();
						msgListE.setMessage("myrequests");
						
						msgListE.setMessageList(details);
						msg.obj = msgListE;

						// String a = responseString;

						handler.sendMessage(msg);
						}
					}

				} catch (Exception e) {
					e.getMessage();
					e.printStackTrace();
					Message m_fail = new Message();
					m_fail.obj = "failed";

					handler.sendMessage(m_fail);
				}
			}
		}.start();

	}

}