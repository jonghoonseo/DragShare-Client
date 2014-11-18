package kr.dragshare.dragshare_client;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import com.kth.baasio.callback.BaasioDownloadAsyncTask;
import com.kth.baasio.callback.BaasioDownloadCallback;
import com.kth.baasio.entity.file.BaasioFile;
import com.kth.baasio.exception.BaasioException;

public class ReceiverFragment extends Fragment {
    public static final String TAG = "[R]";
	View rootView;
	
	OSCPortIn server = null;	
	
	
	
	
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
							Log.i(TAG + "OSC Server", "Receiver: packet received: listening   " + (String)(arg1.getArguments().size() + " arguments are received"));
							
							// 파일 다운로드
							receive((String)(arg1.getArguments().get(0)));
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
	
	// BaaS.io 에서 파일 다운로드
	//--------------------------
	protected void receive(String uuidString) {
    	String localPath = Environment.getExternalStorageDirectory() + "/dragshare/";
    	
    	UUID uuid = UUID.fromString(uuidString);

    	BaasioFile downloadFile = new BaasioFile();
    	downloadFile.setUuid(uuid);
    	downloadFile.setFilename(uuidString+".jpg");
    	
    	// 다운로드
    	@SuppressWarnings("unused")
		BaasioDownloadAsyncTask downloadFileAsyncTask = downloadFile.fileDownloadInBackground(
    	    localPath       // 다운로드 경로
    	    , new BaasioDownloadCallback() {

    	            @Override
    	            public void onResponse(String localFilePath) {
    	                // 성공
    	            	EditText et = (EditText)rootView.findViewById(R.id.editText_receiver);
    	            	et.setText("Download success");
    	            	Toast.makeText(getActivity(), "다운로드가 성공하였습니다",	Toast.LENGTH_LONG).show();
    	            }

    	            @Override
    	            public void onProgress(long total, long current) {
    	                // 진행 상황
    	            }

    	            @Override
    	            public void onException(BaasioException e) {
    	                // 실패
    	            	EditText et = (EditText)rootView.findViewById(R.id.editText_receiver);
    	            	et.setText("Download failed");
    	            	Toast.makeText(getActivity(), "다운로드가 실패하였습니다",	Toast.LENGTH_LONG).show();
    	            	
    	            	Log.e(TAG, "다운로드 실패: "+ e.getErrorCode());
    	            }
    	        });

	}
	
	protected void receive(ArrayList<String> uuidStrings) {
		
		
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
