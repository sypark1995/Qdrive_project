package com.giosis.util.qdrive.list;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "StdCustomResultOfPrintDataResult")
public class PrintDataResult {

    public static ResultObject ResultObject;

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

    @Element(required = false, name = "ResultObject")
    private ResultObject resultObject;

    public ResultObject getResultObject() {
        return resultObject;
    }

    public void setResultObject(ResultObject resultObj) {
        this.resultObject = resultObj;
    }

    //	@Root(strict=false, name="QdriveCNRList")
//	public static class QdriveCNRList {
    @Root(strict = false, name = "ResultObject")
    public static class ResultObject {
        @Element(name = "contr_no", required = false)
        private String ContrNo = "";

        @Element(name = "partner_ref_no", required = false)
        private String partnerRefNo = "";

        @Element(name = "invoice_no", required = false)
        private String invoiceNo = "";

        @Element(name = "tel_no", required = false)
        private String telNo = "";

        @Element(name = "hp_no", required = false)
        private String hpNo = "";

        @Element(name = "zip_code", required = false)
        private String zipCode = "";

        @Element(name = "front_address", required = false)
        private String frontAddress = "";

        @Element(name = "back_address", required = false)
        private String Backaddress = "";

        @Element(name = "seller_shop_nm", required = false)
        private String sellerShop = "";

        @Element(name = "delivery_course_code", required = false)
        private String deliveryCouse = "";

        @Element(name = "rcv_nm", required = false)
        private String custName = "";

        @Element(name = "partner_id", required = false)
        private String partnerID = "";


        public String getFrontAddress() {
            return frontAddress;
        }

        public void setFrontAddress(String frontAddress) {
            this.frontAddress = frontAddress;
        }

        public String getBackaddress() {
            return Backaddress;
        }

        public void setBackaddress(String backaddress) {
            Backaddress = backaddress;
        }

        public String getSellerShop() {
            return sellerShop;
        }

        public void setSellerShop(String sellerShop) {
            this.sellerShop = sellerShop;
        }

        public String getDeliveryCouse() {
            return deliveryCouse;
        }

        public void setDeliveryCouse(String deliveryCouse) {
            this.deliveryCouse = deliveryCouse;
        }

        public void setInvoiceNo(String invoiceNo) {
            this.invoiceNo = invoiceNo;
        }

        public void setTelNo(String telNo) {
            this.telNo = telNo;
        }

        public void setHpNo(String hpNo) {
            this.hpNo = hpNo;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public void setCustName(String custName) {
            this.custName = custName;
        }

        public void setPartnerID(String partnerID) {
            this.partnerID = partnerID;
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

        public String getTelNo() {
            return telNo;
        }

        public String getHpNo() {
            return hpNo;
        }

        public String getZipCode() {
            return zipCode;
        }


        public String getCustName() {
            return custName;
        }


        public String getPartnerID() {
            return partnerID;
        }

        public void setPartnerRefNo(String partnerRefNo) {
            this.partnerRefNo = partnerRefNo;
        }

    }
}