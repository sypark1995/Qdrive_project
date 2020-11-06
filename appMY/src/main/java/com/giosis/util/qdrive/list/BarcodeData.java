package com.giosis.util.qdrive.list;

import java.io.Serializable;

public class BarcodeData implements Serializable {

    private String barcode;
    private String state;               // type : 'D', 'P'... 등등~
    private String changeDeliveryText;  // Change Driver 에서 표시되는 글자.  ex) "MY19612073 | DPC3-OUT | hyemi"

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public String getChangeDeliveryText() {
        return changeDeliveryText;
    }

    public void setChangeDeliveryText(String changeDeliveryText) {
        this.changeDeliveryText = changeDeliveryText;
    }
}