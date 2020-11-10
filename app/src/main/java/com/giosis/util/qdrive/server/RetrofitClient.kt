package com.giosis.util.qdrive.server

import android.util.Log
import com.giosis.util.qdrive.singapore.MyApplication
import com.giosis.util.qdrive.util.DataUtil
import com.giosis.util.qdrive.util.QDataUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    const val TAG = "Retrofit"
    private var BASE_URL :String = ""
        get() {
            return "https://qxapi.qxpress.net/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi/"
        }

    class AppInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
            val newRequest = request().newBuilder()
                    .addHeader("User-Agent", QDataUtil.getCustomUserAgent(MyApplication.getContext())) // TODO kjyoo user agent check
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
        return interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    private fun provideOkHttpClient(interceptor: AppInterceptor): OkHttpClient =
            OkHttpClient.Builder().run {
                addInterceptor(interceptor)
                addInterceptor(loggingInterceptor())
                build()
            }

    private lateinit var instanceDynamic: RetrofitService
    fun instanceDynamic(): RetrofitService {

        val url = MyApplication.preferences.serverURL + DataUtil.API_ADDRESS + "/"
        Log.e("krm0219", "instanceDynamic  URL $BASE_URL   $url")

        val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(provideOkHttpClient(AppInterceptor()))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }
}