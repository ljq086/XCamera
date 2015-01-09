package com.xxboy.services.asynctasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.xxboy.log.Logger;
import com.xxboy.utils.XCacheUtil;
import com.xxboy.xcamera.XCamera.XCameraConst;

public class XBitmapCacheAsyncTask extends AsyncTask<Void, Void, Void> {

	private String resourcePath;
	private ImageView imageView;

	private Bitmap varBitmap;

	public XBitmapCacheAsyncTask(String resourcePath, ImageView imageView) {
		super();
		this.resourcePath = resourcePath;
		this.imageView = imageView;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			this.varBitmap = XCacheUtil.getFromMemCache(this.resourcePath);// get from memory cache, the fastest
			if (this.varBitmap == null) {
				this.varBitmap = XCacheUtil.getFromCache(this.resourcePath);// get from soft reference cache or disk cache, the 2nd fast.
				if (this.varBitmap == null) {
					try {
						this.varBitmap = BitmapFactory.decodeFile(this.resourcePath, this.getOptionsInCalculate());// the slowest one, from file to decode.
						Logger.log("Bitmap size:" + this.varBitmap.getByteCount());
						XCacheUtil.pushToCache(this.resourcePath, this.varBitmap);
					} catch (Exception e) {
						this.varBitmap = null;
					}
				}
			}

			if (this.varBitmap != null) {
				if (this.imageView == null) {
					Logger.log("There is one exception it shouldn't be: " + this.resourcePath + ", and the image view is null");
				} else {
					this.imageView.getHandler().post(new Runnable() {
						@Override
						public void run() {
							imageView.setImageBitmap(varBitmap);
						}
					});
				}
			}
		} catch (Exception e) {
			Logger.log(e.getMessage(), e);
		}

		return null;
	}

	private BitmapFactory.Options getOptionsInCalculate() {
		BitmapFactory.Options opt = new BitmapFactory.Options();

		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(this.resourcePath, opt);
		float width_divide_height = opt.outHeight / opt.outWidth;
		int cal_width = 0, cal_height = 0;
		if (width_divide_height > XCameraConst.WIDTH_DIVIDE_HEIGHT) {
			if (opt.outWidth > XCameraConst.PHOTO_ITEM_WIDTH) {
				cal_width = XCameraConst.PHOTO_ITEM_WIDTH;
				cal_height = cal_width * (opt.outHeight / opt.outWidth);
			} else {
				cal_height = opt.outHeight;
				cal_width = opt.outWidth;
			}
			opt.inSampleSize = opt.outWidth / cal_width;
		} else {
			if (opt.outHeight > XCameraConst.PHOTO_ITEM_HEIGHT) {
				cal_height = XCameraConst.PHOTO_ITEM_HEIGHT;
				cal_width = cal_height * (opt.outWidth / opt.outHeight);
			} else {
				cal_height = opt.outHeight;
				cal_width = opt.outWidth;
			}
			opt.inSampleSize = opt.outHeight / cal_height;
		}
		opt.outHeight = cal_height;
		opt.outWidth = cal_width;

		opt.inSampleSize = opt.inSampleSize;

		Logger.log("cal_height: " + cal_height + "==cal_width:" + cal_width + "==inSampleSize:" + opt.inSampleSize);
		opt.inJustDecodeBounds = false;
		return opt;
	}
}
