package com.printer.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.printer.sdk.PrinterConstants.PAlign;
import com.printer.sdk.monochrome.BitmapConvertor;
import com.artifex.mupdf.MuPDFCore;
import com.printer.demo.PicturePrintActivity.ConvertInBackground;
import com.printer.demo.utils.PictureUtils;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class PdfActivity extends Activity implements OnClickListener {

	private Button test;
	private ImageView iv;
	private String path = null;
	private String filePath = null;
	private ProgressDialog mPd;
	private BitmapConvertor convertor;
	private MuPDFCore core = null;
	private DisplayMetrics metric = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_pdf);
		initView();
	}

	private void initView() {

		test = (Button) findViewById(R.id.btn_test);
		test.setOnClickListener(this);
		iv = (ImageView) findViewById(R.id.iv);
		convertor = new BitmapConvertor(this);
		metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
	}

	@Override
	public void onClick(View v) {

		// 打印PDF
		if (v == test) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			startActivityForResult(intent, 1);
		}

	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			super.onActivityResult(requestCode, resultCode, data);
			if (requestCode == 1) {
				Uri uri = data.getData();
				// Toast.makeText(this, "文件路径："+uri.getPath().toString(),
				// Toast.LENGTH_SHORT).show();
				filePath = uri.getPath().toString();
				if (filePath == null || !filePath.endsWith(".pdf")) {
					Toast.makeText(this, "文件不合法!", Toast.LENGTH_LONG).show();
					return;
				}
				try {
					core = new MuPDFCore(this, filePath);
				} catch (Exception e) {

					e.printStackTrace();
				}

				/********** 以下处理转成图片 ***********/
				int count = core.countPages();
				Log.i("sprt", "count:" + count);
				PointF pageSize = core.getPageSize(0);
				float pageW = pageSize.x;
				float pageH = pageSize.y;
				Log.i("sprt", "pageW:" + pageW + "  pageH:" + pageH);
				Bitmap bitmap = Bitmap.createBitmap((int)pageW, (int)pageH, Bitmap.Config.ARGB_8888);
				core.drawPage(0, bitmap, (int) pageW, (int) pageH, 0, 0, (int)pageW, (int)pageH);
				// try {
				// saveBitmap(bitmap, "test1.png");
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// Bitmap zoomImage = Utils.zoomImage(bitmap,
				// metric.widthPixels);

				// 使用屏幕大小
//				String saveBitmap = null;
//				try {
//					 saveBitmap = saveBitmap(bitmap, "test2.jpg");
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				Bitmap decodeFile = BitmapFactory.decodeFile(saveBitmap);
				new ConvertInBackground().execute(bitmap);

			}
		}
	}

	/*
	 * @SuppressLint("NewApi") public Bitmap getBitmap(String filepath) {
	 * 
	 * // File f = new File("/sdcard/test2.pdf"); File f = new
	 * File("/sdcard/smart.pdf"); ParcelFileDescriptor open; PdfRenderer
	 * mPdfRenderer = null; try { open = ParcelFileDescriptor.open(f,
	 * ParcelFileDescriptor.MODE_READ_WRITE); mPdfRenderer = new
	 * PdfRenderer(open); } catch (FileNotFoundException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch (Exception e) {
	 * // TODO: handle exception e.printStackTrace(); } int pageCount =
	 * mPdfRenderer.getPageCount(); Log.i("sprt", "pageCount:" + pageCount);
	 * Page openPage = mPdfRenderer.openPage(0); Log.i("sprt", "getWidth:" +
	 * openPage.getWidth() + "getHeight:" + openPage.getHeight()); Float ff =
	 * (float) (openPage.getWidth() / openPage.getHeight()); int newWidth = 720;
	 * // Utils.zoomImage(bgimage, newWidth, newHeight); Bitmap bitmap =
	 * Bitmap.createBitmap(newWidth, (int) (newWidth / ff),
	 * Bitmap.Config.ARGB_8888);
	 * 
	 * openPage.render(bitmap, null, null,
	 * PdfRenderer.Page.RENDER_MODE_FOR_PRINT); return bitmap;
	 * 
	 * }
	 */

	private String saveBitmap(Bitmap bitmap, String bitName) throws IOException {
		File file = new File("/sdcard/sprt/" + bitName);
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}

	Bitmap monoChromeBitmap = null;

	class ConvertInBackground extends AsyncTask<Bitmap, String, Void> {

		@Override
		protected Void doInBackground(Bitmap... params) {

			Bitmap bitmap = params[0];
			Log.i("sprt", "宽：" + bitmap.getWidth() + "  高：" + bitmap.getHeight());
			Bitmap zoomImage = Utils.zoomImage(bitmap, 500);
			Log.i("sprt", "zoomImage宽：" + zoomImage.getWidth() + "  zoomImage高：" + zoomImage.getHeight());
			 monoChromeBitmap = convertor.convertBitmap(zoomImage);
			 Log.i("sprt", "monoChromeBitmap宽：" + monoChromeBitmap.getWidth() + "  monoChromeBitmap高：" + monoChromeBitmap.getHeight());
//			 PrinterInstance.mPrinter.printImage(zoomImage, PAlign.START, 0, false);
			 PrinterInstance.mPrinter.printImage(monoChromeBitmap, PAlign.START, 0, false);
//			 PrinterInstance.mPrinter.printColorImg2Gray(zoomImage, PAlign.START, 0, false);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			iv.setImageBitmap(monoChromeBitmap);
			mPd.dismiss();
		}

		@Override
		protected void onPreExecute() {
			mPd = ProgressDialog.show(PdfActivity.this, "Handing the PDF file....", "Please Wait", true, false, null);
		}
	}

}
