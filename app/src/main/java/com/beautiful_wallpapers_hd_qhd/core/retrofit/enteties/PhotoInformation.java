package com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 03.07.2016.
 */
public class PhotoInformation {

    @SerializedName("photo")
    private Photo photo;

    public Photo getPhoto() {
        return photo;
    }

    public class Photo{

        @SerializedName("owner")
        private Owner owner;
        @SerializedName("license")
        private String license;
        @SerializedName("tags")
        private Tags tags;

        public Owner getOwner() {
            return owner;
        }

        public String getLicense() {
            return license;
        }

        public List<Tags.Tag> getTags() {
            return tags.getTagsList();
        }

        public class Owner{

            @SerializedName("nsid")
            private String nsid;
            @SerializedName("realname")
            private String realName;
            @SerializedName("username")
            private String userName;
            @SerializedName("location")
            private String location;

            public String getNsid() {
                return nsid;
            }

            public String getRealName() {
                return realName;
            }

            public String getUserName() {
                return userName;
            }

            public String getLocation() {
                return location;
            }
        }

        public class Tags{

            @SerializedName("tag")
            private List<Tag> tagsList;

            public List<Tag> getTagsList() {
                return tagsList;
            }

            public class Tag{

                @SerializedName("raw")
                private String tag;

                public String getTag() {
                    return tag;
                }
            }
        }
    }
}
