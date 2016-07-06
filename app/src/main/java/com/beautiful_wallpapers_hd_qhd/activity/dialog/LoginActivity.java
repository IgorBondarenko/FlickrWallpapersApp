package com.beautiful_wallpapers_hd_qhd.activity.dialog;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.google.android.gms.common.AccountPicker;

/**
 * Created by Igor on 14.06.2016.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_ACCOUNT = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ((Button)(findViewById(R.id.login_button))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                Log.d("MyTag", "type: " + data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
                Log.d("MyTag", "name: " + data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                Log.d("MyTag", "type: " + data.getStringExtra(AccountManager.KEY_PASSWORD));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
            }
        }
    }
}
