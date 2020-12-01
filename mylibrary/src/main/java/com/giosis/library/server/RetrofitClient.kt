package com.giosis.library.server

import android.util.Log
import com.giosis.library.util.DataUtil
import com.giosis.library.util.Preferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    const val TAG = "Retrofit"
    private var BASE_URL: String = ""
        get() {
            return "https://qxapi.qxpress.net/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi/"
        }

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
            OkHttpClient.Builder().run {
                addInterceptor(interceptor)
                addInterceptor(loggingInterceptor())
                build()
            }

    private lateinit var instanceDynamic: RetrofitService
    fun instanceDynamic(): RetrofitService {

        val serverURL = Preferences.serverURL + DataUtil.API_ADDRESS
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

    fun instanceTestServer(): RetrofitService {

        val serverURL = DataUtil.SERVER_TEST + DataUtil.API_ADDRESS
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

        val imageUrl = "http://encoding.image-gmkt.com/"

        val retrofit = Retrofit.Builder()
                .baseUrl(imageUrl)
                .client(provideOkHttpClient(AppInterceptor()))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }

    fun instanceXRoute(): RetrofitService {

        //val xRouteUrl = "http://xrouter.qxpress.net/"
        val xRouteUrl = "http://211.115.100.24/"

        val retrofit = Retrofit.Builder()
                .baseUrl(xRouteUrl)
                .client(provideOkHttpClient(AppInterceptor()))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }

}