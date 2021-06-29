package com.giosis.library.barcodescanner;

import org.simpleframework.xml.Element;

public class CnRPickupResult {

    private int resultCode = -1;
    private String resultMsg = "";
    public CnRPickupData ResultObject;

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

    public CnRPickupData getResultObject() {
        return ResultObject;
    }

    public void setResultObject(CnRPickupData resultObject) {
        ResultObject = resultObject;
    }


    public static class CnRPickupData {

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

        @Element(name = "tel_no", required = false)
        private String tel_no = "";

        @Element(name = "hp_no", required = false)
        private String hp_no = "";

        @Element(name = "zip_code", required = false)
        private String zip_code = "";

        @Element(name = "address", required = false)
        private String address = "";

        @Element(name = "pickup_hopeday", required = false)
        private String pickup_hopeday = "";

        @Element(name = "del_memo", required = false)
        private String del_memo = "";

        @Element(name = "driver_memo", required = false)
        private String driver_memo = "";

        @Element(name = "fail_reason", required = false)
        private String fail_reason = "";

        @Element(name = "qty", required = false)
        private String qty = "";

        @Element(name = "route", required = false)
        private String route = "";

        @Element(name = "cust_no", required = false)
        private String cust_no = "";


        public String getContrNo() {
            return contr_no;
        }

        public void setContrNo(String contrNo) {
            this.contr_no = contrNo;
        }

        public String getPartnerRefNo() {
            return partner_ref_no;
        }

        public void setPartnerRefNo(String partnerRefNo) {
            this.partner_ref_no = partnerRefNo;
        }

        public String getInvoiceNo() {
            return invoice_no;
        }

        public void setInvoiceNo(String invoiceNo) {
            this.invoice_no = invoiceNo;
        }

        public String getStat() {
            return stat;
        }

        public void setStat(String stat) {
            this.stat = stat;
        }

        public String getReqName() {
            return req_nm;
        }

        public void setReqName(String reqName) {
            this.req_nm = reqName;
        }

        public String getTelNo() {
            return tel_no;
        }

        public void setTelNo(String telNo) {
            this.tel_no = telNo;
        }

        public String getHpNo() {
            return hp_no;
        }

        public void setHpNo(String hpNo) {
            this.hp_no = hpNo;
        }

        public String getZipCode() {
            return zip_code;
        }

        public void setZipCode(String zipCode) {
            this.zip_code = zipCode;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPickupHopeDay() {
            return pickup_hopeday;
        }

        public void setPickupHopeDay(String pickupHopeDay) {
            this.pickup_hopeday = pickupHopeDay;
        }

        public String getDelMemo() {
            return del_memo;
        }

        public void setDelMemo(String delMemo) {
            this.del_memo = delMemo;
        }

        public String getDriverMemo() {
            return driver_memo;
        }

        public void setDriverMemo(String driverMemo) {
            this.driver_memo = driverMemo;
        }

        public String getFailReason() {
            return fail_reason;
        }

        public void setFailReason(String failReason) {
            this.fail_reason = failReason;
        }

        public String getQty() {
            return qty;
        }

        public void setQty(String qty) {
            this.qty = qty;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public String getCust_no() {
            return cust_no;
        }

        public void setCust_no(String cust_no) {
            this.cust_no = cust_no;
        }
    }
}