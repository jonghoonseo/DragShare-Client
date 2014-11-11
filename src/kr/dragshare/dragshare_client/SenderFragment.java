package kr.dragshare.dragshare_client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import kr.dragshare.dragshare_client.networkManager.FTPNetworkManager;
import kr.dragshare.server.OSCPacketAddresses;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class SenderFragment extends Fragment {	
	
	private View rootView;
	
	//=======================================================================================
	//===		FTP Asynchronous Task 
	public class FTPTask extends AsyncTask<String, Integer, Void> {
		FTPNetworkManager network;
		
		final String 	host = "192.168.0.14";
		final int		port = 21;
		final String 	id	 = "Jonghoon_Seo";
		final String	pw	 = "0823";
		final String	pathPrefix = "/Users/Jonghoon_Seo/DragShare";
		
		String	targetPath = "/";
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			network = new FTPNetworkManager();
			
			// Progress Update
			//------------------------
//			network.setFTPTask(this);						// to process upload progress, transfer this instance to FTPNetworkManager 
		}
		
		@Override
		protected Void doInBackground(String... params) {
			network.initialize(host, port, id, pw);
			
			targetPath = pathPrefix + "/" + Util.getUniqueDirectory(rootView) + "/";

			if(!network.send(params[0], targetPath + Util.getFileName(params[0]))){
				Log.e("NetworkManager", "Sending Failed");
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Notify done
			Toast.makeText(rootView.getContext(), "FTP Upload Done", Toast.LENGTH_SHORT).show();

			OSCTask osc = new OSCTask();
			osc.execute(targetPath);
			
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
//        	Toast.makeText(getApplicationContext(), "transferred: " + ftp.percent + "%", Toast.LENGTH_SHORT).show();
			super.onProgressUpdate(values);
		}	
	}
	//=======================================================================================
	
	
	
	
	
	//=======================================================================================
	//===		OSC Asynchronous Task 
	public class OSCTask extends AsyncTask<String, Void, String> {		
		@Override
		protected String doInBackground(String... params) {
			// Sending OSC packet
			//----------------------------------------------------
			try {
				OSCPortOut sender = new OSCPortOut(InetAddress.getByName("192.168.0.14"), 3746);
//				OSCMessage msg = new OSCMessage(OSCPacketAddresses.OSC_SENDER_ID_PACKET, new Object[]{params[0], Util.getCurrentTimeString()});
				List<Object> arg = new ArrayList<Object>();
				arg.add(params[0]);
				arg.add(Util.getCurrentTimeString());
				OSCMessage msg = new OSCMessage(OSCPacketAddresses.OSC_SENDER_ID_PACKET, arg);
				

				sender.send(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return params[0];
		}
		
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			EditText log = (EditText)rootView.findViewById(R.id.editText1);
			log.setText("OSC sent: " + result);
		}
	}
	//=======================================================================================
	
	public SenderFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_sender, container, false);
		
		Button button = (Button)rootView.findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 1. FTP ����
	        	// Initialize FTP Task instance
	        	FTPTask ftp = new FTPTask();
	        	
	        	// Get the file name of last picture 
	        	final String lastPicture = getLastPictureName();

	        	// Notify
	        	Toast.makeText(rootView.getContext(), "Image upload: " + lastPicture, Toast.LENGTH_LONG).show();
	        	
	        	// Go and upload
	        	ftp.execute(lastPicture);
				
//				// 2. OSC ����
//				new OSCTask().execute(Util.getDeviceIdentifier(rootView), Util.getCurrentDateString());
			}
		});
		
		return rootView;
	}

	private final String getLastPictureName() {
		// Find the last picture
		String[] projection = new String[]{
		    MediaStore.Images.ImageColumns._ID,
		    MediaStore.Images.ImageColumns.DATA,
		    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
		    MediaStore.Images.ImageColumns.DATE_TAKEN,
		    MediaStore.Images.ImageColumns.MIME_TYPE
		    };
		final Cursor cursor = rootView.getContext().getContentResolver()
		        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, 
		               null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

		// Put it in the image view
		if (cursor.moveToFirst()) {
		    String imageLocation = cursor.getString(1);

		    cursor.close();
		    
		    return imageLocation;
		} 
	    cursor.close();
	    
	    return null;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

}
