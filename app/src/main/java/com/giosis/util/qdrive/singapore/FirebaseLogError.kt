package com.giosis.util.qdrive.singapore

import android.content.Context
import android.telephony.TelephonyManager
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object FirebaseLogError {

    var qoo10Result = ""
    var daumResult = ""
    var teleInfo = ""
    var nowTime = ""

    fun pingCheck() {

        CoroutineScope(Dispatchers.IO).launch {
            val result: String = try {
                val runTime = Runtime.getRuntime()
                val cmd = "ping -c 1 -W 10 qxapi.qxpress.net"

                val proc = runTime.exec(cmd)
                proc.waitFor()

                proc.exitValue().toString()
            } catch (e: Exception) {
                e.toString()
            }

            val returnString = when (result) {
                "0" -> "Ping Success"
                "1" -> "Ping Fail"
                "2" -> "Ping Error"
                else -> result
            }

            FirebaseCrashlytics.getInstance().setCustomKey(
                "PING qxapi",
                returnString
            )

            nowTimeCheck()
            urlConnectionCheck()
            telephonyInfo()

            if (result != "0") {

                delay(1000)

                val resultString = qoo10Result + daumResult + nowTime + teleInfo
                FirebaseCrashlytics.getInstance().setCustomKey(
                    "ERROR INFO", resultString
                )

                RetrofitClient.instanceDynamic().requestWriteLog(
                    "1", "PING ERROR", "DNS error in RetrofitClient",
                    resultString
                ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ }) {}
            }
        }
    }

    private fun urlConnectionCheck() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val url = URL("https://www.qoo10.com")
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                qoo10Result = try {
                    "qoo10 connect / ${urlConnection.responseCode}"
                } catch (e: java.lang.Exception) {
                    "qoo10 connect $e"
                }

                val url1 = URL("https://www.daum.net")
                val urlConnection1: HttpURLConnection = url1.openConnection() as HttpURLConnection
                daumResult = try {
                    "daum connect / ${urlConnection1.responseCode}"
                } catch (e: java.lang.Exception) {
                    "daum connect $e"
                }
            }
        }
    }

    private fun nowTimeCheck() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val regDataString = dateFormat.format(Date())

        nowTime = " / Time : $regDataString"
    }

    private fun telephonyInfo() {
        val tm =
            MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        teleInfo = "Telephony " + tm.simOperatorName
    }

    fun adminLogCallApi(string: String) {
        RetrofitClient.instanceDynamic().requestWriteLog(
            "1", " ERROR", "DNS error in RetrofitClient",
            string + " / "+ qoo10Result + daumResult + nowTime + teleInfo
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }) {}
    }
}