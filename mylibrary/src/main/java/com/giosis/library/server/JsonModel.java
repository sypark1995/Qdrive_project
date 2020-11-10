package com.giosis.library.server;

import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;


/**
 * Created by inyion on 15. 12. 11.
 */
public class JsonModel {

    public <T> T fromJson(String jsonStr, Class<T> c) {
        try {
            Gson gson = new Gson();
            Object obj = gson.fromJson(jsonStr, c);

            return c.cast(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (IncompatibleClassChangeError error) {
            error.printStackTrace();
            return null;
        }
    }

    public <T> T fromJson(JsonElement jsonEm, Class<T> c) {
        try {
            Gson gson = new Gson();
            Object obj = gson.fromJson(jsonEm, c);

            return c.cast(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (IncompatibleClassChangeError error) {
            error.printStackTrace();
            return null;
        }
    }

    public String toJson(Object obj) {
        if (obj != null) {
            return new Gson().toJson(obj);
        } else {
            return null;
        }
    }

    public <T> T getObject(Object obj, Class<T> c) {
        try {
            if(obj != null) {
                return c.cast(obj);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } catch (IncompatibleClassChangeError error) {
            error.printStackTrace();
            return null;
        }
    }

    public String checkColumn(Cursor cursor, String columnName) {
        String data = null;
        int columnIdx = cursor.getColumnIndex(columnName);
        if(columnIdx != -1) {
            data = cursor.getString(columnIdx);
        }

        return data;
    }

    public int checkColumnIntValue(Cursor cursor, String columnName) {
        int data = 0;
        int columnIdx = cursor.getColumnIndex(columnName);
        if(columnIdx != -1) {
            data = cursor.getInt(columnIdx);
        }

        return data;
    }

}
