package com.beautiful_wallpapers_hd_qhd.core.retrofit;

import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.ImageEXIF;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.UserIcon;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotoInformation;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotoSizes;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotosObject;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Igor on 01.07.2016.
 */
public interface FlickrAPI  {

    String GET_ARGS = "/services/rest/?api_key=" + FlickrHelper.API_KEY +
            "&" + FlickrHelper.ARG_GET_GROUP_ID + "=" + FlickrHelper.GROUP_ID +
            "&format=json&nojsoncallback=1";

    @GET(GET_ARGS)
    Observable<PhotosObject> getPhotosInGroup(@Query(FlickrHelper.ARG_METHOD) String method);

    @GET(GET_ARGS)
    Observable<PhotosObject> getPhotosInGroupByText(@Query(FlickrHelper.ARG_METHOD) String method, @Query(FlickrHelper.ARG_TEXT) String text);

    @GET(GET_ARGS)
    Observable<PhotosObject> getPhotosInGroupByTags(@Query(FlickrHelper.ARG_METHOD) String method, @Query(FlickrHelper.ARG_TAGS) String tags);

    @GET(GET_ARGS)
    Observable<PhotoSizes> getPhotoSizes(@Query(FlickrHelper.ARG_METHOD) String method, @Query(FlickrHelper.ARG_GET_PHOTO_ID) String photoId);

    @GET(GET_ARGS)
    Observable<PhotoInformation> getPhotoInformation(@Query(FlickrHelper.ARG_METHOD) String method, @Query(FlickrHelper.ARG_GET_PHOTO_ID) String photoId);

    @GET(GET_ARGS)
    Observable<UserIcon> getAuthorIcon(@Query(FlickrHelper.ARG_METHOD) String method, @Query(FlickrHelper.ARG_USER_ID) String userId);

    @GET(GET_ARGS)
    Observable<ImageEXIF> getImageEXIF(@Query(FlickrHelper.ARG_METHOD) String method, @Query(FlickrHelper.ARG_GET_PHOTO_ID) String photoId);

    @GET(GET_ARGS)
    Observable<PhotosObject> getPhotosByAuthors(@Query(FlickrHelper.ARG_METHOD) String method, @Query(FlickrHelper.ARG_USER_ID) String userId);

}
