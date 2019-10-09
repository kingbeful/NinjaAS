package io.github.mthli.Ninja.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;

import io.github.mthli.Ninja.Fragment.SettingFragment;
import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.Task.ImportBookmarksTask;
import io.github.mthli.Ninja.Task.ImportWhitelistTask;
import io.github.mthli.Ninja.Unit.IntentUnit;
import io.github.mthli.Ninja.View.NinjaToast;

public class SettingActivity extends Activity {
    private SettingFragment fragment;
    private InterstitialAd mInterstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4693448877034157/7180213761");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e("TAG", "onAdFailedToLoad: "+errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
//                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                SettingActivity.this.finish();
            }

        });
        fragment = new SettingFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    public boolean showAds() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            return false;
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                IntentUnit.setDBChange(fragment.isDBChange());
                IntentUnit.setSPChange(fragment.isSPChange());
                if (this.showAds()) {
                    // no ads will return true, and finish the activity
                    finish();
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            IntentUnit.setDBChange(fragment.isDBChange());
            IntentUnit.setSPChange(fragment.isSPChange());
            if (this.showAds()) {
                // no ads will return true, and finish the activity
                finish();
            }

            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentUnit.REQUEST_BOOKMARKS) {
            if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                NinjaToast.show(this, R.string.toast_import_bookmarks_failed);
            } else {
                File file = new File(data.getData().getPath());
                new ImportBookmarksTask(fragment, file).execute();
            }
        } else if (requestCode == IntentUnit.REQUEST_WHITELIST) {
            if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                NinjaToast.show(this, R.string.toast_import_whitelist_failed);
            } else {
                File file = new File(data.getData().getPath());
                new ImportWhitelistTask(fragment, file).execute();
            }
        } else if (requestCode == IntentUnit.REQUEST_CLEAR) {
            if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(ClearActivity.DB_CHANGE)) {
                fragment.setDBChange(data.getBooleanExtra(ClearActivity.DB_CHANGE, false));
            }
        }
    }
}
