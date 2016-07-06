package com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties;

import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 04.07.2016.
 */
public class ImageEXIF {

    @SerializedName("photo")
    private Photo photo;

    public Photo getPhoto() {
        return photo;
    }

    public class Photo{

        private final String NO_INFORMATION = "No information";

        @SerializedName("exif")
        private List<EXIF> exif;

        public String getExif(String tag) {
            for (EXIF e : exif) {
                if(e.getTag().equals(tag)){
                    return e.getValue();
                }
            }
            return null;
        }

        public class EXIF{

            @SerializedName("tag")
            private String tag;

            @SerializedName("raw")
            private EXIFValue value;

            public String getTag() {
                return tag;
            }

            public String getValue() {
                return value.getValue();
            }

            private class EXIFValue{

                @SerializedName("_content")
                private String value;

                public String getValue() {
                    return value;
                }
            }

        }

    }

}
