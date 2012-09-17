package com.google.flowwall.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.http.util.EncodingUtils;

import android.util.Log;

public class FileUtil {

	private static String TAG = "FileUtil";

	public static void writeFile(String content, String path) {
		File file = new File(path);
		FileOutputStream fos = null;
		try {
			if (!file.exists()) {
					file.createNewFile();
			}
			fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			fos.flush();
		} catch (IOException e) {
			Log.d(TAG, "FileUtil writeFile = " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fos = null;
			}
		}
	}

	public static void readDev(String[] ethData, String[] gprsData, String[] wifiData) {
		FileReader fr = null;
		try {
			fr = new FileReader(Constants.DEV_FLOW_FILE);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "readDev = " + e.getMessage());
			return;
		}
		
		String line = null;;
		String[] segs;
		String[] netData;
		int count = 0;
		int k,j;
		BufferedReader br = new BufferedReader(fr, 500);
		try {
			while ((line = br.readLine())!= null) {
				segs = line.split(":");
				if(line.startsWith(Constants.ETHLINE)) {
					netData = segs[1].trim().split(" ");
					for(k=0,j=0;k<netData.length;k++) {  
	                     if(netData[k].length()>0) {   
	                          ethData[j]=netData[k];  
	                          j++;  
	                     }  
	                  }  
				}else if(line.startsWith(Constants.GPRSLINE)){
					netData=segs[1].trim().split(" ");
					 for(k=0,j=0;k<netData.length;k++){
						 if(netData[k].length()>0) {
							 gprsData[j]=netData[k];  
		                     j++; 
						 }
					 }
				} else if(line.startsWith(Constants.WIFILINE)) {
					netData=segs[1].trim().split(" ");
					for(k=0,j=0;k<netData.length;k++){
						if(netData[k].length()>0) {
							 wifiData[j]=netData[k];  
		                     j++;
						}
					}
				}
				count++;
			}
		} catch (IOException e) {
			Log.d(TAG, "readDev " + e.getMessage());
		} finally {
			if(fr != null) {
				try {
					fr.close();
					fr = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				br = null;
			}
		}
	}
	
	public static String getFileInfo(String path) {
		File file;
		String str = "";
		FileInputStream in = null;
		try {
			file = new File(path);
			in = new FileInputStream(file);
			int length = (int) file.length();
			byte[] temp = new byte[length];
			in.read(temp, 0, length);
			str = EncodingUtils.getString(temp, Constants.TEXT_ENCODING);
		} catch (IOException e) {
			Log.d(TAG, "FileUtil getFileInfo = " + e.getMessage());
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				in = null;
			}
		}
		return str;
	}
	
	public static String formetFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = fileS + " B";
		} else {
			fileSizeString = df.format((double) fileS / 1024) + " KB";
		}/* else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + " MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + " GB";
		}*/
		return fileSizeString;
	}
}
