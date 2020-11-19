package com.giosis.library.server

import com.giosis.library.util.Preferences
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface RetrofitService {

    @POST("LoginQDRIVE")
    @FormUrlEncoded
    fun requestServerLogin(
            @Field("login_id") login_id: String,
            @Field("password") password: String,
            @Field("chanel") chanel: String,
            @Field("ip") ip: String,
            @Field("referer") referer: String,
            @Field("vehicle") vehicle: String,
            @Field("latitude") latitude: String,
            @Field("longitude") longitude: String,
            @Field("app_id") app_id: String,
            @Field("nation_cd") nation_cd: String
    ): Single<APIModel>


    @POST("changePassword")
    @FormUrlEncoded
    fun requestChangePwd(
            @Field("op_id") login_id: String,
            @Field("old_pwd") old_pwd: String,
            @Field("new_pwd") new_pwd: String,
            @Field("app_id") app_id: String,
            @Field("nation_cd") nation_cd: String,
    ): Single<APIModel>


    @POST("changeMyInfo")
    @FormUrlEncoded
    fun requestChangeMyInfo(
            @Field("name") name: String,
            @Field("email") email: String,
            @Field("op_id") op_id: String = Preferences.userId,
            @Field("app_id") app_id: String = "QDRIVE",
            @Field("nation_cd") nation_cd: String = Preferences.userNation,
    ): Single<APIModel>

    @POST("GetNoticeData")
    @FormUrlEncoded
    fun requestGetNoticeData(
            @Field("nid") nid: String,
            @Field("gubun") gubun: String = "DETAIL",
            @Field("page_no") page_no: Int = 0,
            @Field("page_size") page_size: Int = 0,
            @Field("opId") opId: String = Preferences.userId,
            @Field("officeCd") officeCode: String = Preferences.officeCode,
            @Field("kind") kind: String = "QSIGN",
            @Field("svc_nation_cd") svc_natiion_cd: String = Preferences.userNation,
            @Field("app_id") app_id: String = "QDRIVE",
            @Field("nation_cd") nation_cd: String = Preferences.userNation
    ): Single<APIModel>


    @POST("GetShuttleDriverForFederatedlockerInfo")
    @FormUrlEncoded
    fun requestGetLockerUserInfo(
            @Field("op_id") opId: String = Preferences.userId,
            @Field("app_id") app_id: String = "QDRIVE",
            @Field("nation_cd") nation_cd: String = Preferences.userNation
    ): Single<APIModel>


    @GET("code128/code128.php")
    fun requestGetBarcode(
            @Query("no") no: String
    ): Single<ResponseBody>
}