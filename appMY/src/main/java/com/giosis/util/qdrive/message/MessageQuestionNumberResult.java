package com.giosis.util.qdrive.message;

import com.google.gson.annotations.SerializedName;

public class MessageQuestionNumberResult {

    @SerializedName("ResultCode")
    String resultCode;

    @SerializedName("ResultMsg")
    String resultMsg;

    @SerializedName("question_seq_no")
    int questionNo;


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

    public int getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(int questionNo) {
        this.questionNo = questionNo;
    }
}
