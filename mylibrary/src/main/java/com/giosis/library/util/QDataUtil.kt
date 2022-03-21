package com.giosis.library.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


object QDataUtil {

    //Android_QX.QUICK_2.1.21_166(GMKTV2_LnLqASDoia7jbyb21xgXnyJlib0fet6FKDRPgw50Oe8_g_3_;SM-G960N;10;ko_KR)
    //Android_QX MSMARTSHIP_1.0.1_2(GMKTV2_uzraf4dlXrPFBonIFPW0T4SGWotK2In00yfKtjUWABT0XUqh6YRC9jO8T4FUfLJ1_g_2_i_g_2_Icr2FQBY_g_3_;HMA-AL00;10;en_)
    //Android_QX.QDRIVE_3.4.2_93(GMKTV2_M1PeViVdTpFfkUXKlaIokacEEck0spYtBQClRi1osXmHRl8FFPskyRL0M2bvNOgMDHwffWKnXvQ_g_3_;HMA-AL00;10;ko_KR)
    //Android_QX.QDRIVE_3.4.2_93(GMKTV2_emuSLleJ3UIN5Rv29oCED7Td5beIHgQG8ctYtFPvDRVr7q2dCnCEESVCvK1eseEAq_g_2_ZB9mkCfFA_g_3_;SM-G960N;10;ko_KR)
    fun setCustomUserAgent(appContext: Context): String {

        var appVersionName = ""
        var appVersionCode = ""
        val appName = "QX.QDRIVE"

        try {
            val info = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            appVersionName = info.versionName
            appVersionCode = info.versionCode.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val deviceCode = getSecretCode(getUUID(appContext), appVersionName)

        val config: Configuration = appContext.resources.configuration
        val systemLocale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) config.locales[0] else config.locale

        val appLanguage = systemLocale.language
        val localeNation = if (safeEqual(appLanguage, "in")) {
            "id"
        } else {
            appLanguage
        }
        val localeAndLanguageCode =
            localeNation + "_" + getLocaleCodeByDeviceSetting(appContext)


        val userAgent = StringBuffer()
        userAgent.append("Android")
        userAgent.append("_")
        userAgent.append(appName)
        userAgent.append("_")
        userAgent.append(appVersionName)
        userAgent.append("_")
        userAgent.append(appVersionCode)
        userAgent.append("(")

        userAgent.append(deviceCode)
        userAgent.append(";")
        userAgent.append(Build.MODEL)
        userAgent.append(";")
        userAgent.append(Build.VERSION.RELEASE)
        userAgent.append(";")
        userAgent.append(localeAndLanguageCode)
        userAgent.append(")")

        Preferences.userAgent = userAgent.toString()

        return Preferences.userAgent
    }


    fun safeEqual(s1: String?, s2: String?): Boolean {
        return isSafeEmptyCheck(s1, s2) && s1 == s2
    }

    fun isSafeEmptyCheck(vararg strColum: String?): Boolean {
        var bool = true
        val count = strColum.size
        for (str in strColum) {
            if (TextUtils.isEmpty(str)) {
                bool = false
                break
            }
        }
        return bool
    }


    private fun getLocaleCodeByDeviceSetting(context: Context): String? {
        return context.resources.configuration.locale.country
    }

    private fun getUUID(context: Context): String {
        var androidID =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        if (androidID == null) {
            androidID = ""
        }
        val uuid = androidID

        return uuid
    }


    private fun getSecretCode(uuid: String?, versionName: String): String? {
        val prefix = "GMKTV2_"
        val appCode: String = "AQDRIVE"
        var key = appCode + versionName

        if (key.length < 8) {
            var i = 1
            while (8 - key.length == i) {
                key += " "
                i++
            }
        } else if (key.length > 8) {
            key = key.substring(0, 8)
        }
        var encryptStr = "$uuid:::$appCode$versionName"
        // cb5d1fe91c3b6462____50:77:05:C7:09:5A:::AQDRIVE3.4.2

        try {
            encryptStr = CryptDES.encrypt(encryptStr, key)
            encryptStr = giosisUrlEncode(encryptStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return prefix + encryptStr
    }


    fun giosisUrlEncode(data: String?): String {
        var returnData = ""
        if (data != null) {
            returnData =
                data.replace("_g_", "_g_8_").replace("+", "_g_1_").replace("/", "_g_2_")
                    .replace("=", "_g_3_").replace("&", "_g_4_").replace("<", "_g_5_")
                    .replace(">", "_g_6_").replace("@", "_g_7_")
        }
        return returnData
    }

    suspend fun getBitmapString(
        context: Context,
        view: View,
        basePath: String,
        path: String,
        trackingNo: String
    ): String =
        withContext(Dispatchers.Default) {

            view.buildDrawingCache()
            val viewBitmap = view.drawingCache
            return@withContext DataUtil.bitmapToString(
                context,
                viewBitmap,
                basePath,
                path,
                trackingNo
            )
        }
}