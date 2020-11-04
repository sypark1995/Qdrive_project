package com.giosis.util.qdrive.server

import com.giosis.util.qdrive.singapore.MyApplication
import com.giosis.util.qdrive.util.QDataUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    const val TAG = "Retrofit"
    private var BASE_URL :String = ""
        get() {
            return "https://qxapi.qxpress.net/GMKT.INC.GLPS.MobileApiService/GlobalMobileService.qapi"
        }

    class AppInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response = with(chain) {

            val newRequest = request().newBuilder()
                    .addHeader("User-Agent", QDataUtil.getCustomUserAgent(MyApplication.getContext())) // TODO kjyoo user agent check
                    .build()

            proceed(newRequest)
        }
    }

    private fun provideOkHttpClient(interceptor: AppInterceptor): OkHttpClient =
            OkHttpClient.Builder().run {

                addInterceptor(interceptor)
                build()
            }

    private lateinit var instanceDynamic: RetrofitService
    fun instanceDynamic(): RetrofitService {

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(provideOkHttpClient(AppInterceptor()))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        instanceDynamic = retrofit.create(RetrofitService::class.java)
        return instanceDynamic
    }
}