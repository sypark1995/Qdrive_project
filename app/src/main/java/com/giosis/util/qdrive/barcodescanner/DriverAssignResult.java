package com.giosis.util.qdrive.barcodescanner;

import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict = false, name = "StdCustomResultOfListOfQSignDeliveryList")
public class DriverAssignResult {

    @Element(name = "ResultCode", required = false)
    @SerializedName("ResultCode")
    private int ResultCode = -1;

    @Element(name = "ResultMsg", required = false)
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


    @ElementList(required = false, name = "ResultObject")
    @SerializedName("ResultObject")
    private List<QSignDeliveryList> ResultObject;

    public List<QSignDeliveryList> getResultObject() {
        return ResultObject;
    }

    public void setResultObject(List<QSignDeliveryList> resultObj) {
        this.ResultObject = resultObj;
    }

    @Root(strict = false, name = "QSignDeliveryList")
    public static class QSignDeliveryList {

        @Element(name = "contr_no", required = false)
        @SerializedName("contr_no")
        private String contr_no = "";

        @Element(name = "partner_ref_no", required = false)
        @SerializedName("partner_ref_no")
        private String partner_ref_no = "";

        @Element(name = "invoice_no", required = false)
        @SerializedName("invoice_no")
        private String invoice_no = "";

        @Element(name = "stat", required = false)
        @SerializedName("stat")
        private String stat = "";

        @Element(name = "rcv_nm", required = false)
        @SerializedName("rcv_nm")
        private String rcv_nm = "";

        @Element(name = "tel_no", required = false)
        @SerializedName("tel_no")
        private String tel_no = "";

        @Element(name = "hp_no", required = false)
        @SerializedName("hp_no")
        private String hp_no = "";

        @Element(name = "zip_code", required = false)
        @SerializedName("zip_code")
        private String zip_code = "";

        @Element(name = "address", required = false)
        @SerializedName("address")
        private String address = "";

        @Element(name = "sender_nm", required = false)
        @SerializedName("sender_nm")
        private String sender_nm = "";

        @Element(name = "del_memo", required = false)
        @SerializedName("del_memo")
        private String del_memo = "";

        @Element(name = "driver_memo", required = false)
        @SerializedName("driver_memo")
        private String driver_memo = "";

        @Element(name = "fail_reason", required = false)
        @SerializedName("fail_reason")
        private String fail_reason = "";

        @Element(name = "partner_ref_no_fail_assign", required = false)
        @SerializedName("partner_ref_no_fail_assign")
        private String partner_ref_no_fail_assign = "";

        @Element(name = "reason_fail_assign", required = false)
        @SerializedName("reason_fail_assign")
        private String reason_fail_assign = "";

        @Element(name = "delivery_first_date", required = false)
        @SerializedName("delivery_first_date")
        private String delivery_first_date = "";

        @Element(name = "delivery_count", required = false)
        @SerializedName("delivery_count")
        private String delivery_count = "";

        @Element(name = "route", required = false)
        @SerializedName("route")
        private String route = "";

        @Element(name = "secret_no_type", required = false)
        @SerializedName("secret_no_type")
        private String secret_no_type = "";

        @Element(name = "secret_no", required = false)
        @SerializedName("secret_no")
        private String secret_no = "";

        @Element(name = "secure_delivery_yn", required = false)
        @SerializedName("secure_delivery_yn")
        private String secure_delivery_yn = "";

        @Element(name = "parcel_amount", required = false)
        @SerializedName("parcel_amount")
        private String parcel_amount = "";

        @Element(name = "currency", required = false)
        @SerializedName("currency")
        private String currency = "";

        // krm0219
        @Element(name = "order_type_etc", required = false)
        @SerializedName("order_type_etc")
        private String order_type_etc = "";

        @Element(name = "lat_lng", required = false)
        @SerializedName("lat_lng")
        private String lat_lng = "";


        public String getContrNo() {
            return contr_no;
        }

        public String getPartnerRefNo() {
            return partner_ref_no;
        }

        public String getInvoiceNo() {
            return invoice_no;
        }

        public String getStat() {
            return stat;
        }

        public String getRcvName() {
            return rcv_nm;
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

        public String getAddress() {
            return address;
        }

        public String getSenderName() {
            return sender_nm;
        }

        public String getDelMemo() {
            return del_memo;
        }

        public String getDriverMemo() {
            return driver_memo;
        }

        public String getFailReason() {
            return fail_reason;
        }

        public String getPartnerRefNoFailAssign() {
            return partner_ref_no_fail_assign;
        }

        public String getReasonFailAssign() {
            return reason_fail_assign;
        }

        public void setPartnerRefNoFailAssign(String assignNo) {
            this.partner_ref_no_fail_assign = assignNo;
        }

        public void setReasonFailAssign(String reasonFailAssign) {
            this.reason_fail_assign = reasonFailAssign;
        }

        public String getDeliveryFirstDate() {
            return delivery_first_date;
        }

        public String getDeliveryCount() {
            return delivery_count;
        }

        public String getRoute() {
            return route;
        }

        public String getSecretNoType() {
            return secret_no_type;
        }

        public String getSecretNo() {
            return secret_no;
        }

        public String getSecureDeliveryYN() {
            return secure_delivery_yn;
        }

        public String getParcelAmount() {
            return parcel_amount;
        }

        public String getCurrency() {
            return currency;
        }

        public String getOrder_type_etc() {
            return order_type_etc;
        }

        public String getLat_lng() {
            return lat_lng;
        }
    }
}