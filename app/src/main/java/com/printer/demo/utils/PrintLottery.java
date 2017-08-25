package com.printer.demo.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import com.printer.sdk.PrinterInstance;
import com.printer.sdk.utils.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class PrintLottery {
	
	
	private static final String TAG = "PrintLottery";

	public static void printLottery(Context mcontxt,PrinterInstance mPrinter){
		
		String path = "/sdcard/fucaiA.txt";
		XTUtils.copyFilesFromassets(mcontxt, "fucaiB.txt", path);
		File f = new File(path);
		Log.i(TAG, f.getAbsolutePath());
		if (!f.exists()) {
			Log.i(TAG, "文件不存在！");
			Toast.makeText(mcontxt, "文件不存在！", 1).show();
			return ;
		}
		String readFromFile = XTUtils.readFromFile(f);
		 Log.i(TAG, "readFromFile:"+readFromFile);;
		 byte[] conver16HexToByte = XTUtils.conver16HexToByte(readFromFile);
		 mPrinter.sendBytesData(conver16HexToByte);
//		 Utils.write2File(conver16HexToByte,"/sdcard/Logs/", "log"+new Random().nextInt(100)+".log", true);
//		return conver16HexToByte;
	}

}
