package com.giosis.util.qdrive.singapore.message

import com.google.gson.annotations.SerializedName

class MessageDetailResult {
    @SerializedName("tracking_No")
    var tracking_no: String = ""

    @SerializedName("question_seq_no")
    var question_seq_no: String = ""

    @SerializedName("title")
    var title: String = ""

    @SerializedName("contents")
    var message: String = ""

    @SerializedName("sender_id")
    var sender_id: String = ""

    @SerializedName("rcv_id")
    var receive_id: String = ""

    @SerializedName("send_dt")
    var send_date: String = ""

    @SerializedName("align")
    var align: String = ""
    // left or right
}