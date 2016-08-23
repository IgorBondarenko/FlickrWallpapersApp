package com.beautiful_wallpapers_hd_qhd.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.beautiful_wallpapers_hd_qhd.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Igor on 22.08.2016.
 */
public class SwipeLeftTutorialActivity extends Activity {

    @BindView(R.id.tutorial_swipe_left_iv) ImageView swipeLeftIV;
    @BindView(R.id.got_it_left_btn) Button gotItBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tutorial_swipe_left);
        ButterKnife.bind(this);

        float x = getIntent().getFloatExtra("swipe_x", 0);
        float y = getIntent().getFloatExtra("swipe_y", 0);

        swipeLeftIV.setX(x);
        swipeLeftIV.setY(y);

        gotItBtn.setOnClickListener(v -> finish());
    }
}
