package com.beautiful_wallpapers_hd_qhd.activity;

import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.activity.dialog.ExitDialog;
import com.beautiful_wallpapers_hd_qhd.core.Device;
import com.beautiful_wallpapers_hd_qhd.core.adapter.AuthorAdapter;
import com.beautiful_wallpapers_hd_qhd.core.adapter.ImageRecyclerAdapter;
import com.beautiful_wallpapers_hd_qhd.core.billing.InAppConfig;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.receiver.notification.NotificationReceiver;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotosObject;
import com.google.android.gms.common.AccountPicker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE_EXIT = 9000;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 9001;

    private static int mCurrentSelectedPosition = R.id.nav_category_all;
    private static int mTitle = 0;
    private static String mCategory = "all";

    private List<String> mCurrentFlickrImages = new ArrayList<>();
    private List<String> flickrImageIds = new ArrayList<>();
    private List<String> flickrAuthorIds = new ArrayList<>();

    @BindView(R.id.images_grid_view) RecyclerView mGridViewImages;
    @BindView(R.id.authors_grid_view) GridView mGridViewAuthors;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    private ImageRecyclerAdapter mImageAdapter;

    @Inject AnimationController animationController;
    @Inject FlickrAPI flickrAPI;
    @Inject FlickrDatabase flickrDB;
    @Inject SharedPreferencesController sPref;
    @Inject OpenIabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DaggerAppComponent.builder().myModule(new MyModule(this)).build().inject(this);
        ButterKnife.bind(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        new NotificationReceiver().start(this);

        String tag = getIntent().getStringExtra("tag");
        if(tag != null){
            initImageAdapter(flickrImageIds, "tagSearch");

            //// TODO: 02.07.2016 new
            //search(FlickrHelper.ARG_TEXT, tag);
            updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, tag));

            setTitle("#"+tag);
        } else {
            if(mTitle != 0){
                setTitle(mTitle);
            }
            navigationView.getMenu().findItem(mCurrentSelectedPosition).setChecked(true);
            onNavigationItemSelected(navigationView.getMenu().findItem(mCurrentSelectedPosition));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivityForResult(new Intent(getString(R.string.exit_app_dialog)), REQUEST_CODE_EXIT);
        }
    }

    private String mAccountEmail;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_EXIT:
                if(resultCode == ExitDialog.EXIT_TRUE){
                    ImageLoader.getInstance().getDiskCache().clear();
                    new FlickrDatabase(this).closeConnection();
                    finish();
                }
                break;
            case REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    mAccountEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    //initializePurchases();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void initializePurchases(){
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    showToast("Problem setting up in-app billing: " + result);
                    return;
                }
                if (mHelper == null) return;
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                showToast("Failed to query inventory: " + result);
                return;
            }
            Purchase premiumPurchase = inventory.getPurchase(InAppConfig.SKU_PRO_VERSION);
            if(!sPref.getBool(SharedPreferencesController.SP_PRO_VERSION, false)){
                sPref.setBool(SharedPreferencesController.SP_PRO_VERSION, (premiumPurchase != null && premiumPurchase.getDeveloperPayload().equals(mAccountEmail)));
            } else {
                showToast("You haven't bought PRO-version");
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                mCurrentFlickrImages.clear();
                mCurrentFlickrImages.addAll(flickrImageIds);

                final SearchManager manager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = getSearchView(item, manager);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        initImageAdapter(flickrImageIds, "search");
                        // TODO: 02.07.2016
                        //search(FlickrHelper.ARG_TEXT, query);
                        updateAdapter(flickrAPI.getPhotosInGroupByText(FlickrHelper.METHOD_SEARCH, query));
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
                MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        initImageAdapter(mCurrentFlickrImages, mCategory);
                        mProgressBar.setVisibility(View.GONE);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }
                });
                return true;
            case R.id.action_set_auto_update:
                if(sPref.getBool(SharedPreferencesController.SP_PRO_VERSION, false)){
                    startActivity(new Intent(getString(R.string.auto_update_activity)));
                } else {
                    startActivity(new Intent(getString(R.string.buy_pro_dialog)));
                }
                return true;
            case R.id.action_restore_purchase:
                signIn();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signIn() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private SearchView getSearchView(MenuItem item, SearchManager searchManager){
        SearchView search = (SearchView) item.getActionView();
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return search;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()){
            case R.id.nav_category_all:
                initImageAdapter(flickrImageIds, "all");

                //// TODO: 02.07.2016 new
                //updateFlickrImageAdapter();
                updateAdapter(flickrAPI.getPhotosInGroup(FlickrHelper.METHOD_GET_PHOTOS_BY_GROUP));

                saveCurrent(R.id.nav_category_all, R.string.title_section1);
                break;
            case R.id.nav_category_fav:
                flickrImageIds = flickrDB.getFavourites(FlickrDatabase.FAVOURITE_PHOTO);
                initImageAdapter(flickrImageIds, "mFavouriteImage");
                mProgressBar.setVisibility(View.GONE);
                saveCurrent(R.id.nav_category_fav, R.string.title_section3);
                break;
            case R.id.nav_category_subscriptions:
                mProgressBar.setVisibility(View.GONE);
                mGridViewImages.setVisibility(View.GONE);
                mGridViewAuthors.setVisibility(View.VISIBLE);
                flickrAuthorIds = flickrDB.getFavourites(FlickrDatabase.FAVOURITE_AUTHOR);
                AuthorAdapter mAuthorAdapter = new AuthorAdapter(this, flickrAuthorIds);
                mGridViewAuthors.setAdapter(mAuthorAdapter);
                mGridViewAuthors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent authorPageIntent = new Intent(getResources().getString(R.string.author_page_activity));
                        authorPageIntent.putExtra(getResources().getString(R.string.flickr_author_id), flickrAuthorIds.get(position));
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                            animationController.transition(view.findViewById(R.id.sub_profile_image), "transition_author_image", authorPageIntent);
                        } else {
                            startActivity(authorPageIntent);
                        }
                    }
                });
                saveCurrent(R.id.nav_category_subscriptions, R.string.title_section23);
                break;
            case R.id.nav_category_landscape:
                initImageAdapter(flickrImageIds, "landscape");
                // TODO: 02.07.2016
