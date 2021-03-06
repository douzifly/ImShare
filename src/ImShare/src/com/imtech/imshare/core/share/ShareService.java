/**
 * douzifly @Nov 25, 2013
 * github.com/douzifly
 * douzifly@gmail.com
 */
package com.imtech.imshare.core.share;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.imtech.imshare.core.auth.AuthService;
import com.imtech.imshare.core.share.IShareQueue.IShareQueueListener;
import com.imtech.imshare.sns.SnsType;
import com.imtech.imshare.sns.auth.AccessToken;
import com.imtech.imshare.sns.share.IShare;
import com.imtech.imshare.sns.share.IShareListener;
import com.imtech.imshare.sns.share.ImageUploadInfo;
import com.imtech.imshare.sns.share.QQShare;
import com.imtech.imshare.sns.share.ShareObject;
import com.imtech.imshare.sns.share.ShareRet;
import com.imtech.imshare.sns.share.ShareRet.ShareRetState;
import com.imtech.imshare.sns.share.WeiboShare;
import com.imtech.imshare.utils.BitmapUtil;
import com.imtech.imshare.utils.FileUtil;
import com.imtech.imshare.utils.Log;

/**
 * @author douzifly
 *
 */
public class ShareService implements IShareService{
	
	final static String TAG = "SNS_ShareService";
	
	private LinkedList<IShareServiceListener> mListeners 
				= new LinkedList<IShareServiceListener>();
	
	private IShareQueue mShareQueue;
	private Context mAppContext;
	private Activity mActivity;
	private static ShareService sSharedInstance;

    private String mTmpScaleImageDir;

    private ExecutorService mExecutorService;

    public void setTmpScaledImagePath (String dir) {
        mTmpScaleImageDir = dir;
    }
	
	public synchronized static ShareService sharedInstance() {
	    if (sSharedInstance == null) {
	        sSharedInstance = new ShareService();
	    }
	    return sSharedInstance;
	}
	
	public ShareService() {
		mShareQueue = new ShareQueue();
		mShareQueue.setListener(new QueueListener());
        mExecutorService = Executors.newFixedThreadPool(1);
    }

	private IShare getShare(SnsType type) {
		 if (type == SnsType.WEIBO) {
			 return new WeiboShare();
		 } else if (type == SnsType.TENCENT_WEIBO) {
			 return new QQShare();
		 }
		 return null;
	}
	
	private AccessToken getToken(SnsType type) {
		return AuthService.getInstance().getAccessToken(type); } 
    /**
     * 检查是否需要压缩图片
     * @param filePath 原始图片路径
     * @param scaleWidth 压缩后的大小
     * @return 如果压缩了返回true， 否则返回false
     */
    public boolean checkCompressAndRotateImage(String filePath, int scaleWidth, String savePath) throws IOException {
        Log.d(TAG, "checkCompressImage path:" + filePath);
        Options opt = new Options();
        BitmapFactory.decodeFile(filePath, opt);
        if (opt.outWidth <= scaleWidth) {
            Log.d(TAG, "checkCompressImage size:" + opt.outWidth + " scaleWidth:" + scaleWidth + " no need scale");
            return BitmapUtil.checkRotateAndSave(filePath, savePath);
        }
        return BitmapUtil.scaleAndSave(filePath, opt,  scaleWidth, savePath, true);
    }

    public void checkCompressAndRotateImage(ShareObject obj) {
        Log.d(TAG, "checkCompressImage");
        if (obj.images == null || obj.images.size() == 0) {
            Log.d(TAG, "checkCompressImage, no image");
            return;
        }
        for (ShareObject.Image image : obj.images) {
            if (image.filePath != null)  {
                String fileName = null;
                try {
                    fileName = FileUtil.getFileName(image.filePath);
                } catch(Exception e) {
                    fileName = "scaled.png";
                }
                String savePath = mTmpScaleImageDir+ fileName;
                Log.d(TAG, "savePath:" + savePath);
                File f = new File(savePath);
                if (f.exists() && f.length() > 0) {
                    Log.d(TAG, "sacled image exists:" + savePath);
                    continue;
                }

                try {
                   boolean compressed = checkCompressAndRotateImage(image.filePath, obj.maxPicWidth, savePath);
                    Log.d(TAG, "compressed:" + compressed + " savePath:" + savePath);
                    if (compressed) {
                        image.scaledPath = savePath;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "exp:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

	@Override
	public int addShare(Activity activity, ShareObject obj, SnsType snsType) {
		Log.d(TAG, "share obj:" + obj + " type:" + snsType);
		mAppContext = activity.getApplicationContext();
		mActivity = activity;
		notifyShareAddeds(obj);
		return mShareQueue.add(obj, snsType);
	}
	
	@Override
	public void cancel(int shareId) {
		mShareQueue.remove(shareId);
	}
	
	@Override
	public void clear() {
	    mShareQueue.clear();
	}

	@Override
	public void addListener(IShareServiceListener listener) {
		mListeners.add(listener);
	}

	@Override
	public void removeListener(IShareServiceListener l) {
		mListeners.remove(l);
	}
	
	class QueueListener implements IShareQueueListener {

		@Override
		public void onNextShare(final ShareObject obj, final SnsType type) {
			Log.d(TAG, "onNextShare:" + obj + " type:" + type);
	        final IShare share = getShare(type);
			if (share == null) {
				Log.e(TAG, "unknown share type:" + type);
				ShareRet ret = new ShareRet(ShareRetState.FAILED, obj, type);
				notifyShareFinishend(ret);
				mShareQueue.checkNext();
				return;
			}
			final AccessToken token = getToken(type);
			if (token == null) {
				Log.e(TAG, "no token:" + type);
				ShareRet ret = new ShareRet(ShareRetState.TOKEN_EXPIRED, obj, type);
				notifyShareFinishend(ret);
				mShareQueue.checkNext();
				return;
			}
			share.setListener(new ShareListener());
			
			notifyShareBegin(obj);
            // check compress
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    checkCompressAndRotateImage(obj);
                    Log.d(TAG, "begin share");
			        share.share(mAppContext, mActivity, token, obj);
                }
            });
		}
		
	}
	
	private void notifyShareFinishend(ShareRet ret) {
		for (IShareListener l : mListeners) {
			l.onShareFinished(ret);
		}
	}
	
	private void notifyShareComplete() {
		for (IShareServiceListener l : mListeners) {
			l.onShareComplete();
		}
	}
	
	private void notifyShareAddeds(ShareObject obj) {
		for (IShareServiceListener l : mListeners) {
			l.onShareAdded(obj);
		}
	}
	
	private void notifyShareBegin(ShareObject obj) {
		for (IShareServiceListener l : mListeners) {
			l.onShareBegin(obj);
		}
	}
	
	private void notifyImageUploadChange(ImageUploadInfo info) {
        for (IShareListener l : mListeners) {
            l.onShareImageUpload(info);
        }
    }
	
	class ShareListener implements IShareListener {

		@Override
		public void onShareFinished(ShareRet ret) {
			notifyShareFinishend(ret);
			boolean haveTask = mShareQueue.checkNext();
			if (!haveTask) {
				notifyShareComplete();
			}
		}
		
		@Override
		public void onShareImageUpload(ImageUploadInfo info) {
		    notifyImageUploadChange(info);
		}

	}
  
}
