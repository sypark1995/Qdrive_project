package com.giosis.library.list;

import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "StdCustomResultOfPrintDataResult")
public class PrintDataResult {

    @SerializedName("ResultCode")
    @Element(name = "ResultCode", required = false)
    private int ResultCode = -1;

    @SerializedName("ResultMsg")
    @Element(name = "ResultMsg", required = false)
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
    @Element(required = false, name = "ResultObject")
    private ResultObject ResultObject;

    public ResultObject getResultObject() {
        return ResultObject;
    }

    public void setResultObject(ResultObject resultObj) {
        this.ResultObject = resultObj;
    }

    //	@Root(strict=false, name="QdriveCNRList")
//	public static class QdriveCNRList {
    @Root(strict = false, name = "ResultObject")
    public static class ResultObject {

        @SerializedName("invoice_no")
        @Element(name = "invoice_no", required = false)
        private String invoice_no = "";

        @SerializedName("tel_no")
        @Element(name = "tel_no", required = false)
        private String tel_no = "";

        @SerializedName("hp_no")
        @Element(name = "hp_no", required = false)
        private String hp_no = "";

        @SerializedName("zip_code")
        @Element(name = "zip_code", required = false)
        private String zip_code = "";

        @SerializedName("front_address")
        @Element(name = "front_address", required = false)
        private String front_address = "";

        @SerializedName("back_address")
        @Element(name = "back_address", required = false)
        private String back_address = "";

        @SerializedName("seller_shop_nm")
        @Element(name = "seller_shop_nm", required = false)
        private String seller_shop_nm = "";

        @SerializedName("delivery_course_code")
        @Element(name = "delivery_course_code", required = false)
        private String delivery_course_code = "";

        @SerializedName("rcv_nm")
        @Element(name = "rcv_nm", required = false)
        private String rcv_nm = "";


        public String getFrontAddress() {
            return front_address;
        }

        public String getBackaddress() {
            return back_address;
        }

        public String getSellerShop() {
            return seller_shop_nm;
        }

        public String getDeliveryCouse() {
            return delivery_course_code;
        }

        public String getInvoiceNo() {
            return invoice_no;
        }

        public String getTelNo() {
            return tel_no;
        }

        public String getHpNo() {
            return hp_no;
        }

        public String getZipCode() {
            return zip_code;
        }

        public String getCustName() {
            return rcv_nm;
        }
    }
}