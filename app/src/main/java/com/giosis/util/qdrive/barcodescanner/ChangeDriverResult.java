package com.giosis.util.qdrive.barcodescanner;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "StdCustomResultOfChgDelDriverResult")
public class ChangeDriverResult {

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
        private String contrNo = "";


        @Element(name = "tracking_no", required = false)
        private String trackingNo = "";

        @Element(name = "del_driver_id", required = false)
        private String currentDriver = "";

        @Element(name = "status", required = false)
        private String status = "";

        public String getContrNo() {
            return contrNo;
        }


        public void setContrNo(String contrNo) {
            this.contrNo = contrNo;
        }

        public String getTrackingNo() {
            return trackingNo;
        }

        public void setTrackingNo(String trackingNo) {
            this.trackingNo = trackingNo;
        }

        public String getCurrentDriver() {
            return currentDriver;
        }

        public void setCurrentDriver(String currentDriver) {
            this.currentDriver = currentDriver;
        }

        public String getStatus() {
            return status;
        }


        public void setStatus(String status) {
            this.status = status;
        }


    }

}
