package com.beautiful_wallpapers_hd_qhd.activity.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.beautiful_wallpapers_hd_qhd.R;

/**
 * Created by Igor on 07.12.2015.
 */
public class ExitDialog extends Activity implements View.OnClickListener {

    public static final int EXIT_TRUE = 1;
    public static final int EXIT_FALSE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_exit);

        TextView title = (TextView) this.findViewById(android.R.id.title);
        TextView title_old = (TextView) this.findViewById(R.id.dialog_exit_tv);
        if(title != null){
            title.setSingleLine(false);
        }else {
            title_old.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.confirm_quit_btn).setOnClickListener(this);
        findViewById(R.id.cancel_quit_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirm_quit_btn:
                setResult(EXIT_TRUE);
                finish();
                break;
            case R.id.cancel_quit_btn:
                setResult(EXIT_FALSE);
                finish();
                break;
        }
    }
}
