package com.giosis.util.qdrive.singapore.list.pickup;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class OutletPickupDoneResult implements Serializable {

    @SerializedName("ResultCode")
    String resultCode;

    @SerializedName("ResultMsg")
    String resultMsg;

    @SerializedName("ResultObject")
    OutletPickupDoneItem resultObject;


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

    public OutletPickupDoneItem getResultObject() {
        return resultObject;
    }

    public void setResultObject(OutletPickupDoneItem resultObject) {
        this.resultObject = resultObject;
    }


    public class OutletPickupDoneItem implements Serializable {

        @SerializedName("PickupNo")
        String pickupNo;

        @SerializedName("JobNumber")
        String jobNumber;

        @SerializedName("QRCode")
        String QRCode;

        @SerializedName("ListTrackingNo")
        String TrackingNumbers;


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

        public class OutletPickupDoneTrackingNoItem implements Serializable {

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


}
