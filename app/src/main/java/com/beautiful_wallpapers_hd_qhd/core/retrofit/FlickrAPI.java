package com.beautiful_wallpapers_hd_qhd.core.retrofit;

import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.ImageEXIF;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.UserIcon;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotoInformation;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotoSizes;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotosObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Igor on 01.07.2016.
 */
public interface FlickrAPI  {

    @GET("/services/rest/?api_key=" + FlickrHelper.API_KEY + "&group_id="+ FlickrHelper.GROUP_ID +"&format=json&nojsoncallback=1")
    Observable<PhotosObject> getPhotosInGroup(@Query("method") String method);

    @GET("/services/rest/?api_key=" + FlickrHelper.API_KEY + "&group_id="+ FlickrHelper.GROUP_ID +"&format=json&nojsoncallback=1")
    Observable<PhotosObject> getPhotosInGroupByText(@Query("method") String method, @Query("text") String text);

    @GET("/services/rest/?api_key=" + FlickrHelper.API_KEY + "&group_id="+ FlickrHelper.GROUP_ID +"&format=json&nojsoncallback=1")
    Observable<PhotosObject> getPhotosInGroupByTags(@Query("method") String method, @Query("tags") String tags);

    @GET("/services/rest/?api_key=" + FlickrHelper.API_KEY + "&group_id="+ FlickrHelper.GROUP_ID +"&format=json&nojsoncallback=1")
    Observable<PhotoSizes> getPhotoSizes(@Query("method") String method, @Query(FlickrHelper.ARG_GET_PHOTO_ID) String photoId);

    @GET("/services/rest/?api_key=" + FlickrHelper.API_KEY + "&group_id="+ FlickrHelper.GROUP_ID +"&format=json&nojsoncallback=1")
    Observable<PhotoInformation> getPhotoInformation(@Query("method") String method, @Query(FlickrHelper.ARG_GET_PHOTO_ID) String photoId);

    @GET("/services/rest/?api_key=" + FlickrHelper.API_KEY + "&group_id="+ FlickrHelper.GROUP_ID +"&format=json&nojsoncallback=1")
    Observable<UserIcon> getAuthorIcon(@Query("method") String method, @Query(FlickrHelper.ARG_USER_ID) String userId);

    @GET("/services/rest/?api_key=" + FlickrHelper.API_KEY + "&group_id="+ FlickrHelper.GROUP_ID +"&format=json&nojsoncallback=1")
    Observable<ImageEXIF> getImageEXIF(@Query("method") String method, @Query(FlickrHelper.ARG_GET_PHOTO_ID) String photoId);

}
