package com.giosis.util.qdrive.singapore;

import java.util.ArrayList;

/**
 * @author krm0219
 */
public class StatisticsResult {

    private int resultCode = -1;
    private String resultMsg = "";

    private ArrayList<SummaryData> summaryDataArrayList;
    private ArrayList<DetailData> detailDataArrayList;


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

    public ArrayList<SummaryData> getSummaryDataArrayList() {
        return summaryDataArrayList;
    }

    public void setSummaryDataArrayList(ArrayList<SummaryData> summaryDataArrayList) {
        this.summaryDataArrayList = summaryDataArrayList;
    }

    public ArrayList<DetailData> getDetailDataArrayList() {
        return detailDataArrayList;
    }

    public void setDetailDataArrayList(ArrayList<DetailData> detailDataArrayList) {
        this.detailDataArrayList = detailDataArrayList;
    }


    public static class SummaryData {

        int totalCount = 0;
        String percent;
        String date;
        String avgDate;

        int deliveredCount = 0;

        int doneCount = 0;
        int failedCount = 0;
        int confirmedCount = 0;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public String getPercent() {
            return percent;
        }

        public void setPercent(String percent) {
            this.percent = percent;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getAvgDate() {
            return avgDate;
        }

        public void setAvgDate(String avgDate) {
            this.avgDate = avgDate;
        }

        public int getDeliveredCount() {
            return deliveredCount;
        }

        public void setDeliveredCount(int deliveredCount) {
            this.deliveredCount = deliveredCount;
        }

        public int getDoneCount() {
            return doneCount;
        }

        public void setDoneCount(int doneCount) {
            this.doneCount = doneCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(int failedCount) {
            this.failedCount = failedCount;
        }

        public int getConfirmedCount() {
            return confirmedCount;
        }

        public void setConfirmedCount(int confirmedCount) {
            this.confirmedCount = confirmedCount;
        }
    }


    public static class DetailData {

        String stat;
        String date;

        String shippingNo;
        String trackingNo;

        String pickupNo;
        String pickupQty;

        public String getStat() {
            return stat;
        }

        public void setStat(String stat) {
            this.stat = stat;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getShippingNo() {
            return shippingNo;
        }

        public void setShippingNo(String shippingNo) {
            this.shippingNo = shippingNo;
        }

        public String getTrackingNo() {
            return trackingNo;
        }

        public void setTrackingNo(String trackingNo) {
            this.trackingNo = trackingNo;
        }

        public String getPickupNo() {
            return pickupNo;
        }

        public void setPickupNo(String pickupNo) {
            this.pickupNo = pickupNo;
        }

        public String getPickupQty() {
            return pickupQty;
        }

        public void setPickupQty(String pickupQty) {
            this.pickupQty = pickupQty;
        }
    }
}
