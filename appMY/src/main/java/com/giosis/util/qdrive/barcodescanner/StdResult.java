package com.giosis.util.qdrive.barcodescanner;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "StdResult")
public class StdResult {

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
}
