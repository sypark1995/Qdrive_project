package com.giosis.util.qdrive.main;

import java.util.List;

/**
 * @author krm0219  2018.07.26
 * @editor krm0219 2020.09
 */
public class NotInHousedResult {

    private int ResultCode = -1;
    private String ResultMsg = "";
    private List<NotInHousedList> ResultObject;

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

    public List<NotInHousedList> getResultObject() {
        return ResultObject;
    }

    public void setResultObject(List<NotInHousedList> resultObject) {
        this.ResultObject = resultObject;
    }


    public static class NotInHousedList {

        private String invoice_no = "";
        private String req_nm = "";
        private String partner_id = "";
        private String zip_code = "";
        private String address = "";
        private String pickup_cmpl_dt;
        private String real_qty = "";
        private String not_processed_qty = "";
        private List<NotInHousedSubList> qdriveOutstandingInhousedPickupLists;

        public String getInvoiceNo() {
            return invoice_no;
        }

        public void setInvoiceNo(String invoiceNo) {
            this.invoice_no = invoiceNo;
        }

        public String getReqName() {
            return req_nm;
        }

        public void setReqName(String reqName) {
            this.req_nm = reqName;
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

        public String getPartner_id() {
            return partner_id;
        }

        public void setPartner_id(String partner_id) {
            this.partner_id = partner_id;
        }

        public String getPickup_date() {
            return pickup_cmpl_dt;
        }

        public void setPickup_date(String pickup_date) {
            this.pickup_cmpl_dt = pickup_date;
        }

        public String getReal_qty() {
            return real_qty;
        }

        public void setReal_qty(String real_qty) {
            this.real_qty = real_qty;
        }

        public String getNot_processed_qty() {
            return not_processed_qty;
        }

        public void setNot_processed_qty(String not_processed_qty) {
            this.not_processed_qty = not_processed_qty;
        }

        public List<NotInHousedSubList> getSubLists() {
            return qdriveOutstandingInhousedPickupLists;
        }

        public void setSubLists(List<NotInHousedSubList> subLists) {
            this.qdriveOutstandingInhousedPickupLists = subLists;
        }


        public static class NotInHousedSubList {

            private String packing_no = "";
            private String purchased_amt = "";
            private String purchased_currency = "";

            public String getPackingNo() {
                return packing_no;
            }

            public void setPackingNo(String packingNo) {
                this.packing_no = packingNo;
            }

            public String getPurchasedAmount() {
                return purchased_amt;
            }

            public void setPurchasedAmount(String purchasedAmount) {
                this.purchased_amt = purchasedAmount;
            }

            public String getPurchaseCurrency() {
                return purchased_currency;
            }

            public void setPurchaseCurrency(String purchaseCurrency) {
                this.purchased_currency = purchaseCurrency;
            }
        }
    }
}
