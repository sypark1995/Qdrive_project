package com.giosis.util.qdrive.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.giosis.util.qdrive.singapore.MyApplication
import java.net.NetworkInterface
import java.util.*

class QDataUtil {

    companion object {

        //Android_QX.QUICK_2.1.21_166(GMKTV2_LnLqASDoia7jbyb21xgXnyJlib0fet6FKDRPgw50Oe8_g_3_;SM-G960N;10;ko_KR)
        //Android_QX MSMARTSHIP_1.0.1_2(GMKTV2_uzraf4dlXrPFBonIFPW0T4SGWotK2In00yfKtjUWABT0XUqh6YRC9jO8T4FUfLJ1_g_2_i_g_2_Icr2FQBY_g_3_;HMA-AL00;10;en_)
        //Android_QX.QDRIVE_3.4.2_93(GMKTV2_M1PeViVdTpFfkUXKlaIokacEEck0spYtBQClRi1osXmHRl8FFPskyRL0M2bvNOgMDHwffWKnXvQ_g_3_;HMA-AL00;10;ko_KR)
        //Android_QX.QDRIVE_3.4.2_93(GMKTV2_emuSLleJ3UIN5Rv29oCED7Td5beIHgQG8ctYtFPvDRVr7q2dCnCEESVCvK1eseEAq_g_2_ZB9mkCfFA_g_3_;SM-G960N;10;ko_KR)
        fun getCustomUserAgent(appContext: Context): String {

            var appVersionName = ""
            var appVersionCode = ""
            val appName = "QX.QDRIVE"

            try {
                val info = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
                appVersionName = info.versionName
                appVersionCode = info.versionCode.toString()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                //GMKT_Log.i(e.toString());
            }


            val deviceCode = getSecretCode(getUUID(appContext), appVersionName)

            val config: Configuration = appContext.resources.configuration
            val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) config.locales[0] else config.locale

            val appLanguage = MyApplication.preferences.localeLanguage
            val localeNation = if (safeEqual(appLanguage, "in")) {
                "id"
            } else {
                appLanguage
            }
            val localeAndLanguageCode = localeNation + "_" + getLocaleCodeByDeviceSetting(appContext)

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

            return userAgent.toString()
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

        private fun getUUID(context: Context): String? {
            var androidID =
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (androidID == null) {
                androidID = ""
            }
            // 권한이 되는사람은 있던 값 쓰는게 맞을 것 같습니다.
//        String mac = getMacAddress(context);
//        String uuid = androidID;
//
//        uuid = String.format("%s____%s", androidID, mac);
// 권한 때문에 IMEI 값 사용하지 않는다.
//            val imei: String? = getIMEI(context)
//            val mac: String? = getMacAddress()
//            val uuid = if (imei == null || imei == "") {
//                String.format("%s____%s", androidID, mac)
//            } else {
//                String.format("%s____%s", androidID, imei)
//            }


            val uuid = androidID

            return uuid
        }

        fun getIMEI(context: Context): String? {
            val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            return if (telephonyManager != null) {
                var imei: String? = null

                //   if (SmartShip.PERMISSION.PHONE.isCheck) {

                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return null
                }

                imei = if (Build.VERSION.SDK_INT >= 26) {

                    telephonyManager.imei
                } else {
                    telephonyManager.deviceId
                }
                //   }

                if (imei == null || imei.length < 1) {
                    null
                } else {
                    imei
                }
            } else {
                null
            }
        }


        fun getMacAddress(): String? {
            val interfaceName = "wlan0"
            try {
                val interfaces: List<NetworkInterface> =
                        Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    if (interfaceName != null) {
                        if (!intf.name.equals(interfaceName, ignoreCase = true)) continue
                    }
                    val mac = intf.hardwareAddress ?: return ""
                    val buf = StringBuilder()
                    for (idx in mac.indices) buf.append(String.format("%02X:", mac[idx]))
                    if (buf.length > 0) buf.deleteCharAt(buf.length - 1)
                    return buf.toString()
                }
            } catch (ex: java.lang.Exception) {
            } // for now eat exceptions
            return ""
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
    }
}