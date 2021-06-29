package com.giosis.library.list;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "StdCustomResultOfPrintDataResult")
public class PrintDataResult {

    @Element(name = "ResultCode", required = false)
    private int ResultCode = -1;

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
        @Element(name = "contr_no", required = false)
        private String contr_no = "";

        @Element(name = "partner_ref_no", required = false)
        private String partner_ref_no = "";

        @Element(name = "invoice_no", required = false)
        private String invoice_no = "";

        @Element(name = "tel_no", required = false)
        private String tel_no = "";

        @Element(name = "hp_no", required = false)
        private String hp_no = "";

        @Element(name = "zip_code", required = false)
        private String zip_code = "";

        @Element(name = "front_address", required = false)
        private String front_address = "";

        @Element(name = "back_address", required = false)
        private String back_address = "";

        @Element(name = "seller_shop_nm", required = false)
        private String seller_shop_nm = "";

        @Element(name = "delivery_course_code", required = false)
        private String delivery_course_code = "";

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


        public String getContrNo() {
            return contr_no;
        }

        public String getPartnerRefNo() {
            return partner_ref_no;
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