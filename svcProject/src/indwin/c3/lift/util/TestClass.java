package indwin.c3.lift.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestClass {
	public static void main(String[] args) throws Exception
	{
	    String str = "25-10-2014 18:21";
	    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	    Date date = df.parse(str);
	    long epoch = date.getTime();
	    System.out.println(epoch); // 1055545912454
	}
}
