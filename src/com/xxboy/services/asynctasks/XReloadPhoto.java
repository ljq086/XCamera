package com.xxboy.services.asynctasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;

import com.xxboy.activities.mainview.XCamera;
import com.xxboy.activities.mainview.XCamera.XCameraConst;
import com.xxboy.activities.mainview.adapters.xdata.XAdapterBase;
import com.xxboy.activities.mainview.adapters.xdata.XAdapterDate;
import com.xxboy.activities.mainview.adapters.xdata.XAdapterPicture;
import com.xxboy.common.XFunction;
import com.xxboy.log.Logger;
import com.xxboy.utils.XColorUtil;

public final class XReloadPhoto extends AsyncTask<Void, Void, Void> {

	private ArrayList<String> resources = new ArrayList<String>();

	protected static final class Mover {
		public static Integer moveAllPhotos() {
			File[] freshFile = checkExistingImages();
			if (freshFile == null || freshFile.length == 0) {
				return 0;
			}
			int movedPhotosCount = movePhotos();
			return movedPhotosCount;
		}

		/**
		 * check whether there're images in the default image path
		 * 
		 * @return
		 */
		private static File[] checkExistingImages() {
			File defaultFolder = new File(XCameraConst.GLOBAL_X_DEFAULT_CAMERA_PATH);
			if (!defaultFolder.exists()) {
				return null;
			}
			return defaultFolder.listFiles();
		}

		/**
		 * generate current date folder and move camera photos to the date folder.
		 */
		private static int movePhotos() {
			File[] pictures = checkExistingImages();
			if (pictures != null && pictures.length > 0) {
				XFunction.XDate date = new XFunction.XDate();
				String currentTargetFolderName = ""//
						+ XCameraConst.GLOBAL_X_CAMERA_PATH//
						+ File.separator + date.getYear() + "." + date.getMonth() //
						+ File.separator//
						+ date.getYear() + "." + date.getMonth() + "." + date.getDay();

				/** get picture folder and create system locale date folder */
				File pictureFolder = new File(currentTargetFolderName);
				if (!pictureFolder.exists()) {
					pictureFolder.mkdirs();
				}

				/** moving pictures */
				for (File pictureItem : pictures) {
					pictureItem.renameTo(new File(currentTargetFolderName + File.separator + pictureItem.getName()));
				}
			} else {
				Logger.log("There're no files in the default camera folder");
			}
			return pictures.length;
		}
	}

	@Override
	protected Void doInBackground(Void... param) {
		// moving files
		int hasCount = XCamera.getxView().getCount();
		Integer moveCount = Mover.moveAllPhotos();
		// if there's no change, it won't reload picture.
		if ((moveCount == null || moveCount <= 0) && hasCount > 0) {
			return null;
		}

		LinkedList<XAdapterBase> imageResources = getDaysPhotoResourceX();
		LinkedList<XAdapterBase> allResources = new LinkedList<XAdapterBase>();
		Collections.reverse(imageResources);
		allResources.addAll(imageResources);
		/**
		 * reload gridview images
		 */
		XCamera.reloadGridview(allResources);

		// set resources info
		for (XAdapterBase item : imageResources) {
			this.resources.add((String) item.get(XCameraConst.VIEW_NAME_IMAGE_RESC));
		}
		XCamera.setAllResourcePath(resources);
		return null;
	}

	/**
	 * get all xCamera photos
	 * 
	 * @return
	 */
	private LinkedList<XAdapterBase> getDaysPhotoResourceX() {
		String xcameraPath = XCameraConst.GLOBAL_X_CAMERA_PATH;
		File xCameraFolder = new File(xcameraPath);
		if (!xCameraFolder.exists()) {
			xCameraFolder.mkdirs();
			return new LinkedList<XAdapterBase>();
		}

		LinkedList<XAdapterBase> result = new LinkedList<XAdapterBase>();
		File[] xyyyymmFolders = xCameraFolder.listFiles();
		if (xyyyymmFolders == null || xyyyymmFolders.length == 0) {
			return result;
		}
		for (File yyyymmFolder : xyyyymmFolders) {
			if (!yyyymmFolder.isDirectory() || yyyymmFolder.isHidden()) {
				continue;
			}
			File[] yyyymmddFolders = yyyymmFolder.listFiles();
			for (File yyyymmddFolder : yyyymmddFolders) {
				List<XAdapterBase> itemResult = get1DayPhotoResourceX(yyyymmddFolder);
				if (itemResult != null && itemResult.size() > 0) {
					result.addAll(itemResult);
				}
			}
		}
		return result;
	}

	/**
	 * generate 1 folder's image view item list.
	 * 
	 * @param xcameraDateFolder
	 * @return
	 */
	private LinkedList<XAdapterBase> get1DayPhotoResourceX(File xcameraDateFolder) {
		int color = XColorUtil.getBackgroundColor(xcameraDateFolder.getName());
		LinkedList<XAdapterBase> photoResource = new LinkedList<XAdapterBase>();
		if (!xcameraDateFolder.exists()) {
			return photoResource;
		}

		File[] photos = xcameraDateFolder.listFiles();
		if (photos != null && photos.length > 0) {
			for (File photoItem : photos) {
				if (photoItem.isDirectory()) {
					continue;
				} else if (photoItem.isHidden()) {
					continue;
				}
				HashMap<String, Object> item = new HashMap<String, Object>();
				// item.put(XCameraConst.VIEW_NAME_IMAGE_ITEM, R.drawable.big_load);
				item.put(XCameraConst.VIEW_NAME_IMAGE_ITEM, photoItem.getAbsolutePath());
				item.put(XCameraConst.VIEW_NAME_IMAGE_RESC, photoItem.getAbsolutePath());
				photoResource.add(new XAdapterPicture(item, color));
			}

			// -- add date show
			HashMap<String, Object> dateItem = new HashMap<String, Object>();
			if (xcameraDateFolder.getName().length() >= 4) {
				dateItem.put(XAdapterDate.ID_ITEM_DATE_YEAR, xcameraDateFolder.getName().substring(0, 4));
			}
			if (xcameraDateFolder.getName().length() >= 7) {
				dateItem.put(XAdapterDate.ID_ITEM_DATE_MONTH, XFunction.getMonthTranslation(xcameraDateFolder.getName().substring(5, 7)));
			}
			if (xcameraDateFolder.getName().length() >= 10) {
				dateItem.put(XAdapterDate.ID_ITEM_DATE_DAY, xcameraDateFolder.getName().substring(8, 10));
			}
			photoResource.add(new XAdapterDate(dateItem, color));
		}

		return photoResource;
	}

}
