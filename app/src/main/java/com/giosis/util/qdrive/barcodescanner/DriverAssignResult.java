package com.giosis.util.qdrive.barcodescanner;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict = false, name = "StdCustomResultOfListOfQSignDeliveryList")
public class DriverAssignResult {

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
    private List<QSignDeliveryList> resultObject;

    public List<QSignDeliveryList> getResultObject() {
        return resultObject;
    }

    public void setResultObject(List<QSignDeliveryList> resultObj) {
        this.resultObject = resultObj;
    }

    @Root(strict = false, name = "QSignDeliveryList")
    public static class QSignDeliveryList {

        @Element(name = "contr_no", required = false)
        private String contr_no = "";

        @Element(name = "partner_ref_no", required = false)
        private String partner_ref_no = "";

        @Element(name = "invoice_no", required = false)
        private String invoice_no = "";

        @Element(name = "stat", required = false)
        private String stat = "";

        @Element(name = "rcv_nm", required = false)
        private String rcv_nm = "";

        @Element(name = "tel_no", required = false)
        private String tel_no = "";

        @Element(name = "hp_no", required = false)
        private String hp_no = "";

        @Element(name = "zip_code", required = false)
        private String zip_code = "";

        @Element(name = "address", required = false)
        private String address = "";

        @Element(name = "sender_nm", required = false)
        private String sender_nm = "";

        @Element(name = "del_memo", required = false)
        private String del_memo = "";

        @Element(name = "driver_memo", required = false)
        private String driver_memo = "";

        @Element(name = "fail_reason", required = false)
        private String fail_reason = "";

        @Element(name = "partner_ref_no_fail_assign", required = false)
        private String partner_ref_no_fail_assign = "";

        @Element(name = "reason_fail_assign", required = false)
        private String reason_fail_assign = "";

        @Element(name = "delivery_first_date", required = false)
        private String delivery_first_date = "";

        @Element(name = "delivery_count", required = false)
        private String delivery_count = "";

        @Element(name = "route", required = false)
        private String route = "";

        @Element(name = "secret_no_type", required = false)
        private String secret_no_type = "";

        @Element(name = "secret_no", required = false)
        private String secret_no = "";


        @Element(name = "secure_delivery_yn", required = false)
        private String secure_delivery_yn = "";

        @Element(name = "parcel_amount", required = false)
        private String parcel_amount = "";


        @Element(name = "currency", required = false)
        private String currency = "";

        // krm0219
        @Element(name = "order_type_etc", required = false)
        private String order_type_etc = "";

        @Element(name = "lat_lng", required = false)
        private String lat_lng = "";

		/*
		@Element(name="cust_no", required=false)
		private String cust_no = "";
		
		
		public String getCustNo() {
			return cust_no;
		}
		*/

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