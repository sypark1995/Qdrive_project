package com.giosis.util.qdrive.singapore.message

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

class MessageListResult {
    @SerializedName("question_seq_no")
    var question_seq_no = 0

    @SerializedName("contents")
    var message: String = ""

    @SerializedName("send_dt")
    var time: String = ""

    @SerializedName("read_yn")
    var read_yn: String = ""

    @SerializedName("tracking_No")
    var tracking_no: String = ""

    @SerializedName("total_page")
    var total_page_size = 0

    @SerializedName("sender_id")
    var sender_id: String = ""

    @SuppressLint("SimpleDateFormat")
    fun getTime(calledFragment: String): String {

        var result = time

        try {
            var simpleDateFormat: SimpleDateFormat? = null
            if (calledFragment.equals("C", ignoreCase = true)) {

                //	String s = "2018-05-25 오후 4:40:14";
                simpleDateFormat = SimpleDateFormat("yyyy-MM-dd a hh:mm:ss") //, Locale.ENGLISH);
            } else if (calledFragment.equals("A", ignoreCase = true)) {
                //	String s = "2018-05-25 4:40:14";
                simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss") //, Locale.ENGLISH);
            }
            val date = simpleDateFormat!!.parse(time)
            val today = Date()
            val fmt = SimpleDateFormat("yyyyMMdd")
            val isToday = fmt.format(date) == fmt.format(today)

            result = if (isToday) {
                val dateFormat = SimpleDateFormat("a hh:mm", Locale.ENGLISH)
                dateFormat.format(date)
            } else {
                val dateFormat = SimpleDateFormat("MM/dd")
                dateFormat.format(date)
            }

        } catch (e: Exception) {
            Log.e("Exception", "DateParser Exception $e")
        }

        return result
    }
}