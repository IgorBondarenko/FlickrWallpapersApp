package com.beautiful_wallpapers_hd_qhd.activity.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.receiver.updater.UpdateWallpapersReceiver;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Igor on 03.02.2015.
 */
public class AutoUpdateDialog extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener{

    private UpdateWallpapersReceiver updateWallpapersReceiver = new UpdateWallpapersReceiver();
    private int intervalIndex = 0;

    private boolean state;
    @Inject SharedPreferencesController sPref;
    @Inject FlickrDatabase flickrDB;

    @BindView(R.id.auto_hint) LinearLayout hintLayout;
    @BindView(R.id.auto_update_sb) SeekBar seekBar;
    @BindView(R.id.auto_sb_text) TextView sbTextView;
    @BindView(R.id.auto_update_state_on) TextView stateOnTextView;
    @BindView(R.id.auto_update_state_off) TextView stateOffTextView;
    @BindView(R.id.auto_start_btn) Button startBtn;
    @BindView(R.id.auto_stop_btn) Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_auto_update);

        DaggerAppComponent.builder().myModule(new MyModule(this)).build().inject(this);
        ButterKnife.bind(this);

        state = sPref.getBool(SharedPreferencesController.SP_WUS_STATE, false);

        seekBar.setOnSeekBarChangeListener(this);
        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        findViewById(R.id.auto_restart_btn).setOnClickListener(this);
        findViewById(R.id.auto_cancel_btn).setOnClickListener(this);

        controlLayout = (LinearLayout)findViewById(R.id.service_control_btn_layout);
        restartLayout = (LinearLayout)findViewById(R.id.service_restart_btn_layout);

        if(state){
            setState(View.GONE, View.VISIBLE, "#ff51a9ee", "#ff747474");
        } else {
            setState(View.VISIBLE, View.GONE, "#ff747474", "#ff51a9ee");
        }

        intervalIndex = sPref.getInt(SharedPreferencesController.SP_WUS_INTERVAL, 0);
        seekBar.setProgress(intervalIndex);
        setIntervalTitle(intervalIndex);
    }

    public void hint(View v){
        if(hintLayout.getVisibility() == View.GONE){
            hintLayout.setVisibility(View.VISIBLE);
        } else{
            hintLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setIntervalTitle(progress);
    }

    private void setIntervalTitle(int progress){
        switch (progress){
            case 0:
                setInterval(R.string.auto_update_sb_title1, progress);
                break;
            case 1:
                setInterval(R.string.auto_update_sb_title2, progress);
                break;
            case 2:
                setInterval(R.string.auto_update_sb_title3, progress);
                break;
            case 3:
                setInterval(R.string.auto_update_sb_title4, progress);
                break;
            case 4:
                setInterval(R.string.auto_update_sb_title5, progress);
                break;
        }
    }

    private void setState(int visibilityOff, int visibilityOn, String stopBtnColor, String startBtnColor){
        stateOffTextView.setVisibility(visibilityOff);
        stateOnTextView.setVisibility(visibilityOn);
        stopBtn.setTextColor(Color.parseColor(stopBtnColor));
        startBtn.setTextColor(Color.parseColor(startBtnColor));
    }

    private void setInterval(int title, int interval){
        sbTextView.setText(title);
        this.intervalIndex = interval;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    private boolean restart = false;
    private AnimationController animControl = new AnimationController(this);
    private LinearLayout controlLayout;
    private LinearLayout restartLayout;

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(!restart && state){
            restart = true;
            animControl.replace(R.anim.slide_out_right, android.R.anim.slide_in_left, controlLayout, restartLayout);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.auto_start_btn:
                start(R.string.auto_update_start_success);
                break;
            case R.id.auto_stop_btn:
                stop(true);
                break;
            case R.id.auto_restart_btn:
                stop(false);
                start(R.string.auto_update_restart_success);
                break;
            case R.id.auto_cancel_btn:
                animControl.replace(R.anim.slide_out_left, R.anim.slide_in_right, restartLayout, controlLayout);
                sPref.setInt(SharedPreferencesController.SP_WUS_COUNT, 0);
                restart = false;
                seekBar.setProgress(sPref.getInt(SharedPreferencesController.SP_WUS_INTERVAL, 0));
                break;
        }
    }

    public void start(int successToast){
        if(flickrDB.getFavourites(FlickrDatabase.FAVOURITE_PHOTO).size() > 0){
            if(!state){
                updateWallpapersReceiver.startUpdateWallpapersService(getBaseContext());
                state = true;
                sPref.setBool(SharedPreferencesController.SP_WUS_STATE, true);
                sPref.setInt(SharedPreferencesController.SP_WUS_INTERVAL, intervalIndex);
                Toast.makeText(getBaseContext(), successToast, Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(getBaseContext(), R.string.auto_update_already_run, Toast.LENGTH_LONG).show();
            }
        } else{
            Toast.makeText(getBaseContext(), R.string.auto_update_empty_favorites, Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void stop(boolean showToast){
        if(state) {
            updateWallpapersReceiver.cancelUpdateWallpapersService(getBaseContext());
            state = false;
            sPref.setInt(SharedPreferencesController.SP_WUS_INTERVAL, 0);
            sPref.setBool(SharedPreferencesController.SP_WUS_STATE, false);
            if(showToast){
                Toast.makeText(getBaseContext(), R.string.auto_update_stop_success, Toast.LENGTH_LONG).show();
            }
        } else{
            Toast.makeText(getBaseContext(), R.string.auto_update_already_stoped, Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
