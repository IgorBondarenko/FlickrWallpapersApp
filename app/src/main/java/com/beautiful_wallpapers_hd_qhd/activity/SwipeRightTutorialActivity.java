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
public class SwipeRightTutorialActivity extends Activity {

    @BindView(R.id.tutorial_swipe_right_iv) ImageView swipeRightIV;
    @BindView(R.id.got_it_right_btn) Button gotItBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tutorial_swipe_right);
        ButterKnife.bind(this);

        float x = getIntent().getFloatExtra("swipe_x", 0);
        float y = getIntent().getFloatExtra("swipe_y", 0);
        Log.d("myLog", "x="+x + " y="+y);

        swipeRightIV.setX(x);
        swipeRightIV.setY(y);

        gotItBtn.setOnClickListener(v -> finish());
    }
}
