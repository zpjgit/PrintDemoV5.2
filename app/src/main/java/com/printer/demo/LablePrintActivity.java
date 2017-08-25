package com.printer.demo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.printer.demo.utils.PrintLabel100;
import com.printer.demo.utils.PrintLabel58;
import com.printer.demo.utils.PrintLabel80;
import com.printer.demo.utils.PrintLabelDrink;
import com.printer.demo.utils.PrintLabelExpress;
import com.printer.demo.utils.PrintLabelFruit;
import com.printer.demo.utils.PrintLabelMaterial;
import com.printer.demo.utils.PrintLabelStorage;
import com.printer.demo.utils.PrintLablel;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.exception.ParameterErrorException;
import com.printer.sdk.exception.PrinterPortNullException;
import com.printer.sdk.exception.WriteException;

public class LablePrintActivity extends BaseActivity implements OnClickListener {
	private Button btnPrintLable100mm;
	private Button btnPrintLable80mm;
	private Button btnPrintLable50mm;
	private Button btnPrintLable;
	private LinearLayout header;
	public static final int PRINT_START = 0x1557; // 15:57
	public static final int PRINT_DONE = 0x1558;
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);

	protected Handler printerHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(android.os.Message msg) {

			return false;
		}
	});

	PrinterInstance mPritner;

	// 与TSPL指令有关的控件
	private Button btnFruitLabel;
	private Button btnDrinkLabel;
	private Button btnMaterialLabel;
	private Button btnExpressLabel;
	private Button btnStorageLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lable_print);

		header = (LinearLayout) findViewById(R.id.ll_headerview_LablePrintactivity);
		btnPrintLable100mm = (Button) findViewById(R.id.btn_100mm);
		btnPrintLable80mm = (Button) findViewById(R.id.btn_80mm);
		btnPrintLable50mm = (Button) findViewById(R.id.btn_58mm);
		btnPrintLable = (Button) findViewById(R.id.btn_lable);
		btnPrintLable100mm.setOnClickListener(this);
		btnPrintLable80mm.setOnClickListener(this);
		btnPrintLable50mm.setOnClickListener(this);
		btnPrintLable.setOnClickListener(this);
		// TSPL指令相关的控件初始化
		btnFruitLabel = (Button) findViewById(R.id.btn_fruit_tspl);
		btnDrinkLabel = (Button) findViewById(R.id.btn_drink_tspl);
		btnMaterialLabel = (Button) findViewById(R.id.btn_material_tspl);
		btnExpressLabel = (Button) findViewById(R.id.btn_express_tspl);
		btnStorageLabel = (Button) findViewById(R.id.btn_storage_tspl);

		btnFruitLabel.setOnClickListener(this);
		btnDrinkLabel.setOnClickListener(this);
		btnMaterialLabel.setOnClickListener(this);
		btnExpressLabel.setOnClickListener(this);
		btnStorageLabel.setOnClickListener(this);
		mPritner = PrinterInstance.mPrinter;
		initHeader();
		// 判断设置界面是不是选中TSPL指令集的选择框,从而选择界面上需要显示的标签
		if (DemoApplication.isSettingTSPL) {
			btnPrintLable100mm.setVisibility(View.GONE);
			btnPrintLable80mm.setVisibility(View.GONE);
			btnPrintLable50mm.setVisibility(View.GONE);
			btnPrintLable.setVisibility(View.GONE);

			btnFruitLabel.setVisibility(View.VISIBLE);
			btnDrinkLabel.setVisibility(View.VISIBLE);
			btnMaterialLabel.setVisibility(View.VISIBLE);
			btnExpressLabel.setVisibility(View.VISIBLE);
			btnStorageLabel.setVisibility(View.VISIBLE);
		} else {
			btnPrintLable100mm.setVisibility(View.VISIBLE);
			btnPrintLable80mm.setVisibility(View.VISIBLE);
			btnPrintLable50mm.setVisibility(View.VISIBLE);
			btnPrintLable.setVisibility(View.VISIBLE);

			btnFruitLabel.setVisibility(View.GONE);
			btnDrinkLabel.setVisibility(View.GONE);
			btnMaterialLabel.setVisibility(View.GONE);
			btnExpressLabel.setVisibility(View.GONE);
			btnStorageLabel.setVisibility(View.GONE);
		}
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
		setHeaderLeftImage(header, new OnClickListener() {// 初始化了
			// headerConnecedState
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		headerConnecedState.setText(getTitleState());
		setHeaderCenterText(header, getString(R.string.headview_LablePrint));
	}

	@Override
	public void onClick(View v) {
		if (mPritner == null && !SettingActivity.isConnected) {
			Toast.makeText(LablePrintActivity.this, getString(R.string.no_connected), 0).show();
			return;
		}
		switch (v.getId()) {
		case R.id.btn_100mm:
			new PrintLabel100().doPrint(mPritner);

			break;
		case R.id.btn_80mm:
			new PrintLabel80().doPrint(mPritner);
			break;
		case R.id.btn_58mm:
			new PrintLabel58().doPrint(mPritner);
			break;
		case R.id.btn_lable:
			// new CanvasUtils().printCustomImage3(getResources(),
			// PrinterInstance.mPrinter, false, false);
			new PrintLablel().doPrint(mPritner);
			break;
		case R.id.btn_fruit_tspl:
			// TODO 开发蔬菜水果标签打印模版
			new PrintLabelFruit().doPrintTSPL(mPritner, getApplicationContext());
			break;
		case R.id.btn_drink_tspl:
			// TODO 开发奶茶标签打印模版
			new PrintLabelDrink().doPrintTSPL(mPritner, getApplicationContext());
			break;
		case R.id.btn_material_tspl:
			// TODO 开发物流标签打印模版
			new PrintLabelMaterial().doPrintTSPL(mPritner, getApplicationContext());
			break;
		case R.id.btn_express_tspl:
			// TODO 开发快递单号标签打印模版
			new PrintLabelExpress().doPrintTSPL(mPritner, getApplicationContext());
			break;
		case R.id.btn_storage_tspl:
			// TODO 开发仓储行业打印模版
			new PrintLabelStorage().doPrintTSPL(mPritner, getApplicationContext());
			break;
		}

	}

	private String getFilePath() {

		String PATH_LOGCAT = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
			PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyPicture";
			Log.i("yxz", "sdka ");
		} else {// 如果SD卡不存在，就保存到本应用的目录下
			PATH_LOGCAT = this.getFilesDir().getAbsolutePath() + File.separator + "MyPicture";
			Log.i("yxz", "neicun");
		}
		File dir = new File(PATH_LOGCAT);
		if (!dir.exists()) {
			dir.mkdir(); // 创建文件夹
		}
		String remp_dir = PATH_LOGCAT + File.separator + "tmpPhoto.jpg";
		Log.i("yxz", "remp_dir:" + remp_dir);
		return remp_dir;
	}

	private void print() {
		if (PrinterInstance.mPrinter == null) {
			Toast.makeText(LablePrintActivity.this, "请连接打印机", Toast.LENGTH_SHORT).show();
		} else {
			new PrintThread().start();
		}
	}

	private class PrintThread extends Thread {
		String codeStr, destinationStr, countStr, weightStr, volumeStr, dispatchModeStr, businessModeStr, packModeStr,
				receiverAddress;

		public PrintThread() {
			codeStr = "DF1234567890";
			destinationStr = "西安长线";
			countStr = "1";
			weightStr = "2";
			volumeStr = "1";
			dispatchModeStr = "派送";
			businessModeStr = "定时达";
			packModeStr = "袋装";
			receiverAddress = "陕西省西安市临潼区秦始皇陵兵马俑一号坑五排三列俑";
		}

		@Override
		public void run() {
			Looper.prepare();
			try {
				printerHandler.obtainMessage(PRINT_START).sendToTarget();
				printing(codeStr, destinationStr, countStr, weightStr, volumeStr, dispatchModeStr, businessModeStr,
						packModeStr, receiverAddress);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				printerHandler.obtainMessage(PRINT_DONE).sendToTarget();
			}
			Looper.loop();
		}
	}

	public void printing(String codeStr, String destinationStr, String countStr, String weightStr, String volumeStr,
			String dispatchModeStr, String businessModeStr, String packModeStr, String receiverAddress)
			throws Exception {
		String centerName = "西安分拔中心"; // 目的地分拨
		String centerCode = "0292001"; // 目的地分拨编号

		String userSite = "测试二级网点" + "(" + dateFormat.format(new Date()) + ")"; // 出发网点

		int count = Integer.parseInt(countStr);
		for (int c = 1; c <= count; c++) {
			// 子单号
			String serialNum = String.format("%03d", c);
			String subCodeStr = codeStr + serialNum + centerCode;
			String serialStr = "第" + c + "件";
			//
			// LablePrintUtils.doPrint(this, mPritner, codeStr,
			// businessModeStr, centerName, destinationStr, userSite,
			// receiverAddress, countStr, serialStr, dispatchModeStr,
			// packModeStr, subCodeStr);
		}

		// onPrintSucceed();
	}
}
