package com.beautiful_wallpapers_hd_qhd.core.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Igor on 01.11.2014.
 */
public class ResizableLandscapeImageView extends ImageView {

    public ResizableLandscapeImageView(Context context) {
        super(context);
    }

    public ResizableLandscapeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizableLandscapeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Drawable d = getDrawable();
        if(d != null){
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int width = (int) Math.ceil((float) height * (float) d.getIntrinsicWidth() / (float) d.getIntrinsicHeight());
            setMeasuredDimension(width, height);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
