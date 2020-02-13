package com.giosis.util.qdrive.list.pickup;

import java.io.Serializable;
import java.util.ArrayList;

public class OutletPickupDoneResult implements Serializable {

    String resultCode;
    String resultMsg;

    String pickupNo;
    String jobNumber;
    String QRCode;
    String TrackingNumbers;


    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }


    public String getPickupNo() {
        return pickupNo;
    }

    public void setPickupNo(String pickupNo) {
        this.pickupNo = pickupNo;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getQRCode() {
        return QRCode;
    }

    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
    }

    public String getTrackingNumbers() {
        return TrackingNumbers;
    }

    public void setTrackingNumbers(String trackingNumbers) {
        TrackingNumbers = trackingNumbers;

        TrackingNumbers = TrackingNumbers.replace("[", "");
        TrackingNumbers = TrackingNumbers.replace("]", "");
        TrackingNumbers = TrackingNumbers.replace("\"", "");

        splitString(TrackingNumbers);
    }

    void splitString(String tracking_no_list) {

        ArrayList<OutletPickupDoneTrackingNoItem> list = new ArrayList<>();


        String[] strings = tracking_no_list.split(",");


        for (int i = 0; i < strings.length; i++) {

            OutletPickupDoneTrackingNoItem item = new OutletPickupDoneTrackingNoItem();
            item.setTrackingNo(strings[i]);
            item.setScanned(false);

            list.add(item);
        }

        setTrackingNoList(list);
    }


    ArrayList<OutletPickupDoneTrackingNoItem> trackingNoList = new ArrayList<>();

    public ArrayList<OutletPickupDoneTrackingNoItem> getTrackingNoList() {
        return trackingNoList;
    }

    public void setTrackingNoList(ArrayList<OutletPickupDoneTrackingNoItem> trackingNoList) {
        this.trackingNoList = trackingNoList;
    }

    public static class OutletPickupDoneTrackingNoItem implements Serializable {

        String trackingNo;
        String receiver;
        boolean isScanned;

        public String getTrackingNo() {
            return trackingNo;
        }

        public void setTrackingNo(String trackingNo) {
            this.trackingNo = trackingNo;
        }

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public boolean isScanned() {
            return isScanned;
        }

        public void setScanned(boolean scanned) {
            isScanned = scanned;
        }
    }
}
