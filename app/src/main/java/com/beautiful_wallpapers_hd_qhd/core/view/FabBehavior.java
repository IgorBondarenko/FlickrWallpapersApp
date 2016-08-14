package com.beautiful_wallpapers_hd_qhd.core.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;

/**
 * Created by Igor on 14.08.2016.
 */
public class FabBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    private AnimationController mAnimationController;

    public FabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAnimationController = new AnimationController(context);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof ResizablePortraitImageView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {

        if(child.getVisibility() == View.VISIBLE){
            if(child.getTop() < 120){
                mAnimationController.hide(child, R.anim.zoom_out);
            }
        }

        if(child.getVisibility() == View.INVISIBLE){
            if(child.getTop() > 200){
                mAnimationController.show(child, R.anim.zoom_in);
            }
        }

        return super.onDependentViewChanged(parent, child, dependency);
    }
}
