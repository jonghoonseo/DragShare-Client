package kr.dragshare.dragshare_client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
				OSCPortOut sender = new OSCPortOut(InetAddress.getByName("165.132.107.90"), 3746);

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
//	        	final String lastPicture = getLastPictureName();

				// Notify
//	        	Toast.makeText(rootView.getContext(), "Image upload: " + lastPicture, Toast.LENGTH_LONG).show();
//	        	TextView tv = (TextView)rootView.findViewById(R.id.editText1);
//	        	tv.setText("Upload: "+lastPicture);
//
//	    		BaasioManager baas = new BaasioManager();
//	        	baas.send(lastPicture);

	        	List<String> pictures = getPictureNames();

	        	Toast.makeText(getActivity(), "Image upload: " + pictures.get(0) + ", " + pictures.get(1), Toast.LENGTH_LONG).show();
	        	TextView tv = (TextView)rootView.findViewById(R.id.editText1);
	        	tv.setText("Upload: " + pictures.get(0) + ", " + pictures.get(1));
	        	
	    		BaasioManager baas = new BaasioManager();
	        	baas.send(pictures);	        	
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
	
	private final List<String> getPictureNames() {
		ArrayList<String> ret = new ArrayList<String>(2);
		
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
		    ret.add(imageLocation);
		    
		    if(cursor.moveToPosition(3)) {
			    imageLocation = cursor.getString(1);
			    ret.add(imageLocation);
			    
			    return ret;
		    }
		} 
	    cursor.close();
	    
	    return ret;
	}

	public class BaasioManager {
		public BaasioManager() {
			
		}
		
		public void send(String srcFilePath ) {
			String filename = Util.getFileName(srcFilePath );

			BaasioFile uploadFile = new BaasioFile();

			@SuppressWarnings("unused")
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
		
		
		// Multi file send
		//--------------------------------------------
		boolean isProcessing;
		int totalSend;
		int finishedSend;
		List<UUID> finishedUUIDs = new ArrayList<UUID>();
		
		public void doFinishedWork() {
			finishedSend++;
			
			if(finishedSend == totalSend) {	// 완전 끝나
				OSCTask osc = new OSCTask();
				
				// UUID 배열 생성
				String[] uuidStrings = new String[finishedUUIDs.size()];
				for(int i=0; i<finishedUUIDs.size(); ++i) {
					uuidStrings[i] = finishedUUIDs.get(i).toString();
				}
				// 전송 
				osc.execute(uuidStrings);
			}
		}
		
		public void send(List<String> srcFilePaths ) {
			isProcessing = true;		// 복수개 처리를 위한 processing 플래
			totalSend = srcFilePaths.size();
			finishedSend = 0;
			
			for(String srcFilePath : srcFilePaths) {
				String filename = Util.getFileName(srcFilePath );

				BaasioFile uploadFile = new BaasioFile();

				@SuppressWarnings("unused")
				BaasioUploadAsyncTask uploadFileAsyncTask = uploadFile.fileUploadInBackground(
						srcFilePath             // 업로드하려는 파일 경로
						, filename              // 설정하려는 파일 이름
						, new BaasioUploadCallback() {

							@Override
							public void onException(BaasioException ex) {
								doFinishedWork();
								
								Log.e(TAG, "BaaS.io send error(code:" + ex.getErrorCode() + ")");
								// TODO 재전송 등의 처리가 필 
							}

							@Override
							public void onProgress(long arg0, long arg1) {
								// DO NOTHING
							}

							@Override
							public void onResponse(BaasioFile arg0) {
								Log.i(TAG, "BaaS.io send success: "+arg0.getFilename());

								// save saved file info
								finishedUUIDs.add(arg0.getUuid());
								
								// 뒷처리
								doFinishedWork();
							}
							
						});			
			}
		}
	}
}
