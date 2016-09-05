package com.beautiful_wallpapers_hd_qhd.activity.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.activity.dialog.InformationCardDialog;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.entity.FlickrImageEXIF;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.view.helper.ListViewHelper;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Igor on 01.09.2016.
 */
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_FLICKR_IMAGE_ID = "flickr_image_id";

    public PlaceholderFragment() {
    }

    /**
     * Returns a test_new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber, String flickrImageId) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_FLICKR_IMAGE_ID, flickrImageId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerAppComponent.builder().myModule(new MyModule(context)).build().inject(this);
    }

    @Inject FlickrAPI flickrAPI;
    @Inject FlickrDatabase flickrDB;

    private final String NO_INFORMATION = "No information";

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.content_card_preview, container, false);

        final TextView textView = (TextView) rootView.findViewById(R.id.inform_card_title);
        final ListView listView = (ListView) rootView.findViewById(R.id.information_lv);
        listView.setScrollContainer(false);

        final RelativeLayout viewMore = (RelativeLayout) rootView.findViewById(R.id.view_more_info);

        final ProgressBar informationLoadingPB = (ProgressBar)rootView.findViewById(R.id.information_loading_pb);
        final ListViewHelper listViewHelper = new ListViewHelper(getActivity());

        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                textView.setText(R.string.preview_author_label);
                flickrAPI.getPhotoInformation(FlickrHelper.METHOD_PHOTOS_GET_INFO, getArguments().getString(ARG_FLICKR_IMAGE_ID))
                        .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                        .map(photoInformation -> photoInformation.getPhoto())
                        .map(photo -> {
                            final Author author = new Author();
                            author.setNsid(photo.getOwner().getNsid());
                            author.setRealName(photo.getOwner().getRealName());
                            author.setUserName(photo.getOwner().getUserName());
                            author.setLocation(photo.getOwner().getLocation());
                            author.setLicenseNumber(Integer.parseInt(photo.getLicense()));

                            flickrAPI.getAuthorIcon(FlickrHelper.METHOD_PEOPLE_GET_INFO, photo.getOwner().getNsid())
                                    .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                                    .map(userIcon -> userIcon.getIcon())
                                    .subscribe(icon -> {
                                        String nsid = icon.getNsid(); String iconfarm = icon.getIconFarm(); String iconserver = icon.getIconServer();
                                        author.setUserAvatar(Integer.valueOf(iconserver) == 0 ? null : FlickrHelper.getUserAvatar(iconfarm, iconserver, nsid));

                                        if(flickrDB.getAuthor(nsid) == null){
                                            flickrDB.addAuthor(author);
                                        } else {
                                            flickrDB.updateAuthor(author);
                                        }
                                    });
                            return author;
                        })
                        .doOnCompleted(() -> informationLoadingPB.setVisibility(View.GONE))
                        .doOnError(e -> Log.d("myLog", e.fillInStackTrace().toString()))
                        .subscribe(
                                author -> {
                                    viewMore.setOnClickListener(getOnClickListener(rootView, InformationCardDialog.AUTHOR_INFORMATION, author));
                                    listView.setOnItemClickListener(listViewHelper.getAuthorOnClickListener(author));
                                    listViewHelper.setupAdapter(author.getIcons(), new String[]{}, author.toArray(), listView);
                                }//),
                                //e -> Log.d("RX-JaVa", "ERROR: "+e.getMessage())
                        );

                break;
            case 2:
                textView.setText(R.string.preview_image_label);
                Observable.zip(
                        flickrAPI.getImageEXIF(FlickrHelper.METHOD_GET_EXIF, getArguments().getString(ARG_FLICKR_IMAGE_ID))
                                .map(exif -> exif.getPhoto()),
                        flickrAPI.getPhotoSizes(FlickrHelper.METHOD_GET_PHOTO_SIZES, getArguments().getString(ARG_FLICKR_IMAGE_ID))
                                .map(sizes -> sizes.getSizes().getSizesArray().get(sizes.getSizes().getSizesArray().size() - 1).getResolution()),
                        (image, resolution) -> {
                            final FlickrImageEXIF imageData = new FlickrImageEXIF();
                            if(image != null){
                                String camera = image.getExif(FlickrHelper.EXIF_CAMERA);
                                String cameraModel = image.getExif(FlickrHelper.EXIF_CAMERA_MODEL);
                                imageData.setCamera(camera != null ? cameraModel.contains(camera) ? cameraModel : camera + " " + cameraModel : NO_INFORMATION);
                                imageData.setAperture(compare(image.getExif(FlickrHelper.EXIF_APERTURE)));
                                imageData.setFocalLength(compare(image.getExif(FlickrHelper.EXIF_FOCAL_LENGTH)));
                                imageData.setISO(compare(image.getExif(FlickrHelper.EXIF_ISO)));
                                imageData.setExposureTime(compare(image.getExif(FlickrHelper.EXIF_EXPOSURE_TIME)));
                                imageData.setWhiteBalance(compare(image.getExif(FlickrHelper.EXIF_WHITE_BALANCE)));
                                imageData.setMeteringMode(compare(image.getExif(FlickrHelper.EXIF_METERING_MODE)));
                                imageData.setExposureMode(compare(image.getExif(FlickrHelper.EXIF_EXPOSURE_MODE)));
                            } else {
                                imageData.setCamera(NO_INFORMATION);
                                imageData.setAperture(NO_INFORMATION);
                                imageData.setLocked(true);
                            }
                            imageData.setResolution(resolution);

                            return imageData;
                        })
                        .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                        .doOnCompleted(() -> informationLoadingPB.setVisibility(View.GONE))
                        .subscribe(
                                image -> {
                                    if(!image.isLocked()){
                                        listView.setOnItemClickListener(listViewHelper.getImageOnItemClickListener(image));
                                        viewMore.setOnClickListener(getOnClickListener(rootView, InformationCardDialog.IMAGE_INFORMATION, image));
                                    } else
                                        viewMore.setVisibility(View.GONE);
                                    //todo check getActivity()
                                    listViewHelper.setupAdapter(image.getIcons(), image.getEXIFTitles(getActivity()), image.toArray(), listView);
                                },
                                e -> Log.d("RX-JaVa", "ERROR: "+e.fillInStackTrace())
                        );
                break;
        }
        return rootView;
    }

    private String compare(String str){
        return str != null ? str : NO_INFORMATION;
    }

    private View.OnClickListener getOnClickListener(final View rootView, final int informNum, final Object object){
        return v -> {
            CardView card = (CardView) rootView.findViewById(R.id.information_card);
            Intent dialog = new Intent(getString(R.string.card_information_dialog));
            dialog.putExtra("inform_num", informNum);
            switch (informNum){
                case InformationCardDialog.AUTHOR_INFORMATION:
                    dialog.putExtra("author_obj", (Author)object);
                    break;
                case InformationCardDialog.IMAGE_INFORMATION:
                    dialog.putExtra("image_obj", (FlickrImageEXIF)object);
                    break;
            }
            new AnimationController(getContext()).transition(card, getString(R.string.transition_card), dialog);
        };
    }
}
