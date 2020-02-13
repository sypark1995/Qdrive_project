package com.giosis.util.qdrive.main;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict = false, name = "StdCustomResultOfQSignShippinfInfo")
public class ShippingInfoResult {

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
    private List<String> resultObject;

    public List<String> getResultObject() {
        return resultObject;
    }

    public void setResultObject(List<String> resultObj) {
        this.resultObject = resultObj;
    }

    @Root(strict = false, name = "ResultObject")
    public static class ResultObject {

        @Element(name = "rev_nm", required = false)
        private String revNm = "";

        @Element(name = "cust_nm", required = false)
        private String custNm = "";

        public String getRevNm() {
            return revNm;
        }

        public void setRevNm(String revNm) {
            this.revNm = revNm;
        }

        public String getCustNm() {
            return custNm;
        }

        public void setCustNm(String custNm) {
            this.custNm = custNm;
        }

    }
}
