package com.giosis.library.barcodescanner;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "StdCustomResultOfChgDelDriverResult")
public class ChangeDriverResult {

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
    private Data ResultObject;

    public Data getResultObject() {
        return ResultObject;
    }

    public void setResultObject(Data resultObj) {
        this.ResultObject = resultObj;
    }

    @Root(strict = false, name = "ResultObject")
    public static class Data {

        @Element(name = "contr_no", required = false)
        private String contr_no = "";

        @Element(name = "tracking_no", required = false)
        private String tracking_no = "";

        @Element(name = "del_driver_id", required = false)
        private String del_driver_id = "";

        @Element(name = "status", required = false)
        private String status = "";


        public String getContrNo() {
            return contr_no;
        }

        public String getTrackingNo() {
            return tracking_no;
        }

        public String getCurrentDriver() {
            return del_driver_id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
