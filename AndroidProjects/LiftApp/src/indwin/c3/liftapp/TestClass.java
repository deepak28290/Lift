package indwin.c3.liftapp;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.Message;
import android.util.Log;

public class TestClass {
	  public static void main(String[] args) {
	        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	        int[] len = {2,3,4,5,6,7,8,9,10};
	        for(int i=0;i<9;i++){
	        iterate(chars, len[i], new char[len[i]], 0);
	        }
	    }

	    public static void iterate(char[] chars, int len, char[] build, int pos) {
	        if (pos == len) {
	            String word = new String(build);
	        /*    
	        	HttpClient myClient = new DefaultHttpClient();
				
				String call_url ="https://www.ridingo.com/singleFieldUpdate?authentication=bearer_YW5kcm9pZDphaEJ6Zm5KcFpHVnBibTE1WTJGeWQyVmljaEVMRWdSVmMyVnlHSUNBZ0xhdG43c0tEQTowNS0xMS0yMDE0IDIwNTM6MDUtMTItMjAxNCAyMDUz";
				JSONObject riderPayload=new JSONObject();
				riderPayload.put("key","work_email");
				riderPayload.put("value",email);
				HttpPost post = new HttpPost(
						call_url);
				post.setHeader("Content-type",
						"application/json");
				StringEntity entity = new StringEntity(
						riderPayload.toString());

				post.setEntity(entity);
				HttpResponse response = myClient.execute(post);
				HttpEntity ent = response.getEntity();
				String responseString = EntityUtils.toString(
						ent, "UTF-8");
				if (response.getStatusLine().getStatusCode() != 200) {

							} else {
				
				}
	            // do what you need with each word here
	            return;
	        }*/

	        for (int i = 0; i < chars.length; i++) {
	            build[pos] = chars[i];
	            iterate(chars, len, build, pos + 1);
	        }
	    }

}
}