package com.giosis.util.qdrive.singapore.main

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.giosis.util.qdrive.singapore.MyApplication.Companion.context
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.UploadData
import com.giosis.util.qdrive.singapore.data.QSignDeliveryList
import com.giosis.util.qdrive.singapore.data.QSignPickupList
import com.giosis.util.qdrive.singapore.database.DatabaseHelper
import com.giosis.util.qdrive.singapore.server.RetrofitClient
import com.giosis.util.qdrive.singapore.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_home.view.*
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object MainActivityServer {
    const val TAG = "MainDownload"

    const val PFC = "PFC"
    const val DFC = "DFC"

    suspend fun download(progress: ProgressDialog, network: String): String {

        // 2020.12  Failed Code 가져오기
        getDFCFailedCode()
        getPFCFailedCode()

        try {
            DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "")
        } catch (e: java.lang.Exception) {
            try {
                val params = Bundle()
                params.putString("Activity", TAG)
                params.putString("message", " DB.delete > $e")
            } catch (ignored: java.lang.Exception) {
            }
        }

        var pickupList = ArrayList<QSignPickupList>()
        var deliveryList = ArrayList<QSignDeliveryList>()
        var outletDeliveryList = ArrayList<QSignDeliveryList>()
        var errorMsg = ""

        try {
            val response =
                RetrofitClient.instanceCoroutine().requestGetPickupList(network)

            if (response.resultCode >= 0) {
                pickupList = Gson().fromJson(
                    response.resultObject,
                    object : TypeToken<ArrayList<QSignPickupList>>() {}.type
                )

            } else {
                errorMsg += response.resultMsg
            }

        } catch (e: java.lang.Exception) {

        }

        try {
            val response =
                RetrofitClient.instanceCoroutine().requestGetDeliveryList(network)

            if (response.resultCode >= 0) {
                deliveryList = Gson().fromJson(
                    response.resultObject,
                    object : TypeToken<ArrayList<QSignDeliveryList>>() {}.type
                )
            } else {
                errorMsg += response.resultMsg
            }
        } catch (e: java.lang.Exception) {

        }

        if (Preferences.userNation == "SG") {
            try {

                FirebaseCrashlytics.getInstance().setCustomKey("network Type", network)
                try {
                    pingCheck()
                } catch (e: Exception) {

                }

                val response =
                    RetrofitClient.instanceCoroutine().requestGetDeliveryOutlet(network)

                val testData =
                //    "[{\"contr_no\":\"155480148\",\"partner_ref_no\":\"KRSG7789656\",\"invoice_no\":\"SGP181775380\",\"stat\":\"D3\",\"rcv_nm\":\"Homer\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9367-5763\",\"zip_code\":\"100088\",\"address\":\"88 TELOK BLANGAH HEIGHTS Telok Blangah Heights (Operation Hours: 24 Hours)\",\"sender_nm\":\"mezzang\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오후 12:41:55\",\"route\":\"FL PK138 88 Telok Blangah Heights\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"19.98\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.277519,103.808172\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155635756\",\"partner_ref_no\":\"SGSG30384487\",\"invoice_no\":\"SGP181851697\",\"stat\":\"D3\",\"rcv_nm\":\"Rachel Chok\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9693-5810\",\"zip_code\":\"108943\",\"address\":\"TELOK BLANGAH COMMUNITY CLUB 450 TELOK BLANGAH STREET 31 At L2 Corridor (Operation Hours: 24 hours)\",\"sender_nm\":\"JR Life Sciences Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 12:40:57\",\"route\":\"FL PK444 Telok Blangah CC, 450 Telok Blangah Street 31\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"156.01\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.274824,103.807841\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155660794\",\"partner_ref_no\":\"SGSG30388581\",\"invoice_no\":\"SGP181881719\",\"stat\":\"D3\",\"rcv_nm\":\"Lim Han Siong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9169-1438\",\"zip_code\":\"129580\",\"address\":\"CLEMENTI MRT STATION 3150 COMMONWEALTH AVENUE WEST #02-01 (Operation hours: 24 hours)\",\"sender_nm\":\"TheMobileHub\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"If send to home, please knock hard, door bell not working.\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 4:26:31\",\"route\":\"7E 754 Clementi MRT\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"29.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.315073,103.765231\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155645794\",\"partner_ref_no\":\"SGSG30387764\",\"invoice_no\":\"SGP181629815\",\"stat\":\"D3\",\"rcv_nm\":\"Genevieve Lee\",\"tel_no\":\"+65-9749-4024\",\"hp_no\":\"+65-9749-4024\",\"zip_code\":\"130002\",\"address\":\"DOVER COURT 2 DOVER ROAD Near Lift B (Operation Hours: 24 hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 2:01:44\",\"route\":\"FL PK753 2 Dover Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"20.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.303032,103.782950\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155644000\",\"partner_ref_no\":\"SGSG30386609\",\"invoice_no\":\"SGP181871259\",\"stat\":\"D3\",\"rcv_nm\":\"Young C Kiang\",\"tel_no\":\"+65-6523-3693\",\"hp_no\":\"+65-9769-1998\",\"zip_code\":\"130022\",\"address\":\"DOVER VILLE 22 DOVER CRESCENT Near Lift B (Operation Hours: 24 hours)\",\"sender_nm\":\"Essentials (SG) Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Please call 97691998 before delivery\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:46:07\",\"route\":\"FL PK933 22 Dover Crescent\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"9.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.307100,103.783533\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155671667\",\"partner_ref_no\":\"SGSG30387502\",\"invoice_no\":\"SGP181849627\",\"stat\":\"D3\",\"rcv_nm\":\"Jennifer Chew\",\"tel_no\":\"+65-9630-4480\",\"hp_no\":\"+65-9630-4480\",\"zip_code\":\"140019\",\"address\":\"19 QUEENS CLOSE Next to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"SGMART\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 7:22:01\",\"route\":\"FL PK144 19 Queens Close\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"8.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.292301,103.800170\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155648389\",\"partner_ref_no\":\"SGSG30384329\",\"invoice_no\":\"SGP181807198\",\"stat\":\"D3\",\"rcv_nm\":\"Jennifer Chew\",\"tel_no\":\"+65-9630-4480\",\"hp_no\":\"+65-9630-4480\",\"zip_code\":\"140019\",\"address\":\"19 QUEENS CLOSE Next to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"iPrincess\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 2:30:44\",\"route\":\"FL PK144 19 Queens Close\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"8.70\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.292301,103.800170\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155283840\",\"partner_ref_no\":\"CNSG5325899\",\"invoice_no\":\"SGP181678175\",\"stat\":\"D3\",\"rcv_nm\":\"Diana Ong\",\"tel_no\":\"+65-9119-6442\",\"hp_no\":\"+65-9119-6442\",\"zip_code\":\"150053\",\"address\":\"53 LENGKOK BAHRU Near Staircase (Operation Hours: 24 hours)\",\"sender_nm\":\"SMTinternationaltrade\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-19 오후 1:41:35\",\"route\":\"FL PK743 53 Lengkok Bahru\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"29.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.288862,103.813475\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155637855\",\"partner_ref_no\":\"SGSG30384516\",\"invoice_no\":\"SGP181829923\",\"stat\":\"D3\",\"rcv_nm\":\"Eddie Chong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9078-2992\",\"zip_code\":\"160022\",\"address\":\"22 Havelock Road #01-707 (Operation Hours: 24 hours)\",\"sender_nm\":\"BEAUTYPARF ENTERPRISE  PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Pls help to send out ASAP.\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 12:55:59\",\"route\":\"7E 759 HDB Bukit Ho Swee Court\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"34.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.288746,103.828840\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155643615\",\"partner_ref_no\":\"SGSG30388389\",\"invoice_no\":\"SGP181631904\",\"stat\":\"D3\",\"rcv_nm\":\"Leong Aaron\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8716-0780\",\"zip_code\":\"161051\",\"address\":\"HAVELOCK VIEW 51 HAVELOCK ROAD Along Walkway to BS10231 (Operation Hours: 24 Hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:43:09\",\"route\":\"FL PK149 51 Havelock Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"10.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.289919,103.827573\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155480008\",\"partner_ref_no\":\"KRSG7789635\",\"invoice_no\":\"SGP181781394\",\"stat\":\"D3\",\"rcv_nm\":\"Geoffrey Soo\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9832-1132\",\"zip_code\":\"266268\",\"address\":\"TAN KAH KEE MRT STATION 651 BUKIT TIMAH ROAD Exit A (Operation Hours: 7.00am to 11.30pm)\",\"sender_nm\":\"mezzang\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오후 12:40:51\",\"route\":\"FL PK1012 Tan Kah Kee MRT Station, 651 Bukit Timah Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"26.98\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.326337,103.806844\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155672429\",\"partner_ref_no\":\"SGSG30388534\",\"invoice_no\":\"SGP181827600\",\"stat\":\"D3\",\"rcv_nm\":\"Joel Lai\",\"tel_no\":\"+65-8884-3058\",\"hp_no\":\"+65-8884-3058\",\"zip_code\":\"590002\",\"address\":\"TOH YI GARDENS 2 TOH YI DRIVE Block 2 (Operation Hours: 24 Hours)\",\"sender_nm\":\"XAVIER\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 7:46:31\",\"route\":\"FL PK020 2 Toh Yi Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"1.62\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.339095,103.775286\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155632124\",\"partner_ref_no\":\"SGSG30384328\",\"invoice_no\":\"SGP181814595\",\"stat\":\"D3\",\"rcv_nm\":\"Jovin Lim\",\"tel_no\":\"+65-9686-4364\",\"hp_no\":\"+65-9686-4364\",\"zip_code\":\"590011\",\"address\":\"TOH YI GARDENS 11 TOH YI DRIVE Near Center Lift Lobby (Operation Hours: 24 hours)\",\"sender_nm\":\"John\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오전 11:43:22\",\"route\":\"FL PK355 11 Toh Yi Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"8.74\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.337960,103.772575\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155667278\",\"partner_ref_no\":\"SGSG30386052\",\"invoice_no\":\"SGP181862411\",\"stat\":\"D3\",\"rcv_nm\":\"Shawn\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8228-6420\",\"zip_code\":\"600110\",\"address\":\"JURONG EAST VILLE 110 JURONG EAST STREET 13 Block 110 (Operation Hours: 24 Hours)\",\"sender_nm\":\"Bolehmart\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 5:35:42\",\"route\":\"FL PK009 110 Jurong East St. 13\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"10.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.339008,103.736496\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155643611\",\"partner_ref_no\":\"SGSG30388475\",\"invoice_no\":\"SGP181618840\",\"stat\":\"D3\",\"rcv_nm\":\"Siew Chuin\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8438-5186\",\"zip_code\":\"600210\",\"address\":\"210 JURONG EAST STREET 21 Near Lift E (Operation Hours: 24 hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:43:09\",\"route\":\"FL PK604 210 Jurong East Street 21\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"10.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.338821,103.738617\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155635757\",\"partner_ref_no\":\"SGSG30384526\",\"invoice_no\":\"SGP181857637\",\"stat\":\"D3\",\"rcv_nm\":\"Sandra\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9692-8794\",\"zip_code\":\"610182\",\"address\":\"CORPORATION SPRING 182 YUNG SHENG ROAD Near Staircase B (Operation Hours: 24 hours)\",\"sender_nm\":\"JR Life Sciences Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 12:40:57\",\"route\":\"FL PK581 182 Yung Sheng Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"71.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.333762,103.722511\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155656966\",\"partner_ref_no\":\"SGSG30386479\",\"invoice_no\":\"SGP181866107\",\"stat\":\"D3\",\"rcv_nm\":\"Firdaus Mohamad\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8498-8956\",\"zip_code\":\"730671\",\"address\":\"671 WOODLANDS DRIVE 71 Near Lift A \\u0026amp; B (Operation Hours: 24 hours)\",\"sender_nm\":\"Clinton Chang\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 3:49:26\",\"route\":\"FL PK836 671 Woodlands Drive 71\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"5.99\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.439092,103.798784\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155632820\",\"partner_ref_no\":\"SGSG30385859\",\"invoice_no\":\"SGP181866108\",\"stat\":\"D3\",\"rcv_nm\":\"Firdaus Mohamad\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8498-8956\",\"zip_code\":\"730671\",\"address\":\"671 WOODLANDS DRIVE 71 Near Lift A \\u0026amp; B (Operation Hours: 24 hours)\",\"sender_nm\":\"Kaki Bukit\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오전 11:55:50\",\"route\":\"FL PK836 671 Woodlands Drive 71\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"6.98\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.439092,103.798784\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155632717\",\"partner_ref_no\":\"SGSG30387022\",\"invoice_no\":\"SGP181873557\",\"stat\":\"D3\",\"rcv_nm\":\"tan caibao\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9247-7369\",\"zip_code\":\"730768\",\"address\":\"768 Woodlands Avenue 6 #01-06 Woodlands Mart (Operation Hours: 24 Hours)\",\"sender_nm\":\"OKONZ\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Fragile\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오전 11:53:23\",\"route\":\"7E 134 Woodlands Mart\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"22.99\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.445855,103.798158\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155632055\",\"partner_ref_no\":\"SGSG30383596\",\"invoice_no\":\"SGP181870956\",\"stat\":\"D3\",\"rcv_nm\":\"Shahrun Adam Angullia\",\"tel_no\":\"+65-8511-1736\",\"hp_no\":\"+65-8292-4549\",\"zip_code\":\"730794\",\"address\":\"794 WOODLANDS DRIVE 72 Near Lift A (Operation Hours: 24 hours)\",\"sender_nm\":\"Fida International (S) Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오전 11:42:47\",\"route\":\"FL PK873 794 Woodlands Drive 72\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"84.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.442580,103.803565\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155464083\",\"partner_ref_no\":\"KRSG7789369\",\"invoice_no\":\"SGP181749973\",\"stat\":\"D3\",\"rcv_nm\":\"Christina\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8181-1405\",\"zip_code\":\"733685\",\"address\":\"685C WOODLANDS DRIVE 73 Near to Lift Lobby (Operation Hours: 24 Hours)\",\"sender_nm\":\"KIM MINJAE\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오전 9:34:30\",\"route\":\"FL PK212 685C Woodlands Drive 73\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"26.98\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.441736,103.805795\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155643480\",\"partner_ref_no\":\"SGSG30388419\",\"invoice_no\":\"SGP181620084\",\"stat\":\"D3\",\"rcv_nm\":\"Lee Si Rong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9008-3656\",\"zip_code\":\"750406\",\"address\":\"406 Sembawang Drive #01-822 (Operation Hours: 24 hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Put beside shoe rack under window if no one is around\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:42:38\",\"route\":\"7E 738 HDB Sembawang 406\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"60.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.452908,103.816945\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155672422\",\"partner_ref_no\":\"SGSG30388561\",\"invoice_no\":\"SGP181837353\",\"stat\":\"D3\",\"rcv_nm\":\"Khai Thim Sew\",\"tel_no\":\"+65-9622-2419\",\"hp_no\":\"+65-9622-2419\",\"zip_code\":\"750470\",\"address\":\"470 SEMBAWANG DRIVE Near to Lift Lobby (Operation Hours: 24 Hours)\",\"sender_nm\":\"XAVIER\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 7:46:31\",\"route\":\"FL PK215 470 Sembawang Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"4.72\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.451515,103.815095\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155642442\",\"partner_ref_no\":\"SGSG30386080\",\"invoice_no\":\"SGP181803802\",\"stat\":\"D3\",\"rcv_nm\":\"Zoe\",\"tel_no\":\"+65-6610-8465\",\"hp_no\":\"+65-9846-0950\",\"zip_code\":\"750474\",\"address\":\"474 SEMBAWANG DRIVE Near Switch Room (Operation Hours: 24 hours)\",\"sender_nm\":\"abbottsgflagship\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:33:05\",\"route\":\"FL PK345 474 Sembawang Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"80.62\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.450690,103.816508\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155667883\",\"partner_ref_no\":\"SGSG30386197\",\"invoice_no\":\"SGP181863411\",\"stat\":\"D3\",\"rcv_nm\":\"Zuhe\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8694-1164\",\"zip_code\":\"750486\",\"address\":\"486 ADMIRALTY LINK Near Switch Room (Operation Hours: 24 hours)\",\"sender_nm\":\"Straitsstar Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 5:45:13\",\"route\":\"FL PK331 486 Admiralty Link\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"20.99\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.456002,103.816122\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155642390\",\"partner_ref_no\":\"SGSG30386169\",\"invoice_no\":\"SGP181807753\",\"stat\":\"D3\",\"rcv_nm\":\"Ng Leong Keong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8154-1853\",\"zip_code\":\"760123\",\"address\":\"123 Yishun Street 11 #01-495 (Operation Hours: 24 Hours)\",\"sender_nm\":\"abbottsgflagship\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:32:39\",\"route\":\"7E 171 HDB Yishun 123\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"63.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.434649,103.831798\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155665709\",\"partner_ref_no\":\"SGSG30387250\",\"invoice_no\":\"SGP181860577\",\"stat\":\"D3\",\"rcv_nm\":\"Angeline Tan\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9106-3412\",\"zip_code\":\"760149\",\"address\":\"149 YISHUN STREET 11 Near to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"Century Healthcare Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 5:16:39\",\"route\":\"FL PK219 149 Yishun Street 11\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"15.97\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.431516,103.832771\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155662345\",\"partner_ref_no\":\"SGSG30384655\",\"invoice_no\":\"SGP181772692\",\"stat\":\"D3\",\"rcv_nm\":\"Tan Khai Ming\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9387-7866\",\"zip_code\":\"760220\",\"address\":\"YISHUN GARDENS 220 YISHUN STREET 21 Block 220 (Operation Hours: 24 Hours)\",\"sender_nm\":\"Sean\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 4:40:37\",\"route\":\"FL PK041 220 Yishun Street 21\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"9.76\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.433325,103.835280\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155663899\",\"partner_ref_no\":\"SGSG30388093\",\"invoice_no\":\"SGP181820967\",\"stat\":\"D3\",\"rcv_nm\":\"Law Suan Leng\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8918-3931\",\"zip_code\":\"760269\",\"address\":\"269 YISHUN STREET 22 Near Switch Room (Operation Hours: 24 hours)\",\"sender_nm\":\"Progreso Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 4:58:37\",\"route\":\"FL PK463 269 Yishun Street 22\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"12.16\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.437098,103.840317\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155316802\",\"partner_ref_no\":\"USSG502968\",\"invoice_no\":\"SGP181723871\",\"stat\":\"D3\",\"rcv_nm\":\"EmilyL.\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9181-9482\",\"zip_code\":\"760280\",\"address\":\"280 YISHUN STREET 22 Block 280 (Operation Hours: 24 Hours)\",\"sender_nm\":\"QXPRESS USA INC\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-20 오전 1:51:32\",\"route\":\"FL PK043 280 Yishun Street 22\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"70.16\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.438633,103.836976\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155643612\",\"partner_ref_no\":\"SGSG30388369\",\"invoice_no\":\"SGP181629910\",\"stat\":\"D3\",\"rcv_nm\":\"Lee Chu Hong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8200-9016\",\"zip_code\":\"760296\",\"address\":\"296 YISHUN STREET 20 Below #02-05 (Operation Hours: 24 hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:43:09\",\"route\":\"FL PK267 296 Yishun Street 20\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"10.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.435357,103.835327\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155635920\",\"partner_ref_no\":\"SGSG30386086\",\"invoice_no\":\"SGP181829495\",\"stat\":\"D3\",\"rcv_nm\":\"EI MON KYAW\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8184-0851\",\"zip_code\":\"760296\",\"address\":\"296 YISHUN STREET 20 Below #02-05 (Operation Hours: 24 hours)\",\"sender_nm\":\"CHLOE\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 12:41:58\",\"route\":\"FL PK267 296 Yishun Street 20\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"26.20\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.435357,103.835327\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155653093\",\"partner_ref_no\":\"SGSG30383972\",\"invoice_no\":\"SGP181846466\",\"stat\":\"D3\",\"rcv_nm\":\"Lim Chee Hiong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9389-9104\",\"zip_code\":\"760624\",\"address\":\"624 YISHUN RING ROAD Block 624 (Operation Hours: 24 Hours)\",\"sender_nm\":\"MIZON KOREA\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 3:10:47\",\"route\":\"FL PK032 624 Yishun Ring Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"53.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.417692,103.835422\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155656217\",\"partner_ref_no\":\"SGSG30388158\",\"invoice_no\":\"SGP181852653\",\"stat\":\"D3\",\"rcv_nm\":\"Mr Chen\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9885-9491\",\"zip_code\":\"760870\",\"address\":\"870 YISHUN STREET 81 Near Switch Room (Operation Hours: 24 Hours)\",\"sender_nm\":\"NS HARDWARE PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 3:38:24\",\"route\":\"FL PK264 870 Yishun Street 81\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"27.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.413110,103.837967\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155670493\",\"partner_ref_no\":\"SGSG30388144\",\"invoice_no\":\"SGP181875917\",\"stat\":\"D3\",\"rcv_nm\":\"Jasmine\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9788-0891\",\"zip_code\":\"791414\",\"address\":\"FERNVALE RIVERBOW 414A FERNVALE LINK Near Letterbox (Operation Hours: 24 hours)\",\"sender_nm\":\"Kinohimitsu\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 6:40:53\",\"route\":\"FL PK549 414A Fernvale Link\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"53.80\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.389550,103.879475\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155291994\",\"partner_ref_no\":\"CNSG5325992\",\"invoice_no\":\"SGP181612500\",\"stat\":\"D3\",\"rcv_nm\":\"Julie\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8798-4253\",\"zip_code\":\"590011\",\"address\":\"TOH YI GARDENS 11 TOH YI DRIVE Near Center Lift Lobby (Operation Hours: 24 hours)\",\"sender_nm\":\"wanghaiping\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-19 오후 3:05:12\",\"route\":\"FL PK355 11 Toh Yi Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"23.25\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.337960,103.772575\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155216552\",\"partner_ref_no\":\"CNSG5325778\",\"invoice_no\":\"SGP181647811\",\"stat\":\"D3\",\"rcv_nm\":\"J Yap\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8620-8876\",\"zip_code\":\"640501\",\"address\":\"501 Jurong West Street 51 #01-255 (Operation Hours: 24 Hours)\",\"sender_nm\":\"Wei\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-18 오후 3:24:32\",\"route\":\"7E 043 HDB Hong Kah Point\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"4.50\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.350356,103.719105\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155483815\",\"partner_ref_no\":\"KRSG7789709\",\"invoice_no\":\"SGP181753799\",\"stat\":\"D3\",\"rcv_nm\":\"Sakthi Kalathy\",\"tel_no\":\"+65-9003-6570\",\"hp_no\":\"+65-3159-9700\",\"zip_code\":\"640659\",\"address\":\"659 Jurong West Street 65 #01-01 (Operation Hours: 24 hours)\",\"sender_nm\":\"이희연(와이엔디.에프(Y\\u0026D.F))\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오후 1:09:46\",\"route\":\"7E 539 HDB Jurong West 659\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"180.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.337302,103.702118\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155463297\",\"partner_ref_no\":\"KRSG7789323\",\"invoice_no\":\"SGP181693263\",\"stat\":\"D3\",\"rcv_nm\":\"yx\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8737-5998\",\"zip_code\":\"649846\",\"address\":\"BOON LAY MRT STATION 301 BOON LAY WAY #01-35 (Operation Hours: 24 Hours)\",\"sender_nm\":\"TAEHA GLOBAL\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오전 9:30:59\",\"route\":\"7E 224 Boon Lay MRT #01-35\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"111.84\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.338530,103.706040\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155462858\",\"partner_ref_no\":\"KRSG7789312\",\"invoice_no\":\"SGP181746960\",\"stat\":\"D3\",\"rcv_nm\":\"Neo Koon Kian\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9831-9203\",\"zip_code\":\"760149\",\"address\":\"149 YISHUN STREET 11 Near to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"주식회사트렉시\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오전 9:29:07\",\"route\":\"FL PK219 149 Yishun Street 11\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"11.99\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.431516,103.832771\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155360895\",\"partner_ref_no\":\"CNSG5326178\",\"invoice_no\":\"SGP181646662\",\"stat\":\"D3\",\"rcv_nm\":\"Tanoy Edward Allan\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9477-1627\",\"zip_code\":\"760285\",\"address\":\"285 Yishun Avenue 6 #01-04 (Operation Hours: 24 Hours)\",\"sender_nm\":\"ROWEE\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-20 오후 2:52:50\",\"route\":\"7E 375 HDB Yishun 285\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"3.40\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.439917,103.839388\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155483839\",\"partner_ref_no\":\"KRSG7789711\",\"invoice_no\":\"SGP181799068\",\"stat\":\"D3\",\"rcv_nm\":\"Sue Jane\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9614-2026\",\"zip_code\":\"794430\",\"address\":\"FERNVALE RIDGE 430D FERNVALE LINK Next to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"이희연(와이엔디.에프(Y\\u0026D.F))\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Please deliver to 430D, #10-243\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오후 1:10:03\",\"route\":\"FL PK112 430D Fernvale Link\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"18.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.394852,103.878028\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null}]"
                 "[{\"contr_no\":\"155480148\",\"partner_ref_no\":\"KRSG7789656\",\"invoice_no\":\"SGP181775380\",\"stat\":\"D3\",\"rcv_nm\":\"Homer\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9367-5763\",\"zip_code\":\"100088\",\"address\":\"88 TELOK BLANGAH HEIGHTS Telok Blangah Heights (Operation Hours: 24 Hours)\",\"sender_nm\":\"mezzang\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오후 12:41:55\",\"route\":\"FL PK138 88 Telok Blangah Heights\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"19.98\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.277519,103.808172\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155635756\",\"partner_ref_no\":\"SGSG30384487\",\"invoice_no\":\"SGP181851697\",\"stat\":\"D3\",\"rcv_nm\":\"Rachel Chok\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9693-5810\",\"zip_code\":\"108943\",\"address\":\"TELOK BLANGAH COMMUNITY CLUB 450 TELOK BLANGAH STREET 31 At L2 Corridor (Operation Hours: 24 hours)\",\"sender_nm\":\"JR Life Sciences Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 12:40:57\",\"route\":\"FL PK444 Telok Blangah CC, 450 Telok Blangah Street 31\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"156.01\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.274824,103.807841\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155660794\",\"partner_ref_no\":\"SGSG30388581\",\"invoice_no\":\"SGP181881719\",\"stat\":\"D3\",\"rcv_nm\":\"Lim Han Siong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9169-1438\",\"zip_code\":\"129580\",\"address\":\"CLEMENTI MRT STATION 3150 COMMONWEALTH AVENUE WEST #02-01 (Operation hours: 24 hours)\",\"sender_nm\":\"TheMobileHub\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"If send to home, please knock hard, door bell not working.\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 4:26:31\",\"route\":\"7E 754 Clementi MRT\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"29.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.315073,103.765231\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155645794\",\"partner_ref_no\":\"SGSG30387764\",\"invoice_no\":\"SGP181629815\",\"stat\":\"D3\",\"rcv_nm\":\"Genevieve Lee\",\"tel_no\":\"+65-9749-4024\",\"hp_no\":\"+65-9749-4024\",\"zip_code\":\"130002\",\"address\":\"DOVER COURT 2 DOVER ROAD Near Lift B (Operation Hours: 24 hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 2:01:44\",\"route\":\"FL PK753 2 Dover Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"20.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.303032,103.782950\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155644000\",\"partner_ref_no\":\"SGSG30386609\",\"invoice_no\":\"SGP181871259\",\"stat\":\"D3\",\"rcv_nm\":\"Young C Kiang\",\"tel_no\":\"+65-6523-3693\",\"hp_no\":\"+65-9769-1998\",\"zip_code\":\"130022\",\"address\":\"DOVER VILLE 22 DOVER CRESCENT Near Lift B (Operation Hours: 24 hours)\",\"sender_nm\":\"Essentials (SG) Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Please call 97691998 before delivery\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:46:07\",\"route\":\"FL PK933 22 Dover Crescent\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"9.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.307100,103.783533\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155671667\",\"partner_ref_no\":\"SGSG30387502\",\"invoice_no\":\"SGP181849627\",\"stat\":\"D3\",\"rcv_nm\":\"Jennifer Chew\",\"tel_no\":\"+65-9630-4480\",\"hp_no\":\"+65-9630-4480\",\"zip_code\":\"140019\",\"address\":\"19 QUEENS CLOSE Next to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"SGMART\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 7:22:01\",\"route\":\"FL PK144 19 Queens Close\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"8.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.292301,103.800170\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155648389\",\"partner_ref_no\":\"SGSG30384329\",\"invoice_no\":\"SGP181807198\",\"stat\":\"D3\",\"rcv_nm\":\"Jennifer Chew\",\"tel_no\":\"+65-9630-4480\",\"hp_no\":\"+65-9630-4480\",\"zip_code\":\"140019\",\"address\":\"19 QUEENS CLOSE Next to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"iPrincess\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 2:30:44\",\"route\":\"FL PK144 19 Queens Close\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"8.70\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.292301,103.800170\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155283840\",\"partner_ref_no\":\"CNSG5325899\",\"invoice_no\":\"SGP181678175\",\"stat\":\"D3\",\"rcv_nm\":\"Diana Ong\",\"tel_no\":\"+65-9119-6442\",\"hp_no\":\"+65-9119-6442\",\"zip_code\":\"150053\",\"address\":\"53 LENGKOK BAHRU Near Staircase (Operation Hours: 24 hours)\",\"sender_nm\":\"SMTinternationaltrade\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-19 오후 1:41:35\",\"route\":\"FL PK743 53 Lengkok Bahru\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"29.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.288862,103.813475\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155637855\",\"partner_ref_no\":\"SGSG30384516\",\"invoice_no\":\"SGP181829923\",\"stat\":\"D3\",\"rcv_nm\":\"Eddie Chong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9078-2992\",\"zip_code\":\"160022\",\"address\":\"22 Havelock Road #01-707 (Operation Hours: 24 hours)\",\"sender_nm\":\"BEAUTYPARF ENTERPRISE  PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Pls help to send out ASAP.\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 12:55:59\",\"route\":\"7E 754 HDB Bukit Ho Swee Court\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"34.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.288746,103.828840\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155643615\",\"partner_ref_no\":\"SGSG30388389\",\"invoice_no\":\"SGP181631904\",\"stat\":\"D3\",\"rcv_nm\":\"Leong Aaron\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8716-0780\",\"zip_code\":\"161051\",\"address\":\"HAVELOCK VIEW 51 HAVELOCK ROAD Along Walkway to BS10231 (Operation Hours: 24 Hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:43:09\",\"route\":\"FL PK149 51 Havelock Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"10.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.289919,103.827573\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155480008\",\"partner_ref_no\":\"KRSG7789635\",\"invoice_no\":\"SGP181781394\",\"stat\":\"D3\",\"rcv_nm\":\"Geoffrey Soo\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9832-1132\",\"zip_code\":\"266268\",\"address\":\"TAN KAH KEE MRT STATION 651 BUKIT TIMAH ROAD Exit A (Operation Hours: 7.00am to 11.30pm)\",\"sender_nm\":\"mezzang\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오후 12:40:51\",\"route\":\"FL PK1012 Tan Kah Kee MRT Station, 651 Bukit Timah Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"26.98\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.326337,103.806844\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155672429\",\"partner_ref_no\":\"SGSG30388534\",\"invoice_no\":\"SGP181827600\",\"stat\":\"D3\",\"rcv_nm\":\"Joel Lai\",\"tel_no\":\"+65-8884-3058\",\"hp_no\":\"+65-8884-3058\",\"zip_code\":\"590002\",\"address\":\"TOH YI GARDENS 2 TOH YI DRIVE Block 2 (Operation Hours: 24 Hours)\",\"sender_nm\":\"XAVIER\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 7:46:31\",\"route\":\"FL PK020 2 Toh Yi Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"1.62\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.339095,103.775286\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155632124\",\"partner_ref_no\":\"SGSG30384328\",\"invoice_no\":\"SGP181814595\",\"stat\":\"D3\",\"rcv_nm\":\"Jovin Lim\",\"tel_no\":\"+65-9686-4364\",\"hp_no\":\"+65-9686-4364\",\"zip_code\":\"590011\",\"address\":\"TOH YI GARDENS 11 TOH YI DRIVE Near Center Lift Lobby (Operation Hours: 24 hours)\",\"sender_nm\":\"John\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오전 11:43:22\",\"route\":\"FL PK355 11 Toh Yi Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"8.74\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.337960,103.772575\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155667278\",\"partner_ref_no\":\"SGSG30386052\",\"invoice_no\":\"SGP181862411\",\"stat\":\"D3\",\"rcv_nm\":\"Shawn\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8228-6420\",\"zip_code\":\"600110\",\"address\":\"JURONG EAST VILLE 110 JURONG EAST STREET 13 Block 110 (Operation Hours: 24 Hours)\",\"sender_nm\":\"Bolehmart\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 5:35:42\",\"route\":\"FL PK009 110 Jurong East St. 13\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"10.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.339008,103.736496\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155643611\",\"partner_ref_no\":\"SGSG30388475\",\"invoice_no\":\"SGP181618840\",\"stat\":\"D3\",\"rcv_nm\":\"Siew Chuin\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8438-5186\",\"zip_code\":\"600210\",\"address\":\"210 JURONG EAST STREET 21 Near Lift E (Operation Hours: 24 hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:43:09\",\"route\":\"FL PK604 210 Jurong East Street 21\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"10.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.338821,103.738617\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155635757\",\"partner_ref_no\":\"SGSG30384526\",\"invoice_no\":\"SGP181857637\",\"stat\":\"D3\",\"rcv_nm\":\"Sandra\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9692-8794\",\"zip_code\":\"610182\",\"address\":\"CORPORATION SPRING 182 YUNG SHENG ROAD Near Staircase B (Operation Hours: 24 hours)\",\"sender_nm\":\"JR Life Sciences Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 12:40:57\",\"route\":\"FL PK581 182 Yung Sheng Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"71.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.333762,103.722511\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155656966\",\"partner_ref_no\":\"SGSG30386479\",\"invoice_no\":\"SGP181866107\",\"stat\":\"D3\",\"rcv_nm\":\"Firdaus Mohamad\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8498-8956\",\"zip_code\":\"730671\",\"address\":\"671 WOODLANDS DRIVE 71 Near Lift A \\u0026amp; B (Operation Hours: 24 hours)\",\"sender_nm\":\"Clinton Chang\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 3:49:26\",\"route\":\"FL PK836 671 Woodlands Drive 71\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"5.99\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.439092,103.798784\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155632820\",\"partner_ref_no\":\"SGSG30385859\",\"invoice_no\":\"SGP181866108\",\"stat\":\"D3\",\"rcv_nm\":\"Firdaus Mohamad\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8498-8956\",\"zip_code\":\"730671\",\"address\":\"671 WOODLANDS DRIVE 71 Near Lift A \\u0026amp; B (Operation Hours: 24 hours)\",\"sender_nm\":\"Kaki Bukit\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오전 11:55:50\",\"route\":\"FL PK836 671 Woodlands Drive 71\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"6.98\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.439092,103.798784\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155632717\",\"partner_ref_no\":\"SGSG30387022\",\"invoice_no\":\"SGP181873557\",\"stat\":\"D3\",\"rcv_nm\":\"tan caibao\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9247-7369\",\"zip_code\":\"730768\",\"address\":\"768 Woodlands Avenue 6 #01-06 Woodlands Mart (Operation Hours: 24 Hours)\",\"sender_nm\":\"OKONZ\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Fragile\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오전 11:53:23\",\"route\":\"7E 754 Woodlands Mart\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"22.99\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.445855,103.798158\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155632055\",\"partner_ref_no\":\"SGSG30383596\",\"invoice_no\":\"SGP181870956\",\"stat\":\"D3\",\"rcv_nm\":\"Shahrun Adam Angullia\",\"tel_no\":\"+65-8511-1736\",\"hp_no\":\"+65-8292-4549\",\"zip_code\":\"730794\",\"address\":\"794 WOODLANDS DRIVE 72 Near Lift A (Operation Hours: 24 hours)\",\"sender_nm\":\"Fida International (S) Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오전 11:42:47\",\"route\":\"FL PK873 794 Woodlands Drive 72\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"84.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.442580,103.803565\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155464083\",\"partner_ref_no\":\"KRSG7789369\",\"invoice_no\":\"SGP181749973\",\"stat\":\"D3\",\"rcv_nm\":\"Christina\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8181-1405\",\"zip_code\":\"733685\",\"address\":\"685C WOODLANDS DRIVE 73 Near to Lift Lobby (Operation Hours: 24 Hours)\",\"sender_nm\":\"KIM MINJAE\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오전 9:34:30\",\"route\":\"FL PK212 685C Woodlands Drive 73\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"26.98\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.441736,103.805795\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155643480\",\"partner_ref_no\":\"SGSG30388419\",\"invoice_no\":\"SGP181620084\",\"stat\":\"D3\",\"rcv_nm\":\"Lee Si Rong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9008-3656\",\"zip_code\":\"750406\",\"address\":\"406 Sembawang Drive #01-822 (Operation Hours: 24 hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Put beside shoe rack under window if no one is around\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:42:38\",\"route\":\"7E 754 HDB Sembawang 406\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"60.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.452908,103.816945\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155672422\",\"partner_ref_no\":\"SGSG30388561\",\"invoice_no\":\"SGP181837353\",\"stat\":\"D3\",\"rcv_nm\":\"Khai Thim Sew\",\"tel_no\":\"+65-9622-2419\",\"hp_no\":\"+65-9622-2419\",\"zip_code\":\"750470\",\"address\":\"470 SEMBAWANG DRIVE Near to Lift Lobby (Operation Hours: 24 Hours)\",\"sender_nm\":\"XAVIER\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 7:46:31\",\"route\":\"FL PK215 470 Sembawang Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"4.72\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.451515,103.815095\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155642442\",\"partner_ref_no\":\"SGSG30386080\",\"invoice_no\":\"SGP181803802\",\"stat\":\"D3\",\"rcv_nm\":\"Zoe\",\"tel_no\":\"+65-6610-8465\",\"hp_no\":\"+65-9846-0950\",\"zip_code\":\"750474\",\"address\":\"474 SEMBAWANG DRIVE Near Switch Room (Operation Hours: 24 hours)\",\"sender_nm\":\"abbottsgflagship\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:33:05\",\"route\":\"FL PK345 474 Sembawang Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"80.62\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.450690,103.816508\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155667883\",\"partner_ref_no\":\"SGSG30386197\",\"invoice_no\":\"SGP181863411\",\"stat\":\"D3\",\"rcv_nm\":\"Zuhe\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8694-1164\",\"zip_code\":\"750486\",\"address\":\"486 ADMIRALTY LINK Near Switch Room (Operation Hours: 24 hours)\",\"sender_nm\":\"Straitsstar Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 5:45:13\",\"route\":\"FL PK331 486 Admiralty Link\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"20.99\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.456002,103.816122\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155642390\",\"partner_ref_no\":\"SGSG30386169\",\"invoice_no\":\"SGP181807753\",\"stat\":\"D3\",\"rcv_nm\":\"Ng Leong Keong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8154-1853\",\"zip_code\":\"760123\",\"address\":\"123 Yishun Street 11 #01-495 (Operation Hours: 24 Hours)\",\"sender_nm\":\"abbottsgflagship\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:32:39\",\"route\":\"7E 754 HDB Yishun 123\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"63.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.434649,103.831798\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155665709\",\"partner_ref_no\":\"SGSG30387250\",\"invoice_no\":\"SGP181860577\",\"stat\":\"D3\",\"rcv_nm\":\"Angeline Tan\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9106-3412\",\"zip_code\":\"760149\",\"address\":\"149 YISHUN STREET 11 Near to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"Century Healthcare Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 5:16:39\",\"route\":\"FL PK219 149 Yishun Street 11\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"15.97\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.431516,103.832771\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155662345\",\"partner_ref_no\":\"SGSG30384655\",\"invoice_no\":\"SGP181772692\",\"stat\":\"D3\",\"rcv_nm\":\"Tan Khai Ming\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9387-7866\",\"zip_code\":\"760220\",\"address\":\"YISHUN GARDENS 220 YISHUN STREET 21 Block 220 (Operation Hours: 24 Hours)\",\"sender_nm\":\"Sean\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 4:40:37\",\"route\":\"FL PK041 220 Yishun Street 21\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"9.76\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.433325,103.835280\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155663899\",\"partner_ref_no\":\"SGSG30388093\",\"invoice_no\":\"SGP181820967\",\"stat\":\"D3\",\"rcv_nm\":\"Law Suan Leng\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8918-3931\",\"zip_code\":\"760269\",\"address\":\"269 YISHUN STREET 22 Near Switch Room (Operation Hours: 24 hours)\",\"sender_nm\":\"Progreso Pte Ltd\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 4:58:37\",\"route\":\"FL PK463 269 Yishun Street 22\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"12.16\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.437098,103.840317\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155316802\",\"partner_ref_no\":\"USSG502968\",\"invoice_no\":\"SGP181723871\",\"stat\":\"D3\",\"rcv_nm\":\"EmilyL.\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9181-9482\",\"zip_code\":\"760280\",\"address\":\"280 YISHUN STREET 22 Block 280 (Operation Hours: 24 Hours)\",\"sender_nm\":\"QXPRESS USA INC\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-20 오전 1:51:32\",\"route\":\"FL PK043 280 Yishun Street 22\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"70.16\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.438633,103.836976\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155643612\",\"partner_ref_no\":\"SGSG30388369\",\"invoice_no\":\"SGP181629910\",\"stat\":\"D3\",\"rcv_nm\":\"Lee Chu Hong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8200-9016\",\"zip_code\":\"760296\",\"address\":\"296 YISHUN STREET 20 Below #02-05 (Operation Hours: 24 hours)\",\"sender_nm\":\"GLADLEIGH PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 1:43:09\",\"route\":\"FL PK267 296 Yishun Street 20\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"10.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.435357,103.835327\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155635920\",\"partner_ref_no\":\"SGSG30386086\",\"invoice_no\":\"SGP181829495\",\"stat\":\"D3\",\"rcv_nm\":\"EI MON KYAW\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8184-0851\",\"zip_code\":\"760296\",\"address\":\"296 YISHUN STREET 20 Below #02-05 (Operation Hours: 24 hours)\",\"sender_nm\":\"CHLOE\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 12:41:58\",\"route\":\"FL PK267 296 Yishun Street 20\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"26.20\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.435357,103.835327\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155653093\",\"partner_ref_no\":\"SGSG30383972\",\"invoice_no\":\"SGP181846466\",\"stat\":\"D3\",\"rcv_nm\":\"Lim Chee Hiong\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9389-9104\",\"zip_code\":\"760624\",\"address\":\"624 YISHUN RING ROAD Block 624 (Operation Hours: 24 Hours)\",\"sender_nm\":\"MIZON KOREA\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 3:10:47\",\"route\":\"FL PK032 624 Yishun Ring Road\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"53.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.417692,103.835422\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155656217\",\"partner_ref_no\":\"SGSG30388158\",\"invoice_no\":\"SGP181852653\",\"stat\":\"D3\",\"rcv_nm\":\"Mr Chen\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9885-9491\",\"zip_code\":\"760870\",\"address\":\"870 YISHUN STREET 81 Near Switch Room (Operation Hours: 24 Hours)\",\"sender_nm\":\"NS HARDWARE PTE LTD\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 3:38:24\",\"route\":\"FL PK264 870 Yishun Street 81\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"27.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.413110,103.837967\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155670493\",\"partner_ref_no\":\"SGSG30388144\",\"invoice_no\":\"SGP181875917\",\"stat\":\"D3\",\"rcv_nm\":\"Jasmine\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9788-0891\",\"zip_code\":\"791414\",\"address\":\"FERNVALE RIVERBOW 414A FERNVALE LINK Near Letterbox (Operation Hours: 24 hours)\",\"sender_nm\":\"Kinohimitsu\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-25 오후 6:40:53\",\"route\":\"FL PK549 414A Fernvale Link\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"53.80\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.389550,103.879475\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155291994\",\"partner_ref_no\":\"CNSG5325992\",\"invoice_no\":\"SGP181612500\",\"stat\":\"D3\",\"rcv_nm\":\"Julie\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8798-4253\",\"zip_code\":\"590011\",\"address\":\"TOH YI GARDENS 11 TOH YI DRIVE Near Center Lift Lobby (Operation Hours: 24 hours)\",\"sender_nm\":\"wanghaiping\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-19 오후 3:05:12\",\"route\":\"FL PK355 11 Toh Yi Drive\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"23.25\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.337960,103.772575\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155216552\",\"partner_ref_no\":\"CNSG5325778\",\"invoice_no\":\"SGP181647811\",\"stat\":\"D3\",\"rcv_nm\":\"J Yap\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8620-8876\",\"zip_code\":\"640501\",\"address\":\"501 Jurong West Street 51 #01-255 (Operation Hours: 24 Hours)\",\"sender_nm\":\"Wei\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-18 오후 3:24:32\",\"route\":\"7E 754 HDB Hong Kah Point\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"4.50\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.350356,103.719105\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155483815\",\"partner_ref_no\":\"KRSG7789709\",\"invoice_no\":\"SGP181753799\",\"stat\":\"D3\",\"rcv_nm\":\"Sakthi Kalathy\",\"tel_no\":\"+65-9003-6570\",\"hp_no\":\"+65-3159-9700\",\"zip_code\":\"640659\",\"address\":\"659 Jurong West Street 65 #01-01 (Operation Hours: 24 hours)\",\"sender_nm\":\"이희연(와이엔디.에프(Y\\u0026D.F))\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오후 1:09:46\",\"route\":\"7E 754 HDB Jurong West 659\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"180.00\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.337302,103.702118\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155463297\",\"partner_ref_no\":\"KRSG7789323\",\"invoice_no\":\"SGP181693263\",\"stat\":\"D3\",\"rcv_nm\":\"yx\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-8737-5998\",\"zip_code\":\"649846\",\"address\":\"BOON LAY MRT STATION 301 BOON LAY WAY #01-35 (Operation Hours: 24 Hours)\",\"sender_nm\":\"TAEHA GLOBAL\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오전 9:30:59\",\"route\":\"7E 754 Boon Lay MRT #01-35\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"111.84\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.338530,103.706040\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155462858\",\"partner_ref_no\":\"KRSG7789312\",\"invoice_no\":\"SGP181746960\",\"stat\":\"D3\",\"rcv_nm\":\"Neo Koon Kian\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9831-9203\",\"zip_code\":\"760149\",\"address\":\"149 YISHUN STREET 11 Near to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"주식회사트렉시\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오전 9:29:07\",\"route\":\"FL PK219 149 Yishun Street 11\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"11.99\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.431516,103.832771\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155360895\",\"partner_ref_no\":\"CNSG5326178\",\"invoice_no\":\"SGP181646662\",\"stat\":\"D3\",\"rcv_nm\":\"Tanoy Edward Allan\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9477-1627\",\"zip_code\":\"760285\",\"address\":\"285 Yishun Avenue 6 #01-04 (Operation Hours: 24 Hours)\",\"sender_nm\":\"ROWEE\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-20 오후 2:52:50\",\"route\":\"7E 754 HDB Yishun 285\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"3.40\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.439917,103.839388\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null},{\"contr_no\":\"155483839\",\"partner_ref_no\":\"KRSG7789711\",\"invoice_no\":\"SGP181799068\",\"stat\":\"D3\",\"rcv_nm\":\"Sue Jane\",\"tel_no\":\"+65--\",\"hp_no\":\"+65-9614-2026\",\"zip_code\":\"794430\",\"address\":\"FERNVALE RIDGE 430D FERNVALE LINK Next to Letter Box (Operation Hours: 24 Hours)\",\"sender_nm\":\"이희연(와이엔디.에프(Y\\u0026D.F))\",\"sender_hp_no\":null,\"sender_tel_no\":null,\"sender_zip_code\":null,\"sender_address\":null,\"del_memo\":\"Please deliver to 430D, #10-243\",\"driver_memo\":\"\",\"fail_reason\":\"  \",\"partner_ref_no_fail_assign\":null,\"reason_fail_assign\":null,\"delivery_count\":\"0\",\"delivery_first_date\":\"2022-05-23 오후 1:10:03\",\"route\":\"FL PK112 430D Fernvale Link\",\"secret_no_type\":\" \",\"secret_no\":\"\",\"del_hopeday\":\"\",\"course\":null,\"course_driver\":null,\"secure_delivery_yn\":\"N\",\"parcel_amount\":\"18.90\",\"currency\":\"SGD\",\"qwms_yn\":null,\"order_type_etc\":\"DPC\",\"del_hopedaybyDBData\":null,\"del_hopetime\":null,\"GoogleMap\":null,\"delivery_nation_cd\":null,\"lat_lng\":\"1.394852,103.878028\",\"high_amount_yn\":\"N\",\"receive_state\":null,\"receive_city\":null,\"receive_street\":null,\"sku_location_zone\":null,\"fresh_type\":null,\"order_type\":\"Standard\",\"desired_delivery_location\":null}]"
                if (response.resultCode >= 0) {
                    outletDeliveryList = Gson().fromJson(
                        response.resultObject,
                        object : TypeToken<ArrayList<QSignDeliveryList>>() {}.type
                    )

                } else {
                    errorMsg += response.resultMsg
                }

            } catch (e: java.lang.Exception) {
                delay(1000)
                RetrofitClient.instanceDynamic().requestWriteLog(
                    "1", "DOWNLOAD", "DNS error in RetrofitClient",
                    qoo10Result + daumResult + nowTime + teleInfo
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ }) {}
                e.printStackTrace()
            }
        }

        val totalItem = pickupList.size + deliveryList.size + outletDeliveryList.size

        Log.e(TAG, "end api $errorMsg total cnt = $totalItem")

        progress.max = totalItem
        var current = 0

        if (pickupList.size > 0) {
            withContext(Dispatchers.IO) {
                for (item in pickupList) {
                    insertDevicePickupData(item)
                    withContext(Dispatchers.Main) {
                        progress.progress = ++current
                    }
                }
            }
        }

        if (deliveryList.size > 0) {
            withContext(Dispatchers.IO) {
                for (item in deliveryList) {
                    insertDeviceDeliveryData(item)
                    withContext(Dispatchers.Main) {
                        progress.progress = ++current
                    }
                }
            }
        }

        if (outletDeliveryList.size > 0) {
            withContext(Dispatchers.IO) {
                for (item in outletDeliveryList) {
                    insertDeviceDeliveryData(item)
                    withContext(Dispatchers.Main) {
                        progress.progress = ++current
                    }
                }
            }
        }

        if (pickupList.size + deliveryList.size + outletDeliveryList.size == 0) {
            errorMsg += "There is no data to download"
        }

        return errorMsg
    }

    suspend fun getDFCFailedCode() {
        try {
            val result = RetrofitClient.instanceCoroutine().requestGetFailedCode(DFC)

            if (result.resultCode == 10) {
                val json = Gson().toJson(result.resultObject)
                Log.i(TAG, "D  getFailedCode  $json")
                Preferences.dFailedCode = json
            }
        } catch (e: Exception) {

        }

    }

    suspend fun getPFCFailedCode() {
        try {
            val result = RetrofitClient.instanceCoroutine().requestGetFailedCode(PFC)

            if (result.resultCode == 10) {
                val json = Gson().toJson(result.resultObject)
                Log.i(TAG, "P  getFailedCode  $json")
                Preferences.pFailedCode = json
            }
        } catch (e: Exception) {

        }
    }

    private fun insertDevicePickupData(data: QSignPickupList): Long {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val regDataString = dateFormat.format(Date())

        val contentVal = ContentValues().apply {
            put("contr_no", data.contrNo)

            // NOTIFICATION.  19/10 - Ref.Pickup No가 존재하면 리스트에서 해당 번호로 표시
            if (data.ref_pickup_no != "") {
                Log.e(TAG, "  Ref. Pickup No >> " + data.ref_pickup_no + " / " + data.partnerRefNo)
                put("partner_ref_no", data.ref_pickup_no)
            } else {
                put("partner_ref_no", data.partnerRefNo)
            }

            put("invoice_no", data.partnerRefNo) //invoice_no = partnerRefNo 사용
            put("stat", data.stat)
            put("tel_no", data.telNo)
            put("hp_no", data.hpNo)
            put("zip_code", data.zipCode)
            put("address", data.address)
            put("route", data.route)
            put("type", StatueType.TYPE_PICKUP)
            put("desired_date", data.pickupHopeDay)
            put("req_qty", data.qty)
            put("req_nm", data.reqName)
            put("rcv_request", data.delMemo)
            put("sender_nm", "")
            put("punchOut_stat", "N")
            put("reg_id", Preferences.userId)
            put("reg_dt", regDataString)
            put("fail_reason", data.failReason)
            put("secret_no_type", data.secretNoType)
            put("secret_no", data.secretNo)
            put("cust_no", data.custNo)
            put("partner_id", data.partnerID)

            if (data.route == "RPC") {
                put("desired_time", data.pickupHopeTime)
            }

            // 2020.06 위, 경도 저장
            val latLng = GeoCodeUtil.getLatLng(data.lat_lng)
            put("lat", latLng[0])
            put("lng", latLng[1])

            put("state", data.state)
            put("city", data.city)
            put("street", data.street)
        }

        return DatabaseHelper.getInstance()
            .insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal)
    }

    private fun insertDeviceDeliveryData(data: QSignDeliveryList): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val regDataString = dateFormat.format(Date())

        val contentVal = ContentValues().apply {
            put("contr_no", data.contr_no)
            put("partner_ref_no", data.partner_ref_no)
            put("invoice_no", data.invoice_no)
            put("stat", data.stat)
            put("rcv_nm", data.rcv_nm)
            put("sender_nm", data.sender_nm)
            put("tel_no", data.tel_no)
            put("hp_no", data.hp_no)
            put("zip_code", data.zip_code)
            put("address", data.address)
            put("rcv_request", data.del_memo)
            put("delivery_dt", data.delivery_first_date)
            put("type", StatueType.TYPE_DELIVERY)
            put("route", data.route)
            put("reg_id", Preferences.userId)
            put("reg_dt", regDataString)
            put("punchOut_stat", "N")
            put("driver_memo", data.driver_memo)
            put("fail_reason", data.fail_reason)
            put("secret_no_type", data.secret_no_type)
            put("secret_no", data.secret_no)
            put("secure_delivery_yn", data.secure_delivery_yn)
            put("parcel_amount", data.parcel_amount)
            put("currency", data.currency)
            put("order_type_etc", data.order_type_etc)

            // 2020.06 위, 경도 저장
            val latLng = GeoCodeUtil.getLatLng(data.lat_lng)
            put("lat", latLng[0])
            put("lng", latLng[1])

            // 2021.04  High Value
            put("high_amount_yn", data.high_amount_yn)
            // 2021.09 Economy

            put("order_type", data.order_type)
            put("state", data.state)
            put("city", data.city)
            put("street", data.street)
        }

        return DatabaseHelper.getInstance()
            .insert(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, contentVal)
    }

    class CountData {
        var inProgressCnt = 0
        var todayUploadedCnt = 0
        var uploadFailedCnt = 0
        var inProgressDeliveryCnt = 0
        var inProgressPickupCnt = 0
        var inProgressRpcCnt = 0
    }

    suspend fun getLocalCount(activity: MainActivity) {

        try {
            val selectQuery =
                "select ifnull(sum(case when chg_dt is null then 1 else 0 end), 0) as InprogressCnt " + //In-Progress
                        " , ifnull(sum(case when punchOut_stat = 'S' and strftime('%Y-%m-%d', chg_dt) = date('now') then 1 else 0 end) ,0) as TodayUploadedCnt " +// Uploaded Today
                        " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is not null  then 1 else 0 end), 0) as UploadFailedCnt " + //Upload Failed
                        " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and type = 'D' then 1 else 0 end), 0) as InprogressDeliveryCnt " +//Delivery
                        " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and type = 'P' and route <> 'RPC' then 1 else 0 end), 0) as InprogressPickupCnt " + //Pickup
                        " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and route = 'RPC' then 1 else 0 end), 0) as InprogressRpcCnt " + //RPC
                        " , datetime(max(reg_dt), 'localtime') as PI_Time " +
                        " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                        " where reg_id= '" + Preferences.userId + "'"

            val cs = DatabaseHelper.getInstance().get(selectQuery)

            if (cs.moveToFirst()) {

                val count = CountData().apply {
                    inProgressCnt = cs.getInt(cs.getColumnIndex("InprogressCnt"))
                    todayUploadedCnt = cs.getInt(cs.getColumnIndex("TodayUploadedCnt"))
                    uploadFailedCnt = cs.getInt(cs.getColumnIndex("UploadFailedCnt"))
                    inProgressDeliveryCnt =
                        cs.getInt(cs.getColumnIndex("InprogressDeliveryCnt"))
                    inProgressPickupCnt = cs.getInt(cs.getColumnIndex("InprogressPickupCnt"))
                    inProgressRpcCnt = cs.getInt(cs.getColumnIndex("InprogressRpcCnt"))
                }

                withContext(Dispatchers.Main) {
                    activity.main_view.text_home_total_qty.text =
                        (count.inProgressPickupCnt + count.todayUploadedCnt + count.uploadFailedCnt).toString()

                    activity.uploadFailedCount = count.uploadFailedCnt.toString()

                    activity.main_view.text_home_in_progress_count.text =
                        count.inProgressCnt.toString()
                    activity.main_view.text_home_delivery_count.text =
                        count.inProgressDeliveryCnt.toString()
                    activity.main_view.text_home_pickup_count.text =
                        count.inProgressPickupCnt.toString()
                    activity.main_view.text_home_rpc_count.text =
                        count.inProgressRpcCnt.toString()

                    //파트너 Office Header - RPC Change Driver 버튼 설정
                    if (Preferences.default == "Y") {
                        if (count.inProgressRpcCnt.toString() != "0") {
                            activity.main_view.btn_home_assign_pickup_driver.visibility =
                                View.VISIBLE
                        } else {
                            activity.main_view.btn_home_assign_pickup_driver.visibility =
                                View.GONE
                        }
                    }
                    Log.e(TAG, "getLocalCount finish")
                }
            }

        } catch (e: java.lang.Exception) {

        }

    }

    fun setDestroyUserInfo(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = dateFormat.format(Date())

                RetrofitClient.instanceDynamic().requestSetAppUserInfo2(
                    "killapp", NetworkUtil.getNetworkType(context), "", date,
                )

            } catch (e: java.lang.Exception) {

            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            Log.e(TAG, "requestSetDriverPerformanceLog start ")
            var accuracy = 0.0
            var latitude = 0.0
            var longitude = 0.0

            try {
                if ((context as MainActivity).gpsEnable && (context).gpsTrackerManager != null) {
                    latitude = (context).gpsTrackerManager!!.latitude
                    longitude = (context).gpsTrackerManager!!.longitude
                    accuracy = (context).gpsTrackerManager!!.accuracy
                }
            } catch (e: java.lang.Exception) {

            }

            val channel = if (Preferences.userNation == "SG") {
                "QDRIVE"
            } else {
                "QDRIVE_V2"
            }
            try {
                val response = RetrofitClient.instanceDynamic().requestSetDriverPerformanceLog(
                    channel, latitude.toString(), longitude.toString(), accuracy.toString()
                )

                if (response.resultCode < 0) {

                    var resultMsg = response.resultMsg
                    if (response.resultCode != -16) {
                        resultMsg = String.format(
                            context.getResources().getString(R.string.text_upload_failed_msg),
                            response.resultMsg
                        )
                    }

                    AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.text_upload_result))
                        .setMessage(resultMsg)
                        .setCancelable(true)
                        .setPositiveButton(
                            context.getResources().getString(R.string.button_ok)
                        ) { dialog1: DialogInterface?, which: Int ->
                            if (dialog1 != null) {
                                if (!(context as Activity).isFinishing) {
                                    dialog1.dismiss()
                                }
                            }
                        }
                        .create()
                        .show()
                }
            } catch (e: java.lang.Exception) {

            }
        }
        Log.e(TAG, "setDestroyUserInfo end ")
    }

    fun upload(context: Context) {
        (context as MainActivity).lifecycleScope.launch(Dispatchers.IO) {

            val songjangList = ArrayList<UploadData>()

            // 업로드 대상건 로컬 DB 조회
            val selectQuery = "select invoice_no" +
                    " , stat " +
                    " , ifnull(rcv_type, '')  as rcv_type" +
                    " , ifnull(fail_reason, '')  as fail_reason" +
                    " , ifnull(driver_memo, '') as driver_memo" +
                    " , ifnull(real_qty, '') as real_qty" +
                    " , ifnull(retry_dt , '') as retry_dt" +
                    " , type " +
                    " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                    " where reg_id= '" + Preferences.userId + "'" +
                    " and punchOut_stat <> 'S' and chg_dt is not null"

            val cs: Cursor = DatabaseHelper.getInstance().get(selectQuery)

            if (cs.moveToFirst()) {
                do {
                    val data = UploadData().apply {
                        noSongjang = cs.getString(cs.getColumnIndex("invoice_no"))
                        stat = cs.getString(cs.getColumnIndex("stat"))
                        receiveType = cs.getString(cs.getColumnIndex("rcv_type"))
                        failReason = cs.getString(cs.getColumnIndex("fail_reason"))
                        driverMemo = cs.getString(cs.getColumnIndex("driver_memo"))
                        realQty = cs.getString(cs.getColumnIndex("real_qty"))
                        retryDay = cs.getString(cs.getColumnIndex("retry_dt"))
                        type = cs.getString(cs.getColumnIndex("type"))
                    }

                    songjangList.add(data)
                } while (cs.moveToNext())
            }

            var latitude = 0.0
            var longitude = 0.0
            if ((context).gpsEnable && (context).gpsTrackerManager != null) {
                latitude = (context).gpsTrackerManager!!.latitude
                longitude = (context).gpsTrackerManager!!.longitude
            }

            for (item in songjangList) {
                if (item.noSongjang.isNotEmpty()) {
                    if (item.type == StatueType.TYPE_DELIVERY) {

                    } else if (item.type == StatueType.TYPE_PICKUP) {

                        var fileData = ""
                        var fileData2 = ""

                        // TODO_kjyoo upload

//                        val dirPath = Environment.getExternalStorageDirectory()
//                            .toString() + "/QdrivePickup"
//                        val filePath = dirPath + "/" + item.noSongjang + ".png"
//                        val imgFile = File(filePath)
//
//                        if (imgFile.exists()) {
//                            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
//                            fileData = DataUtil.bitmapToString(
//                                context,
//                                myBitmap,
//                                ImageUpload.QXPOD,
//                                "qdriver/sign",
//                                item.noSongjang
//                            )
//
//                            if (fileData == "") {
//                                result.setResultCode(-100)
//                                result.setResultMsg(
//                                    context.getResources()
//                                        .getString(R.string.msg_upload_fail_image)
//                                )
//                                return result
//                            }
//                        } else {
//
//                        }
//
//                        if (item.stat == BarcodeType.PICKUP_FAILED) {
//
//                        } else {
//
//
//                        }

                        val result = RetrofitClient.instanceDynamic().requestSetPickupUploadData(
                            item.receiveType,
                            item.stat,
                            NetworkUtil.getNetworkType(context),
                            item.noSongjang,
                            fileData,
                            fileData2,
                            item.driverMemo,
                            latitude,
                            longitude,
                            item.realQty,
                            item.failReason,
                            item.retryDay,
                        )

                        if (result.resultCode == 0) {
                            val contentVal = ContentValues().apply {
                                put("punchOut_stat", "S")
                            }

                            DatabaseHelper.getInstance().update(
                                DatabaseHelper.DB_TABLE_INTEGRATION_LIST,
                                contentVal,
                                "invoice_no=? COLLATE NOCASE and reg_id=?",
                                arrayOf(item.noSongjang, Preferences.userId)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun pingCheck() {

        CoroutineScope(Dispatchers.IO).launch {
            val result: String = try {
                val runTime = Runtime.getRuntime()
                val cmd = "ping -c 1 -W 10 qxapi.qxpress.net"

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
                "PING",
                returnString
            )

            if (result != "0") {
                urlConnectionCheck()
                nowTimeCheck()
                telephonyInfo()
            }
        }
    }

    var qoo10Result = ""
    var daumResult = ""
    private fun urlConnectionCheck() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val url = URL("https://www.qoo10.com")
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                try {
                    FirebaseCrashlytics.getInstance().setCustomKey(
                        "qoo10 url connection",
                        urlConnection.responseCode
                    )

                    qoo10Result = "qoo10 url connection / ${urlConnection.responseCode}"
                } catch (e: java.lang.Exception) {
                    FirebaseCrashlytics.getInstance().setCustomKey(
                        "qoo10 url connection",
                        "error / $e"
                    )

                    qoo10Result = "qoo10 url connection $e"
                }

                val url1 = URL("https://www.daum.net")
                val urlConnection1: HttpURLConnection = url1.openConnection() as HttpURLConnection
                try {
                    FirebaseCrashlytics.getInstance().setCustomKey(
                        "daum url connection",
                        urlConnection1.responseCode
                    )

                    daumResult = "daum url connection / ${urlConnection.responseCode}"
                } catch (e: java.lang.Exception) {
                    FirebaseCrashlytics.getInstance().setCustomKey(
                        "daum url connection",
                        "error / $e"
                    )

                    daumResult = "daum url connection $e"
                }
            }
        }
    }

    var nowTime = ""
    private fun nowTimeCheck() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val regDataString = dateFormat.format(Date())
        FirebaseCrashlytics.getInstance().setCustomKey(
            "now Time",
            regDataString
        )
        nowTime = " / now Time : $regDataString"
    }

    var teleInfo = ""
    private fun telephonyInfo() {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        FirebaseCrashlytics.getInstance().setCustomKey(
            "TelephonyManager",
            tm.simOperatorName
        )

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (tm.signalStrength != null) {
                FirebaseCrashlytics.getInstance().setCustomKey(
                    "level (0~4)",
                    tm.signalStrength!!.level
                )
            }
        }
        teleInfo = "TelephonyManager" + tm.simOperatorName
    }

}