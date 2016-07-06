package com.beautiful_wallpapers_hd_qhd.activity.dialog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.view.helper.ListViewHelper;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.entity.FlickrImageEXIF;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Igor on 14.04.2016.
 */
public class InformationCardDialog extends AppCompatActivity implements OnClickListener{

    public static final int AUTHOR_INFORMATION = 0;
    public static final int IMAGE_INFORMATION = 1;

    @BindView(R.id.inform_card_title) TextView mTitleTextView;
    @BindView(R.id.information_lw) ListView mListView;
    private ListViewHelper listViewHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_information_card);

        ButterKnife.bind(this);
        listViewHelper = new ListViewHelper(this);

        switch (getIntent().getIntExtra("inform_num", 0)){
            case AUTHOR_INFORMATION:
                setAuthorAdapter((Author) getIntent().getParcelableExtra("author_obj"));
                break;
            case IMAGE_INFORMATION:
                setImageAdapter((FlickrImageEXIF)getIntent().getParcelableExtra("image_obj"));
                break;
        }

        RelativeLayout hideInformation = (RelativeLayout)findViewById(R.id.hide_info);
        hideInformation.setOnClickListener(this);

    }

    private void setAuthorAdapter(final Author author){
        mTitleTextView.setText(R.string.preview_author_label);
        mListView.setOnItemClickListener(listViewHelper.getAuthorOnClickListener(author));
        listViewHelper.setupAdapter(author.getIcons(), new String[]{}, author.toArray(), mListView);
    }

    private void setImageAdapter(final FlickrImageEXIF image){
        mTitleTextView.setText(R.string.preview_image_label);
        mListView.setOnItemClickListener(listViewHelper.getImageOnItemClickListener(image));
        listViewHelper.setupAdapter(image.getIcons(), image.getEXIFTitles(this), image.toArray(), mListView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hide_info:
                onBackPressed();
                break;
        }
    }

    enum Information{
        AUTHOR, IMAGE
    }
}
