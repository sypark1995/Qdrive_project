package com.giosis.util.qdrive.server

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {

    @POST("LoginQXQuick")
    @FormUrlEncoded
    fun requestServerLogin(
        @Field("login_id") login_id: String,
        @Field("password") password: String,
        @Field("admin_yn") admin_yn: String,
        @Field("referer") referer: String,
        @Field("vehicle") vehicle: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String
//        @Field("chanel") chanel: String = MobileAPI.APP_ID,
//        @Field("ip") ip: String = "",
//        @Field("app_id") app_id: String = MobileAPI.APP_ID,
//        @Field("nation_cd") nation_cd: String = MobileAPI.NATION_CODE
    ): Call<APIModel>

}