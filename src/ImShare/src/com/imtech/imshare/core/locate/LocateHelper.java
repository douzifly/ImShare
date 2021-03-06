package com.imtech.imshare.core.locate;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * 定位工具
 * @author HuQiming
 * @date 2013-12-5
 *
 */
public class LocateHelper implements BDLocationListener{
	private static final String TAG = "LocateHelper";
//	/**
//	 * release key
//	 */
//	private static final String APP_KEY = "7SiM5b3lTPVLaeTh8VP2Oanb";
	/**
	 * debug key
	 */
	private static final String APP_KEY = "wnEjbrMZBgO1QLiYkVuucfOi";
	
	private static LocateHelper mInstance;
	private LocationClient mLocationClient;
	private LocationListener mLocationListener;
	
	/**
	 * @return the mInstance
	 */
	public static LocateHelper getInstance(Context ctx) {
		if(mInstance == null){
			mInstance = new LocateHelper(ctx);
		}
		return mInstance;
	}
	
	private LocateHelper(Context ctx) {
		mLocationClient = new LocationClient(ctx);
		mLocationClient.setAK(APP_KEY);
		mLocationClient.registerLocationListener(this);
		LocationClientOption opt = new LocationClientOption();
		opt.setAddrType("all");
		opt.disableCache(true);
		opt.setOpenGps(true);
		mLocationClient.setLocOption(opt);
		mLocationClient.start();
	}
	
	public static void release(){
		if(mInstance != null){
			mInstance.destroy();
		}
		mInstance = null;
	}
	
	private void destroy(){
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(this);
	}
	
	public boolean locate(LocationListener l){
		mLocationListener = l;
        if (!mLocationClient.isStarted()) {
            Log.e(TAG, "location service not started");
            return false;
        }
		int res = mLocationClient.requestLocation();
		Log.d(TAG, "locate requestLocation res: " + res);
        return res == 0;
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		StringBuffer sb = new StringBuffer(256);
		sb.append("time : ");
		sb.append(location.getTime());
		sb.append("\nLoc Type : ");
		sb.append(location.getLocType());
		sb.append("\nlatitude : ");
		sb.append(location.getLatitude());
		sb.append("\nlontitude : ");
		sb.append(location.getLongitude());
		sb.append("\nradius : ");
		sb.append(location.getRadius());
		if (location.getLocType() == BDLocation.TypeGpsLocation){
			sb.append("\nspeed : ");
			sb.append(location.getSpeed());
			sb.append("\nsatellite : ");
			sb.append(location.getSatelliteNumber());
            sb.append("\naddr : ");
            sb.append("\n省：");
            sb.append(location.getProvince());
            sb.append("\n市：");
            sb.append(location.getCity());
            sb.append("\n区/县：");
            sb.append(location.getDistrict());
            sb.append(location.getAddrStr());
		} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
			/**
			 * 格式化显示地址信息
			 */
			sb.append("\n省：");
			sb.append(location.getProvince());
			sb.append("\n市：");
			sb.append(location.getCity());
			sb.append("\n区/县：");
			sb.append(location.getDistrict());
			sb.append("\naddr : ");
			sb.append(location.getAddrStr());
		}
		sb.append("\nsdk version : ");
		sb.append(mLocationClient.getVersion());
		sb.append("\nisCellChangeFlag : ");
		sb.append(location.isCellChangeFlag());
		Log.d(TAG, "onReceiveLocation : " + sb.toString());
		
		Location loc = new Location();
		loc.latitude = location.getLatitude();
		loc.longitude = location.getLongitude();
		loc.detail = location.getAddrStr();
		loc.city = location.getCity();
		
		if(mLocationListener != null){
			mLocationListener.onReceiveLocation(loc);
		}
	}

	@Override
	public void onReceivePoi(BDLocation location) {
	    Log.d(TAG, "onReceivePoi:" + location);
	}
}
