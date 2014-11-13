package kr.dragshare.dragshare_client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import kr.dragshare.dragshare_client.networkManager.BaaSNetworkManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import com.kth.baasio.callback.BaasioUploadAsyncTask;
import com.kth.baasio.callback.BaasioUploadCallback;
import com.kth.baasio.entity.file.BaasioFile;
import com.kth.baasio.exception.BaasioException;
import com.kth.common.utils.LogUtils;

public class SenderFragment extends Fragment {	
    private static final String TAG = LogUtils.makeLogTag(SenderFragment.class);

	
	private View rootView;
	

	//=======================================================================================
	//===		OSC Asynchronous Task 
	public class OSCTask extends AsyncTask<String, Void, String> {		
		@Override
		protected String doInBackground(String... params) {
			// Sending OSC packet
			//----------------------------------------------------
			try {
				OSCPortOut sender = new OSCPortOut(InetAddress.getByName("192.168.0.14"), 3746);

				List<Object> arg = new ArrayList<Object>();
				for(String uuid : params) {
					arg.add(uuid);
				}
				OSCMessage msg = new OSCMessage(OSCPacketAddresses.OSC_SENDER_ID_PACKET, arg);

				sender.send(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return params[0];
		}
		
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			TextView log = (TextView)rootView.findViewById(R.id.editText1);
			log.setText("OSC sent: " + result);
		}
	}
	//=======================================================================================
	
	public SenderFragment() {
	}

	// 클릭 이벤트!!!
	//-----------------
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_sender, container, false);
		
		Button button = (Button)rootView.findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
	        	// Get the file name of last picture 
	        	final String lastPicture = getLastPictureName();

	        	// Notify
	        	Toast.makeText(rootView.getContext(), "Image upload: " + lastPicture, Toast.LENGTH_LONG).show();
	        	TextView tv = (TextView)rootView.findViewById(R.id.editText1);
	        	tv.setText("Upload: "+lastPicture);
	        	
	        	send(lastPicture);
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

	private void send(String srcFilePath ) {
		String filename = Util.getFileName(srcFilePath );

		BaasioFile uploadFile = new BaasioFile();

		BaasioUploadAsyncTask uploadFileAsyncTask = uploadFile.fileUploadInBackground(
				srcFilePath             // 업로드하려는 파일 경로
				, filename              // 설정하려는 파일 이름
				, new BaasioUploadCallback() {

					@Override
					public void onResponse(BaasioFile response) {
						// 성공
						Log.i(TAG, "Upload Success: "+ response.getUuid().toString());

						OSCTask osc = new OSCTask();
						osc.execute(response.getUuid().toString());
					}

					@Override
					public void onProgress(long total, long current) {
						// 진행
						TextView tv = (TextView)rootView.findViewById(R.id.editText1);
						tv.setText("Uploading: " + current + "/" + total);
					}

					@Override
					public void onException(BaasioException e) {
						// 실패
						Log.e(TAG, "Upload failed: " + e.getErrorCode());
						Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_LONG).show();
					}

				});
	}

}
