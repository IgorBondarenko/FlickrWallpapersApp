package com.beautiful_wallpapers_hd_qhd.core.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.controller.animation.AnimationEndListener;

/**
 * Created by Igor on 28.11.2015.
 */
public class AnimationController {

    private Context mContext;

    public AnimationController(Context c){
        this.mContext = c;
    }

    public void replace(int slideOutRes, final int slideInRes, final View targetView, final View replacementView){
        Animation slideOut = getAnimation(slideOutRes);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation slideIn = getAnimation(slideInRes);
                slideIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        targetView.setVisibility(View.GONE);
                        replacementView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                replacementView.startAnimation(slideIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        targetView.startAnimation(slideOut);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void transition(View targetView, String transitionName, Intent intent){
        ActivityOptionsCompat options;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)mContext, targetView, transitionName);
        } else{
            options = ActivityOptionsCompat.makeScaleUpAnimation(targetView, (int)targetView.getX(), (int)targetView.getY(), targetView.getWidth(), targetView.getHeight());
        }
        mContext.startActivity(intent, options.toBundle());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void transition(Intent intent, Pair<View, String>... sharedElements){
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)mContext, sharedElements);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            mContext.startActivity(intent, options.toBundle());
        } else {
            mContext.startActivity(intent);
        }
    }

    public void zoomCenter(View targetView, final Intent intent){
        Animation anim_pressed = AnimationUtils.loadAnimation(mContext, R.anim.pressed);
        anim_pressed.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Activity activity = (Activity) mContext;
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        targetView.startAnimation(anim_pressed);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void circularReveal(final View circularView, final View backgroundView, final int backgroundColor) {
        int cx = (circularView.getLeft() + circularView.getRight()) / 2;
        int cy = (circularView.getTop() + circularView.getBottom()) / 2;
        int finalRadius = circularView.getWidth();
        Animator anim = ViewAnimationUtils.createCircularReveal(circularView, cx, cy, 0, finalRadius);
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                circularView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                circularView.setVisibility(View.GONE);
                backgroundView.setBackgroundColor(mContext.getResources().getColor(backgroundColor));
            }
        });
        anim.setDuration(1000);
        anim.start();
    }

    public Animation getAnimation(int animRes){
        return AnimationUtils.loadAnimation(mContext, animRes);
    }

    public Animation getAnimation(int animRes, Animation.AnimationListener animListener){
        Animation anim = getAnimation(animRes);
        anim.setAnimationListener(animListener);
        return anim;
    }

    public Animation.AnimationListener getOnAnimationEndListener(final AnimationEndListener animationEndListener){
        return new Animation.AnimationListener(){

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationEndListener.onEnd();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
    }

    public static class BaseAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
