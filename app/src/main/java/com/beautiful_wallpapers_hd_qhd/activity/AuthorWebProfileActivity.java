package com.beautiful_wallpapers_hd_qhd.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.Advertising;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.google.android.gms.ads.AdView;

/**
 * Created by Igor on 15.03.2016.
 */
public class AuthorWebProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_web_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView browser = (WebView)findViewById(R.id.browser);
        browser.loadUrl(FlickrHelper.FLICKR_USER_HP + getIntent().getStringExtra("author_nsid"));

        boolean IS_PRO = false;
        new Advertising(this, IS_PRO).loadSmartBanner((AdView) findViewById(R.id.author_profile_ad_view));
    }
}
