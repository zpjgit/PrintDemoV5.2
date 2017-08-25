package com.printer.demo;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.printer.demo.global.GlobalContants;
import com.printer.demo.utils.CodePageUtils;
import com.printer.demo.utils.PrefUtils;
import com.printer.demo.utils.XTUtils;
import com.printer.sdk.Barcode;
import com.printer.sdk.CodePagePrinter;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterConstants.BarcodeType;
import com.printer.sdk.PrinterConstants.Command;
import com.printer.sdk.PrinterConstants.PAlign;
import com.printer.sdk.PrinterInstance;

public class TextPrintActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {

	private static final String TAG = "TextPrintActivity";
	private LinearLayout header;
	private Button btn_send, btn_print_note, btn_print_table, btn_print_codepaper;
	private ToggleButton tb_isHexData;
	private EditText et_input;
	private boolean isHexData = false;
	private static PrinterInstance mPrinter;
	private String input;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, " TextPrintActivity onCreate");
		setContentView(R.layout.activity_print_text);
		init();
		mPrinter = PrinterInstance.mPrinter;
		// PrinterConstants.paperWidth =
		// PrefUtils.getInt(TextPrintActivity.this,
		// GlobalContants.PAPERWIDTH, 384);
		// Log.i(TAG, "paperWidth:" + PrinterConstants.paperWidth);
		input = et_input.getText().toString();
		et_input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				input = et_input.getText().toString();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if (GlobalContants.ISCONNECTED) {
		// if ("".equals(GlobalContants.DEVICENAME)
		// || GlobalContants.DEVICENAME == null) {
		// headerConnecedState.setText(R.string.unknown_device);
		//
		// } else {
		//
		// headerConnecedState.setText(GlobalContants.DEVICENAME);
		// }
		//
		// }

	}

	private void init() {

		et_input = (EditText) findViewById(R.id.et_input);
		et_input.setText(R.string.textprintactivty_input_content);
		header = (LinearLayout) findViewById(R.id.ll_headerview_textPrint);
		btn_send = (Button) findViewById(R.id.btn_send);
		btn_send.setOnClickListener(this);
		btn_print_note = (Button) findViewById(R.id.btn_print_note);
		btn_print_note.setOnClickListener(this);
		btn_print_table = (Button) findViewById(R.id.btn_print_table);
		btn_print_table.setOnClickListener(this);
		btn_print_codepaper = (Button) findViewById(R.id.btn_print_codepaper);
		btn_print_codepaper.setOnClickListener(this);
		btn_send.setOnClickListener(this);
		tb_isHexData = (ToggleButton) findViewById(R.id.tb_hex_on);
		tb_isHexData.setOnCheckedChangeListener(this);
		isHexData = tb_isHexData.isChecked();
		initHeader();
	}

	/**
	 * 初始化标题上的信息
	 */
	private void initHeader() {
		setHeaderLeftText(header, getString(R.string.back), new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});
		headerConnecedState.setText(getTitleState());
		setHeaderCenterText(header, getString(R.string.headview_TextPrint));
		setHeaderLeftImage(header, new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	@Override
	public void onClick(View view) {
		if (PrinterInstance.mPrinter != null && SettingActivity.isConnected) {
			if (view == btn_send) {

				final String content = et_input.getText().toString();
				Log.i(TAG, content);
				if (content != null || content.length() != 0) {

					if (isHexData && SettingActivity.isConnected) {
						byte[] srcData = XTUtils.string2bytes(content);
						mPrinter.sendBytesData(srcData);
					} else if (!isHexData && SettingActivity.isConnected) {
						// // PrinterInstance.mPrinter.printText(content +
						// "\r\n");
						new Thread(new  Runnable() {
							public void run() {
								mPrinter.printText(content + "\r\n");
							}
						}).start();
//						Locale locale = Locale.getDefault();
//						String language = locale.getLanguage();
//						String country = locale.getCountry();
//						Toast.makeText(TextPrintActivity.this, "country----" + country + "-language---" + language,
//								Toast.LENGTH_LONG).show();
					}
				}

			} else if (view == btn_print_table) {

			} else if (view == btn_print_note) {

				new Thread(new  Runnable() {
					public void run() {
						XTUtils.printNote(TextPrintActivity.this.getResources(), mPrinter);
					}
				}).start();

				// btn_print_table.setEnabled(false);
//				new Thread(new Runnable() {
//					int timeout = 8000;// 打完文字预计的时间
//
//					@Override
//					public void run() {
//
//						final StringBuffer str = new StringBuffer("");
//						// 等待runOnUiThread执行完成，不然timeout还是为0
//						Log.e("fdh", "timeout:" + timeout);
//						// 函数顺序执行到这个地方，打印的数据write给android系统了，但是不一定就发完了蓝牙了
//						int ret = mPrinter.getPrintingStatus(str, timeout);
//						Log.e("fdh", "getPrintingStatus:" + ret + " str:" + str.toString());
//
//						runOnUiThread(new Runnable() {
//							public void run() {
//								Toast.makeText(TextPrintActivity.this, str.toString(), 0).show();
//								btn_print_table.setEnabled(true);
//							}
//						});
//					}
//				}).start();

				// mPrinter.sendBytesData(new
				// byte[]{0x1d,0x28,0x48,0x06,0x00,0x30,0x30,0x31,0x32,0x33,0x34});
//				mPrinter.setPrinter(Command.ALIGN, Command.ALIGN_CENTER);
//				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.goodwork);
//				mPrinter.printImage(bmp, PAlign.START, 0, false);
//				
//				mPrinter.setPrinter(Command.ALIGN, Command.ALIGN_LEFT);
//				mPrinter.printImage(bmp, PAlign.START, 0, false);
//			    mPrinter.setCharsetName("gb18030");
//				mPrinter.printText("프린터 시험 시연.!\n");
//				mPrinter.printText("プリントテストの実演!\n");
//				new CodePagePrinter(mPrinter).printTextInCP864("Ensaio de demonstração de impressão\n");
//				mPrinter.setLeftMargin(8);
//				mPrinter.printText("hello world!\n");
//				
//				Barcode barcode2 = new Barcode(BarcodeType.QRCODE, 2, 3, 6,
//						"123456");
//				mPrinter.setLeftMargin(40);
//				PrinterInstance.mPrinter.printBarCode(barcode2);
//				mPrinter.printImage(bmp, PAlign.NONE, 0, false);
				
			} else if (view == btn_print_codepaper) {
				new CodePageUtils().selectCodePage(this, PrinterInstance.mPrinter);
			}
		} else {
			Toast.makeText(TextPrintActivity.this, getString(R.string.no_connected), 0).show();
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		if (isChecked) {// 16进制开
			isHexData = true;
			byte[] datas;
			try {
				datas = input.getBytes("GBK");
				et_input.setText(XTUtils.bytesToHexString(datas, datas.length));
				input = et_input.getText().toString();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			isHexData = false;
			if (input == null || input.length() == 0) {
				et_input.setText(R.string.textprintactivty_input_content);
			} else {
				et_input.setText(XTUtils.hexStringToString(input));
				input = et_input.getText().toString();
			}
		}

	}

}
