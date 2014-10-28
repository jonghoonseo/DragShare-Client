/**
 * 
 */
package kr.dragshare.dragshare_client.networkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import kr.dragshare.dragshare_client.MainActivity;
import kr.dragshare.dragshare_client.Util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import android.util.Log;

/**
 * FTP 작업을 수행하는 Wrapper Class 입니다.
 * initialize, send 등의 작업을 쉽게 구현하였습니다.
 * 좀 더 자세한 작업은 FTPClient를 이용하여 수행하면 됩니다.
 * FTPClient는 Apache Commons 프로젝트를 사용하였습니다.
 * 자세한 내용은 http://commons.apache.org/proper/commons-net/ 를 참고하세요.
 * FTP Client 예제: http://commons.apache.org/proper/commons-net/examples/ftp/FTPClientExample.java
 * @author Jonghoon Seo
 *
 */
public class FTPNetworkManager implements NetworkManager {
	public FTPClient ftp;
	public long transferred;

	/**
	 * 
	 */
	public FTPNetworkManager() {
		ftp = new FTPClient();
		
		isFirst = true;
	}
	
	boolean isFirst;

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#initialize(java.lang.String, int, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean initialize(String host, int port, String username, String password) {
		if(!isFirst)
			return true;
		boolean isOK = true;
		// 호스트에 연결 
		try {
			ftp.connect(host, port);
			
	        // After connection attempt, you should check the reply code to verify
	        // success.
	        int reply = ftp.getReplyCode();

	        if (!FTPReply.isPositiveCompletion(reply))
	        {
	            ftp.disconnect();
//	            System.err.println("Error: FTP server refused connection.");
	            Log.e("FTP", "Error: FTP server refused connection.");
	            return false;
	        }
		} catch (SocketException e) {
			System.err.println("Error: socket timeout could not be set.");
			System.err.println(e.getMessage());
			
			Log.e("FTP", "Error: socket timeout could not be set.");
			Log.e("FTP", e.getMessage());
			
            return false;
		} catch (IOException e) {
			System.err.println("Error: the socket could not be opened. In most cases you will only want to catch IOException since SocketException is derived from it.");
			System.err.println(e.getMessage());

			Log.e("FTP", "Error: the socket could not be opened. In most cases you will only want to catch IOException since SocketException is derived from it.");
			Log.e("FTP", e.getMessage());

            return false;		
		}
		
		// Log in
		try {
            if (!ftp.login(username, password))
            {
                ftp.logout();
                
                return false;
            }
		} catch (IOException e) {
			Log.e("FTP", "Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			Log.e("FTP", e.getMessage());

			return false;
		} 
		
		try {
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (IOException e) {
			Log.e("FTP", "Set File Type Erorr");
		}
		
		return isOK;
	}

	
	
	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#send(java.io.File, java.lang.String)
	 */
	@Override
	public boolean send(File file, String targetFileName) {
		FileInputStream stream;
		try {
			stream = new FileInputStream(file); 
		} catch (FileNotFoundException e) {
			Log.e("FTP","Error: sending file can not be openned.");
			Log.e("FTP", e.getMessage());
			
            return false;
		}
		return send(stream, targetFileName);
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#send(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean send(String name, String targetFileName) {
		FileInputStream stream;
		try {
			stream = new FileInputStream(name); 
		} catch (FileNotFoundException e) {
			Log.e("FTP","Error: sending file can not be openned.");
			Log.e("FTP", e.getMessage());
			
            return false;
		}
		return send(stream, targetFileName);
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#send(java.io.FileInputStream, java.lang.String)
	 */
	@Override
	public boolean send(FileInputStream stream, String targetFileName) {
		try {
			ftp.makeDirectory(Util.getDirectoryPath(targetFileName));
			ftp.storeFile(targetFileName, stream);
			stream.close();
			
			Log.i("FTP", "Sending Success");
		} catch (IOException e) {
			Log.e("FTP", "Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			Log.e("FTP", e.getMessage());
			
            return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#receive(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean receive(String sourceFileName, String targetFileName) {
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(targetFileName); 
		} catch (FileNotFoundException e) {
			Log.e("FTP", "Error: receiving file may be exist in local host.");
			Log.e("FTP", e.getMessage());
			
            return false;
		}
		return receive(sourceFileName, stream);
	}
	
	@Override
	public boolean receive(String sourceFileName,
			FileOutputStream targetOutputStream) {
        try {
			ftp.retrieveFile(sourceFileName, targetOutputStream);
			targetOutputStream.close();
		} catch (IOException e) {
			Log.e("FTP", "Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			Log.e("FTP", e.getMessage());
			
            return false;
		}
        return true;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return ftp.isConnected();
	}

	@Override
	public boolean close() {
        try
        {
            ftp.noop(); // check that control connection is working OK
            ftp.logout();
            ftp.disconnect();
        }
        catch (IOException f)
        {
        	Log.e("FTP", "closing error");
        	Log.e("FTP", f.getMessage());
    		return false;
        }
		return true;
	}
}
