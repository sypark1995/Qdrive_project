package com.giosis.util.qdrive.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

/**
 * @author krm0219
 */
public class LocaleManager {
    static String TAG = "LocaleManager";


    private static final String LANGUAGE_KEY = "PREF_LANGUAGE_SETTING";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_MALAY = "ms";
    public static final String LANGUAGE_INDONESIA = "in";

    private Locale systemLocale;
    private final SharedPreferences prefs;

    public LocaleManager(Context context) {

        systemLocale = getLocale(context.getResources());
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Context setLocale(Context c) {

        return updateResources(c, getLanguage());
    }

    public void setNewLocale(Context c, String language) {
        persistLanguage(language);
        updateResources(c, language);
    }

    public String getLanguage() {

        return prefs.getString(LANGUAGE_KEY, LANGUAGE_ENGLISH);
    }

    @SuppressLint("ApplySharedPref")
    private void persistLanguage(String language) {
        // use commit() instead of apply(), because sometimes we kill the application process immediately
        // which will prevent apply() to finish
        prefs.edit().putString(LANGUAGE_KEY, language).commit();
    }

    private Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        return context;
    }

    private static Locale getLocale(Resources res) {

        Configuration config = res.getConfiguration();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? config.getLocales().get(0) : config.locale;
    }
}