package kr.dragshare.dragshare_client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.provider.Settings.Secure;
import android.view.View;

public class Util {
	// DeviceID + 현재 시각
	public static final String getUniqueDirectory(View v) {
		String time = Long.toString(System.currentTimeMillis()); 
		
		return getDeviceID(v) + "_"+time;
	}
	
	// DeviceID
	public static final String getDeviceID(View v) {
		return Secure.getString(v.getContext().getContentResolver(), Secure.ANDROID_ID);
	}
	
	// 현재 시각에 대한 문자열
	public static final String getCurrentTimeString() {
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss:SSS");
		Date dateobj = new Date();
		
		return df.format(dateobj);
	}
	
	// Full Path에서 파일명 추출
	public static String getFileName(String fullPath) {
		int S = fullPath.lastIndexOf("/");
		int M = fullPath.lastIndexOf(".");
		int E = fullPath.length();
		
		String filename = fullPath.substring(S+1, M);
		String extname = fullPath.substring(M+1, E);
		
		String extractFileName = filename + "." + extname;
		return extractFileName;
	}

	public static String getDirectoryPath(String fullPath) {
		int S = fullPath.lastIndexOf("/");
		
		String filename = fullPath.substring(0, S);
		
		return filename;
	}
}
