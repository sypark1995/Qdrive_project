package com.giosis.library.server

import com.giosis.library.server.data.FailedCodeResult
import com.giosis.library.server.data.ImageResult
import com.giosis.library.util.DataUtil
import com.giosis.library.util.Preferences
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
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


    @Multipart
    @POST("GMKT.INC.FileUpload/Upload.aspx?")
    fun upload(
            @Query("size") size: String,
            @Query("ext") ext: String,
            @Query("folder") folder: String,
            @Query("basepath") basepath: String,
            @Query("width") width: String,
            @Query("height") height: String,
            @Query("quality") quality: String,
            @Query("allsizeResize") allsizeResize: String,
            @Query("remainSrcImage") remainSrcImage: String,
            @Query("extent") extent: String,
            @Query("id") id: String,
            @Query("commitYn") commitYn: String,
            @Query("upload_channel") upload_channel: String,
            @Query("png_compress") png_compress: String,
            @Query("inc_org_size") inc_org_size: String,
            @Query("date") date: String,
            @Query("result_flag") result_flag: String,
            @Part file: MultipartBody.Part
    ): Call<Array<ImageResult>>


    @POST("GetCustomSellerInfo")
    @FormUrlEncoded
    fun requestGetCustomSellerInfo(
            @Field("search_kind") kind: String,
            @Field("search_value") value: String,
            @Field("nation_cd") nation_cd: String = Preferences.userNation
    ): Single<APIModel>


    @POST("SetSelfPickupOrder")
    @FormUrlEncoded
    fun requestSetSelfPickupOrder(
            @Field("custNo") custNo: String,
            @Field("rcvName") rcvName: String = Preferences.userName,
            @Field("pickupDate") pickupDate: String = "",
            @Field("pickupTime") pickupTime: String = "",
            @Field("country") country: String = "SG",
            @Field("zipcode") zipcode: String,
            @Field("addr1") addr1: String,
            @Field("addr2") addr2: String,
            @Field("mobileNo") mobileNo: String,
            @Field("telNo") telNo: String = "",
            @Field("quantity") quantity: String = "1",
            @Field("requestMemo") requestMemo: String,
            @Field("regId") regId: String = Preferences.userId,
            @Field("type") type: String
    ): Single<APIModel>


    @POST("GetAddressInfo")
    @FormUrlEncoded
    fun requestGetAddressInfo(
            @Field("search_value") search_value: String,
            @Field("search_type") search_type: String = "",
            @Field("svc_nation_cd") svc_nation_cd: String = Preferences.userNation,
            @Field("page_no") page_no: Int = 1,
            @Field("page_size") page_size: Int = 100,
            @Field("course_type") course_type: String = ""
    ): Single<APIModel>


    @POST("GetCommonCodeData")
    @FormUrlEncoded
    fun requestGetFailedCode(
            @Field("cd_type") cd_type: String,
            @Field("nation_cd") nation_cd: String
    ): Single<FailedCodeResult>


    @POST("WriteLog")
    @FormUrlEncoded
    fun requestWriteLog(
            @Field("logType") type: String,// -	1(info) , 2(Warm), 3(Error), 4(Fatal), 5(Debug)
            @Field("location") location: String,
            @Field("msg") msg: String,
            @Field("detail") detail: String
    ): Single<APIModel>


    @POST("WriteDumpLog")
    @FormUrlEncoded
    fun requestWriteDumpLog(
            @Field("logType") type: String,// -	1(info) , 2(Warm), 3(Error), 4(Fatal), 5(Debug)
            @Field("location") location: String,
            @Field("msg") msg: String,
            @Field("detail") detail: String,
            @Field("ext") ext: String,  // 확장자
            @Field("dumpfile") dumpFile: String // 첨부파일
    ): Single<APIModel>


    @POST("GetValidationCheckDpc3Out")
    @FormUrlEncoded
    fun requestValidationCheckDpc3Out(
            @Field("scanData") scanData: String,
            @Field("type") type: String,
            @Field("driverId") driverId: String = Preferences.userId,
            @Field("app_id") app_id: String = DataUtil.appID,
            @Field("nation_cd") nation_cd: String = Preferences.userNation
    ): Single<APIModel>

    @POST("GetChangeDriverValidationCheck")
    @FormUrlEncoded
    fun requestValidationCheckChangeDriver(
            @Field("scanData") scanData: String,
            @Field("driverId") driverId: String = Preferences.userId,
            @Field("app_id") app_id: String = DataUtil.appID,
            @Field("nation_cd") nation_cd: String = Preferences.userNation
    ): Single<APIModel>

    @POST("SetPickupScanNo")
    @FormUrlEncoded
    fun requestValidationCheckPickup(
            @Field("pickup_no") pickup_no: String,
            @Field("scan_no") scan_no: String,
            @Field("type") type: String = "QX",
            @Field("opId") opId: String = Preferences.userId,
            @Field("app_id") app_id: String = DataUtil.appID,
            @Field("nation_cd") nation_cd: String = Preferences.userNation
    ): Single<APIModel>

    @POST("SetAddScanNo_TakeBack")
    @FormUrlEncoded
    fun requestValidationCheckTakeBack(
            @Field("pickup_no") pickup_no: String,
            @Field("scan_no") scan_no: String,
            @Field("op_id") op_id: String = Preferences.userId,
            @Field("app_id") app_id: String = DataUtil.appID,
            @Field("nation_cd") nation_cd: String = Preferences.userNation
    ): Single<APIModel>

    @POST("GetCnROrderCheck")
    @FormUrlEncoded
    fun requestValidationCheckCnR(
            @Field("pickup_no") pickup_no: String,
            @Field("opId") opId: String = Preferences.userId,
            @Field("app_id") app_id: String = DataUtil.appID,
            @Field("nation_cd") nation_cd: String = Preferences.userNation
    ): Single<APIModel>
}