package com.printer.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.printer.demo.global.GlobalContants;
import com.printer.demo.utils.CodePageUtils;
import com.printer.demo.utils.PrefUtils;
import com.printer.demo.utils.PrintLabel58;
import com.printer.demo.utils.PrintLottery;
import com.printer.demo.utils.XTUtils;
import com.printer.sdk.CodePagePrinter;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterConstants.CommandTSPL;
import com.printer.sdk.PrinterConstants.Connect;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.exception.ParameterErrorException;
import com.printer.sdk.exception.PrinterPortNullException;
import com.printer.sdk.exception.ReadException;
import com.printer.sdk.exception.WriteException;
import com.printer.sdk.usb.USBPort;
import com.printer.sdk.utils.XLog;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class SettingActivity extends BaseActivity
		implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener {
	private LinearLayout header;
	private TextView tvShowPrinterType;
	private TextView tvShowPrinterPortType;
	private Spinner spinner_printer_type;
	private Spinner spinner_interface_type;
	private List<String> data_list;
	private ArrayAdapter<CharSequence> arr_adapter;
	private ArrayAdapter<CharSequence> printType_adapter;
	private final static int SCANNIN_GREQUEST_CODE = 2;
	public static final int CONNECT_DEVICE = 1;
	public static final int ENABLE_BT = 3;
	// 不同蓝牙链接方式的判断依据 “确认连接”
	public static int connectMains = 0;
	protected static final String TAG = "SettingActivity";
	private static Button btn_search_devices, btn_scan_and_connect, btn_selfprint_test;
	public static boolean isConnected = false;// 蓝牙连接状态
	public static String devicesName = "未知设备";
	private static String devicesAddress;
	private ProgressDialog dialog;
	public static PrinterInstance myPrinter;
	private static BluetoothDevice mDevice;
	private static UsbDevice mUSBDevice;
	private Context mContext;
	private int printerId = 0;
	private int interfaceType = 0;
	private List<UsbDevice> deviceList;
	private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";
	private RadioGroup rg__select_paper_size;
	boolean isError;
	private BluetoothAdapter mBtAdapter;
	private TextView tv_device_name, tv_printer_address;
	private IntentFilter bluDisconnectFilter;
	private static boolean hasRegDisconnectReceiver = false;
	private ProgressDialog dialogH;
	private static final String ACTION_UPDATE_CHARACTERS_CANCLE = "canclUpdateCharacters";
	private IntentFilter cancleUpdateCharactersFilter;
	/**
	 * 与TSPL指令相关的控件
	 */
	private RadioGroup rgOrderSet;
	private LinearLayout llTSPL;
	// TODO 因为暂时不能自定义标签的宽高，所以暂时不设置这俩个属性
	// private EditText etLabelWidth;
	// private EditText etLabelHeight;
	private EditText etGapWidth;
	private EditText etGapOffset;
	private EditText etPrintSpeed;
	private EditText etPrintLevel;
	private EditText etLabelOffset;
	private EditText etPrintNumbers;
	private EditText etPrintLeftMargin;
	private EditText etPrintTopMargin;
	private Spinner spTear;
	private Spinner spOpenCashBox;
	private Spinner spIsBeep;
	private ArrayAdapter<CharSequence> tearData_Adapter;
	private ArrayAdapter<CharSequence> OpenCashBoxAdapter;
	private ArrayAdapter<CharSequence> IsBeepAdapter;
	private String tearSet = "撕纸";
	private String OpenCashSet = "禁止开钱箱";
	private String IsBeepSet = "禁止蜂鸣器";

	/**
	 * 设置TSPL指令打印机
	 *
	 * @param mPrinter
	 * @return 是否设置成功
	 */
	private boolean setPrinterTSPL(PrinterInstance mPrinter) {
		boolean isSettingSuccess = false;
		String gapWidth = etGapWidth.getText().toString();
		String gapOffset = etGapOffset.getText().toString();
		String printSpeed = etPrintSpeed.getText().toString();
		String printLevel = etPrintLevel.getText().toString();
		String printLabelOffset = etLabelOffset.getText().toString();
		String printNumbers = etPrintNumbers.getText().toString();
		String printLeftMargin = etPrintLeftMargin.getText().toString();
		String printTopMargin = etPrintTopMargin.getText().toString();
		for (int i = 0; i < 1; i++) {
			try {
				// 处于TSPL指令模式下
				if (llTSPL.getVisibility() == View.VISIBLE) {
					// 设置标签间的缝隙大小
					if (gapWidth == null || gapWidth.equals("") || gapOffset == null || gapOffset.equals("")) {
						Toast.makeText(mContext, "间隙宽度和间隙偏移量不能为空", 0).show();
						break;
					} else {
						int gapWidthTSPL = Integer.parseInt(gapWidth);
						int gapOffsetTSPL = Integer.parseInt(gapOffset);
						mPrinter.setGAPTSPL(gapWidthTSPL, gapOffsetTSPL);
						PrefUtils.setInt(mContext, "gapwidthtspl", gapWidthTSPL);
						PrefUtils.setInt(mContext, "gapoffsettspl", gapOffsetTSPL);
					}
					// 设置打印机打印速度
					if (printSpeed == null || printSpeed.equals("")) {
						Toast.makeText(mContext, "打印机打印速度不能设置为空", 0).show();
						break;
					} else {
						int printSpeedTSPL = Integer.parseInt(printSpeed);
						mPrinter.setPrinterTSPL(CommandTSPL.SPEED, printSpeedTSPL);
						PrefUtils.setInt(mContext, "printspeed", printSpeedTSPL);
					}
					// 设置打印机打印浓度
					if (printLevel == null || printLevel.equals("")) {
						Toast.makeText(mContext, "打印机打印浓度不能设置为空", 0).show();
						break;
					} else {
						int printLevelSpeedTSPL = Integer.parseInt(printLevel);
						mPrinter.setPrinterTSPL(CommandTSPL.DENSITY, printLevelSpeedTSPL);
						PrefUtils.setInt(mContext, "printlevel", printLevelSpeedTSPL);
					}
					// 设置标签偏移量
					if (printLabelOffset == null || printLabelOffset.equals("")) {
						Toast.makeText(mContext, "标签偏移量不能设置为空", 0).show();
						break;
					} else {
						// 标签偏移量（点数）
						int labelOffsetTSPL = Integer.parseInt(printLabelOffset) * 8;
						mPrinter.setPrinterTSPL(CommandTSPL.SHIFT, labelOffsetTSPL);
						PrefUtils.setInt(mContext, "labeloffsettspl", labelOffsetTSPL / 8);

					}
					// 设置打印机打印份数
					if (printNumbers == null || printNumbers.equals("")) {
						Toast.makeText(mContext, "打印机打印数量不能设置为空或者为负数", 0).show();
						break;
					} else {
						int printNumbersTSPL = Integer.parseInt(printNumbers);
						PrefUtils.setInt(mContext, "printnumbers", printNumbersTSPL);
					}

					// 设置打印内容初始位置
					if (printLeftMargin == null || printLeftMargin.equals("") || printTopMargin == null
							|| printLeftMargin.equals("")) {
						Toast.makeText(mContext, "标签偏移量不能为空或者为负数", 0).show();
						break;
					} else {
						int marginLeft = Integer.parseInt(printLeftMargin);
						PrefUtils.setInt(mContext, "leftmargin", marginLeft);
						int marginTop = Integer.parseInt(printTopMargin);
						PrefUtils.setInt(mContext, "topmargin", marginTop);

					}

					// // 撕纸设置
					// if (tearSet == null || tearSet.equals("")) {
					// break;
					// } else if (tearSet.equals("撕纸")) {
					// mPrinter.setPrinterTSPL(CommandTSPL.TEAR, 1);
					// mPrinter.setPrinterTSPL(CommandTSPL.PEEL, 0);
					// PrefUtils.setInt(mContext, "tearAndpeel", 0);
					// } else if (tearSet.equals("剥纸")) {
					// mPrinter.setPrinterTSPL(CommandTSPL.PEEL, 1);
					// PrefUtils.setInt(mContext, "tearAndpeel", 1);
					// } else if (tearSet.equals("关")) {
					// mPrinter.setPrinterTSPL(CommandTSPL.TEAR, 0);
					// mPrinter.setPrinterTSPL(CommandTSPL.PEEL, 0);
					// PrefUtils.setInt(mContext, "tearAndpeel", 2);
					//
					// }
					// 开关钱箱设置
					if (OpenCashSet == null || OpenCashSet.equals("")) {
						break;
					} else if (OpenCashSet.equals(R.string.String_judge1)) {
						PrefUtils.setInt(mContext, "isOpenCash", 0);
					} else if (OpenCashSet.equals(R.string.String_judge2)) {
						PrefUtils.setInt(mContext, "isOpenCash", 1);
					} else if (OpenCashSet.equals(R.string.String_judge3)) {
						PrefUtils.setInt(mContext, "isOpenCash", 2);
					}
					// 开关蜂鸣器设置
					if (IsBeepSet == null || IsBeepSet.equals("")) {
						break;
					} else if (IsBeepSet.equals(R.string.String_judge4)) {
						PrefUtils.setInt(mContext, "isBeep", 2);
					} else if (IsBeepSet.equals(R.string.String_judge5)) {
						PrefUtils.setInt(mContext, "isBeep", 1);
					} else if (IsBeepSet.equals(R.string.String_judge6)) {
						PrefUtils.setInt(mContext, "isBeep", 0);

					}

					isSettingSuccess = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (WriteException e) {
				Toast.makeText(mContext, "向打印机写入数据异常", 0).show();
				e.printStackTrace();
			} catch (PrinterPortNullException e) {
				Toast.makeText(mContext, "打印机为空异常", 0).show();
				e.printStackTrace();
			} catch (ParameterErrorException e) {
				Toast.makeText(mContext, "传入参数异常", 0).show();
				e.printStackTrace();
			} catch (Exception e) {
				Toast.makeText(mContext, "其他未知异常", 0).show();
				e.printStackTrace();
			}
		}

		return isSettingSuccess;
	}

	/**
	 * 显示扫描结果
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		init();
		Log.i(TAG, "" + SettingActivity.this);
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		Log.e("progressdialog", "onCreate()>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

	}

	// 用于接受连接状态消息的 Handler
	private Handler mHandler = new Handler() {
		@SuppressLint("ShowToast")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Connect.SUCCESS:
					isConnected = true;
					GlobalContants.ISCONNECTED = isConnected;
					GlobalContants.DEVICENAME = devicesName;
					if (interfaceType == 0) {
						PrefUtils.setString(mContext, GlobalContants.DEVICEADDRESS, devicesAddress);
						bluDisconnectFilter = new IntentFilter();
						bluDisconnectFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
						mContext.registerReceiver(myReceiver, bluDisconnectFilter);
						hasRegDisconnectReceiver = true;
					}
					// TOTO 暂时将TSPL指令设置参数的设置放在这
					if (setPrinterTSPL(myPrinter)) {
						if (interfaceType == 0) {
							Toast.makeText(mContext, R.string.settingactivitty_toast_bluetooth_set_tspl_successful, 0)
									.show();
						} else if (interfaceType == 1) {
							Toast.makeText(mContext, R.string.settingactivity_toast_usb_set_tspl_succefful, 0).show();
						}
					}
					break;
				case Connect.FAILED:
					isConnected = false;
					Toast.makeText(mContext, R.string.conn_failed, Toast.LENGTH_SHORT).show();
					Log.i(TAG, "连接失败!");
					break;
				case Connect.CLOSED:
					isConnected = false;
					GlobalContants.ISCONNECTED = isConnected;
					GlobalContants.DEVICENAME = devicesName;
					Toast.makeText(mContext, R.string.conn_closed, Toast.LENGTH_SHORT).show();
					Log.i(TAG, "连接关闭!");
					break;
				case Connect.NODEVICE:
					isConnected = false;
					Toast.makeText(mContext, R.string.conn_no, Toast.LENGTH_SHORT).show();
					break;
				case 0:
					Toast.makeText(mContext, "打印机通信正常!", 0).show();
					break;
				case -1:
					Toast.makeText(mContext, "打印机通信异常常，请检查蓝牙连接!", 0).show();
					vibrator();
					break;
				case -2:
					Toast.makeText(mContext, "打印机缺纸!", 0).show();
					vibrator();
					break;
				case -3:
					Toast.makeText(mContext, "打印机开盖!", 0).show();
					vibrator();
					break;
				// case 10:
				// if (setPrinterTSPL(myPrinter)) {
				// Toast.makeText(mContext, "蓝牙连接设置TSPL指令成功", 0).show();
				// }
				default:
					break;
			}

			updateButtonState(isConnected);

			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		}

	};
	int count = 0;

	@SuppressWarnings("static-access")
	public void vibrator() {
		count++;
		PrefUtils.setInt(mContext, "count3", count);
		Log.e(TAG, "" + count);
		// Vibrator vib = (Vibrator) SettingActivity.this
		// .getSystemService(Service.VIBRATOR_SERVICE);
		// vib.vibrate(1000);
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		MediaPlayer player = new MediaPlayer().create(mContext, R.raw.test);
		// MediaPlayer player2 = new MediaPlayer().create(mContext, R.raw.beep);

		player.start();
		// player2.start();
	}

	/**
	 * 初始化界面
	 */
	private void init() {
		mContext = SettingActivity.this;
		header = (LinearLayout) findViewById(R.id.ll_headerview_settingactivity);
		// 初始化标题
		initHeader();
		// 初始化下拉列表框
		// spinner_printer_type = (Spinner)
		// findViewById(R.id.spinner_printer_type);
		spinner_interface_type = (Spinner) findViewById(R.id.spinner_interface_type);
		// 初始化按钮的点击事件
		btn_search_devices = (Button) findViewById(R.id.btn_search_devices);
		btn_scan_and_connect = (Button) findViewById(R.id.btn_scan_and_connect);
		btn_selfprint_test = (Button) findViewById(R.id.btn_selfprint_test);
		// 设置按钮的监听事件
		btn_search_devices.setOnClickListener(this);
		btn_scan_and_connect.setOnClickListener(this);
		btn_selfprint_test.setOnClickListener(this);

		// 展示设备名和设备地址
		tv_device_name = (TextView) findViewById(R.id.tv_device_name);
		tv_printer_address = (TextView) findViewById(R.id.tv_printer_address);
		// // 适配器
		// arr_adapter = ArrayAdapter.createFromResource(this,
		// R.array.printertype, android.R.layout.simple_spinner_item);
		printType_adapter = ArrayAdapter.createFromResource(this, R.array.interface_type,
				android.R.layout.simple_spinner_item);

		// // 设置样式
		// arr_adapter
		// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		printType_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 加载适配器
		// spinner_printer_type.setAdapter(arr_adapter);
		spinner_interface_type.setAdapter(printType_adapter);
		// 下拉列表框的监听事件
		// spinner_printer_type.setOnItemSelectedListener(this);
		spinner_interface_type.setOnItemSelectedListener(this);

		rg__select_paper_size = (RadioGroup) findViewById(R.id.rg__select_paper_size);
		rg__select_paper_size.setOnCheckedChangeListener(this);

		// 初始化对话框
		dialog = new ProgressDialog(mContext);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setTitle(getString(R.string.connecting));
		dialog.setMessage(getString(R.string.please_wait));
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);

		getSaveState();
		updateButtonState(isConnected);
		// 初始化进度条对话框
		dialogH = new ProgressDialog(this);
		dialogH.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialogH.setCancelable(false);
		dialogH.setCanceledOnTouchOutside(false);

		PrinterConstants.paperWidth = PrefUtils.getInt(mContext, GlobalContants.PAPERWIDTH, 576);
		switch (PrinterConstants.paperWidth) {
			case 384:
				rg__select_paper_size.check(R.id.rb_58mm);
				break;
			case 576:
				rg__select_paper_size.check(R.id.rb_80mm);
				break;
			case 724:
				rg__select_paper_size.check(R.id.rb_100mm);
				break;
			default:
				rg__select_paper_size.check(R.id.rb_80mm);
				break;
		}

		/**
		 * 与TSPL指令相关的设置
		 */
		rgOrderSet = (RadioGroup) findViewById(R.id.rg_orderset);
		llTSPL = (LinearLayout) findViewById(R.id.ll_tspls);
		rgOrderSet.setOnCheckedChangeListener(this);
		// TODO 暂时不设置标签的宽和高
		// etLabelWidth = (EditText) findViewById(R.id.et_label_width);
		// etLabelHeight = (EditText) findViewById(R.id.et_label_height);
		etGapWidth = (EditText) findViewById(R.id.et_gap_width);
		etGapOffset = (EditText) findViewById(R.id.et_gap_offset);
		etPrintSpeed = (EditText) findViewById(R.id.et_print_speed);
		etPrintLevel = (EditText) findViewById(R.id.et_print_level);
		etLabelOffset = (EditText) findViewById(R.id.et_print_label_offset);
		etPrintNumbers = (EditText) findViewById(R.id.et_print_numbers);
		etPrintLeftMargin = (EditText) findViewById(R.id.et_left_margin);
		etPrintTopMargin = (EditText) findViewById(R.id.et_top_margin);
		spTear = (Spinner) findViewById(R.id.spinner_tear);
		tearData_Adapter = ArrayAdapter.createFromResource(SettingActivity.this, R.array.tear_data,
				android.R.layout.simple_spinner_item);
		tearData_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spTear.setAdapter(tearData_Adapter);
		spTear.setOnItemSelectedListener(this);
		spOpenCashBox = (Spinner) findViewById(R.id.spinner_cash);
		OpenCashBoxAdapter = ArrayAdapter.createFromResource(SettingActivity.this, R.array.cash_data,
				android.R.layout.simple_spinner_item);
		OpenCashBoxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		spOpenCashBox.setAdapter(OpenCashBoxAdapter);
		spOpenCashBox.setOnItemSelectedListener(this);
		spIsBeep = (Spinner) findViewById(R.id.spinner_beep);
		IsBeepAdapter = ArrayAdapter.createFromResource(SettingActivity.this, R.array.beep_data,
				android.R.layout.simple_spinner_item);
		IsBeepAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		spIsBeep.setAdapter(IsBeepAdapter);
		spIsBeep.setOnItemSelectedListener(this);
		// 根据是不是选中了TSPL指令来选择是不是显示TSPL指令标签
		if (DemoApplication.isSettingTSPL) {
			rgOrderSet.check(R.id.rb_order_tspl);
		} else {
			rgOrderSet.check(R.id.rb_order_cpcl);
		}
		// 初始化TSPL设置界面
		getSavedTSPL();
	}

	private void getSavedTSPL() {
		etPrintNumbers.setText(PrefUtils.getInt(mContext, "printnumbers", 1) + "");
		etPrintLeftMargin.setText(PrefUtils.getInt(mContext, "leftmargin", 0) + "");
		etPrintTopMargin.setText(PrefUtils.getInt(mContext, "topmargin", 0) + "");
		etPrintLevel.setText(PrefUtils.getInt(mContext, "printlevel", 7) + "");
		etPrintSpeed.setText(PrefUtils.getInt(mContext, "printspeed", 12) + "");
		etGapWidth.setText(PrefUtils.getInt(mContext, "gapwidthtspl", 0) + "");
		etGapOffset.setText(PrefUtils.getInt(mContext, "gapoffsettspl", 0) + "");
		etLabelOffset.setText(PrefUtils.getInt(mContext, "labeloffsettspl", 0) + "");
		spTear.setSelection(PrefUtils.getInt(mContext, "tearAndpeel", 0), true);
		spIsBeep.setSelection(PrefUtils.getInt(mContext, "isBeep", 0), true);
		spOpenCashBox.setSelection(PrefUtils.getInt(mContext, "isOpenCash", 0), true);

	}

	private void getSaveState() {
		isConnected = PrefUtils.getBoolean(SettingActivity.this, GlobalContants.CONNECTSTATE, false);
		printerId = PrefUtils.getInt(mContext, GlobalContants.PRINTERID, 0);
		interfaceType = PrefUtils.getInt(mContext, GlobalContants.INTERFACETYPE, 0);
		// spinner_printer_type.setSelection(printerId);
		spinner_interface_type.setSelection(interfaceType);
		Log.i(TAG, "isConnected:" + isConnected);
	}

	/**
	 * 初始化标题上的信息
	 */
	private void initHeader() {
		// setHeaderLeftText(header, getString(R.string.back),
		// new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// finish();
		//
		// }
		// });
		setHeaderLeftImage(header, new OnClickListener() {// 初始化了
			// headerConnecedState
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		setHeaderCenterText(header, getString(R.string.headview_setting));
	}

	@Override
	protected void onStart() {
		Log.e("progressdialog", "onStart()>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("progressdialog", "onResume()>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.e("progressdialog", "onRestart()>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e("progressdialog", "onPause()>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e("progressdialog", "onStop()>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

	}

	@Override
	protected void onDestroy() {
		Log.e("progressdialog", "onDestory()>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		super.onDestroy();

		if (interfaceType == 0 && hasRegDisconnectReceiver) {
			mContext.unregisterReceiver(myReceiver);
			hasRegDisconnectReceiver = false;
			// Log.i(TAG, "关闭了广播！");
		}

	}

	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {

		if (v == btn_search_devices) {
			Log.i(TAG, "isConnected:" + isConnected);
			if (!isConnected) {
				switch (interfaceType) {
					case 0:// kuetooth
						new AlertDialog.Builder(this).setTitle(R.string.str_message).setMessage(R.string.str_connlast)
								.setPositiveButton(R.string.yesconn, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										connectMains = 0;
										// 重新连接
										if (!(mBtAdapter == null)) {
											// 判断设备蓝牙功能是否打开
											if (!mBtAdapter.isEnabled()) {
												// 打开蓝牙功能
												Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
												startActivityForResult(enableIntent, ENABLE_BT);
											} else {
												// mDevice
												devicesAddress = PrefUtils.getString(mContext, GlobalContants.DEVICEADDRESS,
														"");

												if (devicesAddress == null || devicesAddress.length() <= 0) {
													Toast.makeText(SettingActivity.this, "您是第一次启动程序，请选择重新搜索连接！",
															Toast.LENGTH_SHORT).show();
												} else {
													connect2BlueToothdevice();

												}
											}
										}

									}
								}).setNegativeButton(R.string.str_resel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								connectMains = 1;
								if (!(mBtAdapter == null)) {
									// 判断设备蓝牙功能是否打开
									if (!mBtAdapter.isEnabled()) {
										// 打开蓝牙功能
										Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
										startActivityForResult(enableIntent, SettingActivity.ENABLE_BT);
									} else {
										Intent intent = new Intent(mContext, BluetoothDeviceList.class);
										startActivityForResult(intent, CONNECT_DEVICE);

									}

								}

							}

						}).show();

						break;
					case 1:// USB

						new AlertDialog.Builder(this).setTitle(R.string.str_message).setMessage(R.string.str_connlast)
								.setPositiveButton(R.string.yesconn, new DialogInterface.OnClickListener() {
									@SuppressLint("InlinedApi")
									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

										usbAutoConn(manager);
									}
								}).setNegativeButton(R.string.str_resel, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Log.i("yxz", "点击监听事件+++++++++++++++++++++++++++++++++++++++++++" + isConnected);

								Intent intent = new Intent(mContext, UsbDeviceList.class);
								startActivityForResult(intent, CONNECT_DEVICE);

							}

						}).show();
						break;
					case 2:// wifi
						Intent intent = new Intent(mContext, IpEditActivity.class);
						intent.putExtra(GlobalContants.WIFINAME, getWiFiName());
						startActivityForResult(intent, CONNECT_DEVICE);
						break;
					case 3:// serial port
						Intent intentSerial = new Intent(mContext, SerialsDeviceList.class);
						startActivityForResult(intentSerial, CONNECT_DEVICE);
						break;
					default:
						break;
				}
			} else {
				if (myPrinter != null) {
					myPrinter.closeConnection();
					myPrinter = null;
					Log.i(TAG, "已经断开");

					if (interfaceType == 0 && hasRegDisconnectReceiver) {
						mContext.unregisterReceiver(myReceiver);
						hasRegDisconnectReceiver = false;
						// Log.i(TAG, "关闭了广播！");
					}
				}
			}

		}

		if (v == btn_scan_and_connect) {
			connectMains = 2;
			if (!isConnected) {
				// 判断设备蓝牙功能是否打开
				if (!mBtAdapter.isEnabled()) {
					// 打开蓝牙功能
					Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableIntent, ENABLE_BT);
				} else {
					Intent intent = new Intent();
					intent.setClass(mContext, MipcaActivityCapture.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
				}

			} else {
				Toast.makeText(mContext, "当前已经连接到" + devicesName, 0).show();
			}

			// devicesAddress = XTUtils.formatTheString("00：02：5b：1a：8a：19");
			// Log.e("yxz", devicesAddress);
			// if (BluetoothAdapter.checkBluetoothAddress(devicesAddress)) {
			// connect2BlueToothdevice();
			//
			// } else {
			// Toast.makeText(mContext, "蓝牙mac:" + devicesAddress + "不合法", 0)
			// .show();
			// }

			// String x = XTUtils.formatTheString("00:02:5b:1a:8a:1a");
			// Log.e("yxz", x);

		}

		if (v == btn_selfprint_test) {
			//WIFI 由于是网络操作，需要放到线程中，其他通信方式可以放在线程中也可以不必如此
			if (isConnected) {
				XTUtils.printTest(getResources(), myPrinter);
//				PrintLottery.printLottery(mContext, myPrinter);
//				new Thread(new  Runnable() {
//					public void run() {
////						new CodePagePrinter(myPrinter).printTextInWCP1258("Trường hợp kiểm tra in tiếng Việt\n");
//					}
//				}).start();
			} else {

				Toast.makeText(mContext, getString(R.string.no_connected), 0).show();
			}
		}
	}

	public void vibrators() {

		Vibrator vib = (Vibrator) SettingActivity.this.getSystemService(Service.VIBRATOR_SERVICE);
		vib.vibrate(1000);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void doPrint() {
		try {
			int left = PrefUtils.getInt(this, "leftmargin", 0);
			int top = PrefUtils.getInt(this, "topmargin", 0);
			myPrinter.pageSetupTSPL(PrinterConstants.SIZE_58mm, 56 * 8, 45 * 8);
			// 清除缓存区内容
			myPrinter.printText("CLS\r\n");
			// 设置标签的参考坐标原点
			if (left == 0 || top == 0) {
				// 不做设置，默认
				myPrinter.sendStrToPrinterTSPL("REFERENCE " + 20 * 8 + "," + 20 * 8 + "\r\n");

			} else {
				myPrinter.sendStrToPrinterTSPL("REFERENCE " + 20 * 8 + "," + 20 * 8 + "\r\n");
			}
			myPrinter.drawTextTSPL(0, 0, true, 1, 1, PrinterConstants.PRotate.Rotate_0, "中文");
			myPrinter.drawTextTSPL(0, 0, 35 * 8, 7 * 8, PrinterConstants.PAlign.START, PrinterConstants.PAlign.START,
					true, 1, 1, null, "HanBu");
			myPrinter.printTSPL(1, 1);
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (PrinterPortNullException e) {
			e.printStackTrace();
		} catch (ParameterErrorException e) {
			e.printStackTrace();
		}
	}

	public void setMarginAfterPrint(PrinterInstance mPrinter, double magins) {
		DecimalFormat df = new DecimalFormat("######0");
		double dotHeight = 0.37;
		double numbers = magins / dotHeight;
		Log.i("yxz", "总共需要走多少点" + numbers);
		String str = df.format(numbers);
		int nums = Integer.parseInt(str);
		Log.i("yxz", "四舍五入之后的点数" + nums);
		byte[] datas = new byte[2];
		datas[0] = 0x15;
		datas[1] = (byte) nums;
		myPrinter.sendBytesData(datas);
	}

	public static String jsonToStringFromAssetFolder(String fileName, Context context) throws IOException {
		AssetManager manager = context.getAssets();
		InputStream file = manager.open(fileName);
		byte[] data = new byte[file.available()];
		file.read(data);
		file.close();
		return new String(data, "gbk");

	}

	// private Runnable runnable = new Runnable() {
	// int count = 0;
	// @Override
	// public void run() {
	// int ret = myPrinter.getPrinterStatus();
	// mHandler.obtainMessage(ret).sendToTarget();
	// Log.i("fdh", "第" + (++count) + "次检测状态 " + ret);
	// mHandler.postDelayed(this, 3000);
	// }
	// };
	// private Timer timer = new Timer();
	// private TimerTask task = new TimerTask() {
	// int count = 0;
	//
	// @Override
	// public void run() {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// int ret = myPrinter.getCurrentStatus();
	// mHandler.obtainMessage(ret).sendToTarget();
	// Log.i("fdh", "第" + (++count) + "次检测状态 " + ret);
	// mHandler.postDelayed(this, 3000);
	//
	// }
	// }).start();
	// }
	// };

	// 安卓3.1以后才有权限操作USB
	@SuppressLint("ShowToast")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == CONNECT_DEVICE) {// 连接设备
			if (interfaceType == 0) {

				devicesAddress = data.getExtras().getString(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
				devicesName = data.getExtras().getString(BluetoothDeviceList.EXTRA_DEVICE_NAME);
				connect2BlueToothdevice();

			} else if (interfaceType == 1)// usb
			{
				mUSBDevice = data.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				myPrinter = PrinterInstance.getPrinterInstance(mContext, mUSBDevice, mHandler);
				devicesName = "USB device";
				UsbManager mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
				if (mUsbManager.hasPermission(mUSBDevice)) {
					myPrinter.openConnection();
				} else {
					// 没有权限询问用户是否授予权限
					PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
							new Intent(ACTION_USB_PERMISSION), 0);
					IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
					filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
					filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
					mContext.registerReceiver(mUsbReceiver, filter);
					mUsbManager.requestPermission(mUSBDevice, pendingIntent); // 该代码执行后，系统弹出一个对话框
				}

			} else if (interfaceType == 2)// wifi
			{
				devicesName = "Net device";
				devicesAddress = data.getStringExtra("ip_address");
				myPrinter = PrinterInstance.getPrinterInstance(devicesAddress, 9100, mHandler);
				new Thread(new Runnable() {
					@Override
					public void run() {
						myPrinter.openConnection();
					}
				}).start();
			} else if (interfaceType == 3) {// 串口

				int baudrate = 9600;
				String path = data.getStringExtra("path");
				devicesName = "Serial device";
				devicesAddress = path;
				String com_baudrate = data.getExtras().getString("baudrate");
				if (com_baudrate == null || com_baudrate.length() == 0) {
					baudrate = 9600;
				}
				baudrate = Integer.parseInt(com_baudrate);
				XLog.i(TAG, "baudrate:"+baudrate);
				myPrinter = PrinterInstance.getPrinterInstance(new File(path), baudrate, 0, mHandler);
				myPrinter.openConnection();
				// myPrinter
				// .printText("测试串口连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭测试蓝牙连续连接--打印--关闭/n");
				// myPrinter.closeConnection();
				Log.i(TAG, "波特率:" + baudrate + "路径:" + path);
			}

		}
		if (requestCode == SCANNIN_GREQUEST_CODE) {

			// 校验扫描到的mac是否合法
			devicesAddress = data.getExtras().getString(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
			Log.i(TAG, "devicesAddress:" + devicesAddress);
			devicesAddress = XTUtils.formatTheString(devicesAddress);
			if (BluetoothAdapter.checkBluetoothAddress(devicesAddress)) {
				connect2BlueToothdevice();
			} else {
				Log.e("yxz", devicesAddress);
				Toast.makeText(mContext, "蓝牙mac:" + devicesAddress + "不合法", 0).show();
			}
		}
		if (requestCode == ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				if (!mBtAdapter.isEnabled()) {
					// 打开蓝牙功能
					Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableIntent, SettingActivity.ENABLE_BT);
				} else {
					switch (connectMains) {
						case 0:
							// mDevice
							devicesAddress = PrefUtils.getString(mContext, GlobalContants.DEVICEADDRESS, "");

							if (devicesAddress == null || devicesAddress.length() <= 0) {
								Toast.makeText(SettingActivity.this, "您是第一次启动程序，请选择重新搜索连接！", Toast.LENGTH_SHORT).show();
							} else {
								connect2BlueToothdevice();

							}
							break;
						case 1:
							Intent intent = new Intent(mContext, BluetoothDeviceList.class);
							startActivityForResult(intent, CONNECT_DEVICE);
							break;
						case 2:
							Intent scanIntent = new Intent();
							scanIntent.setClass(mContext, MipcaActivityCapture.class);
							scanIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivityForResult(scanIntent, SCANNIN_GREQUEST_CODE);
							break;
					}
				}
			} else {
				Toast.makeText(SettingActivity.this, R.string.bt_not_enabled, 0).show();
			}
		}
	}

	private void connect2BlueToothdevice() {
		// dialog.show();
		mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(devicesAddress);
		devicesName = mDevice.getName();
		myPrinter = PrinterInstance.getPrinterInstance(mDevice, mHandler);
		if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {// 未绑定
			// IntentFilter boundFilter = new IntentFilter();
			// boundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			// mContext.registerReceiver(boundDeviceReceiver, boundFilter);
			PairOrConnect(true);
		} else {
			PairOrConnect(false);
		}
	}

	private void PairOrConnect(boolean pair) {
		if (pair) {
			IntentFilter boundFilter = new IntentFilter();
			boundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			mContext.registerReceiver(boundDeviceReceiver, boundFilter);
			boolean success = false;
			try {
				// // 自动设置pin值
				// Method autoBondMethod =
				// BluetoothDevice.class.getMethod("setPin", new Class[] {
				// byte[].class });
				// boolean result = (Boolean) autoBondMethod.invoke(mDevice, new
				// Object[] { "1234".getBytes() });
				// Log.i(TAG, "setPin is success? : " + result);

				// 开始配对 这段代码打开输入配对密码的对话框
				Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
				success = (Boolean) createBondMethod.invoke(mDevice);
				// // 取消用户输入
				// Method cancelInputMethod =
				// BluetoothDevice.class.getMethod("cancelPairingUserInput");
				// boolean cancleResult = (Boolean)
				// cancelInputMethod.invoke(mDevice);
				// Log.i(TAG, "cancle is success? : " + cancleResult);

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			Log.i(TAG, "createBond is success? : " + success);
		} else {
			new connectThread().start();

		}
	}

	private void updateButtonState(boolean isConnected) {
		if (isConnected) {
			headerConnecedState.setText(R.string.on_line);
			btn_search_devices.setText(R.string.disconnect);
			setTitleState(mContext.getResources().getString(R.string.on_line));
			Log.i("fdh", getString(R.string.printerName).split(":")[0]);
			Log.i("fdh", getString(R.string.printerAddress).split(":")[0]);
			tv_device_name.setText(getString(R.string.printerName).split(":")[0] + ": " + devicesName);
			tv_printer_address.setText(getString(R.string.printerAddress).split(":")[0] + ": " + devicesAddress);
		} else {
			btn_search_devices.setText(R.string.connect);
			headerConnecedState.setText(R.string.off_line);
			setTitleState(mContext.getResources().getString(R.string.off_line));
			tv_device_name.setText(getString(R.string.printerName));
			tv_printer_address.setText(getString(R.string.printerAddress));
			// mHandler.removeCallbacks(runnable);
			// if (isFirst) {
			//
			// } else {
			// timer.cancel();
			// }
			// Log.i(TAG, "定时器取消了");

		}

		PrefUtils.setBoolean(SettingActivity.this, GlobalContants.CONNECTSTATE, isConnected);

	}

	private BroadcastReceiver boundDeviceReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (!mDevice.equals(device)) {
					return;
				}
				switch (device.getBondState()) {
					case BluetoothDevice.BOND_BONDING:
						Log.i(TAG, "bounding......");
						break;
					case BluetoothDevice.BOND_BONDED:
						Log.i(TAG, "bound success");
						// if bound success, auto init BluetoothPrinter. open
						// connect.
						mContext.unregisterReceiver(boundDeviceReceiver);
						dialog.show();
						// 配对完成开始连接
						if (myPrinter != null) {
							new connectThread().start();
						}
						break;
					case BluetoothDevice.BOND_NONE:
						Log.i(TAG, "执行顺序----4");

						mContext.unregisterReceiver(boundDeviceReceiver);
						Log.i(TAG, "bound cancel");
						break;
					default:
						break;
				}

			}
		}
	};

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.w(TAG, "receiver action: " + action);

			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					mContext.unregisterReceiver(mUsbReceiver);
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
							&& mUSBDevice.equals(device)) {
						myPrinter.openConnection();
					} else {
						mHandler.obtainMessage(Connect.FAILED).sendToTarget();
						Log.e(TAG, "permission denied for device " + device);
					}
				}
			}
		}
	};

	private class connectThread extends Thread {
		@Override
		public void run() {
			if (myPrinter != null) {
				isConnected = myPrinter.openConnection();
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		// if (parent == spinner_printer_type) {
		// PrefUtils.setInt(mContext, GlobalContants.PRINTERID, position);
		// String printerName = getResources().getStringArray(
		// R.array.printertype)[position];
		// if (printerName.contains("T10") || printerName.contains("POS58")
		// || printerName.contains("T7")) {
		// rg__select_paper_size.check(R.id.rb_58mm);
		// } else if (printerName.contains("L31")
		// || printerName.contains("T9")
		// || printerName.contains("POS885")
		// || printerName.contains("EU80")) {
		// rg__select_paper_size.check(R.id.rb_80mm);
		// } else if (printerName.contains("L51")) {
		// rg__select_paper_size.check(R.id.rb_100mm);
		// }
		// } else if (parent == spinner_interface_type) {
		// }

		if (parent == spinner_interface_type) {
			PrefUtils.setInt(mContext, GlobalContants.INTERFACETYPE, position);
			interfaceType = position;
			Log.i(TAG, "position:" + position);
		}
		if (parent == spTear) {
			tearSet = spTear.getSelectedItem().toString();
		}
		if (parent == spOpenCashBox) {
			OpenCashSet = spOpenCashBox.getSelectedItem().toString();
		}
		if (parent == spIsBeep) {
			IsBeepSet = spIsBeep.getSelectedItem().toString();
		}
	}

	// @Override
	// protected void onNewIntent(Intent intent) {
	// super.onNewIntent(intent);
	// }
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group == rg__select_paper_size) {
			switch (checkedId) {
				case R.id.rb_58mm:
					PrinterConstants.paperWidth = 384;
					break;
				case R.id.rb_80mm:
					PrinterConstants.paperWidth = 576;
					break;
				case R.id.rb_100mm:
					PrinterConstants.paperWidth = 724;
					break;
				default:
					PrinterConstants.paperWidth = 576;
					break;
			}
			PrefUtils.setInt(mContext, GlobalContants.PAPERWIDTH, PrinterConstants.paperWidth);
		} else if (group == rgOrderSet) {
			if (checkedId == R.id.rb_order_cpcl) {
				llTSPL.setVisibility(View.GONE);
				DemoApplication.isSettingTSPL = false;
			} else if (checkedId == R.id.rb_order_tspl) {
				llTSPL.setVisibility(View.VISIBLE);
				DemoApplication.isSettingTSPL = true;
			}
		}

	}

	/*
	 * @Override public void onConfigurationChanged(Configuration newConfig) {
	 * super.onConfigurationChanged(newConfig); if
	 * (this.getResources().getConfiguration().orientation ==
	 * Configuration.ORIENTATION_LANDSCAPE) { // land } else if
	 * (this.getResources().getConfiguration().orientation ==
	 * Configuration.ORIENTATION_PORTRAIT) { // port } }
	 */

	public void usbAutoConn(UsbManager manager) {

		doDiscovery(manager);

		if (!deviceList.isEmpty()) {
			mUSBDevice = deviceList.get(0);
		}
		if (mUSBDevice != null) {
			PrinterInstance.getPrinterInstance(mContext, mUSBDevice, mHandler).openConnection();
		} else {
			mHandler.obtainMessage(Connect.FAILED).sendToTarget();
			myPrinter.closeConnection();
		}
	}

	@SuppressLint("NewApi")
	private void doDiscovery(UsbManager manager) {
		HashMap<String, UsbDevice> devices = manager.getDeviceList();
		deviceList = new ArrayList<UsbDevice>();
		for (UsbDevice device : devices.values()) {
			if (USBPort.isUsbPrinter(device)) {
				deviceList.add(device);
			}
		}

	}

	private String getWiFiName() {
		String wifiName = null;
		WifiManager mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!mWifi.isWifiEnabled()) {
			mWifi.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = mWifi.getConnectionInfo();
		wifiName = wifiInfo.getSSID();
		Log.i("yxz", "wifiName" + wifiName);
		wifiName = wifiName.replaceAll("\"", "");
		return wifiName;

	}

	public BroadcastReceiver myReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {

				if (device != null && myPrinter != null && isConnected && device.equals(mDevice)) {
					myPrinter.closeConnection();
					mHandler.obtainMessage(Connect.CLOSED).sendToTarget();
				}
			}

		}
	};

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// Toast.makeText(mContext, "测试onKeyDown", 0).show();
	// }
	// return false;
	// }
}
