package com.giosis.util.qdrive.main;

import java.util.List;

/**
 * @author krm0219  2018.07.26
 */
public class ListNotInHousedResult {

    private int resultCode = -1;
    private String resultMsg = "";
    private List<NotInhousedList> resultObject;

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

    public List<NotInhousedList> getResultObject() {
        return resultObject;
    }

    public void setResultObject(List<NotInhousedList> resultObject) {
        this.resultObject = resultObject;
    }


    public static class NotInhousedList {

        private String invoiceNo = "";
        private String reqName = "";
        private String partner_id = "";
        private String zipCode = "";
        private String address = "";
        private String pickup_date;
        private String real_qty = "";
        private String not_processed_qty = "";
        private List<NotInhousedSubList> subLists;

        public String getInvoiceNo() {
            return invoiceNo;
        }

        public void setInvoiceNo(String invoiceNo) {
            this.invoiceNo = invoiceNo;
        }

        String getReqName() {
            return reqName;
        }

        public void setReqName(String reqName) {
            this.reqName = reqName;
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

        public String getPartner_id() {
            return partner_id;
        }

        public void setPartner_id(String partner_id) {
            this.partner_id = partner_id;
        }

        String getPickup_date() {
            return pickup_date;
        }

        public void setPickup_date(String pickup_date) {
            this.pickup_date = pickup_date;
        }

        public String getReal_qty() {
            return real_qty;
        }

        public void setReal_qty(String real_qty) {
            this.real_qty = real_qty;
        }

        String getNot_processed_qty() {
            return not_processed_qty;
        }

        public void setNot_processed_qty(String not_processed_qty) {
            this.not_processed_qty = not_processed_qty;
        }

        List<NotInhousedSubList> getSubLists() {
            return subLists;
        }

        public void setSubLists(List<NotInhousedSubList> subLists) {
            this.subLists = subLists;
        }

        public static class NotInhousedSubList {

            private String packingNo = "";
            private String purchasedAmount = "";
            private String purchaseCurrency = "";

            String getPackingNo() {
                return packingNo;
            }

            public void setPackingNo(String packingNo) {
                this.packingNo = packingNo;
            }

            String getPurchasedAmount() {
                return purchasedAmount;
            }

            public void setPurchasedAmount(String purchasedAmount) {
                this.purchasedAmount = purchasedAmount;
            }

            String getPurchaseCurrency() {
                return purchaseCurrency;
            }

            public void setPurchaseCurrency(String purchaseCurrency) {
                this.purchaseCurrency = purchaseCurrency;
            }
        }
    }
}