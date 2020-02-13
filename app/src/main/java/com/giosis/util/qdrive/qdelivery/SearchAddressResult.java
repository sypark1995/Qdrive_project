package com.giosis.util.qdrive.qdelivery;

import java.util.ArrayList;

/**
 * @author krm0219
 */
public class SearchAddressResult {

    private int resultCode = -1;
    private String resultMsg = "";
    private ArrayList<String> zipCodeArrayList;
    private ArrayList<String> adddressArrayList;

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


    public ArrayList<String> getZipCodeArrayList() {
        return zipCodeArrayList;
    }

    public void setZipCodeArrayList(ArrayList<String> zipCodeArrayList) {
        this.zipCodeArrayList = zipCodeArrayList;
    }

    public ArrayList<String> getAdddressArrayList() {
        return adddressArrayList;
    }

    public void setAdddressArrayList(ArrayList<String> adddressArrayList) {
        this.adddressArrayList = adddressArrayList;
    }
}
