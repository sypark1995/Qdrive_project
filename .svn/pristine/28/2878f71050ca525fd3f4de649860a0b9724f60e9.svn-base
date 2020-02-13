/*
 * Copyright (C) 2009 ZXing authors
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

package com.giosis.util.qdrive.barcodescanner.history;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.barcodescanner.Intents;
import com.giosis.util.qdrive.barcodescanner.PreferencesActivity;
import com.giosis.util.qdrive.barcodescanner.result.ResultHandler;
import com.google.zxing.Result;

/**
 * <p>Manages functionality related to scan history.</p>
 *
 * @author Sean Owen
 */
public final class HistoryManager {
    private static final String TAG = HistoryManager.class.getSimpleName();

    private final CaptureActivity activity;

    public HistoryManager(CaptureActivity activity) {
        this.activity = activity;
    }


    public boolean addHistoryItem(Result result, ResultHandler handler) {
        // Do not save this item to the history if the preference is turned off, or the contents are
        // considered secure.
        if (handler != null) {
            if (!activity.getIntent().getBooleanExtra(Intents.Scan.SAVE_HISTORY, true) ||
                    handler.areContentsSecure()) {
                return false;
            }
        }

        boolean isDuplicate = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (!prefs.getBoolean(PreferencesActivity.KEY_REMEMBER_DUPLICATES, false)) {
            isDuplicate = deletePrevious(result.getText());
        }

        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db;
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException sqle) {
            Log.w(TAG, "Error while opening database", sqle);
            return false;
        }
        try {
            // Insert the new entry into the DB.
            ContentValues values = new ContentValues();
            values.put(DBHelper.TEXT_COL, result.getText());

            if (null != result.getBarcodeFormat())
                values.put(DBHelper.FORMAT_COL, result.getBarcodeFormat().toString());

            if (null != handler)
                values.put(DBHelper.DISPLAY_COL, handler.getDisplayContents().toString());

            values.put(DBHelper.TIMESTAMP_COL, System.currentTimeMillis());
            db.insert(DBHelper.TABLE_NAME, DBHelper.TIMESTAMP_COL, values);
        } finally {
            db.close();
        }

        return isDuplicate;
    }

    public boolean deletePrevious(String text) {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db;
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException sqle) {
            Log.w(TAG, "Error while opening database", sqle);
            return false;
        }

        int iRes = 0;
        try {
            iRes = db.delete(DBHelper.TABLE_NAME, DBHelper.TEXT_COL + "=? COLLATE NOCASE ", new String[]{text});

        } finally {
            db.close();
        }
        if (iRes > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void clearHistory() {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db;
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException sqle) {
            Log.w(TAG, "Error while opening database", sqle);
            return;
        }
        try {
            db.delete(DBHelper.TABLE_NAME, null, null);
        } finally {
            db.close();
        }
    }
}