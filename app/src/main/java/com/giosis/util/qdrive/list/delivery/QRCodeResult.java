package com.giosis.util.qdrive.list.delivery;

import com.google.gson.annotations.SerializedName;

public class QRCodeResult {

    @SerializedName("ResultCode")
    String result_code;

    @SerializedName("ResultMsg")
    String result_msg;

    @SerializedName("ResultObject")
    String qrcode_data;

    public String getResult_code() {
        return result_code;
    }

    public void setResult_code(String result_code) {
        this.result_code = result_code;
    }

    public String getResult_msg() {
        return result_msg;
    }

    public void setResult_msg(String result_msg) {
        this.result_msg = result_msg;
    }

    public String getQrcode_data() {
        return qrcode_data;
    }

    public void setQrcode_data(String qrcode_data) {
        this.qrcode_data = qrcode_data;
    }
}
