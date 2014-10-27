package kr.dragshare.dragshare_client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class SenderFragment extends Fragment {	
	
	private View rootView;
	
	public class OSCTask extends AsyncTask<String, Void, Void> {		
		@Override
		protected Void doInBackground(String... params) {
			// Sending OSC packet
			//----------------------------------------------------
			try {
				OSCPortOut sender = new OSCPortOut(InetAddress.getByName("165.132.107.90"), 3746);
				OSCMessage msg = new OSCMessage("/dragshare/sender", new Object[]{params[0], params[1]});

				sender.send(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPostExecute(Void result) {
			EditText log = (EditText)rootView.findViewById(R.id.editText1);
			log.setText("OSC done");
			super.onPostExecute(result);
		}
	}
	
	public SenderFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_sender, container,
				false);
		
		Button button = (Button)rootView.findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 1. FTP 전송
				// 2. OSC 전송
				new OSCTask().execute(Util.getDeviceIdentifier(), Util.getCurrentDateString());
			}
		});
		
		return rootView;
	}


}
