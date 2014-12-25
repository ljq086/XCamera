package com.xxboy.xcamera;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.xxboy.listeners.CallCameraListener;
import com.xxboy.listeners.XScrollListener;
import com.xxboy.log.Logger;
import com.xxboy.photo.R;
import com.xxboy.services.XCompressPhotosAsync;
import com.xxboy.services.XInitial;
import com.xxboy.services.XPhotoParam;
import com.xxboy.services.XReloadPhoto;
import com.xxboy.services.XViewMovePhotos;
import com.xxboy.view.XPreview;

public class XCamera extends Activity {
	private String xPath, xCachePath, cameraPath;
	public static int count = 20;
	public static Map<String, Bitmap> imageCache = new LinkedHashMap<String, Bitmap>();

	public static final class XCameraConst {
		public static final String VIEW_NAME_IMAGE_ITEM = "ItemImage";
		public static final String VIEW_NAME_IMAGE_RESOURCE = "ImageResource";

		public static final String VIEW_NAME_CAMERA_ID = "id_camera_preview";

		/** screen width */
		public static int SCREEN_WIDTH = -1;
		/** screen height */
		public static int SCREEN_HEIGHT = -1;

		/** photo item width */
		public static int PHOTO_ITEM_WIDTH = -1;
		/** photo item height */
		public static int PHOTO_ITEM_HEIGHT = -1;

		public static void setWidthHeight(int width, int height) {
			SCREEN_WIDTH = width;
			SCREEN_HEIGHT = height;

			PHOTO_ITEM_WIDTH = width / 3;
			PHOTO_ITEM_HEIGHT = PHOTO_ITEM_WIDTH;
		}
	}

	private GridView xGridView;
	private List<Camera> mCameras = new LinkedList<Camera>();
	int numberOfCameras = -1;

	public static final Integer COMPLETED = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xcamera);

		initScreenParameters();

		// get components in the main view.
		this.xGridView = (GridView) findViewById(R.id.photo_grid);
		this.xGridView.setOnScrollListener(new XScrollListener(this));

		this.numberOfCameras = Camera.getNumberOfCameras();
		Logger.log("There're " + this.numberOfCameras + " cameras on this phone");

		this.xPath = getString(R.string.picture_folder_path);
		this.xCachePath = getString(R.string.cash_picture_folder_path);
		this.cameraPath = getString(R.string.default_picture_folder_path);

		new XInitial(new XPhotoParam(xPath, xCachePath, cameraPath)).execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			for (int cameraIndex = 0; cameraIndex < this.numberOfCameras; cameraIndex++) {
				XPreview iPreview = new XPreview(this);
				Camera iCamera = Camera.open(cameraIndex);
				iCamera.startPreview();
				iPreview.setCamera(iCamera);

				iPreview.setOnClickListener(new CallCameraListener(this, iCamera));

				this.mCameras.add(iCamera);
			}
		} catch (Exception e) {
			Toast.makeText(this, "Can't access cameras!", Toast.LENGTH_SHORT).show();
			Logger.log(e);
		}
		moveAndLoadPhotos(true);
		// new XReloadPhoto(this, new XPhotoParam(xPath, xCachePath,
		// cameraPath)).execute();
	}

	@Override
	protected void onResume() {
		try {
			for (int cameraIndex = 0; cameraIndex < this.numberOfCameras; cameraIndex++) {
				this.mCameras.get(cameraIndex).reconnect();
				this.mCameras.get(cameraIndex).startPreview();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		try {
			for (Camera iCamera : this.mCameras) {
				iCamera.stopPreview();
				iCamera.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	private void initScreenParameters() {
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		XCameraConst.setWidthHeight(metric.widthPixels, metric.heightPixels);

		Logger.log("SCREEN_WIDTH: " + XCameraConst.SCREEN_WIDTH);
		Logger.log("SCREEN_HEIGHT: " + XCameraConst.SCREEN_HEIGHT);
	}

	@Override
	protected void onDestroy() {
		try {
			for (Camera iCamera : this.mCameras) {
				// iCamera.stopPreview();
				iCamera.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		moveAndLoadPhotos(false);
	}

	private void moveAndLoadPhotos(boolean reloadFlag) {
		XPhotoParam photoParam = new XPhotoParam(this.xPath, this.xCachePath, this.cameraPath);
		XViewMovePhotos reload = new XViewMovePhotos(photoParam);
		Integer result = null;
		try {
			result = reload.execute().get();
		} catch (InterruptedException e) {
			Logger.log(e);
		} catch (ExecutionException e) {
			Logger.log(e);
		}

		Logger.log("The return result is " + result);
		if (result > 0) {
			// showing message to tell it's doing reloading photos
			Toast.makeText(this, "Reloading your photos", Toast.LENGTH_SHORT).show();
			new XCompressPhotosAsync(photoParam).execute();
		}
		if (reloadFlag || result > 0) {
			new XReloadPhoto(this, photoParam).execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.camera_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	public GridView getxView() {
		return xGridView;
	}

	public void setxView(GridView xView) {
		this.xGridView = xView;
	}

	public List<Camera> getmCameras() {
		return mCameras;
	}

	public void setmCameras(List<Camera> mCameras) {
		this.mCameras = mCameras;
	}

}
