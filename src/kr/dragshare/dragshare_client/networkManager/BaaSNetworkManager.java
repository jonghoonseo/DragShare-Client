/**
 * 
 */
package kr.dragshare.dragshare_client.networkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import kr.dragshare.dragshare_client.Util;
import android.util.Log;

import com.kth.baasio.callback.BaasioUploadAsyncTask;
import com.kth.baasio.callback.BaasioUploadCallback;
import com.kth.baasio.entity.file.BaasioFile;
import com.kth.baasio.exception.BaasioException;
import com.kth.common.utils.LogUtils;

/**
 * @author Jonghoon Seo
 *
 */
public class BaaSNetworkManager implements NetworkManager {
    private static final String TAG = LogUtils.makeLogTag(BaaSNetworkManager.class);

	UUID savedUuid;
	
	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#initialize(java.lang.String, int, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean initialize(String host, int port, String id, String pw) {
//		Baas.io().init(this, BaasioConfig.BAASIO_URL, BaasioConfig.BAASIO_ID,
//        BaasioConfig.APPLICATION_ID);

		return false;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#close()
	 */
	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#send(java.io.File, java.lang.String)
	 */
	@Override
	public boolean send(File file, String targetFileName) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#send(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean send(String name, String targetFileName) {
    	String srcFilePath = name;
    	String filename = Util.getFileName(name);

    	BaasioFile uploadFile = new BaasioFile();
    	uploadFile.setProperty("memo", "이미지 업로드 테스트");   // 파일 추가 정보1
    	uploadFile.setProperty("integer", 1);                       // 파일 추가 정보2
    	uploadFile.setProperty("long", Long.valueOf("1"));          // 파일 추가 정보3
    	uploadFile.setProperty("dragshare", "test");

    	BaasioUploadAsyncTask uploadFileAsyncTask = uploadFile.fileUploadInBackground(
    	    srcFilePath             // 업로드하려는 파일 경로
    	    , filename              // 설정하려는 파일 이름
    	    , new BaasioUploadCallback() {

    	            @Override
    	            public void onResponse(BaasioFile response) {
    	                // 성공
    	                String memo = response.getProperty("memo").getTextValue();
    	                int intValue = response.getProperty("integer").getIntValue();
    	                long longValue = response.getProperty("long").getLongValue();
    	                
                      savedUuid = response.getUuid();
	                    
                      Log.i(TAG, "Upload Success");
    	            }

    	            @Override
    	            public void onProgress(long total, long current) {
                        // 진행
    	            }

    	            @Override
    	            public void onException(BaasioException e) {
    	                // 실패
    	            	Log.e(TAG, "Upload failed");
    	            }
    	        });

		return false;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#send(java.io.FileInputStream, java.lang.String)
	 */
	@Override
	public boolean send(FileInputStream stream, String targetFileName) {
		Log.e(TAG, "not implemented");
		return false;
	}
	
	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#send(java.lang.String, java.lang.String)
	 */
	public List<UUID> send( List<String> files, UUID deviceID) {
		final List<UUID> ret = new ArrayList<UUID>();
		
		for (String file : files) {
			String srcFilePath = file;
	    	String filename = Util.getFileName(deviceID.toString()+file);

	    	BaasioFile uploadFile = new BaasioFile();

	    	BaasioUploadAsyncTask uploadFileAsyncTask = uploadFile.fileUploadInBackground(
	    	    srcFilePath             // 업로드하려는 파일 경로
	    	    , filename              // 설정하려는 파일 이름
	    	    , new BaasioUploadCallback() {
	    	            @Override
	    	            public void onResponse(BaasioFile response) {
	    	                // 성공
	                      ret.add( response.getUuid() );
	                      
	                      Log.i(TAG, "Upload Success");
	    	            }

	    	            @Override
	    	            public void onProgress(long total, long current) {
	                        // 진행
	    	            }

	    	            @Override
	    	            public void onException(BaasioException e) {
	    	                // 실패
	    	            	Log.e(TAG, "Upload failed");
	    	            }
	    	        });
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#receive(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean receive(String sourceFileName, String targetFileName) {
		Log.e(TAG, "not implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#receive(java.lang.String, java.io.FileOutputStream)
	 */
	@Override
	public boolean receive(String sourceFileName,
			FileOutputStream targetOutputStream) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.dragshare_client.networkManager.NetworkManager#isConnected()
	 */
	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

}
