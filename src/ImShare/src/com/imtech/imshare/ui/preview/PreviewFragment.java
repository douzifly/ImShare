/*
 * @project :ImShare
 * @author  :huqiming 
 * @date    :2013-12-4
 */
package com.imtech.imshare.ui.preview;

import java.io.File;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView.ScaleType;

import com.imtech.imshare.R;
import com.imtech.imshare.utils.BitmapUtil;
import com.imtech.imshare.utils.StringUtils;
import com.polites.android.GestureImageView;

/**
 * 图片预览界面
 */
public class PreviewFragment extends Fragment implements OnClickListener {
	private static final String TAG = "PreviewFragment";
	private OnDeleteListener mListener;
	private GestureImageView mImageView;
	private Bitmap mImageBitmap;
	private String mImagePath;
	private Button mBtnDelete;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_preview, null);
		mBtnDelete = (Button) view.findViewById(R.id.delete);
		mBtnDelete.setOnClickListener(this);
		mImageView = (GestureImageView) view.findViewById(R.id.image);
		mImageView.setStrict(true);
		mImageView.setRecycle(true);
		showImage();
		return view;
	}

	public void setOnDeleteListener(OnDeleteListener l) {
		mListener = l;
	}

	private void showImage(){
		if (StringUtils.isEmpty(mImagePath)) return;
		File f = new File(mImagePath);
		if (!f.exists()) return;
		int size = getResources().getDisplayMetrics().widthPixels;
		Bitmap bmp = BitmapUtil.decodeFile(mImagePath, size);
		if (bmp != null) {
			int degree = BitmapUtil.getRotate(mImagePath);
//			mImageView.setScaleType(ScaleType.CENTER);
			mImageView.setImageBitmap(bmp, degree);
			mImageBitmap = bmp;
		}
//		if (StringUtils.isEmpty(mImagePath)) return;
//		File f = new File(mImagePath);
//		if (!f.exists()) return;
//		mImageView.setImageURI(Uri.fromFile(f));
	}
	
	public void setImagePath(String path) {
		Log.d(TAG, "setImagePath");
		mImagePath = path;
		if (isAdded()) {
		    showImage();
		}
	}
	
	public void setShowDelete(boolean visible) {
	    mBtnDelete.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, "onDetach");
		mImageView.setImageBitmap(null);
		if (mImageBitmap != null) {
			mImageBitmap.recycle();
			mImageBitmap = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delete:
			notifyDelete();
			break;
		}
	}

	private void notifyDelete() {
		if (mListener != null) {
			mListener.onDelete();
		}
	}

	public interface OnDeleteListener {
		void onDelete();
	}
}
