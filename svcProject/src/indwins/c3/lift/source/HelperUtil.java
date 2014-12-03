package indwins.c3.lift.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
	
	public static void sendMail(String from, String to, String subject, String body)
	{

        String host = "localhost";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try{
           // Create a default MimeMessage object.
           MimeMessage message = new MimeMessage(session);
           message.setFrom(new InternetAddress(from));
           message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
           message.setSubject(subject);
           message.setText(body);
           Transport.send(message);
           System.out.println("Sent message successfully....");
        }
        catch (MessagingException mex) 
        {
           mex.printStackTrace();
        }
	}

	public static void sendSMS(String to, String message) 
	{
		// TODO Auto-generated method stub
		
	}
}
