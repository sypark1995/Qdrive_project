package com.giosis.util.qdrive.list;

public class ChildItemNotUpload {

    private String tel;
    private String hp;
    private String stat;
    private String stat_reason;
    private String stat_msg;
    private String receiveType;
    private String realQty;
    private String retryDay;
    private String secret_no_type;
    private String secret_no;

    public String getTel() {
        return tel;
    }

    public void setTel(String tel_no) {
        this.tel = tel_no;
    }

    String getHp() {
        return hp;
    }

    void setHp(String hp_no) {
        this.hp = hp_no;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    String getStatReason() {
        return stat_reason;
    }

    void setStatReason(String stat_reason) {
        this.stat_reason = stat_reason;
    }

    String getStatMsg() {
        return stat_msg;
    }

    void setStatMsg(String stat_msg) {
        this.stat_msg = stat_msg;
    }

    public String getReceiveType() {
        return receiveType;
    }

    public void setReceiveType(String receiveType) {
        this.receiveType = receiveType;
    }

    public String getRealQty() {
        return realQty;
    }

    public void setRealQty(String realQty) {
        this.realQty = realQty;
    }

    public String getRetryDay() {
        return retryDay;
    }

    public void setRetryDay(String retryDay) {
        this.retryDay = retryDay;
    }

    public String getSecretNoType() {
        return secret_no_type;
    }

    public void setSecretNoType(String secret_no_type) {
        if (secret_no_type != null) {
            this.secret_no_type = secret_no_type;
        } else {
            this.secret_no_type = "";
        }
    }

    public String getSecretNo() {
        return secret_no;
    }

    public void setSecretNo(String secret_no) {
        if (secret_no != null) {
            this.secret_no = secret_no;
        } else {
            this.secret_no = "";
        }
    }


}
