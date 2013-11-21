/**
 * Author:Xiaoyuan
 * Date: Nov 20, 2013
 * 深圳快播科技
 */
package com.imtech.imshare.sns;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.imtech.imshare.sns.AuthRet.AuthRetState;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

/**
 * QQ 授权
 * @author Xiaoyuan
 *
 */
public class QQAuth implements I3rdAuth{

	private final static String TAG = "3rdAuth_QQAuth";
	private final static String APP_ID = "100560544";
	
	private Tencent mTencent;
	private I3rdAuthListener mListener;
	
	private void init(Context context) {
		if (mTencent != null) return;
		mTencent = Tencent.createInstance(APP_ID, context);
	}
	
	@Override
	public void auth(Context appCtx, Activity activity) {
		Log.d(TAG, "auth");
		if (mListener == null) {
			throw new IllegalStateException("listener not set");
		}
		init(appCtx);
		if (mTencent.isSessionValid()) {
			// 授权还没有失效
			Log.d(TAG, "session valid");
			AuthRet ret = new AuthRet(AuthRetState.SUCESS);
			ret.getBundle().putString(AuthRet.KEY_ACCESS_TOKEN, mTencent.getAccessToken());
			ret.getBundle().putString(AuthRet.KEY_UID, mTencent.getOpenId());
			ret.getBundle().putLong(AuthRet.KEY_EXPIRES_WHEN, mTencent.getExpiresIn() + System.currentTimeMillis());
			mListener.onAuthFinished(ret);
			return;
		}
		String scope = "all"; // 需要获取的权限，由 ',' 分割
		mTencent.login(activity, scope, new QQAuthListener());
	}
	
	@Override
	public void logout(Context appCtx, Activity activity) {
		Log.d(TAG, "logout");
		init(appCtx);
		mTencent.logout(activity);
	}

	@Override
	public void setListener(I3rdAuthListener l) {
		mListener = l;
	}

	@Override
	public void checkActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "checkActivityResult");
		if (mTencent != null) {
			mTencent.onActivityResult(requestCode, resultCode, data);
		}
	}

	class QQAuthListener implements IUiListener {

		@Override
		public void onCancel() {
			Log.d(TAG, "onCancel");
			AuthRet ret = new AuthRet(AuthRetState.CANCELED);
			mListener.onAuthFinished(ret);
		}

		@Override
		public void onComplete(JSONObject json) {
			Log.d(TAG, "onComplete");
			/*
			返回的原始JSON 数据示例
			{
				"ret":0,
				"pay_token":"xxxxxxxxxxxxxxxx",
				"pf":"openmobile_android",
				"expires_in":"7776000",
				"openid":"xxxxxxxxxxxxxxxxxxx",
				"pfkey":"xxxxxxxxxxxxxxxxxxx",
				"msg":"sucess",
				"access_token":"xxxxxxxxxxxxxxxxxxxxx"
			}
			*/
			String accessToken = "", openId = "";
			long expires_in = 0;
			int jsonret = -1;
			boolean parseFailed = false;
			try {
				jsonret = json.getInt("ret");
				accessToken = json.getString("access_token");
				openId = json.getString("openid");
				expires_in = json.getLong("expires_in");
			} catch(JSONException e) {
				Log.e(TAG, "jsonException:" + e.getMessage());
				parseFailed = true;
			}
			Log.d(TAG, "ret:" + jsonret + " token:" + accessToken + " openId:" + openId + " exp:" + expires_in);
			AuthRet ret = null;
			if (jsonret != 0 || parseFailed) {
				ret = new AuthRet(AuthRetState.FAILED);
				ret.getBundle().putString(AuthRet.KEY_ERROR_ERROR_MESSAGE, "parse json failed");
			} else {
				ret = new AuthRet(AuthRetState.SUCESS);
				ret.getBundle().putString(AuthRet.KEY_ACCESS_TOKEN, accessToken);
				ret.getBundle().putLong(AuthRet.KEY_EXPIRES_WHEN, expires_in + System.currentTimeMillis());
				ret.getBundle().putString(AuthRet.KEY_UID, openId);
			}
			mListener.onAuthFinished(ret);
		}

		@Override
		public void onError(UiError err) {	
			Log.d(TAG, "onError, errorCode:" + err.errorCode + " errorMessage:" + err.errorMessage
					+ " errorDetail:" + err.errorDetail);
			AuthRet ret = new AuthRet(AuthRetState.FAILED);
			ret.getBundle().putInt(AuthRet.KEY_ERROR_CODE, err.errorCode);
			ret.getBundle().putString(AuthRet.KEY_ERROR_ERROR_MESSAGE, err.errorMessage);
			ret.getBundle().putString(AuthRet.KEY_ERROR_ERROR_DETAIL, err.errorDetail);
			mListener.onAuthFinished(ret);
		}
		
	}
}