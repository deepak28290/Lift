package indwins.c3.lift.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class HelperUtil 
{
		public static JSONObject sendPostRequest(PostMethod method) throws HttpException, IOException, JSONException
	{
		HttpClient httpClient = new HttpClient();
		StringBuffer result = new StringBuffer();
		try 
		{
			int responsecode = httpClient.executeMethod(method);
			BufferedReader rd = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			String line = "";
			while ((line = rd.readLine()) != null) 
			{
				result.append(line);
			}
			System.out.println("response code = "+ responsecode);
			System.out.println("response"+ result);
		}
		catch (UnsupportedEncodingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			method.releaseConnection();
		}
		return new JSONObject(result.toString());
	}
}
