package com.giosis.util.qdrive.singapore

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object FirebaseLogError {

//    private suspend fun pingCheck() {
//        val result: String = try {
//            val runTime = Runtime.getRuntime()
//            val cmd = "ping -c 1 -W 10 api.qxpress.net"
//
//            val proc = runTime.exec(cmd)
//            proc.waitFor()
//
//            proc.exitValue().toString()
//        } catch (e: Exception) {
//            e.toString()
//        }
//        val returnString = when (result) {
//            "0" -> "Ping Success"
//            "1" -> "Ping Fail"
//            "2" -> "Ping Error"
//            else -> result
//        }
//
//        FirebaseCrashlytics.getInstance().setCustomKey(
//            "PING qxapi",
//            returnString
//        )
//    }

    private fun urlConnectionCheck(): String {
//        Log.e("TAG", "urlConnectionCheck s")
        var daum = ""
        runCatching {
            val url1 = URL("https://www.daum.net")
            val urlConnection1: HttpURLConnection = url1.openConnection() as HttpURLConnection
            urlConnection1.responseCode

        }.onSuccess {
            daum = "daum connect / $it"
        }.onFailure {
            daum = "daum connect $it"
        }
//        Log.e("TAG", "urlConnectionCheck e")
        return daum
    }

    private fun nowTimeCheck(): String {
//        Log.e("TAG", "nowTimeCheck s")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val regDataString = dateFormat.format(Date())
//        Log.e("TAG", "nowTimeCheck E")
        return " / Time : $regDataString"
    }

    private fun telephonyInfo(): String {
//        Log.e("TAG", "telephonyInfo s")
        val tm =
            MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

//        Log.e("TAG", "telephonyInfo e")
        return "Telephony " + tm.simOperatorName
    }

    private fun nsLookup1(): String {
//        Log.e("TAG", "nsLookup1 s")
        var lookup = ""
        runCatching {
            val ipAddress = InetAddress.getByName("qxpress.net")
            ipAddress.hostName + " / " + ipAddress.hostAddress

        }.onSuccess {
            lookup = it
        }.onFailure {
            lookup = "qxpress.net : $it"
        }
//        Log.e("TAG", "nsLookup1 e")
        return lookup
    }

    private fun nsLookup2(): String {
//        Log.e("TAG", "nsLookup2 s")
        var lookup = ""
        runCatching {
            val ipAddress = InetAddress.getByName("api.qxpress.net")
            ipAddress.hostName + " / " + ipAddress.hostAddress

        }.onSuccess {
            lookup = it
        }.onFailure {
            lookup = "api.qxpress.net :$it"
        }
//        Log.e("TAG", "nsLookup2 e")
        return lookup
    }

    private fun nsLookup3(): String {
//        Log.e("TAG", "nsLookup3 s")
        var lookup = ""
        runCatching {
            val ipAddress = InetAddress.getByName("qoo10.sg")
            ipAddress.hostName + " / " + ipAddress.hostAddress

        }.onSuccess {
            lookup = it
        }.onFailure {
            lookup = "qoo10.sg :$it"
        }
//        Log.e("TAG", "nsLookup3 e")
        return lookup
    }

    fun adminLogCallApi(string: String) {
        CoroutineScope(Dispatchers.IO).launch {

            var nsLookup1 = ""
            var nsLookup2 = ""
            var nsLookup3 = ""
            var urlCon = ""

            val time = nowTimeCheck()

            launch {
                nsLookup1 = nsLookup1()
            }

            launch {
                nsLookup2 = nsLookup2()
            }

            launch {
                nsLookup3 = nsLookup3()
            }

            launch {
                urlCon = urlConnectionCheck()
            }

            delay(1000)

            val tel = telephonyInfo()

//            Log.e(
//                "TAG",
//                "\n $string \n $time \n $nsLookup1 \n $nsLookup2 \n $nsLookup3 \n $urlCon \n $tel "
//            )

            RetrofitClient.instanceDynamic().requestWriteLog(
                "1", " ERROR", "string DNS error in RetrofitClient",
                "\n $string \n $time \n $nsLookup1 \n $nsLookup2 \n $nsLookup3 \n $urlCon \n $tel "
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }) {}
        }
    }


    fun adminLogImage(string: String) {

        nowTimeCheck()

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)

            var nsLookup1 = ""
            var nsLookup2 = ""
            var nsLookup3 = ""
            var urlCon = ""

            val time = nowTimeCheck()

            launch {
                nsLookup1 = nsLookup1()
            }

            launch {
                nsLookup2 = nsLookup2()
            }

            launch {
                nsLookup3 = nsLookup3()
            }

            launch {
                urlCon = urlConnectionCheck()
            }

            delay(1000)

            val tel = telephonyInfo()

            RetrofitClient.instanceMobileService()
                .requestWriteLog(
                    "1",
                    "IMAGEUPLOAD",
                    "image upload error in RetrofitClient",
                    "\n $string \n $time \n $nsLookup1 \n $nsLookup2 \n $nsLookup3 \n $urlCon \n $tel "
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ })
                { }
        }

    }
}