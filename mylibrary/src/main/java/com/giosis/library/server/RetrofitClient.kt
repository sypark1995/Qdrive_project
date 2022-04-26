package com.giosis.library.server

import android.util.Log
import com.giosis.library.util.DataUtil
import com.giosis.library.util.Preferences
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    const val TAG = "Retrofit"
    const val errorTag = "Retrofit_Error"

    class AppInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
            val newRequest = request().newBuilder()
                .addHeader("User-Agent", Preferences.userAgent)
                .build()

            proceed(newRequest)
        }
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.i(TAG, message + "")
            }
        })

        // BASIC
        // HEADERS
        // BODY
        return interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    }


    private fun provideOkHttpClient(interceptor: AppInterceptor): OkHttpClient =
        provideOkHttpClient(interceptor, false)


    private fun provideOkHttpClient(interceptor: AppInterceptor, image: Boolean): OkHttpClient =
        OkHttpClient.Builder().run {
            addInterceptor(interceptor)
            addInterceptor(loggingInterceptor())

            if (image) {
                // 이미지 업데이트 시 타임아웃 시간을 10 -> 60 으로 변경
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
                connectTimeout(60, TimeUnit.SECONDS)
            } else {
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                connectTimeout(30, TimeUnit.SECONDS)
            }

            build()
        }


    private lateinit var instanceDynamic: RetrofitService

    fun instanceDynamic(): RetrofitService {

        val serverURL = Preferences.serverURL + DataUtil.API_ADDRESS
        //   Log.e("Server", "Server URL  $serverURL")

        val retrofit = Retrofit.Builder()
            .baseUrl(serverURL)
            .client(provideOkHttpClient(AppInterceptor()))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }

    fun instanceCoroutine(): RetrofitService {

        val serverURL = Preferences.serverURL + DataUtil.API_ADDRESS
        //   Log.e("Server", "Server URL  $serverURL")

        return Retrofit.Builder()
            .baseUrl(serverURL)
            .client(provideOkHttpClient(AppInterceptor()))
            .build()
            .create(RetrofitService::class.java)
    }


    fun instanceBarcode(): RetrofitService {

        // "http://image.qxpress.net/code128/code128.php?no="
        val barcodeUrl = "http://image.qxpress.net/"

        val retrofit = Retrofit.Builder()
            .baseUrl(barcodeUrl)
            .client(provideOkHttpClient(AppInterceptor()))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }

    fun instanceImageUpload(): RetrofitService {

        val gson = GsonBuilder().setLenient().create()

        val imageUrl = "http://encoding.image-gmkt.com/"

        val retrofit = Retrofit.Builder()
            .baseUrl(imageUrl)
            .client(provideOkHttpClient(AppInterceptor(), true))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }

    fun instanceXRoute(): RetrofitService {

        //val xRouteUrl = "http://xrouter.qxpress.net/"
//        val xRouteUrl = "http://211.115.100.24/"
        val xRouteUrl = Preferences.xRouteServerURL

        val retrofit = Retrofit.Builder()
            .baseUrl(xRouteUrl)
            .client(provideOkHttpClient(AppInterceptor()))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }


    fun instanceMobileService(): RetrofitService {

        val serverURL = Preferences.serverURL + DataUtil.API_ADDRESS_MOBILE_SERVICE
        Log.e("Server", "Server URL  $serverURL")

        val retrofit = Retrofit.Builder()
            .baseUrl(serverURL)
            .client(provideOkHttpClient(AppInterceptor()))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }
}