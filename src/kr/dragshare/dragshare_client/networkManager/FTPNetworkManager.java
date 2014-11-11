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

import kr.dragshare.dragshare_client.Util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;

/**
 * FTP ������ �������� Wrapper Class ������.
 * initialize, send ���� ������ ���� ��������������.
 * �� �� ������ ������ FTPClient�� �������� �������� ������.
 * FTPClient�� Apache Commons ���������� ��������������.
 * ������ ������ http://commons.apache.org/proper/commons-net/ �� ����������.
 * FTP Client ����: http://commons.apache.org/proper/commons-net/examples/ftp/FTPClientExample.java
 * @author Jonghoon Seo
 *
 */
public class FTPNetworkManager implements NetworkManager {
    public static final String TAG = "FTPNetworkManager";

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
		// �������� ���� 
		try {
			ftp.connect(host, port);
			
	        // After connection attempt, you should check the reply code to verify
	        // success.
	        int reply = ftp.getReplyCode();

	        if (!FTPReply.isPositiveCompletion(reply))
	        {
	            ftp.disconnect();
//	            System.err.println("Error: FTP server refused connection.");
	            Log.e(TAG, "Error: FTP server refused connection.");
	            return false;
	        }
		} catch (SocketException e) {
			System.err.println("Error: socket timeout could not be set.");
			System.err.println(e.getMessage());
			
			Log.e(TAG, "Error: socket timeout could not be set.");
			Log.e(TAG, e.getMessage());
			
            return false;
		} catch (IOException e) {
			System.err.println("Error: the socket could not be opened. In most cases you will only want to catch IOException since SocketException is derived from it.");
			System.err.println(e.getMessage());

			Log.e(TAG, "Error: the socket could not be opened. In most cases you will only want to catch IOException since SocketException is derived from it.");
			Log.e(TAG, e.getMessage());

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
			Log.e(TAG, "Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			Log.e(TAG, e.getMessage());

			return false;
		} 
		
		try {
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (IOException e) {
			Log.e(TAG, "Set File Type Erorr");
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
			Log.e(TAG,"Error: sending file can not be openned.");
			Log.e(TAG, e.getMessage());
			
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
			Log.e(TAG,"Error: sending file can not be openned.");
			Log.e(TAG, e.getMessage());
			
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
			
			Log.i(TAG, "Sending Success");
		} catch (IOException e) {
			Log.e(TAG, "Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			Log.e(TAG, e.getMessage());
			
            return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#receive(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean receive(String sourceFileName, String targetFileName) {
		// Check type between src and dst 
		if(sourceFileName.toCharArray()[sourceFileName.length()-1] == '/' && targetFileName.toCharArray()[targetFileName.length()-1] != '/'
				|| sourceFileName.toCharArray()[sourceFileName.length()-1] != '/' && targetFileName.toCharArray()[targetFileName.length()-1] == '/') {
			Log.e(TAG, "type mismatch between source and target");
			return false;
		}
		
		if(sourceFileName.lastIndexOf("/") == sourceFileName.length()-1) {		// if a directory
			// at first, check whether target directory is exist
			File dir = new File(targetFileName);
			if(!dir.exists())
				dir.mkdirs();
			
            try {
				for (FTPFile f : ftp.listFiles(sourceFileName)) {
				    String fileName = f.getName();
				    if(!receive(sourceFileName+fileName, targetFileName+fileName)) {
				    	Log.e(TAG, "receive error");
				    	return false;
				    }
				}
			} catch (IOException e) {
				Log.e(TAG,  e.getMessage());
			}
			
			return true;
		} else {																// if a file
			FileOutputStream stream;
			try {
				stream = new FileOutputStream(targetFileName); 
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Error: receiving file may be exist in local host.");
				Log.e(TAG, e.getMessage());
				
	            return false;
			}
			return receive(sourceFileName, stream);
		}
	}
	
	@Override
	public boolean receive(String sourceFileName,
			FileOutputStream targetOutputStream) {
        try {
			ftp.retrieveFile(sourceFileName, targetOutputStream);
			targetOutputStream.close();
		} catch (IOException e) {
			Log.e(TAG, "Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			Log.e(TAG, e.getMessage());
			
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
        	Log.e(TAG, "closing error");
        	Log.e(TAG, f.getMessage());
    		return false;
        }
		return true;
	}
}
