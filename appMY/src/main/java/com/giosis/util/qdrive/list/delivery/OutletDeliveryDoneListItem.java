package com.giosis.util.qdrive.list.delivery;

import java.io.Serializable;

public class OutletDeliveryDoneListItem implements Serializable {

    private String trackingNo;
    private String receiverName;

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    String getReceiverName() {
        return receiverName;
    }

    void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    private String jobID;
    private String VendorCode;
    private String QrCode;

    String getJobID() {
        return jobID;
    }

    void setJobID(String jobID) {
        this.jobID = jobID;
    }

    String getVendorCode() {
        return VendorCode;
    }

    void setVendorCode(String vendorCode) {
        VendorCode = vendorCode;
    }

    String getQrCode() {
        return QrCode;
    }

    void setQrCode(String qrCode) {
        QrCode = qrCode;
    }
}