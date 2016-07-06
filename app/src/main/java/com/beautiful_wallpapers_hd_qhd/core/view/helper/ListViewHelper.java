package com.beautiful_wallpapers_hd_qhd.core.view.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.License;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.entity.FlickrImageEXIF;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Igor on 19.04.2016.
 */
public class ListViewHelper {

    private final static String GOOGLE_SEARCH_URL = "https://www.google.com/search?q=";
    private final static String NO_INFORMATION = "No information";

    private Context mContext;

    public ListViewHelper(Context context){
        this.mContext = context;
    }

    public AdapterView.OnItemClickListener getAuthorOnClickListener(final Author author){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 1:
                        Intent browser = new Intent(mContext.getResources().getString(R.string.author_web_profile_activity));
                        browser.putExtra("author_nsid", author.getNsid());
                        mContext.startActivity(browser);
                        break;
                    case 2:
                        Intent authorPageIntent = new Intent(mContext.getResources().getString(R.string.author_page_activity));
                        authorPageIntent.putExtra(mContext.getResources().getString(R.string.flickr_author_id), author.getNsid());
                        mContext.startActivity(authorPageIntent);
                        break;
                    case 3:
                        if (!author.getLocation().equals(NO_INFORMATION)) {
                            openLink("geo:0,0?q=" + author.getLocation());
                        }
                        break;
                    case 4:
                        openLink(FlickrHelper.FLICKR_USER_SEND_MAIL + author.getNsid());
                        break;
                    case 5:
                        if(author.getLicenseNumber() != 0){
                            openLink(new License(author.getLicenseNumber()).getUrl());
                        }
                        break;
                }
            }
        };
    }

    public AdapterView.OnItemClickListener getImageOnItemClickListener(final FlickrImageEXIF image){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!image.getCamera().equals(NO_INFORMATION) && i == 0){
                    openLink(GOOGLE_SEARCH_URL + image.getCamera());
                }
            }
        };
    }

    public void setupAdapter(int[] icons, String[] titles, CharSequence[] inform, ListView listView){
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(3);

        addData(0, icons.length, icons, null, inform, data);
        addData(icons.length, inform.length, null, titles, inform, data);

        String[] from = {"icon", "title", "value"};
        int[] to = { R.id.image_inform_iv, R.id.image_inform_title_tv, R.id.image_inform_tv};
        SimpleAdapter adapter = new SimpleAdapter(mContext, data, R.layout.image_information_layout, from, to);
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                switch (view.getId()){
                    case R.id.image_inform_tv:
                        ((TextView)view).setText(textRepresentation);
                        if(textRepresentation.contains("http://")){
                            ((TextView)view).setTextColor(Color.BLUE);
                            ((TextView)view).setPaintFlags(((TextView)view).getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        } else {
                            ((TextView)view).setTextColor(Color.BLACK);
                            ((TextView)view).setPaintFlags(Paint.ANTI_ALIAS_FLAG);
                        }
                        return true;
                }
                return false;
            }
        });
        listView.setAdapter(adapter);
    }

    private void addData(int first, int last, int[] icons, String[] titles, CharSequence[] inform, ArrayList<Map<String, Object>> data){
        Map<String, Object> m;
        for (int i = first; i < last; i++) {
            m = new HashMap<String, Object>();
            m.put("icon", icons != null ? icons[i] : null);
            m.put("title", titles != null ? titles[i] : "");
            m.put("value", inform[i]);
            data.add(m);
        }
    }

    private void openLink(String url){
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

}
