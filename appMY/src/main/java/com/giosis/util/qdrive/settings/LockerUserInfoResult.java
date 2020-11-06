package com.giosis.util.qdrive.settings;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * @author krm0219
 */
public class LockerUserInfoResult {

    @SerializedName("ResultCode")
    private String result_code;

    @SerializedName("ResultMsg")
    private String result_msg;

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


    @SerializedName("ResultObject")
    private LockerResultObject resultObject;

    public LockerResultObject getResultObject() {
        return resultObject;
    }

    public void setResultObject(LockerResultObject resultObject) {
        this.resultObject = resultObject;
    }


    class LockerResultObject {

        @SerializedName("rows")
        private ArrayList<LockerResultRow> resultRows;

        public ArrayList<LockerResultRow> getResultRows() {
            return resultRows;
        }

        public void setResultRows(ArrayList<LockerResultRow> resultRows) {
            this.resultRows = resultRows;
        }


        class LockerResultRow {

            @SerializedName("lsp_user_key")
            private String user_key;

            @SerializedName("lsp_user_status")
            private String user_status;

            @SerializedName("hp_no")
            private String user_mobile;

            @SerializedName("lsp_user_expired_date")
            private String user_expiry_date;

            @SerializedName("lst_user_id")
            private String user_id;


            public String getUser_key() {
                return user_key;
            }

            public void setUser_key(String user_key) {
                this.user_key = user_key;
            }

            public String getUser_status() {
                return user_status;
            }

            public void setUser_status(String user_status) {
                this.user_status = user_status;
            }

            public String getUser_mobile() {
                return user_mobile;
            }

            public void setUser_mobile(String user_mobile) {
                this.user_mobile = user_mobile;
            }

            public String getUser_expiry_date() {
                return user_expiry_date;
            }

            public void setUser_expiry_date(String user_expiry_date) {
                this.user_expiry_date = user_expiry_date;
            }

            public String getUser_id() {
                return user_id;
            }

            public void setUser_id(String user_id) {
                this.user_id = user_id;
            }

        }
    }
}