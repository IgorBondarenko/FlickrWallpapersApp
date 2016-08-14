package com.beautiful_wallpapers_hd_qhd.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.Advertising;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Igor on 15.03.2016.
 */
public class AuthorWebProfileActivity extends AppCompatActivity {

    @BindView(R.id.browser) WebView mBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_web_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        mBrowser.getSettings().setJavaScriptEnabled(true);
        mBrowser.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mBrowser.loadUrl(FlickrHelper.FLICKR_USER_HP + getIntent().getStringExtra("author_nsid"));

        Log.d("myLog", FlickrHelper.FLICKR_USER_HP + getIntent().getStringExtra("author_nsid"));

        if(!new SharedPreferencesController(this).getBool(SharedPreferencesController.SP_PRO_VERSION, false)){
            new Advertising(this).loadSmartBanner((AdView) findViewById(R.id.author_profile_ad_view));
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(mBrowser.canGoBack()){
            mBrowser.goBack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
