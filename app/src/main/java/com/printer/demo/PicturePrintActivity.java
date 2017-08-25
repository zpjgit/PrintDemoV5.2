package com.printer.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.printer.demo.global.GlobalContants;
import com.printer.demo.utils.CanvasUtils;
import com.printer.demo.utils.PictureUtils;
import com.printer.sdk.PrinterConstants.Command;
import com.printer.sdk.PrinterConstants.PAlign;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.monochrome.BitmapConvertor;
import com.printer.sdk.utils.Utils;

/**
 * 
 * @Description 处理图片打印相关
 */
public class PicturePrintActivity extends BaseActivity implements OnClickListener, OnItemSelectedListener {
	private Context mContext;
	private boolean is58mm = false;
	private boolean isStylus = false;
	private Button btn_photo_print, btn_select_photo, btn_canvas_print, btn_monochrome_print;
	private ImageView iv_Original_picture, iv_monochrome_picture;
	private LinearLayout header;
	private final static int IMAGE_CAPTURE_FROM_CAMERA = 1;
	private final static int IMAGE_CAPTURE_FROM_GALLERY = 2;
	private static final String TAG = "PicturePrintActivity";
	private BitmapConvertor convertor;
	private WindowManager wm;
	private ProgressDialog mPd;
	private String remp_dir = null;
	private static PrinterInstance mPrinter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_print_picture);
		init();
		Log.e(TAG, "onCreate");
		// if (savedInstanceState != null) {
		// remp_dir = savedInstanceState.getString("remp_dir");
		// // System.out.println("onCreate: temp = " + remp_dir);
		// Log.e(TAG, "remp_dir:"+remp_dir);
		// }
		mPrinter = PrinterInstance.mPrinter;
		remp_dir = getFilePath();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");
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

		header = (LinearLayout) findViewById(R.id.ll_header_picture);
		btn_photo_print = (Button) findViewById(R.id.btn_photo_print);
		btn_photo_print.setOnClickListener(this);
		btn_monochrome_print = (Button) findViewById(R.id.btn_monochrome_print);
		btn_monochrome_print.setOnClickListener(this);
		btn_select_photo = (Button) findViewById(R.id.btn_select_photo);
		btn_select_photo.setOnClickListener(this);
		btn_canvas_print = (Button) findViewById(R.id.btn_canvas_print);
		btn_canvas_print.setOnClickListener(this);
		// iv_Original_picture = (ImageView)
		// findViewById(R.id.iv_Original_picture);
		iv_monochrome_picture = (ImageView) findViewById(R.id.iv_monochrome_picture);
		initHeader(header);
		convertor = new BitmapConvertor(this);
		wm = this.getWindowManager();
	}

	/**
	 * 初始化标题上的信息
	 */
	private void initHeader(LinearLayout header) {
		setHeaderLeftText(header, getString(R.string.back), new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});
		headerConnecedState.setText(getTitleState());
		setHeaderLeftImage(header, new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		setHeaderCenterText(header, getString(R.string.headview_PicturePrint));

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View view) {

		if (mPrinter == null || !SettingActivity.isConnected) {
			Toast.makeText(mContext, getString(R.string.no_connected), 0).show();
			return;
		}
		if (view == btn_monochrome_print) {

			mPrinter.printText("打印单色位图演示：");
			mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1);
			// Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),
			// R.drawable.my_monochrome_image);
			Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.goodwork);
			mPrinter.printImage(bitmap1, PAlign.NONE, 0, false);

		} else if (view == btn_photo_print) {
			remp_dir = getFilePath();
			// 拍照我们用Action为MediaStroe.ACTION_IMAGE_CAPTURE,
			// 有些人使用其他的Action但我发现在有些机子中会出问题，所以优先选择这个
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(remp_dir)));
			startActivityForResult(intent, IMAGE_CAPTURE_FROM_CAMERA);

		} else if (view == btn_select_photo) {

			// 拍照我们用Action为Intent.ACTION_GET_CONTENT,
			// 有些人使用其他的Action但我发现在有些机子中会出问题，所以优先选择这个
			Intent intent2 = new Intent();
			intent2.setType("image/*");
			intent2.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent2, IMAGE_CAPTURE_FROM_GALLERY);

		} else if (view == btn_canvas_print) {
			// for (int i = 0; i < 10; i++) {
			// SettingActivity.myPrinter.printText("test\n");
			// }
			if (mPrinter == null) {
				Log.i(TAG, "mPrinter为空");
			} else {
				 new CanvasUtils().printCustomImage2(mContext.getResources(),
				 PrinterInstance.mPrinter, isStylus, is58mm);
			}
		}

	}

	public static byte[] bitmap2PrinterBytes_stylus2(Bitmap bitmap, int multiple) {
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();
		Log.i("yxz", "图片宽度是：" + width);
		Log.i("yxz", "图片高度是：" + height);
		byte[] imgBuf;
		byte[] tmpBuf;// 存放临时的1b，2a指令，判断若全是0，就发送1b，4a，24走24点行
		// 24点行数据 发送一个 1b 2a m nL nH指令
		imgBuf = new byte[((height / 8) + 1) * width + (height / 24 + 1) * 7];
		Log.i("yxz", "imgBuf的长度：" + imgBuf.length);
		// 每24点行需要的字节数
		tmpBuf = new byte[width * 3 + 7];
		Log.i("yxz", "tmpBuf的长度：" + tmpBuf.length);

		int[] p = new int[8];
		int s = 0;
		int t = 0;

		// // 对图像的每个像素进行格式转换
		// // 24点一次发24点数据
		// for (int y = 0; y < (height / 8) + 1; y = y + 3) {
		// // 初始化
		// t = 0;
		// // 为图像的每三行字节流添加打印机协议字节1b，2a
		// tmpBuf[t] = (byte) 0x1b;// 1
		// tmpBuf[++t] = (byte) 0x2a;// 2
		//
		// // 放大倍数，32为单倍放大，只倍宽，33为图片原始大小
		// tmpBuf[++t] = (byte) multiple;// 3
		// // nl,nH确定行向打印点数，总的点数为nL+nH*256；
		//
		// // nL
		// int nL = width % 256;
		// tmpBuf[++t] = (byte) (nL * 3);// 4
		//
		// // nH
		// int nH = width / 256;
		// if (nH > 1) {
		// nH = 1;
		// }
		// tmpBuf[++t] = (byte) (nH * 3);// 5
		// // tmpBuf[++t] = (byte) ((width % maxWidth) * 3);
		// // tmpBuf[++t] = (byte) ((width / maxWidth > 0 ? 1 : 0) * 3);
		// // 发送每次 1b 2a m nL nH d1......dh的数据 d1......dh
		// /**
		// * 发送数据的格式 x= 0 1 2....
		// *
		// * y=0 d1 d4 d7...dh-2
		// *
		// * 1 d2 d5 d8...dh-1
		// *
		// * 2 d3 d6 d9...dh
		// *
		// */
		// for (int x = 0; x < width; x++) {
		// int z = y;
		// while (z < (y + 3)) {
		// for (int m = 0; m < 8; m++) {
		// if ((z * 8) + m >= height) {
		// p[m] = 0;
		// } else {
		// p[m] = bitmap.getPixel(x, z * 8 + m);
		// }
		// }
		// int value = p[0] * 128 + p[1] * 64 + p[2] * 32 + p[3] * 16
		// + p[4] * 8 + p[5] * 4 + p[6] * 2 + p[7];
		// tmpBuf[++t] = (byte) value;
		// z++;
		// }
		// }
		//
		// for (int i = 0; i < t + 1; i++) {
		// if (i == 0 && s == 0) {
		// imgBuf[s] = tmpBuf[i];
		// } else {
		// imgBuf[++s] = tmpBuf[i];
		// }
		// }
		// }
		for (int y = 0; y < (height / 24) + 1; y++) {
			// 初始化
			t = 0;
			// 为图像的每三行字节流添加打印机协议字节1b，2a
			tmpBuf[t] = (byte) 0x1b;// 1
			tmpBuf[++t] = (byte) 0x2a;// 2
			Log.i("yxz", "tmpBuf[0]  " + tmpBuf[0]);
			Log.i("yxz", "tmpBuf[1]  " + tmpBuf[1]);
			// 放大倍数，32为单倍放大，只倍宽，33为图片原始大小
			tmpBuf[++t] = (byte) multiple;// 3
			Log.i("yxz", "tmpBuf[2]" + tmpBuf[2]);
			// nl,nH确定行向打印点数，总的点数为nL+nH*256；

			// nL
			int nL = width % 256;
			tmpBuf[++t] = (byte) (nL * 3);// 4
			// tmpBuf[++t] = (byte) 210;// 4
			Log.i("yxz", "nL  " + nL);
			Log.i("yxz", "tmpBuf[3]" + (int) tmpBuf[3]);
			// nH
			int nH = width / 256;
			if (nH > 3) {
				nH = 3;
			}
			tmpBuf[++t] = (byte) (nH * 3);// 5
			Log.i("yxz", "nH  " + nH);
			Log.i("yxz", "tmpBuf[4]" + tmpBuf[4]);
			// tmpBuf[++t] = (byte) ((width % maxWidth) * 3);
			// tmpBuf[++t] = (byte) ((width / maxWidth > 0 ? 1 : 0) * 3);
			// 发送每次 1b 2a m nL nH d1......dh的数据 d1......dh
			/**
			 * 发送数据的格式 x= 0 1 2....
			 * 
			 * y=0 d1 d4 d7...dh-2
			 * 
			 * 1 d2 d5 d8...dh-1
			 * 
			 * 2 d3 d6 d9...dh
			 * 
			 */
			for (int x = 0; x < width; x++) {
				for (int b = y * 3; b < y * 3 + 3; b++) {
					for (int m = 0; m < 8; m++) {
						if ((b * 8) + m >= height) {
							p[m] = 0;
						} else {
							p[m] = bitmap.getPixel(x, b * 8 + m) == -1 ? 0 : 1;

						}
					}
					int value = p[0] * 128 + p[1] * 64 + p[2] * 32 + p[3] * 16 + p[4] * 8 + p[5] * 4 + p[6] * 2 + p[7];
					tmpBuf[++t] = (byte) value;
				}
			}
			tmpBuf[++t] = 0x0d;// 6
			tmpBuf[++t] = 0x0a;// 7
			for (int i = 0; i < t + 1; i++) {
				if (i == 0 && s == 0) {
					imgBuf[s] = tmpBuf[i];
				} else {
					imgBuf[++s] = tmpBuf[i];
				}
			}
		}
		Log.e("yxz2", "imgBuf[i]   " + Utils.bytesToHexString(imgBuf, imgBuf.length));
		Log.e("yxz2", "imgBuf[i]   " + imgBuf.length + "-------------------");
		return imgBuf;
	}

	private String getFilePath() {

		String PATH_LOGCAT = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
			PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyPicture";
			Log.i(TAG, "sdka ");
		} else {// 如果SD卡不存在，就保存到本应用的目录下
			PATH_LOGCAT = PicturePrintActivity.this.getFilesDir().getAbsolutePath() + File.separator + "MyPicture";
			Log.i(TAG, "neicun");
		}
		File dir = new File(PATH_LOGCAT);
		if (!dir.exists()) {
			dir.mkdir(); // 创建文件夹
		}
		remp_dir = PATH_LOGCAT + File.separator + "tmpPhoto.jpg";
		Log.i(TAG, "remp_dir:" + remp_dir);
		return remp_dir;
	}

	Bitmap monoChromeBitmap = null;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != RESULT_OK) {
			return;
		}
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		Log.i(TAG, "windows height:" + height + "----" + "windows width:" + width);

		switch (requestCode) {
		case IMAGE_CAPTURE_FROM_CAMERA:
			if (remp_dir == null)
				Log.e(TAG, "remp_dir为空！");
			else
				Log.i(TAG, "remp_dir" + remp_dir);
			File f = new File(remp_dir);
			Uri capturedImage = null;
			Bitmap photoBitmap = null;
			try {
				capturedImage = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),
						f.getAbsolutePath(), null, null));
				Log.i(TAG, capturedImage.toString());
				photoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), capturedImage);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i("camera", "Selected image: " + capturedImage.toString());
			f.delete();
			Log.i(TAG, "height1:" + photoBitmap.getHeight() + "----" + "width1:" + photoBitmap.getWidth());
			new ConvertInBackground().execute(photoBitmap);
