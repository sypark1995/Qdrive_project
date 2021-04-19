package com.giosis.library.barcodescanner;

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

        private String contrNo = "";
        private String partnerRefNo = "";
        private String invoiceNo = "";
        private String stat = "";
        private String reqName = "";
        private String reqDate = "";
        private String telNo = "";
        private String hpNo = "";
        private String zipCode = "";
        private String address = "";
        private String pickupHopeDay = "";
        private String pickupHopeTime = "";
        private String senderName = "";
        private String delMemo = "";
        private String driverMemo = "";
        private String failReason = "";
        private String qty = "";
        private String custName = "";
        private String partnerID = "";
        private String route = "";
        private String cust_no = "";

        public String getContrNo() {
            return contrNo;
        }

        public void setContrNo(String contrNo) {
            this.contrNo = contrNo;
        }

        public String getPartnerRefNo() {
            return partnerRefNo;
        }

        public void setPartnerRefNo(String partnerRefNo) {
            this.partnerRefNo = partnerRefNo;
        }

        public String getInvoiceNo() {
            return invoiceNo;
        }

        public void setInvoiceNo(String invoiceNo) {
            this.invoiceNo = invoiceNo;
        }

        public String getStat() {
            return stat;
        }

        public void setStat(String stat) {
            this.stat = stat;
        }

        public String getReqName() {
            return reqName;
        }

        public void setReqName(String reqName) {
            this.reqName = reqName;
        }

        public String getReqDate() {
            return reqDate;
        }

        public void setReqDate(String reqDate) {
            this.reqDate = reqDate;
        }

        public String getTelNo() {
            return telNo;
        }

        public void setTelNo(String telNo) {
            this.telNo = telNo;
        }

        public String getHpNo() {
            return hpNo;
        }

        public void setHpNo(String hpNo) {
            this.hpNo = hpNo;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPickupHopeDay() {
            return pickupHopeDay;
        }

        public void setPickupHopeDay(String pickupHopeDay) {
            this.pickupHopeDay = pickupHopeDay;
        }

        public String getPickupHopeTime() {
            return pickupHopeTime;
        }

        public void setPickupHopeTime(String pickupHopeTime) {
            this.pickupHopeTime = pickupHopeTime;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getDelMemo() {
            return delMemo;
        }

        public void setDelMemo(String delMemo) {
            this.delMemo = delMemo;
        }

        public String getDriverMemo() {
            return driverMemo;
        }

        public void setDriverMemo(String driverMemo) {
            this.driverMemo = driverMemo;
        }

        public String getFailReason() {
            return failReason;
        }

        public void setFailReason(String failReason) {
            this.failReason = failReason;
        }

        public String getQty() {
            return qty;
        }

        public void setQty(String qty) {
            this.qty = qty;
        }

        public String getCustName() {
            return custName;
        }

        public void setCustName(String custName) {
            this.custName = custName;
        }

        public String getPartnerID() {
            return partnerID;
        }

        public void setPartnerID(String partnerID) {
            this.partnerID = partnerID;
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