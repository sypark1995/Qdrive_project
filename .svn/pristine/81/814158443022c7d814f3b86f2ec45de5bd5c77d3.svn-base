package com.giosis.util.qdrive.singapore;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.giosis.util.qdrive.util.MySharedPreferences;

/*********
 *
 * @author jtpark_eurasia
 * 전역 변수 관리
 * @editor krm0219
 */
public class MyApplication extends MultiDexApplication {
    String TAG = "MyApplication";

    public static MySharedPreferences preferences;
    private static Context context;

    private int badgeCnt;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = new MySharedPreferences(getApplicationContext());
        context = getApplicationContext();
        badgeCnt = 0;
    }

    public static Context getContext() {
        return context;
    }

    public void setBadgeCnt(int badgeCnt) {
        this.badgeCnt = badgeCnt;
    }

    public int getBadgeCnt() {
        return badgeCnt;
    }
}