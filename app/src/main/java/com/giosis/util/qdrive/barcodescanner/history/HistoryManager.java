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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.giosis.util.qdrive.barcodescanner.CaptureActivityTemp;
import com.google.zxing.Result;

/**
 * <p>Manages functionality related to scan history.</p>
 *
 * @author Sean Owen
 */
@Deprecated
public final class HistoryManager {

    private static final String TAG = HistoryManager.class.getSimpleName();
    private final CaptureActivityTemp activity;


    public HistoryManager(CaptureActivityTemp activity) {

        this.activity = activity;
    }


    public boolean addHistoryItem(Result result) {

        boolean isDuplicate = deletePrevious(result.getText());

        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db;

        try {

            db = helper.getWritableDatabase();
        } catch (Exception e) {

            Log.w(TAG, "Error while opening database", e);
            return false;
        }

        try {

            // Insert the new entry into the DB.
            ContentValues values = new ContentValues();
            values.put(DBHelper.TEXT_COL, result.getText());
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
        } catch (Exception e) {

            Log.w(TAG, "Error while opening database", e);
            return false;
        }


        int deleteCount;
        try {

            deleteCount = db.delete(DBHelper.TABLE_NAME, DBHelper.TEXT_COL + "=? COLLATE NOCASE ", new String[]{text});
        } finally {

            db.close();
        }

        return 0 < deleteCount;
    }

    public void clearHistory() {

        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db;

        try {

            db = helper.getWritableDatabase();
        } catch (Exception e) {

            Log.w(TAG, "Error while opening database", e);
            return;
        }

        try {

            db.delete(DBHelper.TABLE_NAME, null, null);
        } finally {

            db.close();
        }
    }
}