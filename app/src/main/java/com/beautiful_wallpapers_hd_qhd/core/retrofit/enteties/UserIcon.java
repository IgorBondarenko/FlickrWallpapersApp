package com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 04.07.2016.
 */
public class UserIcon {

    @SerializedName("person")
    private Icon icon;

    public UserIcon(Icon icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        return icon;
    }

    public class Icon{

        @SerializedName("nsid")
        private String nsid;
        @SerializedName("iconfarm")
        private String iconFarm;
        @SerializedName("iconserver")
        private String iconServer;

        public Icon(String nsid, String iconFarm, String iconServer) {
            this.nsid = nsid;
            this.iconFarm = iconFarm;
            this.iconServer = iconServer;
        }

        public String getNsid() {
            return nsid;
        }

        public String getIconFarm() {
            return iconFarm;
        }

        public String getIconServer() {
            return iconServer;
        }
    }

}
