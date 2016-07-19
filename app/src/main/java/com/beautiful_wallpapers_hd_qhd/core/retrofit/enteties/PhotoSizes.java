package com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 03.07.2016.
 */
public class PhotoSizes {

    @SerializedName("sizes")
    private SizesArray sizes;

    public SizesArray getSizes() {
        return sizes;
    }

    public static class SizesArray{

        @SerializedName("size")
        private List<Size> sizesArray;

        public List<Size> getSizesArray() {
            return sizesArray;
        }

        public class Size{

            @SerializedName("source")
            private String size;
            @SerializedName("width")
            private String height;
            @SerializedName("height")
            private String width;

            public String getSize() {
                return size;
            }

            public String getResolution() {
                return width+ "x" + height;
            }
        }

    }
}