//			mPrinter.printImage(bitmap, alignType, left, isCompressed);
			break;
		case IMAGE_CAPTURE_FROM_GALLERY:
			Uri mImageCaptureUri = data.getData();
			// 这个方法是根据Uri获取Bitmap图片的静态方法
			try {
				// 选择相册
				Bitmap photoBitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);
				new ConvertInBackground().execute(photoBitmap2);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		default:
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// 其实这里什么都不要做
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.e(TAG, "onDestroy");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.e(TAG, "onStart");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "onPause");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("remp_dir", remp_dir);
		Log.e(TAG, "onSaveInstanceState");

	}

	class ConvertInBackground extends AsyncTask<Bitmap, String, Void> {

		@Override
		protected Void doInBackground(Bitmap... params) {
			Bitmap compress = PictureUtils.compress(params[0]);
			Log.i(TAG, "heightC:" + compress.getHeight() + "----" + "widthC:" + compress.getWidth());
			// monoChromeBitmap = convertor.convertBitmap(compress);
			monoChromeBitmap = compress;
			if (monoChromeBitmap == null) {
				Log.i(TAG, "monoChromeBitmap为空!");
				return null;
			}
			Log.i(TAG, "heightD:" + monoChromeBitmap.getHeight() + "----" + "widthD:" + monoChromeBitmap.getWidth());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			iv_monochrome_picture.setImageBitmap(monoChromeBitmap);
			mPd.dismiss();
			if (PrinterInstance.mPrinter == null) {
				// Log.i(TAG, "PrinterInstance.mPrinter为空");
				Toast.makeText(mContext, getString(R.string.not_support), 1).show();
			} else {
				// mPrinter.printImage(monoChromeBitmap, PAlign.NONE, 0, false);
				mPrinter.printColorImg2Gray(monoChromeBitmap, PAlign.NONE, 0, false);
			}
		}

		@Override
		protected void onPreExecute() {
			mPd = ProgressDialog.show(PicturePrintActivity.this, "Converting Image", "Please Wait", true, false, null);
		}
	}

}
