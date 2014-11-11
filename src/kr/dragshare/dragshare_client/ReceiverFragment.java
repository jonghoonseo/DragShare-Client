package kr.dragshare.dragshare_client;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.dragshare.dragshare_client.networkManager.FTPNetworkManager;
import kr.dragshare.server.OSCPacketAddresses;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class ReceiverFragment extends Fragment {
    public static final String TAG = "[R]";
	View rootView;
	
	OSCPortIn server = null;
	
	//=======================================================================================
	//===		FTP Asynchronous Task 
	public class FTPTask extends AsyncTask<String, Integer, Void> {
		FTPNetworkManager network;
		
		final String 	host = "192.168.0.14";
		final int		port = 21;
		final String 	id	 = "Jonghoon_Seo";
		final String	pw	 = "0823";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			network = new FTPNetworkManager();
			
			// Progress Update
			//------------------------
//			network.setFTPTask(this);						// to process upload progress, transfer this instance to FTPNetworkManager 
		}
		
		@Override
		protected Void doInBackground(String... params) {
			network.initialize(host, port, id, pw);
			
			Log.i(TAG + "FTP", "will download from " + params[0]);

			network.receive(params[0],
							Environment.getExternalStorageDirectory() + "/dragshare/");
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Notify done
			Log.i(TAG + "FTP", "Download Done");
			Toast.makeText(rootView.getContext(), "FTP Download Done", Toast.LENGTH_SHORT).show();
			
			OSCTask osc = new OSCTask();
			osc.execute(OSCTask.SECOND);
			
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
	public class OSCTask extends AsyncTask<Boolean, Void, Void> {
		public static final boolean FIRST = true;
		public static final boolean SECOND = false;
		
		@Override
		protected Void doInBackground(Boolean... params) {
			
			// OSC Sender 
			//--------------------------------
			OSCPortOut osc = null;
			try {
				osc = new OSCPortOut(InetAddress.getByName("192.168.0.14"), 3746);		// OSC Server
			} catch (SocketException e1) {
				Log.e(TAG + "OSC_out", e1.getMessage());
			} catch (UnknownHostException e1) {
				Log.e(TAG + "OSC_out", e1.getMessage());
			}
			
			Log.i(TAG + "OSC_out", "Creation OK");
			
			try {
				if(params[0]) {						// Send OSC packet - ID and turn on the server
					List<Object> arg = new ArrayList<Object>();
					arg.add(Util.getDeviceID(rootView));
					arg.add(Util.getCurrentTimeString());
					OSCMessage msg = new OSCMessage(OSCPacketAddresses.OSC_RECEIVER_ID_PACKET, arg);

					osc.send(msg);
					
					Log.i(TAG + "OSC_out", "ID message is sending" + arg);
				} else {							// Send OSC Final Packet
					
					OSCMessage msg = new OSCMessage(OSCPacketAddresses.OSC_RECEIVER_FINISH_PACKET, null);
 
					osc.send(msg);
					Log.i(TAG + "OSC_out", "Final message is sending");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG + "OSC_out", "Sending Error");
			}			
			return null;
		}
		
		protected void onPostExecute(Void result) {
			Log.i(TAG + "OSC_out", "Send OK");

			super.onPostExecute(result);
		}
	}
	//=======================================================================================

	public void serverOn() {
		try {
			if(server == null) {
				server = new OSCPortIn(9097);
				
				Log.i(TAG + "OSC Server", "created");
			}
			if(!server.isListening()) {
				server.addListener(OSCPacketAddresses.OSC_SERVER_PATH_PACKET, new OSCListener() {

					@Override
					public void acceptMessage(Date arg0, OSCMessage arg1) {		// inside this method is Network Thread, so it is prohibited to control UI in the this method
						Log.i(TAG + "OSC Server", "received");

						if(server.isListening()) {
							Log.i(TAG + "OSC Server", "Receiver: packet received: listening   " + (String)(arg1.getArguments().get(0)));
							
							new FTPTask().execute((String)arg1.getArguments().get(0));
						}else
							Log.i(TAG + "OSC Server", "Receiver: packet received: listening(Not)   " + (String)(arg1.getArguments().get(0)));
					}
				});
				server.startListening();
				
				showText("OSC Server Start");
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public ReceiverFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_receiver, container,
				false);
		
		Button button = (Button)rootView.findViewById(R.id.button_receiver);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 1. OSC Sending
				OSCTask task = new OSCTask();
				task.execute(OSCTask.FIRST);
			}
		});
		
		return rootView;
	}

	private void showText(String str) {
		EditText log = (EditText)rootView.findViewById(R.id.editText_receiver);
		log.setText(str);
	}
	
	@Override
	public void onDestroy() {
		server.close();
		
		super.onDestroy();
	}


	@Override
	public void onStart() {
		serverOn();
		super.onStart();
	}

	@Override
	public void onStop() {
		if(server.isListening())
			server.stopListening();
		
		super.onStop();
	}
}
