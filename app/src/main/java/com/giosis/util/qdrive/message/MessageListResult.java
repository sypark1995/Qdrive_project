package com.giosis.util.qdrive.message;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageListResult {

    @SerializedName("ResultCode")
    private int resultCode = -1;

    @SerializedName("ResultMsg")
    private String resultMsg = "";

    @SerializedName("ResultObject")
    private List<MessageList> resultObject;

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

    public List<MessageList> getResultObject() {
        return resultObject;
    }

    public void setResultObject(List<MessageList> resultObject) {
        this.resultObject = resultObject;
    }

    public static class MessageList {

        @SerializedName("question_seq_no")
        int question_seq_no;

        @SerializedName("contents")
        String message;

        @SerializedName("send_dt")
        String time;

        @SerializedName("read_yn")
        String read_yn;

        @SerializedName("tracking_No")
        String tracking_no;

        @SerializedName("total_page")
        int total_page_size;

        @SerializedName("sender_id")
        String sender_id;        // admin_id;


        public int getQuestion_seq_no() {
            return question_seq_no;
        }

        public void setQuestion_seq_no(int question_seq_no) {
            this.question_seq_no = question_seq_no;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTime(String calledFragment) {
            String result = parsingTime(time, calledFragment);
            return result;
        }

        public void setTime(String time) {
            this.time = time;
        }


        public String getRead_yn() {
            return read_yn;
        }

        public void setRead_yn(String read_yn) {
            this.read_yn = read_yn;
        }


        public String getTracking_no() {
            return tracking_no;
        }

        public void setTracking_no(String tracking_no) {
            this.tracking_no = tracking_no;
        }

        public int getTotal_page_size() {
            return total_page_size;
        }

        public void setTotal_page_size(int total_page_size) {
            this.total_page_size = total_page_size;
        }

        public String getSender_id() {
            return sender_id;
        }

        public void setSender_id(String sender_id) {
            this.sender_id = sender_id;
        }


        private String parsingTime(String s, String calledFragment) {
            String result = "";

            try {
                SimpleDateFormat simpleDateFormat = null;
                if (calledFragment.equalsIgnoreCase("C")) {

                    //	String s = "2018-05-25 오후 4:40:14";
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss");//, Locale.ENGLISH);
                } else if (calledFragment.equalsIgnoreCase("A")) {
                    //	String s = "2018-05-25 4:40:14";
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");//, Locale.ENGLISH);
                }

                Date date = simpleDateFormat.parse(s);

                Date today = new Date();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                boolean isToday = fmt.format(date).equals(fmt.format(today));

                if (isToday) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("a hh:mm", Locale.ENGLISH);
                    String regDataString = dateFormat.format(date);
                    result = regDataString;
                } else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
                    String regDataString = dateFormat.format(date);
                    result = regDataString;
                }

            } catch (Exception e) {

                result = s;
            }

            return result;
        }
    }
}
