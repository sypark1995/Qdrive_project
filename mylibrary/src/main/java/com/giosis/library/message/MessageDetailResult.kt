package com.giosis.library.message;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MessageDetailResult {

    @SerializedName("ResultCode")
    private int resultCode = -1;

    @SerializedName("ResultMsg")
    private String resultMsg = "";

    @SerializedName("ResultObject")
    private List<MessageDetailList> resultObject;

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

    public List<MessageDetailList> getResultObject() {
        return resultObject;
    }

    public void setResultObject(List<MessageDetailList> resultObject) {
        this.resultObject = resultObject;
    }

    public static class MessageDetailList {

        @SerializedName("tracking_No")
        String tracking_no;

        @SerializedName("question_seq_no")
        String question_seq_no;

        @SerializedName("title")
        String title;

        @SerializedName("contents")
        String message;

        @SerializedName("sender_id")
        String sender_id;

        @SerializedName("rcv_id")
        String receive_id;

        @SerializedName("send_dt")
        String send_date;

        @SerializedName("align")
        String align;            // left or right


        public String getTracking_no() {
            return tracking_no;
        }

        public void setTracking_no(String tracking_no) {
            this.tracking_no = tracking_no;
        }

        public String getQuestion_seq_no() {
            return question_seq_no;
        }

        public void setQuestion_seq_no(String question_seq_no) {
            this.question_seq_no = question_seq_no;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSender_id() {
            return sender_id;
        }

        public void setSender_id(String sender_id) {
            this.sender_id = sender_id;
        }

        public String getReceive_id() {
            return receive_id;
        }

        public void setReceive_id(String receive_id) {
            this.receive_id = receive_id;
        }

        public String getSend_date() {
            return send_date;
        }

        public void setSend_date(String send_date) {
            this.send_date = send_date;
        }

        public String getAlign() {
            return align;
        }

        public void setAlign(String align) {
            this.align = align;
        }
    }
}
