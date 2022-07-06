package com.giosis.util.qdrive.singapore

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object FirebaseLogError {

    var qoo10Result = ""
    var daumResult = ""
    var teleInfo = ""
    var nowTime = ""
    var qxpressUrl = ""
    var apiQxpressUrl = ""
    var sgQxpressUrl = ""

    fun pingCheck() {

        CoroutineScope(Dispatchers.IO).launch {
            val result: String = try {
                val runTime = Runtime.getRuntime()
                val cmd = "ping -c 1 -W 10 api.qxpress.net"

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
            nsLookup()

            if (result != "0") {

                delay(1000)

                val resultString =
                    qoo10Result + daumResult + nowTime + teleInfo + qxpressUrl + apiQxpressUrl
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
                urlConnection.responseCode

            }.onSuccess {
                qoo10Result = "qoo10 connect / $it"
            }.onFailure {
                qoo10Result = "qoo10 connect $it"
            }

            runCatching {
                val url1 = URL("https://www.daum.net")
                val urlConnection1: HttpURLConnection = url1.openConnection() as HttpURLConnection
                urlConnection1.responseCode

            }.onSuccess {
                daumResult = "daum connect / $it"
            }.onFailure {
                daumResult = "daum connect $it"
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

    private fun nsLookup() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val ipAddress = InetAddress.getByName("qxpress.net")
                ipAddress.hostName + " / " + ipAddress.hostAddress

            }.onSuccess {
                qxpressUrl = it
            }.onFailure {
                qxpressUrl = "api.qxpress.net : $it"
            }

            runCatching {
                val ipAddress = InetAddress.getByName("api.qxpress.net")
                ipAddress.hostName + " / " + ipAddress.hostAddress

            }.onSuccess {
                apiQxpressUrl = it
            }.onFailure {
                apiQxpressUrl = "api.qxpress.net :$it"
            }

            runCatching {
                val ipAddress = InetAddress.getByName("qoo10.sg")
                ipAddress.hostName + " / " + ipAddress.hostAddress

            }.onSuccess {
                sgQxpressUrl = it
            }.onFailure {
                sgQxpressUrl = "qoo10.sg :$it"
            }
        }
    }

    fun adminLogCallApi(string: String) {
        RetrofitClient.instanceDynamic().requestWriteLog(
            "1", " ERROR", "DNS error in RetrofitClient",
            "$string / $qoo10Result\n$daumResult\n$nowTime\n$teleInfo\n$qxpressUrl\n$apiQxpressUrl\n$sgQxpressUrl"
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }) {}
    }


    fun adminLogImage(string: String) {

        nowTimeCheck()

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)

            urlConnectionCheck()
            telephonyInfo()

            RetrofitClient.instanceMobileService()
                .requestWriteLog(
                    "1",
                    "IMAGEUPLOAD",
                    "image upload error in RetrofitClient",
                    "$qoo10Result\n$daumResult\n$nowTime\n$teleInfo\n$qxpressUrl\n$apiQxpressUrl\n$sgQxpressUrl\nRetrofitClient Exception $string"
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ })
                { }
        }

    }
}