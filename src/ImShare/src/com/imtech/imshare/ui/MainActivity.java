package com.imtech.imshare.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Toast;

import com.imtech.imshare.R;
import com.imtech.imshare.sns.SnsType;
import com.imtech.imshare.sns.auth.AuthRet;
import com.imtech.imshare.sns.auth.IAuthListener;
import com.imtech.imshare.sns.share.IShareListener;
import com.imtech.imshare.sns.share.ImageUploadInfo;
import com.imtech.imshare.sns.share.ShareRet;
import com.imtech.imshare.sns.share.WeiboShare;
import com.imtech.imshare.utils.Log;

public class MainActivity extends Activity implements OnClickListener{
    final static String TAG = "Share#MainActivity";
    private static final int PHOTO_REQUEST_GALLERY = 12;// 从相册中选择
    WeiboShare mShare = new WeiboShare();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_guide);
        Button btn = (Button)findViewById(R.id.share);
        btn.setOnClickListener(this);
    }
    
    class AuthListener implements IAuthListener {

        @Override
        public void onAuthFinished(SnsType snsType, AuthRet ret) {
            Toast.makeText(MainActivity.this, "finished:" + ret.state, Toast.LENGTH_SHORT).show();
        }
        
    }
    
    private void shakeView(View v) {
    	TranslateAnimation a = new TranslateAnimation(0, 30, 0, 30);
    	a.setRepeatCount(3);
    	a.setRepeatMode(Animation.REVERSE);
    	a.setDuration(100);
    	v.startAnimation(a);
    }

    @Override
    public void onClick(View v) {
    	switch(v.getId()){
    	case R.id.share:
    		gotoSelectPic();
    		break;
    	}
    }
    
    private void gotoSelectPic(){
    	Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
//    	mAuthService.checkActivityResult(requestCode, resultCode, data);
    	String path = data != null ? data.getDataString() : null;
    	Log.d(TAG, "onActivityResult path: " + path);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }
}
