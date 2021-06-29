package com.giosis.library.main.submenu;

import com.google.gson.annotations.SerializedName;

public class ShippingInfoResult {

    @SerializedName("ResultCode")
    private int ResultCode = -1;

    @SerializedName("ResultMsg")
    private String ResultMsg = "";

    public int getResultCode() {
        return ResultCode;
    }

    public void setResultCode(int resultCode) {
        this.ResultCode = resultCode;
    }

    public String getResultMsg() {
        return ResultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.ResultMsg = resultMsg;
    }


    @SerializedName("ResultObject")
    private ShippingInfo ResultObject;

    public ShippingInfo getResultObject() {
        return ResultObject;
    }

    public void setResultObject(ShippingInfo resultObject) {
        ResultObject = resultObject;
    }

    class ShippingInfo {

        @SerializedName("rev_nm")
        private String rev_nm = "";

        @SerializedName("cust_nm")
        private String cust_nm = "";

        public String getRev_nm() {
            return rev_nm;
        }

        public void setRev_nm(String rev_nm) {
            this.rev_nm = rev_nm;
        }

        public String getCust_nm() {
            return cust_nm;
        }

        public void setCust_nm(String cust_nm) {
            this.cust_nm = cust_nm;
        }
    }
}
