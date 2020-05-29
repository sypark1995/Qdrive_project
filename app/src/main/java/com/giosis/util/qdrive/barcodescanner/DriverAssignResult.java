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
        private String ContrNo = "";

        @Element(name = "partner_ref_no", required = false)
        private String partnerRefNo = "";

        @Element(name = "invoice_no", required = false)
        private String invoiceNo = "";

        @Element(name = "stat", required = false)
        private String stat = "";

        @Element(name = "rcv_nm", required = false)
        private String rcvName = "";

        @Element(name = "tel_no", required = false)
        private String telNo = "";

        @Element(name = "hp_no", required = false)
        private String hpNo = "";

        @Element(name = "zip_code", required = false)
        private String zipCode = "";

        @Element(name = "address", required = false)
        private String address = "";

        @Element(name = "sender_nm", required = false)
        private String senderName = "";

        @Element(name = "del_memo", required = false)
        private String delMemo = "";

        @Element(name = "driver_memo", required = false)
        private String driverMemo = "";

        @Element(name = "fail_reason", required = false)
        private String failReason = "";

        @Element(name = "partner_ref_no_fail_assign", required = false)
        private String partnerRefNoFailAssign = "";

        @Element(name = "reason_fail_assign", required = false)
        private String reasonFailAssign = "";

        @Element(name = "delivery_first_date", required = false)
        private String deliveryFirstDate = "";

        @Element(name = "delivery_count", required = false)
        private String deliveryCount = "";

        @Element(name = "route", required = false)
        private String route = "";

        @Element(name = "secret_no_type", required = false)
        private String secret_no_type = "";

        @Element(name = "secret_no", required = false)
        private String secret_no = "";


        @Element(name = "secure_delivery_yn", required = false)
        private String secureDeliveryYN = "";

        @Element(name = "parcel_amount", required = false)
        private String parcelAmount = "";


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

        public String getRcvName() {
            return rcvName;
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

        public String getSenderName() {
            return senderName;
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

        public String getPartnerRefNoFailAssign() {
            return partnerRefNoFailAssign;
        }

        public String getReasonFailAssign() {
            return reasonFailAssign;
        }

        public void setPartnerRefNoFailAssign(String assignNo) {
            this.partnerRefNoFailAssign = assignNo;
        }

        public void setReasonFailAssign(String reasonFailAssign) {
            this.reasonFailAssign = reasonFailAssign;
        }

        public String getDeliveryFirstDate() {
            return deliveryFirstDate;
        }

        public String getDeliveryCount() {
            return deliveryCount;
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
            return secureDeliveryYN;
        }


        public String getParcelAmount() {
            return parcelAmount;
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
