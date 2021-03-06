package com.beautiful_wallpapers_hd_qhd.activity;

import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.activity.dialog.ExitDialog;
import com.beautiful_wallpapers_hd_qhd.core.Advertising;
import com.beautiful_wallpapers_hd_qhd.core.adapter.AuthorAdapter;
import com.beautiful_wallpapers_hd_qhd.core.adapter.ImageRecyclerAdapter;
import com.beautiful_wallpapers_hd_qhd.core.billing.InAppConfig;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrHelper;
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
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE_EXIT = 9000;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 9001;

    private static int mCurrentSelectedPosition = R.id.nav_category_all;
    private static int mTitle = 0;
    private static String mCategory = "all";
    private boolean isProActivated = false;

    private List<String> mCurrentFlickrImages = new ArrayList<>();
    private List<String> flickrImageIds = new ArrayList<>();
    private List<String> flickrAuthorIds = new ArrayList<>();

    @Inject AnimationController animationController;
    @Inject SharedPreferencesController sPref;
    @Inject FlickrDatabase flickrDB;
    @Inject OpenIabHelper mHelper;
    @Inject FlickrAPI flickrAPI;

    @BindView(R.id.images_grid_view) RecyclerView mGridViewImages;
    @BindView(R.id.authors_grid_view) GridView mGridViewAuthors;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.empty_favourite_folder) LinearLayout mNoFavourites;
    @BindView(R.id.empty_subscriptions) LinearLayout mNoSubscriptions;

    private ImageRecyclerAdapter mImageAdapter;
    private String mAccountEmail;

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

        mNavigationView.setNavigationItemSelectedListener(this);

        new NotificationReceiver().start(this);

        String tag = getIntent().getStringExtra("tag");
        if(tag != null){
            initImageAdapter(flickrImageIds, "tagSearch");
            updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, tag));
            setTitle("#"+tag);
            initSwipe(ItemTouchHelper.RIGHT);
        } else {
            if(mTitle != 0) setTitle(mTitle);
            mNavigationView.getMenu().findItem(mCurrentSelectedPosition).setChecked(true);
            onNavigationItemSelected(mNavigationView.getMenu().findItem(mCurrentSelectedPosition));
        }

        if(!(isProActivated = sPref.getBool(SharedPreferencesController.SP_PRO_VERSION, false))){
            new Advertising(this).loadSmartBanner(R.id.main_ad_stub, R.id.main_ad_view);
        }
    }

    private ItemTouchHelper itemTouchHelper;
    private Paint p = new Paint();

    private void initSwipe(int swipeDirection) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, swipeDirection) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    flickrDB.addFavourite(flickrImageIds.get(viewHolder.getLayoutPosition()), FlickrDatabase.FAVOURITE_PHOTO);
                    mImageAdapter.notifyItemChanged(viewHolder.getLayoutPosition());
                    Toast.makeText(MainActivity.this, R.string.preview_add_to_favourite, Toast.LENGTH_LONG).show();
                } else {
                    flickrDB.removeFavourite(flickrImageIds.get(viewHolder.getLayoutPosition()), FlickrDatabase.FAVOURITE_PHOTO);
                    mImageAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                    flickrImageIds.remove(viewHolder.getLayoutPosition());
                    showFavouriteHint();
                    Toast.makeText(MainActivity.this, R.string.preview_remove_from_favourite, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = ((float) itemView.getBottom() - (float) itemView.getTop())/2;
                    float width = height / 3;

                    if(dX > 0){
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_star_yellow);
                        int alpha = (int) (((255 * dX)/itemView.getWidth()));
                        p.setAlpha(alpha > 255 ? 255 : alpha);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, itemView.getTop() + 2*width, (float) itemView.getLeft() + 3*width, itemView.getBottom() - 2*width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_red);
                        int alpha = (int) (((255 * Math.abs(dX))/itemView.getWidth()));
                        p.setAlpha(alpha > 255 ? 255 : alpha);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 3*width, itemView.getTop() + 2*width, (float) itemView.getRight() - width, itemView.getBottom() - 2*width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if(swipeDirection == ItemTouchHelper.RIGHT & flickrDB.isFavourite(flickrImageIds.get(viewHolder.getLayoutPosition()), FlickrDatabase.FAVOURITE_PHOTO)){
                    viewHolder.itemView.startAnimation(animationController.getAnimation(R.anim.already_added));
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mGridViewImages);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(mCurrentSelectedPosition == R.id.nav_category_all){
                startActivityForResult(new Intent(getString(R.string.exit_app_dialog)), REQUEST_CODE_EXIT);
            } else {
                mCurrentSelectedPosition = R.id.nav_category_all;
                mNavigationView.getMenu().findItem(mCurrentSelectedPosition).setChecked(true);
                onNavigationItemSelected(mNavigationView.getMenu().findItem(mCurrentSelectedPosition));
            }
        }
    }

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
                    initializePurchases();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void initializePurchases(){
        mHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                showToast("Problem setting up in-app billing: " + result);
                return;
            }
            if (mHelper == null) return;
            mHelper.queryInventoryAsync(mGotInventoryListener);
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

            if(premiumPurchase != null && premiumPurchase.getDeveloperPayload().equals(mAccountEmail)){
                sPref.setBool(SharedPreferencesController.SP_PRO_VERSION, true);
                showToast(getString(R.string.restart_app));
            } else {
                showToast(getString(R.string.not_bought_pro));
            }
        }
    };

    private void showFavouriteHint(){
        if(flickrImageIds.size() == 0) { mNoFavourites.setVisibility(View.VISIBLE); }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if(isProActivated){
            menu.findItem(R.id.action_restore_purchase).setVisible(false);
        }
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
                updateAdapter(flickrAPI.getPhotosInGroup(FlickrHelper.METHOD_GET_PHOTOS_BY_GROUP));
                saveCurrent(R.id.nav_category_all, R.string.title_section1);
                break;
            case R.id.nav_category_fav:
                flickrImageIds = flickrDB.getFavourites(FlickrDatabase.FAVOURITE_PHOTO);
                initImageAdapter(flickrImageIds, "favourite");
                mProgressBar.setVisibility(View.GONE);
                saveCurrent(R.id.nav_category_fav, R.string.title_section3);
                showFavouriteHint();
                startHandler(() -> {
                    if(sPref.getBool(SharedPreferencesController.SP_IS_FIRST_TIME_FAV, true)){
                        if(flickrImageIds.size() != 0 && mCurrentSelectedPosition == R.id.nav_category_fav){
                            //sPref.setBool(SharedPreferencesController.SP_IS_FIRST_TIME_FAV, false);
                            startActivity(getSwipeIntent("com.beautiful_wallpapers_hd_qhd.SWIPE_LEFT", getFirstItemX(), getFirstItemY()));
                        }
                    }
                }, 2000);
                break;
            case R.id.nav_category_subscriptions:
                mProgressBar.setVisibility(View.GONE);
                mGridViewImages.setVisibility(View.GONE);
                mGridViewAuthors.setVisibility(View.VISIBLE);
                flickrAuthorIds = flickrDB.getFavourites(FlickrDatabase.FAVOURITE_AUTHOR);
                AuthorAdapter mAuthorAdapter = new AuthorAdapter(this, flickrAuthorIds);
                mGridViewAuthors.setAdapter(mAuthorAdapter);
                mGridViewAuthors.setOnItemClickListener((parent, view, position, id) -> {
                    Intent authorPageIntent = new Intent(getResources().getString(R.string.author_page_activity));
                    authorPageIntent.putExtra(getResources().getString(R.string.extra_flickr_author_id), flickrAuthorIds.get(position));
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                        animationController.transition(view.findViewById(R.id.sub_profile_image), getString(R.string.transition_author_image), authorPageIntent);
                    } else {
                        startActivity(authorPageIntent);
                    }
                });
                saveCurrent(R.id.nav_category_subscriptions, R.string.title_section23);
                if(flickrAuthorIds.size() == 0) { mNoSubscriptions.setVisibility(View.VISIBLE); }
                break;
            case R.id.nav_category_landscape:
                initImageAdapter(flickrImageIds, "landscape");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_landscape"));
                saveCurrent(R.id.nav_category_landscape, R.string.title_section5);
                break;
            case R.id.nav_category_buildings:
                initImageAdapter(flickrImageIds, "building");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_building"));
                saveCurrent(R.id.nav_category_buildings, R.string.title_section6);
                break;
            case R.id.nav_category_castles:
                initImageAdapter(flickrImageIds, "castle");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_castle"));
                saveCurrent(R.id.nav_category_castles, R.string.title_section7);
                break;
            case R.id.nav_category_sea:
                initImageAdapter(flickrImageIds, "sea");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_sea"));
                saveCurrent(R.id.nav_category_sea, R.string.title_section8);
                break;
            case R.id.nav_category_textures:
                initImageAdapter(flickrImageIds, "texture");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_texture"));
                saveCurrent(R.id.nav_category_textures, R.string.title_section9);
                break;
            case R.id.nav_category_flowers:
                initImageAdapter(flickrImageIds, "flower");
                updateAdapter(flickrAPI.getPhotosInGroupByTags(FlickrHelper.METHOD_SEARCH, "WHD_flower"));
                saveCurrent(R.id.nav_category_flowers, R.string.title_section10);
                break;
            case R.id.nav_category_other:
                initImageAdapter(flickrImageIds, "other");
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
        if(itemTouchHelper != null){
            itemTouchHelper.attachToRecyclerView(null);
        }
        initSwipe(position != R.id.nav_category_fav ?ItemTouchHelper.RIGHT : ItemTouchHelper.LEFT);
        mTitle = titleRes;
        setTitle(mTitle);
        mNoFavourites.setVisibility(View.GONE);
        mNoSubscriptions.setVisibility(View.GONE);
    }

    private void initImageAdapter(List<String> imagesId, String category){
        mCategory = category;
        mImageAdapter = new ImageRecyclerAdapter(this, imagesId, category);

        mProgressBar.setVisibility(View.VISIBLE);
        mGridViewAuthors.setVisibility(View.GONE);
        mGridViewImages.setVisibility(View.VISIBLE);

        mGridViewImages.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.columns), StaggeredGridLayoutManager.VERTICAL));
        mGridViewImages.setHasFixedSize(true);
        mGridViewImages.setAdapter(mImageAdapter);
    }

    private void updateAdapter(Observable<PhotosObject> observable){
        flickrImageIds.clear();
        mImageAdapter.notifyDataSetChanged();
        observable
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> {
                    mProgressBar.setVisibility(View.GONE); mImageAdapter.notifyDataSetChanged();
                    startHandler(() -> {
                        if(sPref.getBool(SharedPreferencesController.SP_IS_FIRST_TIME, true)){
                            if(mCurrentSelectedPosition != R.id.nav_category_fav){
                                //sPref.setBool(SharedPreferencesController.SP_IS_FIRST_TIME, false);
                                startActivity(getSwipeIntent("com.beautiful_wallpapers_hd_qhd.SWIPE_RIGHT", getFirstItemX(), getFirstItemY()));
                            }
                        }
                    }, 1000);
                })
                .map(photosObject -> photosObject.getPhotos().getPhoto())
                .flatMap(photos -> Observable.from(photos))
                .subscribe(
                        photo ->
                            flickrImageIds.add(photo.getId()),
                        e -> {
                            Log.d("myLog", e.fillInStackTrace().toString());
                            mProgressBar.setVisibility(View.GONE);
                            Snackbar
                                .make(findViewById(android.R.id.content), R.string.no_internet_connection, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.action_try_again, v -> {mProgressBar.setVisibility(View.VISIBLE); updateAdapter(observable);}).show();}
                );
    }

    private Intent getSwipeIntent(String intentName, float x, float y){
        return new Intent(intentName).putExtra("swipe_x", x).putExtra("swipe_y", y);
    }

    private float getFirstItemX(){
        return mGridViewImages.findViewHolderForAdapterPosition(0).itemView.getWidth() / 4;
    }

    private float getFirstItemY(){
        return (float) (mGridViewImages.findViewHolderForAdapterPosition(0).itemView.getY() + mGridViewImages.findViewHolderForAdapterPosition(0).itemView.getHeight() / 1.5);
    }

    private void startHandler(Runnable runnable, long delay){
        new Handler().postDelayed(runnable, delay);
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
