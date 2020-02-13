package com.giosis.util.qdrive.singapore;

public class UploadData {

    private String no_songjang = "";
    private String stat = "";
    private String driverMemo = ""; // 배송 메모
    private String fail_reason = "";  //  Reason Code
    private String retry_day = "";   //  픽업용 재배달
    private String realQty = "";      // 픽업용 실재 수량
    private String receiveType = ""; //수취방법
    private String type = "";    //D,P

    public String getNoSongjang() {
        return no_songjang;
    }

    public void setNoSongjang(String no_songjang) {
        this.no_songjang = no_songjang;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getDriverMemo() {
        return driverMemo;
    }

    public void setDriverMemo(String driverMemo) {
        this.driverMemo = driverMemo;
    }

    public String getFailReason() {
        return fail_reason;
    }

    public void setFailReason(String fail_reason) {
        this.fail_reason = fail_reason;
    }

    public String getReceiveType() {
        return receiveType;
    }

    public void setReceiveType(String receiveType) {
        this.receiveType = receiveType;
    }

    public String getRetryDay() {
        return retry_day;
    }

    public void setRetryDay(String retry_day) {
        this.retry_day = retry_day;
    }

    public String getRealQty() {
        return realQty;
    }

    public void setRealQty(String realQty) {
        this.realQty = realQty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
