package com.giosis.util.qdrive.message;

import com.google.gson.annotations.SerializedName;

public class MessageSendResult {

    @SerializedName("ResultCode")
    private int resultCode = -1;

    @SerializedName("ResultMsg")
    private String resultMsg = "";


    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }


    @SerializedName("ResultObject")
    private SendResult resultObject;

    public SendResult getResultObject() {
        return resultObject;
    }

    public void setResultObject(SendResult resultObject) {
        this.resultObject = resultObject;
    }

    class SendResult {

        @SerializedName("ResultCode")
        private String resultCode = "-1";

        @SerializedName("ResultMsg")
        private String resultMsg = "";


        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultMsg() {
            return resultMsg;
        }

        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }
    }
}
