/**
 * douzifly @2013-12-22
 * github.com/douzifly
 * douzifly@gmail.com
 */
package com.imtech.imshare.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.imtech.imshare.R;

/**
 * @author douzifly
 *
 */
public class AnimUtil {
    
    static Animation sFadeout;
    static Animation sScaleBig;
    
    public static void fadeOut(View v, AnimationListener l) {
        if (sFadeout == null) {
            sFadeout = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_out);
        }
        sFadeout.reset();
        sFadeout.setAnimationListener(l);
        v.startAnimation(sFadeout);
    }
    
    public static void scaleBig(View v, AnimationListener l) {
        if (sScaleBig == null) {
            sScaleBig = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_big);
        }
        
        sScaleBig.reset();
        sScaleBig.setAnimationListener(l);
        v.startAnimation(sScaleBig);
    }
}
