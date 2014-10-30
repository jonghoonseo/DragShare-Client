package kr.dragshare.dragshare_client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;

public class Util {
	// DeviceID + ���� �ð�
	public static final String getUniqueDirectory(View v) {
		String time = Long.toString(System.currentTimeMillis()); 
		
		return getDeviceID(v) + "_"+time;
	}
	
	// DeviceID
	public static final String getDeviceID(View v) {
		// IP Address ��������
		WifiManager wifiManager = (WifiManager)v.getContext().getApplicationContext().getSystemService(
												v.getContext().getApplicationContext().WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		int addr1 = (ipAddress & 0xff);
		int addr2 = (ipAddress >> 8 & 0xff);
		int addr3 = (ipAddress >> 16 & 0xff);
		int addr4 = (ipAddress >> 24 & 0xff);
		return String.format("%d.%d.%d.%d", addr1, addr2, addr3, addr4);
		
		// �ٸ����� ã�� IP Address �������� �ڵ� - IPv6 �ּҸ� �����ֳ�;;;
//		try { 
//			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) { 
//				NetworkInterface intf = en.nextElement(); 
//				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();  enumIpAddr.hasMoreElements(); ) { 
//					InetAddress inetAddress = enumIpAddr.nextElement(); 
//					if (!inetAddress.isLoopbackAddress())
//						return inetAddress.getHostAddress().toString(); 
//				} 
//			} 
//		} catch (SocketException ex) { 
//			Log.e("Util", ex.toString()); 
//		} 
		
		// UDID
//		return Secure.getString(v.getContext().getContentResolver(), Secure.ANDROID_ID);
	}
	
	// ���� �ð��� ���� ���ڿ�
	public static final String getCurrentTimeString() {
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss:SSS");
		Date dateobj = new Date();
		
		return df.format(dateobj);
	}
	
	// Full Path���� ���ϸ� ����
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
