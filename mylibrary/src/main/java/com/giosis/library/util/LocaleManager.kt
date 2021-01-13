package com.giosis.library.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

/**
 * @author krm0219
 */
class LocaleManager private constructor(val context: Context){

    companion object {
        var TAG = "LocaleManager"

        @Volatile private var instance: LocaleManager? = null

        @JvmStatic fun getInstance(context: Context): LocaleManager =
                instance ?: synchronized(this) {
                    instance ?: LocaleManager(context).also {
                        instance = it
                    }
                }
    }

    private val systemLocale: Locale

    init {
        systemLocale = getLocale(context.resources)
    }

    private fun getLocale(res: Resources): Locale {
        val config = res.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) config.locales[0] else config.locale
    }

    fun setLocale(c: Context): Context {
        return updateResources(c, Preferences.language)
    }

    fun setNewLocale(c: Context, language: String) {
        persistLanguage(language)
        updateResources(c, language)
    }

//    val language: String
//        get() =

    private fun persistLanguage(language: String) {
        // use commit() instead of apply(), because sometimes we kill the application process immediately
        // which will prevent apply() to finish
        Preferences.language = language
    }

    private fun updateResources(context: Context, language: String) :Context{
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        context = context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context
    }


}