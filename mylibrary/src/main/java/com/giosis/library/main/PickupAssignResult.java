package com.giosis.library.main;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict = false, name = "StdCustomResultOfListOfQSignPickupList")
public class PickupAssignResult {

    @Element(name = "ResultCode", required = false)
    private int ResultCode = -1;

    @Element(name = "ResultMsg", required = false)
    private String ResultMsg = "";
    @ElementList(required = false, name = "ResultObject")
    private List<QSignPickupList> ResultObject;

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

    public List<QSignPickupList> getResultObject() {
        return ResultObject;
    }

    public void setResultObject(List<QSignPickupList> resultObj) {
        this.ResultObject = resultObj;
    }

    @Root(strict = false, name = "QSignPickupList")
    public static class QSignPickupList {

        @Element(name = "contr_no", required = false)
        private String contr_no = "";

        @Element(name = "partner_ref_no", required = false)
        private String partner_ref_no = "";

        @Element(name = "invoice_no", required = false)
        private String invoice_no = "";

        @Element(name = "stat", required = false)
        private String stat = "";

        @Element(name = "req_nm", required = false)
        private String req_nm = "";

        @Element(name = "partner_id", required = false)
        private String partner_id = "";

        @Element(name = "req_dt", required = false)
        private String req_dt = "";

        @Element(name = "tel_no", required = false)
        private String tel_no = "";

        @Element(name = "hp_no", required = false)
        private String hp_no = "";

        @Element(name = "zip_code", required = false)
        private String zip_code = "";

        @Element(name = "address", required = false)
        private String address = "";

        @Element(name = "del_memo", required = false)
        private String del_memo = "";

        @Element(name = "driver_memo", required = false)
        private String driver_memo = "";

        @Element(name = "fail_reason", required = false)
        private String fail_reason = "";

        @Element(name = "pickup_hopeday", required = false)
        private String pickup_hopeday = "";

        @Element(name = "qty", required = false)
        private String qty = "";

        @Element(name = "route", required = false)
        private String route = "";

        @Element(name = "secret_no_type", required = false)
        private String secret_no_type = "";

        @Element(name = "secret_no", required = false)
        private String secret_no = "";

        @Element(name = "failed_count", required = false)
        private String failed_count = "";

        @Element(name = "dr_req_no", required = false)
        private String dr_req_no = "";


        @Element(name = "cust_no", required = false)
        private String cust_no = "";

        @Element(name = "ref_pickup_no", required = false)
        private String ref_pickup_no = "";

        @Element(name = "lat_lng", required = false)
        private String lat_lng = "";


        public String getCustNo() {
            return cust_no;
        }

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

        public String getReqName() {
            return req_nm;
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

        public String getDelMemo() {
            return del_memo;
        }

        public String getDriverMemo() {
            return driver_memo;
        }

        public String getFailReason() {
            return fail_reason;
        }

        public String getQty() {
            return qty;
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

        public String getFailedCount() {
            return failed_count;
        }

        public String getReqDate() {
            return req_dt;
        }

        public String getPartnerID() {
            return partner_id;
        }

        public String getPickupHopeDay() {
            return pickup_hopeday;
        }

        public String getPickupHopeTime() {

            String pickupHopeTime = req_dt.substring(10, req_dt.length());

            return pickupHopeTime;
        }

        public String getRef_pickup_no() {
            return ref_pickup_no;
        }

        public String getLat_lng() {
            return lat_lng;
        }
    }
}
