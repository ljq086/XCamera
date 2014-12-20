package com.xxboy.adapters;

import java.util.Map;

import com.xxboy.photo.R;
import com.xxboy.xcamera.XCamera.XCameraConst;

public class XAdapterCamera extends XAdapterBase {

	private Map<String, ?> data;

	public XAdapterCamera(Map<String, ?> data) {
		super();
		this.data = data;
	}

	private static final int[] mTo = { R.id.id_camera_preview };
	private static final String[] mFrom = { XCameraConst.VIEW_NAME_CAMERA_ID };

	@Override
	public int getResource() {
		return R.layout.xcamera_camera;
	}

	@Override
	public String[] getMFrom() {
		return XAdapterCamera.mFrom;
	}

	@Override
	public int[] getMTo() {
		return XAdapterCamera.mTo;
	}

	@Override
	public Object get(String key) {
		return this.data.get(key);
	}

}