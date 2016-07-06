package com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 02.07.2016.
 */
public class PhotosObject {

    @SerializedName("photos")
    private PhotosArray photos;

    public PhotosArray getPhotos() {
        return photos;
    }

    public class PhotosArray {

        @SerializedName("photo")
        private List<Photo> photo;

        public List<Photo> getPhoto() {
            return photo;
        }

        public class Photo{

            @SerializedName("id")
            private String id;

            public String getId() {
                return id;
            }

        }

    }

}
