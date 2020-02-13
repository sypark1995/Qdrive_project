/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.giosis.util.qdrive.barcodescanner.result;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.giosis.util.qdrive.barcodescanner.LocaleManager;
import com.giosis.util.qdrive.barcodescanner.PreferencesActivity;
import com.giosis.util.qdrive.singapore.R;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * A base class for the Android-specific barcode handlers. These allow the app to polymorphically
 * suggest the appropriate actions for each data type.
 * <p>
 * This class also contains a bunch of utility methods to take common actions like opening a URL.
 * They could easily be moved into a helper object, but it can't be static because the Activity
 * instance is needed to launch an intent.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public abstract class ResultHandler {

    private static final String TAG = ResultHandler.class.getSimpleName();

    private static final DateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
        // For dates without a time, for purposes of interacting with Android, the resulting timestamp
        // needs to be midnight of that day in GMT. See:
        // http://code.google.com/p/android/issues/detail?id=8330
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    private static final String GOOGLE_SHOPPER_PACKAGE = "com.google.android.apps.shopper";
    private static final String GOOGLE_SHOPPER_ACTIVITY = GOOGLE_SHOPPER_PACKAGE +
            ".results.SearchResultsActivity";
    private static final String MARKET_URI_PREFIX = "market://search?q=pname:";
    private static final String MARKET_REFERRER_SUFFIX =
            "&referrer=utm_source%3Dbarcodescanner%26utm_medium%3Dapps%26utm_campaign%3Dscan";

    public static final int MAX_BUTTON_COUNT = 4;

    private final ParsedResult result;
    private final Activity activity;
    private final Result rawResult;
    private final String customProductSearch;

    private final DialogInterface.OnClickListener shopperMarketListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI_PREFIX +
                            GOOGLE_SHOPPER_PACKAGE + MARKET_REFERRER_SUFFIX)));
                }
            };

    ResultHandler(Activity activity, ParsedResult result) {
        this(activity, result, null);
    }

    ResultHandler(Activity activity, ParsedResult result, Result rawResult) {
        this.result = result;
        this.activity = activity;
        this.rawResult = rawResult;
        this.customProductSearch = parseCustomSearchURL();

    }

    public ParsedResult getResult() {
        return result;
    }

    boolean hasCustomProductSearch() {
        return customProductSearch != null;
    }

    /**
     * Indicates how many buttons the derived class wants shown.
     *
     * @return The integer button count.
     */
    public abstract int getButtonCount();

    /**
     * The text of the nth action button.
     *
     * @param index From 0 to getButtonCount() - 1
     * @return The button text as a resource ID
     */
    public abstract int getButtonText(int index);


    /**
     * Execute the action which corresponds to the nth button.
     *
     * @param index The button that was clicked.
     */
    public abstract void handleButtonPress(int index);

    /**
     * Some barcode contents are considered secure, and should not be saved to history, copied to
     * the clipboard, or otherwise persisted.
     *
     * @return If true, do not create any permanent record of these contents.
     */
    public boolean areContentsSecure() {
        return false;
    }

    /**
     * The Google Shopper button is special and is not handled by the abstract button methods above.
     *
     * @param listener The on click listener to install for this button.
     */
    protected void showGoogleShopperButton(View.OnClickListener listener) {
    }

    /**
     * Create a possibly styled string for the contents of the current barcode.
     *
     * @return The text to be displayed.
     */
    public CharSequence getDisplayContents() {
        String contents = result.getDisplayResult();
        return contents.replace("\r", "");
    }

    /**
     * A string describing the kind of barcode that was found, e.g. "Found contact info".
     *
     * @return The resource ID of the string.
     */
    public abstract int getDisplayTitle();

    /**
     * A convenience method to get the parsed type. Should not be overridden.
     *
     * @return The parsed type, e.g. URI or ISBN
     */
    public final ParsedResultType getType() {
        return result.getType();
    }


    final void shareByEmail(String contents) {
        sendEmailFromUri("mailto:", null, activity.getString(R.string.msg_share_subject_line), contents);
    }


    // Use public Intent fields rather than private GMail app fields to specify subject and body.
    final void sendEmailFromUri(String uri, String email, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse(uri));
        if (email != null) {
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        }
        putExtra(intent, Intent.EXTRA_SUBJECT, subject);
        putExtra(intent, Intent.EXTRA_TEXT, body);
        intent.setType("text/plain");
        launchIntent(intent);
    }

    final void shareBySMS(String contents) {
        sendSMSFromUri("smsto:", activity.getString(R.string.msg_share_subject_line) + ":\n" + contents);
    }

    final void sendSMSFromUri(String uri, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
        putExtra(intent, "sms_body", body);
        // Exit the app once the SMS is sent
        intent.putExtra("compose_mode", true);
        launchIntent(intent);
    }

    // Uses the mobile-specific version of Product Search, which is formatted for small screens.
    final void openProductSearch(String upc) {
        Uri uri = Uri.parse("http://www.google." + LocaleManager.getProductSearchCountryTLD() +
                "/m/products?q=" + upc + "&source=zxing");
        launchIntent(new Intent(Intent.ACTION_VIEW, uri));
    }

    final void openBookSearch(String isbn) {
        Uri uri = Uri.parse("http://books.google." + LocaleManager.getBookSearchCountryTLD() +
                "/books?vid=isbn" + isbn);
        launchIntent(new Intent(Intent.ACTION_VIEW, uri));
    }

    final void openURL(String url) {
        launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    final void webSearch(String query) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra("query", query);
        launchIntent(intent);
    }

    final void openGoogleShopper(String query) {
        try {

            activity.getPackageManager().getPackageInfo(GOOGLE_SHOPPER_PACKAGE, 0);
            // If we didn't throw, Shopper is installed, so launch it.
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setClassName(GOOGLE_SHOPPER_PACKAGE, GOOGLE_SHOPPER_ACTIVITY);
            intent.putExtra(SearchManager.QUERY, query);
            activity.startActivity(intent);

        } catch (PackageManager.NameNotFoundException e) {
            // Otherwise offer to install it from Market.
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.app_name);
            builder.setMessage(R.string.app_name);
            builder.setPositiveButton(R.string.button_ok, shopperMarketListener);
            builder.setNegativeButton(R.string.button_cancel, null);
            builder.show();
        }
    }

    void launchIntent(Intent intent) {
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            Log.d(TAG, "Launching intent: " + intent + " with extras: " + intent.getExtras());
            try {

                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.msg_intent_failed);
                builder.setPositiveButton(R.string.button_ok, null);
                builder.show();
            }
        }
    }

    private static void putExtra(Intent intent, String key, String value) {
        if (value != null && value.length() > 0) {
            intent.putExtra(key, value);
        }
    }

    protected void showNotOurResults(int index, AlertDialog.OnClickListener proceedListener) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (prefs.getBoolean(PreferencesActivity.KEY_NOT_OUR_RESULTS_SHOWN, false)) {
            // already seen it, just proceed
            proceedListener.onClick(null, index);
        } else {
            // note the user has seen it
            prefs.edit().putBoolean(PreferencesActivity.KEY_NOT_OUR_RESULTS_SHOWN, true).commit();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.msg_not_our_results);
            builder.setPositiveButton(R.string.button_ok, proceedListener);
            builder.show();
        }
    }

    private String parseCustomSearchURL() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String customProductSearch = prefs.getString(PreferencesActivity.KEY_CUSTOM_PRODUCT_SEARCH,
                null);
        if (customProductSearch != null && customProductSearch.trim().length() == 0) {
            return null;
        }
        return customProductSearch;
    }

    String fillInCustomSearchURL(String text) {
        String url = customProductSearch.replace("%s", text);
        if (rawResult != null) {
            url = url.replace("%f", rawResult.getBarcodeFormat().toString());
        }
        return url;
    }
}