package com.beautiful_wallpapers_hd_qhd.activity.dialog;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.billing.InAppConfig;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.google.android.gms.common.AccountPicker;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Igor on 06.07.2016.
 */
public class BuyProDialog extends AppCompatActivity {

    private static final int REQUEST_CODE_PURCHASE = 9000;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 9001;

    private Boolean setupDone;
    private String mAccountEmail;

    @Inject SharedPreferencesController sPref;
    @Inject OpenIabHelper mHelper;
    @BindView(R.id.buy_pro_btn) LinearLayout mBuyProButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_buy_pro);

        DaggerAppComponent.builder().myModule(new MyModule(this)).build().inject(this);
        ButterKnife.bind(this);
        initializePurchases();

        mBuyProButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
                startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
            }
        });
    }

    private void initializePurchases(){
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    setupDone = false;
                    Toast.makeText(getBaseContext(), "Problem setting up in-app billing: " + result, Toast.LENGTH_LONG).show();
                    return;
                }
                if (mHelper == null) return;
                setupDone = true;
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                Toast.makeText(getBaseContext(), "Failed to query inventory: " + result, Toast.LENGTH_LONG).show();
                return;
            }
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Toast.makeText(getBaseContext(), "Error purchasing: " + result, Toast.LENGTH_LONG).show();
                return;
            }
            if (!purchase.getDeveloperPayload().equals(mAccountEmail)) {
                Toast.makeText(getBaseContext(), "Error purchasing. Authenticity verification failed.", Toast.LENGTH_LONG).show();
                return;
            }

            if (purchase.getSku().equals(InAppConfig.SKU_PRO_VERSION)) {
                // bought the premium upgrade!
                sPref.setBool(SharedPreferencesController.SP_PRO_VERSION, true);
                //analytics.registerEvent("PRO", Analytics.PURCHASED, device.getGmailAccounts().get(accountsListView.getCheckedItemPosition()), 0);
                Toast.makeText(getBaseContext(), R.string.restart_app, Toast.LENGTH_LONG).show();
                //mIsPro = true;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case REQUEST_CODE_PICK_ACCOUNT:
                if(resultCode == RESULT_OK){
                    mAccountEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                    if (setupDone == null) {
                        Toast.makeText(getBaseContext(), "Billing Setup is not completed yet", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!setupDone) {
                        Toast.makeText(getBaseContext(), "Billing Setup failed", Toast.LENGTH_LONG).show();
                        return;
                    }

                    mHelper.launchPurchaseFlow(this, InAppConfig.SKU_PRO_VERSION, REQUEST_CODE_PURCHASE, mPurchaseFinishedListener, mAccountEmail);
                }
                break;
            case REQUEST_CODE_PURCHASE:
                if(resultCode == RESULT_OK){
                    if (mHelper == null) return;

                    if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                        super.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;
        }
    }

}
