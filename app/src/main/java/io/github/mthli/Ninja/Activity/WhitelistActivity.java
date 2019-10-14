package io.github.mthli.Ninja.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import java.util.List;

import io.github.mthli.Ninja.Browser.AdBlock;
import io.github.mthli.Ninja.BuildConfig;
import io.github.mthli.Ninja.Database.RecordAction;
import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.Unit.BrowserUnit;
import io.github.mthli.Ninja.View.NinjaToast;
import io.github.mthli.Ninja.View.WhitelistAdapter;

public class WhitelistActivity extends Activity {
    private WhitelistAdapter adapter;
    private List<String> list;
    private InterstitialAd interstitialAd;
    private final static String TAG = WhitelistActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whitelist);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        interstitialAd = new InterstitialAd(this, "447568642531681_447570512531494");
        if (BuildConfig.DEBUG) {
            AdSettings.addTestDevice("1f029cd7-38f0-4f66-b5c0-678bfacb96de");
        }
        // Set listeners for the Interstitial Ad
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        });

        interstitialAd.loadAd();

        RecordAction action = new RecordAction(this);
        action.open(false);
        list = action.listDomains();
        action.close();

        ListView listView = (ListView) findViewById(R.id.whitelist);
        listView.setEmptyView(findViewById(R.id.whitelist_empty));

        adapter = new WhitelistAdapter(this, R.layout.whitelist_item, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        final EditText editText = (EditText) findViewById(R.id.whilelist_edit);
        showSoftInput(editText);

        Button button = (Button) findViewById(R.id.whilelist_add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String domain = editText.getText().toString().trim();
                if (domain.isEmpty()) {
                    NinjaToast.show(WhitelistActivity.this, R.string.toast_input_empty);
                } else if (!BrowserUnit.isURL(domain)) {
                    NinjaToast.show(WhitelistActivity.this, R.string.toast_invalid_domain);
                } else {
                    RecordAction action = new RecordAction(WhitelistActivity.this);
                    action.open(true);
                    if (action.checkDomain(domain)) {
                        NinjaToast.show(WhitelistActivity.this, R.string.toast_domain_already_exists);
                    } else {
                        AdBlock adBlock = new AdBlock(WhitelistActivity.this);
                        adBlock.addDomain(domain.trim());
                        list.add(0, domain.trim());
                        adapter.notifyDataSetChanged();
                        NinjaToast.show(WhitelistActivity.this, R.string.toast_add_whitelist_successful);
                    }
                    action.close();
                }
            }
        });
    }

    @Override
    public void onPause() {
        hideSoftInput(findViewById(R.id.whilelist_edit));
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.whilelist_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.whitelist_menu_clear:
                AdBlock adBlock = new AdBlock(this);
                adBlock.clearDomains();
                list.clear();
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return true;
    }

    private void hideSoftInput(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}