//                search(FlickrHelper.ARG_TEXT, "WHD_landscape");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_landscape"));

                saveCurrent(R.id.nav_category_landscape, R.string.title_section5);
                break;
            case R.id.nav_category_buildings:
                initImageAdapter(flickrImageIds, "building");
//                search(FlickrHelper.ARG_TEXT, "WHD_building");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_building"));

                saveCurrent(R.id.nav_category_buildings, R.string.title_section6);
                break;
            case R.id.nav_category_castles:
                initImageAdapter(flickrImageIds, "castle");
//                search(FlickrHelper.ARG_TEXT, "WHD_castle");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_castle"));

                saveCurrent(R.id.nav_category_castles, R.string.title_section7);
                break;
            case R.id.nav_category_sea:
                initImageAdapter(flickrImageIds, "sea");
//                search(FlickrHelper.ARG_TEXT, "WHD_sea");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_sea"));

                saveCurrent(R.id.nav_category_sea, R.string.title_section8);
                break;
            case R.id.nav_category_textures:
                initImageAdapter(flickrImageIds, "texture");
//                search(FlickrHelper.ARG_TEXT, "WHD_texture");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_texture"));

                saveCurrent(R.id.nav_category_textures, R.string.title_section9);
                break;
            case R.id.nav_category_flowers:
                initImageAdapter(flickrImageIds, "flower");
//                search(FlickrHelper.ARG_TEXT, "WHD_flower");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_flower"));

                saveCurrent(R.id.nav_category_flowers, R.string.title_section10);
                break;
            case R.id.nav_category_other:
                initImageAdapter(flickrImageIds, "other");
//                search(FlickrHelper.ARG_TEXT, "WHD_other");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_other"));

                saveCurrent(R.id.nav_category_other, R.string.title_section22);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void saveCurrent(int position, int titleRes){
        mCurrentSelectedPosition = position;
        mTitle = titleRes;
        setTitle(mTitle);
    }

    private void initImageAdapter(List<String> imagesId, String category){
        mCategory = category;
        //mImageAdapter = new ImageAdapter(this, imagesId, category);
        mImageAdapter = new ImageRecyclerAdapter(this, imagesId, category);

        mProgressBar.setVisibility(View.VISIBLE);
        mGridViewAuthors.setVisibility(View.GONE);
        mGridViewImages.setVisibility(View.VISIBLE);

        //mGridViewImages.setAdapter(mImageAdapter);

        mGridViewImages.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.columns), StaggeredGridLayoutManager.VERTICAL));
        mGridViewImages.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = 8;
                outRect.top = 8;
                outRect.right = 8;
                outRect.left = 8;
            }
        });
        mGridViewImages.setHasFixedSize(true);
        mGridViewImages.setAdapter(mImageAdapter);
    }

    private void updateAdapter(Call<PhotosObject> call){
        Log.d("myTag", "REQUEST="+call.request().toString());
        flickrImageIds.clear();
        mImageAdapter.notifyDataSetChanged();
        call.enqueue(new Callback<PhotosObject>() {
            @Override
            public void onResponse(Call<PhotosObject> call, Response<PhotosObject> response) {
                List<PhotosObject.PhotosArray.Photo> photos = response.body().getPhotos().getPhoto();
                for (PhotosObject.PhotosArray.Photo photo : photos) {
                    flickrImageIds.add(photo.getId());
                    mProgressBar.setVisibility(View.GONE);
                    mImageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(final Call<PhotosObject> call, Throwable t) {
                Snackbar.make(findViewById(android.R.id.content), "No internet connection.", Snackbar.LENGTH_INDEFINITE).setAction("Try again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateAdapter(call);
                    }
                }).show();
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
