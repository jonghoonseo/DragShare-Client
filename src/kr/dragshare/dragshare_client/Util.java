package kr.dragshare.dragshare_client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
	public static final String getDeviceIdentifier() {
		return "Id";
	}
	
	public static final String getCurrentDateString() {
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss:SSS");
		Date dateobj = new Date();
		
		return df.format(dateobj);
	}
}
