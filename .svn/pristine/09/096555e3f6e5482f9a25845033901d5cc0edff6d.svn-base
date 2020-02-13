package com.giosis.util.qdrive.barcodescanner;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict = false, name = "StdCustomResultOfListOfQSignPickupList")
public class PickupAssignResult {

    @Element(name = "ResultCode", required = false)
    private int resultCode = -1;

    @Element(name = "ResultMsg", required = false)
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

    @ElementList(required = false, name = "ResultObject")
    private List<QSignPickupList> resultObject;

    public List<QSignPickupList> getResultObject() {
        return resultObject;
    }

    public void setResultObject(List<QSignPickupList> resultObj) {
        this.resultObject = resultObj;
    }

    @Root(strict = false, name = "QSignPickupList")
    public static class QSignPickupList {

        @Element(name = "contr_no", required = false)
        private String ContrNo = "";

        @Element(name = "partner_ref_no", required = false)
        private String partnerRefNo = "";

        @Element(name = "invoice_no", required = false)
        private String invoiceNo = "";

        @Element(name = "stat", required = false)
        private String stat = "";

        @Element(name = "req_nm", required = false)
        private String reqName = "";

        @Element(name = "partner_id", required = false)
        private String partnerID = "";

        @Element(name = "req_dt", required = false)
        private String reqDate = "";

        @Element(name = "tel_no", required = false)
        private String telNo = "";

        @Element(name = "hp_no", required = false)
        private String hpNo = "";

        @Element(name = "zip_code", required = false)
        private String zipCode = "";

        @Element(name = "address", required = false)
        private String address = "";

        @Element(name = "del_memo", required = false)
        private String delMemo = "";

        @Element(name = "driver_memo", required = false)
        private String driverMemo = "";

        @Element(name = "fail_reason", required = false)
        private String failReason = "";

        @Element(name = "pickup_hopeday", required = false)
        private String pickupHopeDay = "";

        @Element(name = "qty", required = false)
        private String qty = "";

        @Element(name = "route", required = false)
        private String route = "";

        @Element(name = "secret_no_type", required = false)
        private String secretNoType = "";

        @Element(name = "secret_no", required = false)
        private String secretNo = "";

        @Element(name = "failed_count", required = false)
        private String failedCount = "";

        @Element(name = "dr_req_no", required = false)
        private String dr_req_no = "";


        @Element(name = "cust_no", required = false)
        private String cust_no = "";

        @Element(name = "ref_pickup_no", required = false)
        private String ref_pickup_no = "";


        public String getCustNo() {
            return cust_no;
        }


        public String getDrReqNo() {
            return dr_req_no;
        }

        public String getContrNo() {
            return ContrNo;
        }

        public String getPartnerRefNo() {
            return partnerRefNo;
        }

        public String getInvoiceNo() {
            return invoiceNo;
        }

        public String getStat() {
            return stat;
        }

        public String getReqName() {
            return reqName;
        }

        public String getTelNo() {
            return telNo;
        }

        public String getHpNo() {
            return hpNo;
        }

        public String getZipCode() {
            return zipCode;
        }

        public String getAddress() {
            return address;
        }

        public String getDelMemo() {
            return delMemo;
        }

        public String getDriverMemo() {
            return driverMemo;
        }

        public String getFailReason() {
            return failReason;
        }

        public String getQty() {
            return qty;
        }

        public String getRoute() {
            return route;
        }

        public String getSecretNoType() {
            return secretNoType;
        }

        public String getSecretNo() {
            return secretNo;
        }

        public String getFailedCount() {
            return failedCount;
        }

        public String getReqDate() {
            return reqDate;
        }

        public String getPartnerID() {
            return partnerID;
        }

        public String getPickupHopeDay() {
            return pickupHopeDay;
        }

        public String getPickupHopeTime() {

            String pickupHopeTime = reqDate.substring(10, reqDate.length());

            return pickupHopeTime;
        }

        public String getRef_pickup_no() {
            return ref_pickup_no;
        }
    }
}
