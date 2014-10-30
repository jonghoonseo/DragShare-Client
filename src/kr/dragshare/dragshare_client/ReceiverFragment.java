package kr.dragshare.dragshare_client;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import kr.dragshare.dragshare_client.networkManager.FTPNetworkManager;
import kr.dragshare.server.OSCPacketAddresses;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class ReceiverFragment extends Fragment {

	View rootView;
	boolean isFirst = true;
	
	OSCPortIn server = null;
	// TODO: 현재 문제 1. stop listening 이후에 초기 패킷은 들어옴 => Flag! 
	// TODO: 현재 문제 2. start listening 하면 stop 동안 처리 되지 않은 패킷이 몰려서 들어옴

	
	//=======================================================================================
	//===		FTP Asynchronous Task 
	public class FTPTask extends AsyncTask<String, Integer, Void> {
		FTPNetworkManager network;
		
		final String 	host = "165.132.107.90";
		final int		port = 21;
		final String 	id	 = "msl";
		final String	pw	 = "0";
		
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
			
			targetPath = "/" + Util.getUniqueDirectory(rootView) + "/";

			// TODO: FTP Download
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Notify done
			Toast.makeText(rootView.getContext(), "FTP Upload Done", Toast.LENGTH_SHORT).show();

			OSCTask osc = new OSCTask();
			osc.execute();
			
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
	public class OSCTask extends AsyncTask<Void, Void, Void> {

		
		@Override
		protected Void doInBackground(Void... params) {
			
			// 처음에는 일단 연결부터
			//--------------------------------
			OSCPortOut osc = null;
			try {
				osc = new OSCPortOut(InetAddress.getByName("165.132.107.90"), 3746);
			} catch (SocketException e1) {
				Log.e("Receiver OSC", e1.getMessage());
			} catch (UnknownHostException e1) {
				Log.e("Receiver OSC", e1.getMessage());
			}
			

			try {
				if(isFirst) {						// 첫 OSC 패킷이라면 -> 자기 자신을 등록 
					isFirst = false;
					
					OSCMessage msg = new OSCMessage(OSCPacketAddresses.OSC_RECEIVER_ID_PACKET, new Object[]{Util.getDeviceID(rootView), Util.getCurrentTimeString()});

					osc.send(msg);
				} else {							// 두번째 OSC 패킷이라면 -> 전송 완료를 공지
					isFirst = true;
					
					OSCMessage msg = new OSCMessage(OSCPacketAddresses.OSC_RECEIVER_FINISH_PACKET, null);

					osc.send(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			if(!isFirst) {
				Toast.makeText(getActivity(), "FTP download start", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), "ALL FINISH", Toast.LENGTH_SHORT).show();
			}
			
			// 2. FTP로 파일 가져오기
			// 3. OSC Listener에서 완료 여부 OSC 전송
		}
	}
	//=======================================================================================

	
	
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
				// 1. 자기 정보 OSC 전송
				OSCTask task = new OSCTask();
				task.execute();
			}
		});
		
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		isFirst= true;
	}

	@Override
	public void onStop() {
		super.onStop();
		
		
		// TODO: OSC Server를 제거 해줘야 다음번 실행에서 에러가 나지 않을텐데...
//		while(server.isListening()){
//			server.stopListening();
//		}
//		server.close();

	}
	
	
	
}
