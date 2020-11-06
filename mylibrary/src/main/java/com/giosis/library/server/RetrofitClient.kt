package com.giosis.library.server

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    const val TAG = "Retrofit"
    private var BASE_URL: String = ""
        get() {
            return "https://qxapi.qxpress.net/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi/"
        }

    class AppInterceptor(private val userAgent: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
            val newRequest = request().newBuilder()
                    .addHeader("User-Agent", userAgent)
                    .build()

            proceed(newRequest)
        }
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor { message ->
            Log.i(TAG, message + "")
        }
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
    fun instanceDynamic(userAgent: String): RetrofitService {

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(provideOkHttpClient(AppInterceptor(userAgent)))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }
}