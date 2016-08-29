package com.beautiful_wallpapers_hd_qhd.core.di;

import com.beautiful_wallpapers_hd_qhd.activity.AuthorPageActivity;
import com.beautiful_wallpapers_hd_qhd.activity.CropImageActivity;
import com.beautiful_wallpapers_hd_qhd.activity.MainActivity;
import com.beautiful_wallpapers_hd_qhd.activity.PreviewActivity;
import com.beautiful_wallpapers_hd_qhd.activity.ScalingImageActivity;
import com.beautiful_wallpapers_hd_qhd.activity.dialog.AutoUpdateDialog;
import com.beautiful_wallpapers_hd_qhd.activity.dialog.BuyProDialog;
import com.beautiful_wallpapers_hd_qhd.core.FirebaseAnalytic;
import com.beautiful_wallpapers_hd_qhd.core.adapter.AuthorAdapter;
import com.beautiful_wallpapers_hd_qhd.core.adapter.ImageRecyclerAdapter;
import com.beautiful_wallpapers_hd_qhd.core.service.UpdateWallpapersService;

import dagger.Component;

/**
 * Created by Igor on 28.06.2016.
 */

@Component(modules = MyModule.class)
public interface AppComponent {

    void inject(MainActivity activity);
    void inject(PreviewActivity activity);
    void inject(CropImageActivity activity);
    void inject(AutoUpdateDialog activity);
    void inject(AuthorPageActivity activity);
    void inject(ScalingImageActivity activity);
    void inject(ImageRecyclerAdapter adapter);
    void inject(AuthorAdapter adapter);
    void inject(FirebaseAnalytic component);
    void inject(BuyProDialog dialog);
    void inject(UpdateWallpapersService service);

}
